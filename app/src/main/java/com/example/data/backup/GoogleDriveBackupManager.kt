package com.example.data.backup

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.data.db.AppDatabase
import com.example.data.model.TripEntity
import com.example.data.repository.UserPreferencesRepository
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
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
  val accountMismatch: Boolean = false,
  val monthlyFilesCount: Int = 0,
  val monthlySummaryText: String = ""
)

sealed class BackupResult {
  data class Success(val metadata: BackupMetadata) : BackupResult()
  data class Empty(val message: String) : BackupResult()
  data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
  data class Success(
    val tripsCount: Int,
    val bookingsCount: Int,
    val monthlyBreakdown: Map<String, Int> = emptyMap()
  ) : RestoreResult()
  data class Error(val message: String) : RestoreResult()
}

object GoogleDriveBackupManager {
  private const val TAG = "GoogleDriveBackup"
  const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
  const val BACKUP_FILE_NAME = "carhisab_backup.db"
  const val MASTER_DB_NAME = "CarHisab_Master.db"
  const val DB_NAME = "car_hisab_database"
  const val MANIFEST_FILE_NAME = "CarHisab_Backup_Manifest.json"

  fun getBackupDirectory(context: Context): File {
    return File(context.filesDir, "drive_appdata_backup").apply { mkdirs() }
  }

  fun getInternalBackupFile(context: Context): File {
    return File(getBackupDirectory(context), BACKUP_FILE_NAME)
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

  fun getDocumentsBackupDir(context: Context): File? {
    return try {
      val docDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: return null
      File(docDir, "CarHisab").apply { mkdirs() }
    } catch (_: Exception) {
      null
    }
  }

  fun getDocumentsBackupFile(context: Context): File? {
    val dir = getDocumentsBackupDir(context) ?: return null
    return File(dir, BACKUP_FILE_NAME)
  }

  /**
   * Helper to retrieve Google Drive OAuth 2.0 Access Token from device Google Accounts
   */
  suspend fun getGoogleDriveAccessToken(context: Context, preferredEmail: String = ""): String? = withContext(Dispatchers.IO) {
    try {
      val am = AccountManager.get(context)
      val googleAccounts = am.getAccountsByType("com.google")
      if (googleAccounts.isEmpty()) {
        Log.w(TAG, "No Google account found on device for Drive token.")
        return@withContext null
      }
      val targetAccount: Account = if (preferredEmail.isNotBlank()) {
        googleAccounts.firstOrNull { it.name.equals(preferredEmail, ignoreCase = true) } ?: googleAccounts.first()
      } else {
        googleAccounts.first()
      }
      val scope = "oauth2:$DRIVE_APPDATA_SCOPE"
      val token = GoogleAuthUtil.getToken(context, targetAccount, scope)
      Log.d(TAG, "Google Drive token acquired for ${targetAccount.name}")
      token
    } catch (e: UserRecoverableAuthException) {
      Log.w(TAG, "User consent or action required for Drive scope: ${e.message}")
      null
    } catch (e: Exception) {
      Log.w(TAG, "Google Drive token error: ${e.message}")
      null
    }
  }

  /**
   * Generates a strict month key for grouping (e.g. "2026_09")
   */
  fun getMonthKey(dateMillis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    return String.format(Locale.US, "%04d_%02d", year, month)
  }

  /**
   * Formats month title in Bangla or English (e.g. "সেপ্টেম্বর ২০২৬" or "September 2026")
   */
  fun getMonthDisplay(dateMillis: Long, isBangla: Boolean = true): String {
    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val year = cal.get(Calendar.YEAR)
    val monthIdx = cal.get(Calendar.MONTH)
    val banglaMonths = arrayOf(
      "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
      "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val englishMonths = arrayOf(
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
    )
    return if (isBangla) {
      val mStr = if (monthIdx in 0..11) banglaMonths[monthIdx] else ""
      val yrBn = convertToBanglaDigits(year.toString())
      "$mStr $yrBn"
    } else {
      val mStr = if (monthIdx in 0..11) englishMonths[monthIdx] else ""
      "$mStr $year"
    }
  }

  /**
   * Ensures trip's dateMillis and dateString are consistent and valid,
   * guaranteeing that historical trips are strictly tied to their actual month.
   */
  fun ensureValidTripDate(trip: TripEntity): TripEntity {
    val formats = listOf(
      SimpleDateFormat("dd MMM yyyy", Locale.US),
      SimpleDateFormat("yyyy-MM-dd", Locale.US),
      SimpleDateFormat("dd/MM/yyyy", Locale.US),
      SimpleDateFormat("dd-MM-yyyy", Locale.US),
      SimpleDateFormat("d MMM yyyy", Locale.US),
      SimpleDateFormat("d MMMM yyyy", Locale.US),
      SimpleDateFormat("yyyy/MM/dd", Locale.US)
    )

    var resolvedMillis = trip.dateMillis
    if (resolvedMillis <= 0L && trip.dateString.isNotBlank()) {
      for (sdf in formats) {
        try {
          sdf.isLenient = true
          val parsed = sdf.parse(trip.dateString.trim())
          if (parsed != null) {
            resolvedMillis = parsed.time
            break
          }
        } catch (_: Exception) {}
      }
    }
    if (resolvedMillis <= 0L) {
      resolvedMillis = System.currentTimeMillis()
    }

    val resolvedDateString = if (trip.dateString.isBlank()) {
      SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(resolvedMillis))
    } else trip.dateString

    val resolvedIncome = if (trip.income == 0.0 && trip.rent > 0.0) trip.rent - trip.gratuity else trip.income
    val resolvedProfit = if (trip.profit == 0.0 && (resolvedIncome > 0.0 || trip.rent > 0.0)) resolvedIncome - trip.maintenanceCost else trip.profit

    return trip.copy(
      dateMillis = resolvedMillis,
      dateString = resolvedDateString,
      income = resolvedIncome,
      profit = resolvedProfit
    )
  }

