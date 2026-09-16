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
  val fileId: String? = null,
  val accountEmail: String = "",
  val accountMismatch: Boolean = false
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

  fun getInternalBackupFile(context: Context): File {
    val dir = File(context.filesDir, "drive_appdata_backup").apply { mkdirs() }
    return File(dir, BACKUP_FILE_NAME)
  }

  fun getExternalBackupFile(context: Context): File? {
    return try {
      val extDir = context.getExternalFilesDir(null) ?: return null
      val dir = File(extDir, "drive_appdata_backup").apply { mkdirs() }
      File(dir, BACKUP_FILE_NAME)
    } catch (_: Exception) {
      null
    }
  }

  fun getDocumentsBackupFile(context: Context): File? {
    return try {
      val docDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: return null
      val dir = File(docDir, "CarHisab").apply { mkdirs() }
      File(dir, BACKUP_FILE_NAME)
    } catch (_: Exception) {
      null
    }
  }

  fun saveCompanionMetadata(
    context: Context,
    email: String,
    timestamp: Long,
    tripsCount: Int,
    bookingsCount: Int
  ) {
    try {
      val json = JSONObject().apply {
        put("timestamp", timestamp)
        put("account_email", email)
        put("trips_count", tripsCount)
        put("bookings_count", bookingsCount)
      }
      val internalMeta = File(File(context.filesDir, "drive_appdata_backup").apply { mkdirs() }, "backup_metadata.json")
      internalMeta.writeText(json.toString())
      val extDir = context.getExternalFilesDir(null)
      if (extDir != null) {
        val extMeta = File(File(extDir, "drive_appdata_backup").apply { mkdirs() }, "backup_metadata.json")
        extMeta.writeText(json.toString())
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to save companion metadata: ${e.message}")
    }
  }

  fun readCompanionMetadata(context: Context): JSONObject? {
    return try {
      val internalMeta = File(File(context.filesDir, "drive_appdata_backup"), "backup_metadata.json")
      if (internalMeta.exists() && internalMeta.length() > 0) {
        return JSONObject(internalMeta.readText())
      }
      val extDir = context.getExternalFilesDir(null)
      if (extDir != null) {
        val extMeta = File(File(extDir, "drive_appdata_backup"), "backup_metadata.json")
        if (extMeta.exists() && extMeta.length() > 0) {
          return JSONObject(extMeta.readText())
        }
      }
      null
    } catch (_: Exception) {
      null
    }
  }

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
      db.openHelper.writableDatabase.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")).use { cursor ->
        if (cursor.moveToFirst()) {
          Log.d(TAG, "WAL Checkpoint result: busy=${cursor.getInt(0)}, log=${cursor.getInt(1)}, checkpointed=${cursor.getInt(2)}")
        }
      }
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
      val walFile = File(dbFile.path + "-wal")
      val targetStagingFile = getInternalBackupFile(context)
      val externalStagingFile = getExternalBackupFile(context)
      val docStagingFile = getDocumentsBackupFile(context)

      if (dbFile.exists() && dbFile.length() > 0) {
        FileInputStream(dbFile).use { input ->
          FileOutputStream(targetStagingFile).use { output ->
            input.copyTo(output)
          }
        }
        if (externalStagingFile != null) {
          try {
            FileInputStream(targetStagingFile).use { input ->
              FileOutputStream(externalStagingFile).use { output ->
                input.copyTo(output)
              }
            }
          } catch (e: Exception) {
            Log.w(TAG, "Failed to mirror backup to external storage: ${e.message}")
          }
        }
        if (docStagingFile != null) {
          try {
            FileInputStream(targetStagingFile).use { input ->
              FileOutputStream(docStagingFile).use { output ->
                input.copyTo(output)
              }
            }
          } catch (_: Exception) {}
        }
        val stagedWal = File(targetStagingFile.parentFile, BACKUP_FILE_NAME + "-wal")
        if (walFile.exists() && walFile.length() > 0) {
          FileInputStream(walFile).use { input ->
            FileOutputStream(stagedWal).use { output ->
              input.copyTo(output)
            }
          }
        } else {
          if (stagedWal.exists()) {
            stagedWal.delete()
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
  suspend fun queryDriveAppDataFile(
    accessToken: String,
    currentAccountEmail: String = ""
  ): BackupMetadata? = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) return@withContext null
    try {
      val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='$BACKUP_FILE_NAME' and trashed=false&fields=files(id,name,modifiedTime,size,appProperties,description)"
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
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
              timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val parsedDate = try {
              sdf.parse(modifiedTimeStr)
            } catch (_: Exception) {
              val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
              }
              try {
                sdf2.parse(modifiedTimeStr)
              } catch (_: Exception) {
                val sdf3 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                  timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                sdf3.parse(modifiedTimeStr)
              }
            }
            timestamp = parsedDate?.time ?: System.currentTimeMillis()
          } catch (_: Exception) {}

          val appProps = fileObj.optJSONObject("appProperties")
          var backupEmail = appProps?.optString("account_email", "") ?: ""
          if (backupEmail.isBlank()) {
            val desc = fileObj.optString("description", "")
            if (desc.startsWith("account_email:")) {
              backupEmail = desc.substringAfter("account_email:").trim().lowercase()
            }
          }

          val cleanCurrentEmail = currentAccountEmail.trim().lowercase()
          val cleanBackupEmail = backupEmail.trim().lowercase()
          val mismatch = cleanCurrentEmail.isNotBlank() &&
              cleanBackupEmail.isNotBlank() &&
              cleanCurrentEmail != cleanBackupEmail

          return@withContext BackupMetadata(
            exists = true,
            dateString = formatDateString(timestamp),
            sizeString = formatFileSize(size),
            timestamp = timestamp,
            sizeBytes = size,
            fileId = id,
            accountEmail = backupEmail,
            accountMismatch = mismatch
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
    existingFileId: String? = null,
    accountEmail: String = ""
  ): String? = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) return@withContext null
    try {
      val mediaType = "application/octet-stream".toMediaType()
      val cleanEmail = accountEmail.trim().lowercase()

      if (!existingFileId.isNullOrBlank()) {
        // Update metadata and media content of existing AppData file
        val metadataJson = JSONObject().apply {
          val props = JSONObject()
          if (cleanEmail.isNotBlank()) props.put("account_email", cleanEmail)
          put("appProperties", props)
          if (cleanEmail.isNotBlank()) put("description", "account_email:$cleanEmail")
        }

        val updateMetaUrl = "https://www.googleapis.com/drive/v3/files/$existingFileId"
        val metaRequest = Request.Builder()
          .url(updateMetaUrl)
          .addHeader("Authorization", "Bearer $accessToken")
          .patch(metadataJson.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
          .build()
        httpClient.newCall(metaRequest).execute()

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
        val props = JSONObject()
        if (cleanEmail.isNotBlank()) props.put("account_email", cleanEmail)
        put("appProperties", props)
        if (cleanEmail.isNotBlank()) put("description", "account_email:$cleanEmail")
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
   * Formats UTC timestamp or epoch millis into human-readable local date and time string.
   * e.g. Bangla: "১৫ সেপ্টেম্বর ২০২৬, রাত ১০:৩০"
   * e.g. English: "15 Sep 2026, 10:30 PM"
   */
  fun formatDateString(timestampMillis: Long, isBangla: Boolean = true): String {
    if (timestampMillis <= 0L) return if (isBangla) "কোন ব্যাকআপ পাওয়া যায়নি" else "No backup found"

    val cal = java.util.Calendar.getInstance().apply {
      timeInMillis = timestampMillis
    }

    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val monthIndex = cal.get(java.util.Calendar.MONTH)
    val year = cal.get(java.util.Calendar.YEAR)
    val hourOfDay = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = cal.get(java.util.Calendar.MINUTE)

    var hour12 = cal.get(java.util.Calendar.HOUR)
    if (hour12 == 0) hour12 = 12

    if (!isBangla) {
      val englishMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
      val monthStr = if (monthIndex in 0..11) englishMonths[monthIndex] else ""
      val amPm = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
      val minuteStr = String.format(Locale.US, "%02d", minute)
      return "$day $monthStr $year, $hour12:$minuteStr $amPm"
    }

    val banglaMonths = arrayOf(
      "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
      "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val monthStr = if (monthIndex in 0..11) banglaMonths[monthIndex] else ""

    val timePeriod = when (hourOfDay) {
      in 4..5 -> "ভোর"
      in 6..11 -> "সকাল"
      in 12..14 -> "দুপুর"
      in 15..17 -> "বিকেল"
      in 18..19 -> "সন্ধ্যা"
      else -> "রাত"
    }

    val dayBn = convertToBanglaDigits(day.toString())
    val yearBn = convertToBanglaDigits(year.toString())
    val hourBn = convertToBanglaDigits(hour12.toString())
    val minuteBn = convertToBanglaDigits(String.format(Locale.US, "%02d", minute))

    return "$dayBn $monthStr $yearBn, $timePeriod $hourBn:$minuteBn"
  }

  private fun convertToBanglaDigits(input: String): String {
    val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return input.map { ch ->
      if (ch in '0'..'9') banglaDigits[ch - '0'] else ch
    }.joinToString("")
  }

  /**
   * Checks Google Drive appDataFolder, persistent local storage, and Supabase cloud for an existing backup.
   */
  suspend fun checkForBackup(
    context: Context,
    accessToken: String? = null,
    currentAccountEmail: String = "",
    isBangla: Boolean = true
  ): BackupMetadata = withContext(Dispatchers.IO) {
    try {
      val userPrefs = UserPreferencesRepository(context)
      val savedEmail = userPrefs.getLastDriveBackupEmail().ifBlank {
        userPrefs.profileFlow.value.driverEmail
      }
      val cleanUserEmail = currentAccountEmail.ifBlank { savedEmail }.trim().lowercase()

      if (!accessToken.isNullOrBlank()) {
        val remoteMetadata = queryDriveAppDataFile(accessToken, cleanUserEmail)
        if (remoteMetadata != null && remoteMetadata.exists) {
          return@withContext remoteMetadata
        }
      }

      val internalFile = getInternalBackupFile(context)
      val externalFile = getExternalBackupFile(context)
      val docFile = getDocumentsBackupFile(context)
      val companionMeta = readCompanionMetadata(context)

      val candidateFile = when {
        internalFile.exists() && internalFile.length() > 0 -> internalFile
        externalFile != null && externalFile.exists() && externalFile.length() > 0 -> externalFile
        docFile != null && docFile.exists() && docFile.length() > 0 -> docFile
        else -> null
      }
      if (candidateFile != null) {
        val lastModified = candidateFile.lastModified()
        val sizeBytes = candidateFile.length()
        val dateStr = formatDateString(lastModified, isBangla)
        val sizeStr = formatFileSize(sizeBytes, isBangla)
        val backupEmail = companionMeta?.optString("account_email", "")?.ifBlank {
          userPrefs.getLastDriveBackupEmail()
        } ?: userPrefs.getLastDriveBackupEmail()
        val mismatch = cleanUserEmail.isNotBlank() &&
            backupEmail.isNotBlank() &&
            cleanUserEmail != backupEmail

        return@withContext BackupMetadata(
          exists = true,
          dateString = dateStr,
          sizeString = sizeStr,
          timestamp = lastModified,
          sizeBytes = sizeBytes,
          fileId = "appDataFolder_$BACKUP_FILE_NAME",
          accountEmail = backupEmail,
          accountMismatch = mismatch
        )
      }

      val lastTime = userPrefs.getLastDriveBackupTime()
      if (lastTime > 0L) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val sizeBytes = if (dbFile.exists()) dbFile.length() else 102400L
        val backupEmail = companionMeta?.optString("account_email", "")?.ifBlank {
          userPrefs.getLastDriveBackupEmail()
        } ?: userPrefs.getLastDriveBackupEmail()
        val mismatch = cleanUserEmail.isNotBlank() &&
            backupEmail.isNotBlank() &&
            cleanUserEmail != backupEmail

        return@withContext BackupMetadata(
          exists = true,
          dateString = formatDateString(lastTime, isBangla),
          sizeString = formatFileSize(sizeBytes, isBangla),
          timestamp = lastTime,
          sizeBytes = sizeBytes,
          fileId = "appDataFolder_remote_$BACKUP_FILE_NAME",
          accountEmail = backupEmail,
          accountMismatch = mismatch
        )
      }

      // Check Supabase cloud data fallback
      if (cleanUserEmail.isNotBlank()) {
        try {
          val cloudTrips = com.example.data.auth.SupabaseAuthManager.fetchTripsFromSupabase(cleanUserEmail)
          if (cloudTrips.isNotEmpty()) {
            val latest = cloudTrips.maxOfOrNull { it.dateMillis } ?: System.currentTimeMillis()
            val dateStr = formatDateString(latest, isBangla)
            val sizeStr = if (isBangla) "${cloudTrips.size} টি ট্রিপ" else "${cloudTrips.size} trips"
            return@withContext BackupMetadata(
              exists = true,
              dateString = dateStr,
              sizeString = sizeStr,
              timestamp = latest,
              sizeBytes = cloudTrips.size * 512L,
              fileId = "supabase_cloud_trips",
              accountEmail = cleanUserEmail,
              accountMismatch = false
            )
          }
        } catch (se: Exception) {
          Log.d(TAG, "Supabase check during checkForBackup: ${se.message}")
        }
      }

      return@withContext BackupMetadata(exists = false)
    } catch (e: Exception) {
      Log.e(TAG, "Error checking for Drive backup: ${e.message}", e)
      return@withContext BackupMetadata(exists = false)
    }
  }

  /**
   * Performs automated or manual backup to the hidden Google Drive appDataFolder and persistent local storage.
   */
  suspend fun performBackup(
    context: Context,
    accessToken: String? = null,
    currentAccountEmail: String = "",
    isBangla: Boolean = true
  ): BackupResult = withContext(Dispatchers.IO) {
    try {
      val userPrefs = UserPreferencesRepository(context)
      val cleanEmail = currentAccountEmail.ifBlank {
        userPrefs.profileFlow.value.driverEmail
      }.trim().lowercase()

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
        val existingMeta = queryDriveAppDataFile(accessToken, cleanEmail)
        driveFileId = uploadToDriveAppData(stagedFile, accessToken, existingMeta?.fileId, cleanEmail)
      }

      val db = AppDatabase.getDatabase(context)
      val tripsCount = db.tripDao().getAllTripsSnapshot().size
      val bookingsCount = db.bookingDao().getAllBookingsSnapshot().size
      val now = System.currentTimeMillis()

      userPrefs.setLastDriveBackupTime(now)
      userPrefs.setLastBackupTripCount(tripsCount)
      if (cleanEmail.isNotBlank()) {
        userPrefs.setLastDriveBackupEmail(cleanEmail)
      }

      saveCompanionMetadata(context, cleanEmail, now, tripsCount, bookingsCount)

      val metadata = BackupMetadata(
        exists = true,
        dateString = formatDateString(now, isBangla),
        sizeString = formatFileSize(stagedFile.length(), isBangla),
        timestamp = now,
        sizeBytes = stagedFile.length(),
        fileId = driveFileId ?: "appDataFolder_$BACKUP_FILE_NAME",
        accountEmail = cleanEmail,
        accountMismatch = false
      )

      Log.d(TAG, "Backup created successfully: size=${stagedFile.length()} bytes, driveId=$driveFileId, email=$cleanEmail")
      return@withContext BackupResult.Success(metadata)
    } catch (e: Exception) {
      Log.e(TAG, "Error performing backup: ${e.message}", e)
      return@withContext BackupResult.Error(e.localizedMessage ?: "Backup failed")
    }
  }

  /**
   * Downloads and restores database from appDataFolder or persistent local storage seamlessly without app crash.
   */
  suspend fun restoreBackup(
    context: Context,
    accessToken: String? = null,
    currentAccountEmail: String = "",
    isBangla: Boolean = true
  ): RestoreResult = withContext(Dispatchers.IO) {
    try {
      val userPrefs = UserPreferencesRepository(context)
      val cleanCurrentEmail = currentAccountEmail.ifBlank {
        userPrefs.profileFlow.value.driverEmail
      }.trim().lowercase()

      val internalFile = getInternalBackupFile(context)
      val externalFile = getExternalBackupFile(context)
      val docFile = getDocumentsBackupFile(context)
      val companionMeta = readCompanionMetadata(context)

      if (!accessToken.isNullOrBlank()) {
        val driveMeta = queryDriveAppDataFile(accessToken, cleanCurrentEmail)
        if (driveMeta != null && driveMeta.exists) {
          if (driveMeta.accountMismatch) {
            val err = if (isBangla) "ভিন্ন অ্যাকাউন্টের ব্যাকআপ ডাটা রিস্টোর করা যাবে না।" else "Cannot restore backup from a different account."
            return@withContext RestoreResult.Error(err)
          }
          if (driveMeta.fileId != null) {
            downloadFromDriveAppData(driveMeta.fileId, accessToken, internalFile)
          }
        }
      }

      val sourceFile = when {
        internalFile.exists() && internalFile.length() > 0 -> internalFile
        externalFile != null && externalFile.exists() && externalFile.length() > 0 -> externalFile
        docFile != null && docFile.exists() && docFile.length() > 0 -> docFile
        else -> null
      }

      val savedBackupEmail = companionMeta?.optString("account_email", "")?.ifBlank {
        userPrefs.getLastDriveBackupEmail()
      }?.trim()?.lowercase() ?: ""

      if (cleanCurrentEmail.isNotBlank() && savedBackupEmail.isNotBlank() && cleanCurrentEmail != savedBackupEmail) {
        val err = if (isBangla) "ভিন্ন অ্যাকাউন্টের ব্যাকআপ ডাটা রিস্টোর করা যাবে না।" else "Cannot restore backup from a different account."
        return@withContext RestoreResult.Error(err)
      }

      if (sourceFile != null && sourceFile.length() > 0) {
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

        val stagedWalFile = File(sourceFile.parentFile, BACKUP_FILE_NAME + "-wal")
        if (stagedWalFile.exists() && stagedWalFile.length() > 0) {
          FileInputStream(stagedWalFile).use { input ->
            FileOutputStream(walFile).use { output ->
              input.copyTo(output)
            }
          }
        } else {
          if (walFile.exists()) walFile.delete()
        }

        // Mirror to internal backup file if source was external
        if (sourceFile.absolutePath != internalFile.absolutePath) {
          try {
            FileInputStream(sourceFile).use { input ->
              FileOutputStream(internalFile).use { output ->
                input.copyTo(output)
              }
            }
          } catch (me: Exception) {
            Log.w(TAG, "Mirror to internal backup failed: ${me.message}")
          }
        }

        // Re-open DB and verify restored data
        val restoredDb = AppDatabase.getDatabase(context)
        var restoredTrips = restoredDb.tripDao().getAllTripsSnapshot()
        val restoredBookings = restoredDb.bookingDao().getAllBookingsSnapshot()

        // If local file had 0 trips but Supabase has trips, restore them!
        if (restoredTrips.isEmpty() && cleanCurrentEmail.isNotBlank()) {
          val cloudTrips = try {
            com.example.data.auth.SupabaseAuthManager.fetchTripsFromSupabase(cleanCurrentEmail)
          } catch (_: Exception) { emptyList() }
          if (cloudTrips.isNotEmpty()) {
            restoredDb.tripDao().insertTrips(cloudTrips)
            restoredTrips = restoredDb.tripDao().getAllTripsSnapshot()
          }
        }

        val finalTripsCount = if (restoredTrips.isNotEmpty()) restoredTrips.size else (companionMeta?.optInt("trips_count", 0) ?: 0)
        val finalBookingsCount = if (restoredBookings.isNotEmpty()) restoredBookings.size else (companionMeta?.optInt("bookings_count", 0) ?: 0)

        userPrefs.setLastDriveBackupTime(sourceFile.lastModified())
        userPrefs.setLastBackupTripCount(finalTripsCount)

        Log.d(TAG, "Restore SUCCESS from local storage! Restored trips=$finalTripsCount, bookings=$finalBookingsCount")
        return@withContext RestoreResult.Success(
          tripsCount = finalTripsCount,
          bookingsCount = finalBookingsCount
        )
      }

      // If no local file found, check Supabase cloud database
      if (cleanCurrentEmail.isNotBlank()) {
        val cloudTrips = try {
          com.example.data.auth.SupabaseAuthManager.fetchTripsFromSupabase(cleanCurrentEmail)
        } catch (_: Exception) { emptyList() }

        if (cloudTrips.isNotEmpty()) {
          val db = AppDatabase.getDatabase(context)
          db.tripDao().insertTrips(cloudTrips)
          val tripsCount = db.tripDao().getAllTripsSnapshot().size
          val bookingsCount = db.bookingDao().getAllBookingsSnapshot().size

          // Stage backup now so local backup file exists for future
          checkpointDatabase(context)
          getStagedBackupFile(context)
          val now = System.currentTimeMillis()
          saveCompanionMetadata(context, cleanCurrentEmail, now, tripsCount, bookingsCount)
          userPrefs.setLastDriveBackupTime(now)
          userPrefs.setLastBackupTripCount(tripsCount)

          Log.d(TAG, "Restore SUCCESS from Supabase cloud! Restored trips=$tripsCount")
          return@withContext RestoreResult.Success(
            tripsCount = tripsCount,
            bookingsCount = bookingsCount
          )
        }
      }

      return@withContext RestoreResult.Error(
        if (isBangla) "রিস্টোর করার মতো ব্যাকআপ ফাইল বা ডাটা পাওয়া যায়নি।" else "No backup file or data found to restore."
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error restoring backup: ${e.message}", e)
      return@withContext RestoreResult.Error(
        if (isBangla) "রিস্টোর করার সময় সমস্যা হয়েছে: ${e.message}" else "Restore failed: ${e.message}"
      )
    }
  }
}
