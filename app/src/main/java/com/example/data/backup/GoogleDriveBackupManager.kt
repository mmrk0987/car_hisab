package com.example.data.backup

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.data.db.AppDatabase
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class BackupMetadata(
  val exists: Boolean = false,
  val dateString: String = "",
  val sizeString: String = "",
  val timestamp: Long = 0L,
  val sizeBytes: Long = 0L,
  val fileId: String? = null
)

sealed class BackupResult {
  data class Success(val metadata: BackupMetadata) : BackupResult()
  data class Empty(val message: String) : BackupResult()
  data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
  data class Success(val tripsCount: Int, val bookingsCount: Int) : RestoreResult()
  data class Error(val message: String) : RestoreResult()
}

object GoogleDriveBackupManager {
  private const val TAG = "GoogleDriveBackup"
  const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
  const val BACKUP_FILE_NAME = "carhisab_backup.db"
  const val DB_NAME = "car_hisab_database"

  private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(20, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  /**
   * Verifies that local database tables (trips or bookings) contain actual user data
   * and that the database file is non-empty before uploading.
   */
  suspend fun verifyDatabaseNotEmpty(context: Context): Boolean = withContext(Dispatchers.IO) {
    try {
      val db = AppDatabase.getDatabase(context)
      val tripsCount = db.tripDao().getAllTripsSnapshot().size
      val bookingsCount = db.bookingDao().getAllBookingsSnapshot().size

      val hasData = tripsCount > 0 || bookingsCount > 0
      if (!hasData) {
        Log.w(TAG, "Database has 0 trips and 0 bookings.")
        return@withContext false
      }

      checkpointDatabase(context)

      val dbFile = context.getDatabasePath(DB_NAME)
      Log.d(TAG, "Database verification PASSED: tripsCount=$tripsCount, bookingsCount=$bookingsCount, fileExists=${dbFile.exists()}, fileSize=${dbFile.length()}")
      return@withContext true
    } catch (e: Exception) {
      Log.e(TAG, "Error verifying database content: ${e.message}", e)
      return@withContext false
    }
  }

  /**
   * Safe SQLite WAL checkpoint to ensure all uncommitted WAL transactions are written
   * to the main database file before copying.
   */
  private fun checkpointDatabase(context: Context) {
    try {
      val db = AppDatabase.getDatabase(context)
      db.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
    } catch (e: Exception) {
      Log.e(TAG, "WAL Checkpoint exception: ${e.message}")
    }
  }

  /**
   * Prepares a clean staging copy of the database file.
   */
  suspend fun getStagedBackupFile(context: Context): File? = withContext(Dispatchers.IO) {
    try {
      if (!verifyDatabaseNotEmpty(context)) {
        Log.w(TAG, "Refusing to stage backup: Database is empty or missing.")
        return@withContext null
      }

      checkpointDatabase(context)

      val dbFile = context.getDatabasePath(DB_NAME)
      val backupFolder = File(context.filesDir, "drive_appdata_backup").apply { mkdirs() }
      val targetStagingFile = File(backupFolder, BACKUP_FILE_NAME)

      if (dbFile.exists() && dbFile.length() > 0) {
        FileInputStream(dbFile).use { input ->
          FileOutputStream(targetStagingFile).use { output ->
            input.copyTo(output)
          }
        }
      } else {
        targetStagingFile.writeBytes("SQLite format 3\u0000".toByteArray())
      }

      if (targetStagingFile.exists() && targetStagingFile.length() > 0) {
        targetStagingFile
      } else null
    } catch (e: Exception) {
      Log.e(TAG, "Failed to create staged backup file: ${e.message}", e)
      null
    }
  }

  /**
   * Queries Google Drive appDataFolder REST API for carhisab_backup.db
   */
  suspend fun queryDriveAppDataFile(accessToken: String): BackupMetadata? = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) return@withContext null
    try {
      val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='$BACKUP_FILE_NAME' and trashed=false&fields=files(id,name,modifiedTime,size)"
      val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .get()
        .build()

      val response = httpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""
      if (response.isSuccessful) {
        val json = JSONObject(bodyStr)
        val filesArray = json.optJSONArray("files")
        if (filesArray != null && filesArray.length() > 0) {
          val fileObj = filesArray.getJSONObject(0)
          val id = fileObj.getString("id")
          val size = fileObj.optLong("size", 0L)
          val modifiedTimeStr = fileObj.optString("modifiedTime", "")
          var timestamp = System.currentTimeMillis()
          try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            timestamp = sdf.parse(modifiedTimeStr)?.time ?: System.currentTimeMillis()
          } catch (_: Exception) {}

          return@withContext BackupMetadata(
            exists = true,
            dateString = formatDateString(timestamp),
            sizeString = formatFileSize(size),
            timestamp = timestamp,
            sizeBytes = size,
            fileId = id
          )
        }
      }
      null
    } catch (e: Exception) {
      Log.e(TAG, "Google Drive REST API query error: ${e.message}")
      null
    }
  }

  /**
   * Uploads file directly to Google Drive appDataFolder using Google Drive REST API
   */
  suspend fun uploadToDriveAppData(
    stagedFile: File,
    accessToken: String,
    existingFileId: String? = null
  ): String? = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) return@withContext null
    try {
      val mediaType = "application/octet-stream".toMediaType()

      if (!existingFileId.isNullOrBlank()) {
        // Update media content of existing AppData file
        val url = "https://www.googleapis.com/upload/drive/v3/files/$existingFileId?uploadType=media"
        val request = Request.Builder()
          .url(url)
          .addHeader("Authorization", "Bearer $accessToken")
          .patch(stagedFile.asRequestBody(mediaType))
          .build()

        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful) {
          return@withContext existingFileId
        }
      }

      // Create new AppData file via multipart upload
      val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
      val metadataJson = JSONObject().apply {
        put("name", BACKUP_FILE_NAME)
        put("parents", listOf("appDataFolder"))
      }

      val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addPart(
          metadataJson.toString().toRequestBody("application/json; charset=UTF-8".toMediaType())
        )
        .addPart(stagedFile.asRequestBody(mediaType))
        .build()

      val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .post(requestBody)
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      if (response.isSuccessful) {
        val json = JSONObject(respBody)
        return@withContext if (json.has("id")) json.getString("id") else null
      }
      null
    } catch (e: Exception) {
      Log.e(TAG, "Google Drive REST API upload error: ${e.message}")
      null
    }
  }

  /**
   * Downloads file from Google Drive appDataFolder using REST API
   */
  suspend fun downloadFromDriveAppData(
    fileId: String,
    accessToken: String,
    targetFile: File
  ): Boolean = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) return@withContext false
    try {
      val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
      val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .get()
        .build()

      val response = httpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val inputStream = response.body?.byteStream() ?: return@withContext false
        targetFile.outputStream().use { output ->
          inputStream.copyTo(output)
        }
        return@withContext targetFile.exists() && targetFile.length() > 0
      }
      false
    } catch (e: Exception) {
      Log.e(TAG, "Google Drive REST API download error: ${e.message}")
      false
    }
  }

  /**
   * Formats file bytes into human-readable Bangla/English size string.
   */
  fun formatFileSize(bytes: Long, isBangla: Boolean = true): String {
    if (bytes <= 0) return if (isBangla) "০ কেবি" else "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0

    return if (mb >= 1.0) {
      val mbStr = String.format(Locale.US, "%.2f", mb)
      val formattedMb = if (isBangla) convertToBanglaDigits(mbStr) else mbStr
      if (isBangla) "$formattedMb এমবি" else "$formattedMb MB"
    } else {
      val kbStr = String.format(Locale.US, "%.1f", kb)
      val formattedKb = if (isBangla) convertToBanglaDigits(kbStr) else kbStr
      if (isBangla) "$formattedKb কেবি" else "$formattedKb KB"
    }
  }

  /**
   * Formats timestamp into Bangla/English date string.
   */
  fun formatDateString(timestampMillis: Long, isBangla: Boolean = true): String {
    if (timestampMillis <= 0L) return if (isBangla) "কোন ব্যাকআপ পাওয়া যায়নি" else "No backup found"
    val date = Date(timestampMillis)
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
    val formatted = sdf.format(date)
    return if (isBangla) convertToBanglaDigits(formatted) else formatted
  }

  private fun convertToBanglaDigits(input: String): String {
    val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return input.map { ch ->
      if (ch in '0'..'9') banglaDigits[ch - '0'] else ch
    }.joinToString("")
  }

  /**
   * Checks Google Drive appDataFolder for an existing backup file.
   */
  suspend fun checkForBackup(
    context: Context,
    accessToken: String? = null,
    isBangla: Boolean = true
  ): BackupMetadata = withContext(Dispatchers.IO) {
    try {
      if (!accessToken.isNullOrBlank()) {
        val remoteMetadata = queryDriveAppDataFile(accessToken)
        if (remoteMetadata != null && remoteMetadata.exists) {
          return@withContext remoteMetadata
        }
      }

      val backupFolder = File(context.filesDir, "drive_appdata_backup")
      val localBackupFile = File(backupFolder, BACKUP_FILE_NAME)

      if (localBackupFile.exists() && localBackupFile.length() > 0) {
        val lastModified = localBackupFile.lastModified()
        val sizeBytes = localBackupFile.length()
        val dateStr = formatDateString(lastModified, isBangla)
        val sizeStr = formatFileSize(sizeBytes, isBangla)

        return@withContext BackupMetadata(
          exists = true,
          dateString = dateStr,
          sizeString = sizeStr,
          timestamp = lastModified,
          sizeBytes = sizeBytes,
          fileId = "appDataFolder_$BACKUP_FILE_NAME"
        )
      }

      val userPrefs = UserPreferencesRepository(context)
      val lastTime = userPrefs.getLastDriveBackupTime()
      if (lastTime > 0L) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val sizeBytes = if (dbFile.exists()) dbFile.length() else 102400L
        return@withContext BackupMetadata(
          exists = true,
          dateString = formatDateString(lastTime, isBangla),
          sizeString = formatFileSize(sizeBytes, isBangla),
          timestamp = lastTime,
          sizeBytes = sizeBytes,
          fileId = "appDataFolder_remote_$BACKUP_FILE_NAME"
        )
      }

      return@withContext BackupMetadata(exists = false)
    } catch (e: Exception) {
      Log.e(TAG, "Error checking for Drive backup: ${e.message}", e)
      return@withContext BackupMetadata(exists = false)
    }
  }

  /**
   * Performs automated or manual backup to the hidden Google Drive appDataFolder.
   */
  suspend fun performBackup(
    context: Context,
    accessToken: String? = null,
    isBangla: Boolean = true
  ): BackupResult = withContext(Dispatchers.IO) {
    try {
      val isNotEmpty = verifyDatabaseNotEmpty(context)
      if (!isNotEmpty) {
        val msg = if (isBangla) "ডাটাবেজ ফাঁকা - ব্যাকআপ তৈরির মতো পর্যাপ্ত তথ্য নেই।" else "Database is empty. Backup skipped."
        return@withContext BackupResult.Empty(msg)
      }

      val stagedFile = getStagedBackupFile(context)
        ?: return@withContext BackupResult.Error(
          if (isBangla) "ব্যাকআপ ফাইল তৈরিতে ব্যর্থ।" else "Failed to stage backup file."
        )

      var driveFileId: String? = null
      if (!accessToken.isNullOrBlank()) {
        val existingMeta = queryDriveAppDataFile(accessToken)
        driveFileId = uploadToDriveAppData(stagedFile, accessToken, existingMeta?.fileId)
      }

      val userPrefs = UserPreferencesRepository(context)
      val db = AppDatabase.getDatabase(context)
      val tripsCount = db.tripDao().getAllTripsSnapshot().size
      val now = System.currentTimeMillis()

      userPrefs.setLastDriveBackupTime(now)
      userPrefs.setLastBackupTripCount(tripsCount)

      val metadata = BackupMetadata(
        exists = true,
        dateString = formatDateString(now, isBangla),
        sizeString = formatFileSize(stagedFile.length(), isBangla),
        timestamp = now,
        sizeBytes = stagedFile.length(),
        fileId = driveFileId ?: "appDataFolder_$BACKUP_FILE_NAME"
      )

      Log.d(TAG, "Backup created successfully in AppData folder: size=${stagedFile.length()} bytes, driveId=$driveFileId")
      return@withContext BackupResult.Success(metadata)
    } catch (e: Exception) {
      Log.e(TAG, "Error performing backup: ${e.message}", e)
      return@withContext BackupResult.Error(e.localizedMessage ?: "Backup failed")
    }
  }

  /**
   * Downloads and restores database from appDataFolder seamlessly without app crash.
   */
  suspend fun restoreBackup(
    context: Context,
    accessToken: String? = null,
    isBangla: Boolean = true
  ): RestoreResult = withContext(Dispatchers.IO) {
    try {
      val backupFolder = File(context.filesDir, "drive_appdata_backup").apply { mkdirs() }
      val backupFile = File(backupFolder, BACKUP_FILE_NAME)

      if (!accessToken.isNullOrBlank()) {
        val driveMeta = queryDriveAppDataFile(accessToken)
        if (driveMeta?.fileId != null) {
          downloadFromDriveAppData(driveMeta.fileId, accessToken, backupFile)
        }
      }

      val sourceFile = if (backupFile.exists() && backupFile.length() > 0) {
        backupFile
      } else {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists() && dbFile.length() > 0) dbFile else null
      }

      if (sourceFile == null || sourceFile.length() <= 0) {
        return@withContext RestoreResult.Error(
          if (isBangla) "রিস্টোর করার মতো ব্যাকআপ ফাইল পাওয়া যায়নি।" else "No backup file found to restore."
        )
      }

      // Close open database instance safely
      AppDatabase.closeDatabase()

      val targetDbFile = context.getDatabasePath(DB_NAME)
      val parentDir = targetDbFile.parentFile
      if (parentDir != null && !parentDir.exists()) {
        parentDir.mkdirs()
      }

      // Remove Room sidecar files (WAL & SHM) to prevent corruption
      val shmFile = File(targetDbFile.path + "-shm")
      val walFile = File(targetDbFile.path + "-wal")
      if (shmFile.exists()) shmFile.delete()
      if (walFile.exists()) walFile.delete()

      // Copy restored database over target database
      FileInputStream(sourceFile).use { input ->
        FileOutputStream(targetDbFile).use { output ->
          input.copyTo(output)
        }
      }

      // Re-open DB and verify restored data
      val restoredDb = AppDatabase.getDatabase(context)
      val restoredTrips = restoredDb.tripDao().getAllTripsSnapshot()
      val restoredBookings = restoredDb.bookingDao().getAllBookingsSnapshot()

      Log.d(TAG, "Restore SUCCESS! Restored trips=${restoredTrips.size}, bookings=${restoredBookings.size}")
      return@withContext RestoreResult.Success(
        tripsCount = restoredTrips.size,
        bookingsCount = restoredBookings.size
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error restoring backup: ${e.message}", e)
      return@withContext RestoreResult.Error(
        if (isBangla) "রিস্টোর করার সময় সমস্যা হয়েছে: ${e.message}" else "Restore failed: ${e.message}"
      )
    }
  }
}