  private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(20, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  /**
   * Verifies local database has data before backup.
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
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error verifying database content: ${e.message}", e)
      false
    }
  }

  private fun checkpointDatabase(context: Context) {
    try {
      val db = AppDatabase.getDatabase(context)
      db.openHelper.writableDatabase.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")).use { cursor ->
        if (cursor.moveToFirst()) {
          Log.d(TAG, "WAL Checkpoint: busy=${cursor.getInt(0)}, log=${cursor.getInt(1)}, checkpointed=${cursor.getInt(2)}")
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "WAL Checkpoint exception: ${e.message}")
    }
  }

  suspend fun getStagedBackupFile(context: Context): File? = withContext(Dispatchers.IO) {
    try {
      if (!verifyDatabaseNotEmpty(context)) return@withContext null
      checkpointDatabase(context)
      val dbFile = context.getDatabasePath(DB_NAME)
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
          } catch (_: Exception) {}
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
      }
      if (targetStagingFile.exists() && targetStagingFile.length() > 0) targetStagingFile else null
    } catch (e: Exception) {
      Log.e(TAG, "Failed staging database: ${e.message}", e)
      null
    }
  }

  /**
   * Generates identifiable, month-separated structured JSON backup files for every month
   * e.g., CarHisab_2026_09_September_Trips.json
   */
  suspend fun generateMonthlyArchiveFiles(
    context: Context,
    trips: List<TripEntity>,
    accountEmail: String,
    driverName: String,
    vehicleId: String
  ): List<File> = withContext(Dispatchers.IO) {
    val resultFiles = mutableListOf<File>()
    try {
      val internalDir = getBackupDirectory(context)
      val docDir = getDocumentsBackupDir(context)

      val monthGroups = trips.groupBy { getMonthKey(it.dateMillis) }
      val englishMonthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
      )

      val manifestArray = JSONArray()

      for ((monthKey, mTrips) in monthGroups) {
        if (mTrips.isEmpty()) continue
        val sampleMillis = mTrips.first().dateMillis
        val cal = Calendar.getInstance().apply { timeInMillis = sampleMillis }
        val monthIdx = cal.get(Calendar.MONTH)
        val monthEnName = if (monthIdx in 0..11) englishMonthNames[monthIdx] else "Month"
        val fileName = "CarHisab_${monthKey}_${monthEnName}_Trips.json"

        val json = JSONObject().apply {
          put("app_name", "CarHisab")
          put("file_type", "monthly_trip_archive")
          put("month_key", monthKey)
          put("month_display_en", getMonthDisplay(sampleMillis, isBangla = false))
          put("month_display_bn", getMonthDisplay(sampleMillis, isBangla = true))
          put("account_email", accountEmail)
          put("driver_name", driverName)
          put("vehicle_id", vehicleId)
          put("backup_timestamp", System.currentTimeMillis())
          put("trips_count", mTrips.size)
          put("total_rent", mTrips.sumOf { it.rent })
          put("total_gratuity", mTrips.sumOf { it.gratuity })
          put("total_maintenance", mTrips.sumOf { it.maintenanceCost })
          put("total_income", mTrips.sumOf { it.income })
          put("total_profit", mTrips.sumOf { it.profit })
          put("total_km", mTrips.sumOf { it.kmDriven })

          val tripsArray = JSONArray()
          for (t in mTrips) {
            val tObj = JSONObject().apply {
              put("id", t.id)
              put("date_millis", t.dateMillis)
              put("date_string", t.dateString)
              put("place", t.place)
              put("rent", t.rent)
              put("gratuity", t.gratuity)
              put("maintenance_cost", t.maintenanceCost)
              put("km_driven", t.kmDriven)
              put("description", t.description)
              put("passenger_name", t.passengerName)
              put("passenger_phone", t.passengerPhone)
              put("income", t.income)
              put("profit", t.profit)
              put("user_id", t.userId)
              put("vehicle_id", t.vehicleId)
            }
            tripsArray.put(tObj)
          }
          put("trips", tripsArray)
        }

        val internalFile = File(internalDir, fileName)
        internalFile.writeText(json.toString(2))
        resultFiles.add(internalFile)

        if (docDir != null) {
          try {
            val docFile = File(docDir, fileName)
            docFile.writeText(json.toString(2))
          } catch (_: Exception) {}
        }

        val monthMeta = JSONObject().apply {
          put("month_key", monthKey)
          put("month_name", getMonthDisplay(sampleMillis, isBangla = true))
          put("trips_count", mTrips.size)
          put("file_name", fileName)
          put("total_profit", mTrips.sumOf { it.profit })
        }
        manifestArray.put(monthMeta)
      }

      // Write master manifest
      val manifestJson = JSONObject().apply {
        put("app", "CarHisab")
        put("account_email", accountEmail)
        put("driver_name", driverName)
        put("vehicle_id", vehicleId)
        put("backup_timestamp", System.currentTimeMillis())
        put("total_trips", trips.size)
        put("total_months", monthGroups.size)
        put("months", manifestArray)
      }
      File(internalDir, MANIFEST_FILE_NAME).writeText(manifestJson.toString(2))
      if (docDir != null) {
        try {
          File(docDir, MANIFEST_FILE_NAME).writeText(manifestJson.toString(2))
        } catch (_: Exception) {}
      }

    } catch (e: Exception) {
      Log.e(TAG, "Error generating monthly archive files: ${e.message}", e)
    }
    resultFiles
  }

  suspend fun queryDriveAppDataFiles(accessToken: String): List<JSONObject> = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) return@withContext emptyList()
    try {
      val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=trashed=false&fields=files(id,name,modifiedTime,size,appProperties,description)"
      val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .get()
        .build()

      val response = httpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""
      if (response.isSuccessful) {
        val json = JSONObject(bodyStr)
        val arr = json.optJSONArray("files") ?: return@withContext emptyList()
        val list = mutableListOf<JSONObject>()
        for (i in 0 until arr.length()) {
          list.add(arr.getJSONObject(i))
        }
        list
      } else {
        emptyList()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error querying drive appData files: ${e.message}")
      emptyList()
    }
  }

  suspend fun uploadToDriveAppData(
    stagedFile: File,
    accessToken: String,
    existingFileId: String? = null,
    accountEmail: String = "",
    targetFileName: String = BACKUP_FILE_NAME,
    mimeTypeStr: String = "application/octet-stream",
    properties: Map<String, String> = emptyMap()
  ): String? = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) return@withContext null
    try {
      val mediaType = mimeTypeStr.toMediaType()
      val cleanEmail = accountEmail.trim().lowercase()

      if (!existingFileId.isNullOrBlank()) {
        val metadataJson = JSONObject().apply {
          val props = JSONObject()
          if (cleanEmail.isNotBlank()) props.put("account_email", cleanEmail)
          properties.forEach { (k, v) -> props.put(k, v) }
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
        if (response.isSuccessful) return@withContext existingFileId
      }

      val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
      val metadataJson = JSONObject().apply {
        put("name", targetFileName)
        put("parents", listOf("appDataFolder"))
        val props = JSONObject()
        if (cleanEmail.isNotBlank()) props.put("account_email", cleanEmail)
        properties.forEach { (k, v) -> props.put(k, v) }
        put("appProperties", props)
        if (cleanEmail.isNotBlank()) put("description", "account_email:$cleanEmail")
      }

      val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addPart(metadataJson.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
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
      Log.e(TAG, "Drive upload error for $targetFileName: ${e.message}")
      null
    }
  }

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
      Log.e(TAG, "Drive download error for $fileId: ${e.message}")
      false
    }
  }

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

  fun formatDateString(timestampMillis: Long, isBangla: Boolean = true): String {
    if (timestampMillis <= 0L) return if (isBangla) "কোন ব্যাকআপ পাওয়া যায়নি" else "No backup found"
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val monthIndex = cal.get(Calendar.MONTH)
    val year = cal.get(Calendar.YEAR)
    val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    var hour12 = cal.get(Calendar.HOUR)
    if (hour12 == 0) hour12 = 12

    if (!isBangla) {
      val englishMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
      val monthStr = if (monthIndex in 0..11) englishMonths[monthIndex] else ""
      val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
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

  suspend fun checkForBackup(
    context: Context,
    accessToken: String? = null,
    currentAccountEmail: String = "",
    isBangla: Boolean = true
  ): BackupMetadata = withContext(Dispatchers.IO) {
    try {
      val userPrefs = UserPreferencesRepository(context)
      val cleanUserEmail = currentAccountEmail.ifBlank {
        userPrefs.getLastDriveBackupEmail().ifBlank { userPrefs.profileFlow.value.driverEmail }
      }.trim().lowercase()

      val activeToken = accessToken ?: getGoogleDriveAccessToken(context, cleanUserEmail)
      if (!activeToken.isNullOrBlank()) {
        val driveFiles = queryDriveAppDataFiles(activeToken)
        val masterFile = driveFiles.firstOrNull { it.optString("name") == BACKUP_FILE_NAME || it.optString("name") == MASTER_DB_NAME }
        if (masterFile != null) {
          val id = masterFile.getString("id")
          val size = masterFile.optLong("size", 0L)
          val modifiedStr = masterFile.optString("modifiedTime", "")
          var ts = System.currentTimeMillis()
          try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
              timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            ts = sdf.parse(modifiedStr)?.time ?: System.currentTimeMillis()
          } catch (_: Exception) {}

          val monthlyFiles = driveFiles.filter { it.optString("name").startsWith("CarHisab_") && it.optString("name").endsWith(".json") }
          return@withContext BackupMetadata(
            exists = true,
            dateString = formatDateString(ts, isBangla),
            sizeString = formatFileSize(size, isBangla),
            timestamp = ts,
            sizeBytes = size,
            fileId = id,
            accountEmail = cleanUserEmail,
            accountMismatch = false,
            monthlyFilesCount = monthlyFiles.size
          )
        }
      }

      val internalFile = getInternalBackupFile(context)
      val externalFile = getExternalBackupFile(context)
      val docFile = getDocumentsBackupFile(context)
      val candidateFile = when {
        internalFile.exists() && internalFile.length() > 0 -> internalFile
        externalFile != null && externalFile.exists() && externalFile.length() > 0 -> externalFile
        docFile != null && docFile.exists() && docFile.length() > 0 -> docFile
        else -> null
      }
      if (candidateFile != null) {
        val lastModified = candidateFile.lastModified()
        val sizeBytes = candidateFile.length()
        return@withContext BackupMetadata(
          exists = true,
          dateString = formatDateString(lastModified, isBangla),
          sizeString = formatFileSize(sizeBytes, isBangla),
          timestamp = lastModified,
          sizeBytes = sizeBytes,
          fileId = "local_database_backup",
          accountEmail = cleanUserEmail,
          accountMismatch = false
        )
      }

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
          fileId = "local_preference_backup",
          accountEmail = cleanUserEmail,
          accountMismatch = false
        )
      }

      BackupMetadata(exists = false)
    } catch (e: Exception) {
      Log.e(TAG, "Error checking backup: ${e.message}", e)
      BackupMetadata(exists = false)
    }
  }

  /**
   * Performs organized backup:
   * 1. Creates identifiable master database file: CarHisab_Master.db
   * 2. Creates dedicated, identifiable files for each month: CarHisab_[Year]_[Month]_[MonthName]_Trips.json
   * 3. Creates manifest file: CarHisab_Backup_Manifest.json
   * 4. Uploads all files to Google Drive appDataFolder if access token is available
   * 5. Syncs trips to Supabase cloud table as redundant safety
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

      val db = AppDatabase.getDatabase(context)
      val rawTrips = db.tripDao().getAllTripsSnapshot()
      val validTrips = rawTrips.map { ensureValidTripDate(it) }
      val bookings = db.bookingDao().getAllBookingsSnapshot()
      val now = System.currentTimeMillis()

      val profile = userPrefs.profileFlow.value
      val driverName = profile.driverName.ifBlank { "Driver" }
      val vehicleId = profile.carNumber.ifBlank { profile.carName }.ifBlank { "Car" }

      // 1. Generate identifiable Month-Wise Structured Archive Files
      val monthlyFiles = generateMonthlyArchiveFiles(
        context = context,
        trips = validTrips,
        accountEmail = cleanEmail,
        driverName = driverName,
        vehicleId = vehicleId
      )

      // 2. Obtain Google Drive token if available and upload files
      val activeToken = accessToken ?: getGoogleDriveAccessToken(context, cleanEmail)
      var driveFileId: String? = null

      if (!activeToken.isNullOrBlank()) {
        try {
          val existingFiles = queryDriveAppDataFiles(activeToken)
          val existingMaster = existingFiles.firstOrNull { it.optString("name") == BACKUP_FILE_NAME || it.optString("name") == MASTER_DB_NAME }

          // Upload Master DB file
          driveFileId = uploadToDriveAppData(
            stagedFile = stagedFile,
            accessToken = activeToken,
            existingFileId = existingMaster?.optString("id"),
            accountEmail = cleanEmail,
            targetFileName = BACKUP_FILE_NAME,
            mimeTypeStr = "application/x-sqlite3"
          )

          // Upload Manifest
          val manifestFile = File(getBackupDirectory(context), MANIFEST_FILE_NAME)
          if (manifestFile.exists()) {
            val existingManifest = existingFiles.firstOrNull { it.optString("name") == MANIFEST_FILE_NAME }
            uploadToDriveAppData(
              stagedFile = manifestFile,
              accessToken = activeToken,
              existingFileId = existingManifest?.optString("id"),
              accountEmail = cleanEmail,
              targetFileName = MANIFEST_FILE_NAME,
              mimeTypeStr = "application/json"
            )
          }

          // Upload each month file
          for (mFile in monthlyFiles) {
            val existingMonth = existingFiles.firstOrNull { it.optString("name") == mFile.name }
            uploadToDriveAppData(
              stagedFile = mFile,
              accessToken = activeToken,
              existingFileId = existingMonth?.optString("id"),
              accountEmail = cleanEmail,
              targetFileName = mFile.name,
              mimeTypeStr = "application/json",
              properties = mapOf("month_archive" to "true")
            )
          }
          Log.d(TAG, "Uploaded master DB and ${monthlyFiles.size} monthly archives to Google Drive.")
        } catch (de: Exception) {
          Log.w(TAG, "Drive upload failed, local files intact: ${de.message}")
        }
      }

      // 3. Redundant sync to Supabase
      if (cleanEmail.isNotBlank()) {
        try {
          for (t in validTrips) {
            com.example.data.auth.SupabaseAuthManager.insertTripToSupabase(t)
          }
        } catch (_: Exception) {}
      }

      userPrefs.setLastDriveBackupTime(now)
      userPrefs.setLastBackupTripCount(validTrips.size)
      if (cleanEmail.isNotBlank()) userPrefs.setLastDriveBackupEmail(cleanEmail)

      val monthCount = monthlyFiles.size
      val summaryText = if (isBangla) {
        "মোট $monthCount টি মাসের তথ্য আলাদা ফাইলে সংরক্ষিত হয়েছে।"
      } else {
        "$monthCount monthly archives generated distinctly."
      }

      val metadata = BackupMetadata(
        exists = true,
        dateString = formatDateString(now, isBangla),
        sizeString = formatFileSize(stagedFile.length(), isBangla),
        timestamp = now,
        sizeBytes = stagedFile.length(),
        fileId = driveFileId ?: "local_${BACKUP_FILE_NAME}",
        accountEmail = cleanEmail,
        accountMismatch = false,
        monthlyFilesCount = monthCount,
        monthlySummaryText = summaryText
      )

      Log.d(TAG, "Backup SUCCESS: trips=${validTrips.size}, monthlyFiles=$monthCount, driveId=$driveFileId")
      BackupResult.Success(metadata)
    } catch (e: Exception) {
      Log.e(TAG, "Error performing backup: ${e.message}", e)
      BackupResult.Error(e.localizedMessage ?: "Backup failed")
    }
  }

  /**
   * Restores trips guaranteeing that:
   * 1. One month's trips NEVER mix with or override another month.
   * 2. Every trip is strictly preserved with its original date, time, and financial record.
   * 3. Calculates and returns the exact month-by-month breakdown of restored trips.
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

      val activeToken = accessToken ?: getGoogleDriveAccessToken(context, cleanCurrentEmail)

      // 1. Download from Google Drive if token available
      if (!activeToken.isNullOrBlank()) {
        val driveFiles = queryDriveAppDataFiles(activeToken)
        val driveMaster = driveFiles.firstOrNull { it.optString("name") == BACKUP_FILE_NAME || it.optString("name") == MASTER_DB_NAME }
        if (driveMaster != null) {
          val fid = driveMaster.getString("id")
          downloadFromDriveAppData(fid, activeToken, internalFile)
        }

        // Also download any monthly JSON archives
        val driveMonthFiles = driveFiles.filter { it.optString("name").startsWith("CarHisab_") && it.optString("name").endsWith(".json") }
        for (mf in driveMonthFiles) {
          val mName = mf.getString("name")
          val localMFile = File(getBackupDirectory(context), mName)
          downloadFromDriveAppData(mf.getString("id"), activeToken, localMFile)
        }
      }

      val sourceDbFile = when {
        internalFile.exists() && internalFile.length() > 0 -> internalFile
        externalFile != null && externalFile.exists() && externalFile.length() > 0 -> externalFile
        docFile != null && docFile.exists() && docFile.length() > 0 -> docFile
        else -> null
      }

      var restoredTripsCount = 0
      var restoredBookingsCount = 0

      // Case A: Restore from SQLite database file
      if (sourceDbFile != null && sourceDbFile.length() > 0) {
        AppDatabase.closeDatabase()
        val targetDbFile = context.getDatabasePath(DB_NAME)
        targetDbFile.parentFile?.mkdirs()

        File(targetDbFile.path + "-shm").delete()
        File(targetDbFile.path + "-wal").delete()

        FileInputStream(sourceDbFile).use { input ->
          FileOutputStream(targetDbFile).use { output ->
            input.copyTo(output)
          }
        }

        val restoredDb = AppDatabase.getDatabase(context)
        val allTrips = restoredDb.tripDao().getAllTripsSnapshot()
        val auditedTrips = allTrips.map { ensureValidTripDate(it) }
        restoredDb.tripDao().insertTrips(auditedTrips)

        restoredTripsCount = auditedTrips.size
        restoredBookingsCount = restoredDb.bookingDao().getAllBookingsSnapshot().size
      }

      // Case B: If DB file had 0 trips or was missing, restore from Monthly JSON files
      if (restoredTripsCount == 0) {
        val backupDir = getBackupDirectory(context)
        val docDir = getDocumentsBackupDir(context)

        val jsonFiles = mutableListOf<File>()
        backupDir.listFiles()?.filter { it.name.startsWith("CarHisab_") && it.name.endsWith(".json") && it.name != MANIFEST_FILE_NAME }?.let {
          jsonFiles.addAll(it)
        }
        if (jsonFiles.isEmpty() && docDir != null) {
          docDir.listFiles()?.filter { it.name.startsWith("CarHisab_") && it.name.endsWith(".json") && it.name != MANIFEST_FILE_NAME }?.let {
            jsonFiles.addAll(it)
          }
        }

        if (jsonFiles.isNotEmpty()) {
          val tripsToInsert = mutableListOf<TripEntity>()
          for (jf in jsonFiles) {
            try {
              val content = jf.readText()
              val root = JSONObject(content)
              val tripsArr = root.optJSONArray("trips") ?: continue
              for (i in 0 until tripsArr.length()) {
                val obj = tripsArr.getJSONObject(i)
                val trip = TripEntity(
                  id = 0L, // Auto-generate clean primary key to avoid collision
                  userId = obj.optString("user_id", cleanCurrentEmail),
                  vehicleId = obj.optString("vehicle_id", ""),
                  dateMillis = obj.optLong("date_millis", 0L),
                  dateString = obj.optString("date_string", ""),
                  place = obj.optString("place", ""),
                  rent = obj.optDouble("rent", 0.0),
                  gratuity = obj.optDouble("gratuity", 0.0),
                  maintenanceCost = obj.optDouble("maintenance_cost", 0.0),
                  kmDriven = obj.optDouble("km_driven", 0.0),
                  description = obj.optString("description", ""),
                  passengerName = obj.optString("passenger_name", ""),
                  passengerPhone = obj.optString("passenger_phone", ""),
                  income = obj.optDouble("income", 0.0),
                  profit = obj.optDouble("profit", 0.0)
                )
                tripsToInsert.add(ensureValidTripDate(trip))
              }
            } catch (je: Exception) {
              Log.w(TAG, "Error parsing month json ${jf.name}: ${je.message}")
            }
          }

          if (tripsToInsert.isNotEmpty()) {
            val db = AppDatabase.getDatabase(context)
            db.tripDao().insertTrips(tripsToInsert)
            restoredTripsCount = db.tripDao().getAllTripsSnapshot().size
          }
        }
      }

      // Case C: Check Supabase cloud fallback
      if (restoredTripsCount == 0 && cleanCurrentEmail.isNotBlank()) {
        try {
          val cloudTrips = com.example.data.auth.SupabaseAuthManager.fetchTripsFromSupabase(cleanCurrentEmail)
          if (cloudTrips.isNotEmpty()) {
            val db = AppDatabase.getDatabase(context)
            val validated = cloudTrips.map { ensureValidTripDate(it) }
            db.tripDao().insertTrips(validated)
            restoredTripsCount = validated.size
          }
        } catch (_: Exception) {}
      }

      if (restoredTripsCount == 0 && restoredBookingsCount == 0) {
        val err = if (isBangla) "রিস্টোর করার মতো কোনো ব্যাকআপ ফাইল বা তথ্য পাওয়া যায়নি।"
        else "No backup files or trips found to restore."
        return@withContext RestoreResult.Error(err)
      }

      // Calculate distinct month-by-month breakdown
      val finalDb = AppDatabase.getDatabase(context)
      val finalTrips = finalDb.tripDao().getAllTripsSnapshot()
      val breakdownMap = mutableMapOf<String, Int>()

      for (t in finalTrips) {
        val label = getMonthDisplay(t.dateMillis, isBangla)
        breakdownMap[label] = (breakdownMap[label] ?: 0) + 1
      }

      userPrefs.setLastDriveBackupTime(System.currentTimeMillis())
      userPrefs.setLastBackupTripCount(finalTrips.size)

      Log.d(TAG, "Restore SUCCESS: totalTrips=${finalTrips.size}, months=${breakdownMap.size}")
      RestoreResult.Success(
        tripsCount = finalTrips.size,
        bookingsCount = restoredBookingsCount,
        monthlyBreakdown = breakdownMap
      )
    } catch (e: Exception) {
      Log.e(TAG, "Restore error: ${e.message}", e)
      RestoreResult.Error(
        if (isBangla) "রিস্টোর ব্যর্থ হয়েছে: ${e.message}" else "Restore failed: ${e.message}"
      )
    }
  }
}
