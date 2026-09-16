package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.data.db.AppDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.TripEntity
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
  private const val TAG = "CarHisabBackup"
  const val DB_NAME = "car_hisab_database"
  const val MASTER_DB_NAME = "CarHisab_Master.db"
  const val MANIFEST_FILE_NAME = "CarHisab_Backup_Manifest.json"

  suspend fun verifyDatabaseNotEmpty(context: Context): Boolean = withContext(Dispatchers.IO) {
    try {
      val db = AppDatabase.getDatabase(context)
      val trips = db.tripDao().getAllTripsSnapshot()
      val bookings = db.bookingDao().getAllBookingsSnapshot()
      trips.isNotEmpty() || bookings.isNotEmpty()
    } catch (_: Exception) {
      false
    }
  }

  fun getBackupDirectory(context: Context): File {
    return File(context.filesDir, "drive_appdata_backup").apply { mkdirs() }
  }

  fun getDocumentsBackupDir(context: Context): File? {
    return try {
      val docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return null
      File(docDir, "CarHisab").apply { mkdirs() }
    } catch (_: Exception) {
      null
    }
  }

  fun getDefaultBackupZipName(): String {
    val dateStr = SimpleDateFormat("yyyy_MM_dd", Locale.US).format(Date())
    return "CarHisab_Backup_$dateStr.zip"
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
   * Ensures trip's dateMillis and dateString are consistent and valid.
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

  /**
   * Checkpoints SQLite WAL so all in-memory changes are written to the main DB file.
   */
  fun checkpointDatabase(context: Context) {
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

  /**
   * Generates month-separated JSON files for each month distinctly.
   */
  fun generateMonthlyArchiveFiles(
    targetDir: File,
    trips: List<TripEntity>,
    accountEmail: String,
    driverName: String,
    vehicleId: String
  ): List<File> {
    val resultFiles = mutableListOf<File>()
    try {
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

        val file = File(targetDir, fileName)
        file.writeText(json.toString(2))
        resultFiles.add(file)

        val monthMeta = JSONObject().apply {
          put("month_key", monthKey)
          put("month_name", getMonthDisplay(sampleMillis, isBangla = true))
          put("trips_count", mTrips.size)
          put("file_name", fileName)
          put("total_profit", mTrips.sumOf { it.profit })
        }
        manifestArray.put(monthMeta)
      }

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
      val manifestFile = File(targetDir, MANIFEST_FILE_NAME)
      manifestFile.writeText(manifestJson.toString(2))
      resultFiles.add(manifestFile)
    } catch (e: Exception) {
      Log.e(TAG, "Error generating monthly files: ${e.message}", e)
    }
    return resultFiles
  }

  /**
   * Prepares a self-contained, complete backup ZIP archive file.
   * Contains CarHisab_Master.db, individual monthly JSONs, and Manifest.json.
   */
  suspend fun prepareBackupZip(
    context: Context,
    overrideAccountEmail: String = ""
  ): File? = withContext(Dispatchers.IO) {
    try {
      checkpointDatabase(context)
      val db = AppDatabase.getDatabase(context)
      val rawTrips = db.tripDao().getAllTripsSnapshot()
      val validTrips = rawTrips.map { ensureValidTripDate(it) }
      val bookings = db.bookingDao().getAllBookingsSnapshot()

      if (validTrips.isEmpty() && bookings.isEmpty()) {
        Log.w(TAG, "Database has 0 trips and 0 bookings.")
        return@withContext null
      }

      val userPrefs = UserPreferencesRepository(context)
      val profile = userPrefs.profileFlow.value
      val cleanEmail = overrideAccountEmail.ifBlank {
        profile.driverEmail.ifBlank { profile.savedEmail }
      }.trim().lowercase()
      val driverName = profile.driverName.ifBlank { "Driver" }
      val vehicleId = profile.carNumber.ifBlank { profile.carName }.ifBlank { "Car" }

      val stagingDir = File(context.cacheDir, "backup_staging_${System.currentTimeMillis()}").apply { mkdirs() }
      val dbFile = context.getDatabasePath(DB_NAME)
      val stagedDbFile = File(stagingDir, MASTER_DB_NAME)

      if (dbFile.exists() && dbFile.length() > 0) {
        FileInputStream(dbFile).use { input ->
          FileOutputStream(stagedDbFile).use { output ->
            input.copyTo(output)
          }
        }
      }

      val monthlyFiles = generateMonthlyArchiveFiles(
        targetDir = stagingDir,
        trips = validTrips,
        accountEmail = cleanEmail,
        driverName = driverName,
        vehicleId = vehicleId
      )

      // Also generate bookings archive if bookings exist
      if (bookings.isNotEmpty()) {
        try {
          val bJson = JSONObject().apply {
            put("app_name", "CarHisab")
            put("file_type", "bookings_archive")
            put("account_email", cleanEmail)
            put("driver_name", driverName)
            put("vehicle_id", vehicleId)
            val bArr = JSONArray()
            for (b in bookings) {
              bArr.put(JSONObject().apply {
                put("id", b.id)
                put("passenger_name", b.passengerName)
                put("passenger_phone", b.passengerPhone)
                put("pickup_location", b.pickupLocation)
                put("drop_location", b.dropLocation)
                put("trip_date_millis", b.tripDateMillis)
                put("trip_date_string", b.tripDateString)
                put("trip_time_string", b.tripTimeString)
                put("total_fare", b.totalFare)
                put("advance_paid", b.advancePaid)
                put("due_fare", b.dueFare)
                put("status", b.status)
                put("notes", b.notes)
              })
            }
            put("bookings", bArr)
          }
          File(stagingDir, "CarHisab_Bookings_Archive.json").writeText(bJson.toString(2))
        } catch (be: Exception) {
          Log.w(TAG, "Error writing bookings archive: ${be.message}")
        }
      }

      // Create ZIP
      val backupDir = getBackupDirectory(context)
      val zipFile = File(backupDir, getDefaultBackupZipName())
      ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
        // Add DB
        if (stagedDbFile.exists() && stagedDbFile.length() > 0) {
          zos.putNextEntry(ZipEntry(MASTER_DB_NAME))
          FileInputStream(stagedDbFile).use { it.copyTo(zos) }
          zos.closeEntry()
        }
        // Add all JSON archives from staging
        stagingDir.listFiles()?.forEach { f ->
          if (f.name != MASTER_DB_NAME && f.exists() && f.length() > 0) {
            zos.putNextEntry(ZipEntry(f.name))
            FileInputStream(f).use { it.copyTo(zos) }
            zos.closeEntry()
          }
        }
      }

      // Cleanup staging dir
      stagingDir.deleteRecursively()

      // Also copy to Documents/CarHisab if available
      val docDir = getDocumentsBackupDir(context)
      if (docDir != null && zipFile.exists()) {
        try {
          val docZip = File(docDir, zipFile.name)
          FileInputStream(zipFile).use { input ->
            FileOutputStream(docZip).use { output ->
              input.copyTo(output)
            }
          }
        } catch (_: Exception) {}
      }

      if (zipFile.exists() && zipFile.length() > 0) zipFile else null
    } catch (e: Exception) {
      Log.e(TAG, "Error preparing backup zip: ${e.message}", e)
      null
    }
  }

  /**
   * Writes the complete backup zip file directly to a destination Uri (Google Drive or phone folder via SAF).
   */
  suspend fun writeBackupToUri(context: Context, destinationUri: Uri): Boolean = withContext(Dispatchers.IO) {
    try {
      val zipFile = prepareBackupZip(context) ?: return@withContext false
      val outputStream = context.contentResolver.openOutputStream(destinationUri) ?: return@withContext false
      outputStream.use { out ->
        FileInputStream(zipFile).use { inp ->
          inp.copyTo(out)
        }
      }

      val userPrefs = UserPreferencesRepository(context)
      userPrefs.setLastDriveBackupTime(System.currentTimeMillis())
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed writing backup to Uri: ${e.message}", e)
      false
    }
  }

  /**
   * Restores trips & bookings directly from a selected Uri (Google Drive or local storage).
   * Supports ZIP packages, raw SQLite .db files, and .json trip archives.
   */
  suspend fun restoreFromUri(
    context: Context,
    sourceUri: Uri,
    isBangla: Boolean = true
  ): RestoreResult = withContext(Dispatchers.IO) {
    try {
      val inputStream = context.contentResolver.openInputStream(sourceUri)
        ?: return@withContext RestoreResult.Error(
          if (isBangla) "ফাইলটি খোলা যায়নি। দয়া করে আবার নির্বাচন করুন।"
          else "Could not open selected file."
        )

      val tempDir = File(context.cacheDir, "restore_temp_${System.currentTimeMillis()}").apply { mkdirs() }
      val downloadedFile = File(tempDir, "source_backup")

      inputStream.use { input ->
        FileOutputStream(downloadedFile).use { output ->
          input.copyTo(output)
        }
      }

      if (!downloadedFile.exists() || downloadedFile.length() <= 0L) {
        tempDir.deleteRecursively()
        return@withContext RestoreResult.Error(
          if (isBangla) "নির্বাচিত ফাইলটি খালি।" else "Selected file is empty."
        )
      }

      val result = processBackupFileAndRestore(
        context = context,
        backupFile = downloadedFile,
        workDir = tempDir,
        currentAccountEmail = "",
        isBangla = isBangla
      )
      tempDir.deleteRecursively()
      result
    } catch (e: Exception) {
      Log.e(TAG, "Exception during restoreFromUri: ${e.message}", e)
      RestoreResult.Error(
        if (isBangla) "রিস্টোর ব্যর্থ: ${e.localizedMessage ?: e.message}"
        else "Restore failed: ${e.localizedMessage ?: e.message}"
      )
    }
  }

  /**
   * Automatic Device Backup: creates the ZIP package and stores in internal and Documents folders.
   */
  suspend fun performDeviceAutoBackup(
    context: Context,
    overrideAccountEmail: String = "",
    isBangla: Boolean = true
  ): BackupResult = withContext(Dispatchers.IO) {
    try {
      val zipFile = prepareBackupZip(context, overrideAccountEmail)
      if (zipFile == null) {
        val msg = if (isBangla) "ডাটাবেজ ফাঁকা - ব্যাকআপ নেওয়ার মতো তথ্য নেই।" else "Database is empty."
        return@withContext BackupResult.Empty(msg)
      }

      val userPrefs = UserPreferencesRepository(context)
      val now = System.currentTimeMillis()
      userPrefs.setLastDriveBackupTime(now)

      val db = AppDatabase.getDatabase(context)
      val count = db.tripDao().getAllTripsSnapshot().size

      val meta = BackupMetadata(
        exists = true,
        dateString = formatDateString(now, isBangla),
        sizeString = formatFileSize(zipFile.length(), isBangla),
        timestamp = now,
        sizeBytes = zipFile.length(),
        fileId = zipFile.name,
        monthlyFilesCount = count
      )
      BackupResult.Success(meta)
    } catch (e: Exception) {
      Log.e(TAG, "Device auto backup failed: ${e.message}", e)
      BackupResult.Error(e.localizedMessage ?: "Auto backup failed")
    }
  }

  /**
   * Automatically restores from any previously saved backup file on device.
   */
  suspend fun restoreFromDeviceAutoBackup(
    context: Context,
    currentAccountEmail: String = "",
    isBangla: Boolean = true
  ): RestoreResult = withContext(Dispatchers.IO) {
    try {
      val candidates = mutableListOf<File>()
      val backupDir = getBackupDirectory(context)
      backupDir.listFiles()?.filter { it.length() > 0 }?.let { candidates.addAll(it) }

      val docDir = getDocumentsBackupDir(context)
      docDir?.listFiles()?.filter { it.length() > 0 }?.let { candidates.addAll(it) }

      val sorted = candidates.sortedByDescending { it.lastModified() }
      val targetFile = sorted.firstOrNull { it.name.endsWith(".zip") }
        ?: sorted.firstOrNull { it.name.endsWith(".db") }
        ?: sorted.firstOrNull { it.name.endsWith(".json") }

      if (targetFile == null) {
        return@withContext RestoreResult.Error(
          if (isBangla) "ডিভাইসে কোনো ব্যাকআপ ফাইল পাওয়া যায়নি। 'গুগল ড্রাইভ / ফাইল বাছুন' অপশন দিয়ে ফাইল নির্বাচন করুন।"
          else "No auto-backup file found on device. Please select from Google Drive."
        )
      }

      val tempDir = File(context.cacheDir, "auto_restore_${System.currentTimeMillis()}").apply { mkdirs() }
      val copied = File(tempDir, targetFile.name)
      FileInputStream(targetFile).use { inp ->
        FileOutputStream(copied).use { out ->
          inp.copyTo(out)
        }
      }

      val result = processBackupFileAndRestore(context, copied, tempDir, currentAccountEmail, isBangla)
      tempDir.deleteRecursively()
      result
    } catch (e: Exception) {
      Log.e(TAG, "Auto restore error: ${e.message}", e)
      RestoreResult.Error(
        if (isBangla) "রিস্টোর ব্যর্থ: ${e.message}" else "Restore failed: ${e.message}"
      )
    }
  }

  /**
   * Inspects a backup file (zip, db, or json) and restores its records.
   */
  private suspend fun processBackupFileAndRestore(
    context: Context,
    backupFile: File,
    workDir: File,
    currentAccountEmail: String = "",
    isBangla: Boolean
  ): RestoreResult = withContext(Dispatchers.IO) {
    var dbCandidate: File? = null
    val jsonCandidates = mutableListOf<File>()

    // Check if ZIP
    val isZip = isZipFile(backupFile)
    if (isZip) {
      Log.d(TAG, "Unzipping backup archive: ${backupFile.name}")
      unzipArchive(backupFile, workDir)
      workDir.listFiles()?.forEach { file ->
        if (file.name.endsWith(".db") || file.name == MASTER_DB_NAME) {
          dbCandidate = file
        } else if (file.name.endsWith(".json") && file.name != MANIFEST_FILE_NAME) {
          jsonCandidates.add(file)
        }
      }
    } else if (isSqliteDb(backupFile)) {
      dbCandidate = backupFile
    } else if (backupFile.name.endsWith(".json") || isJsonFile(backupFile)) {
      jsonCandidates.add(backupFile)
    }

    if (currentAccountEmail.isNotBlank()) {
      val manifestFile = File(workDir, MANIFEST_FILE_NAME)
      if (manifestFile.exists()) {
        try {
          val mfJson = JSONObject(manifestFile.readText())
          val backedUpEmail = mfJson.optString("account_email", "")
          if (backedUpEmail.isNotBlank() && !backedUpEmail.equals(currentAccountEmail, ignoreCase = true)) {
            return@withContext RestoreResult.Error(
              if (isBangla) "ভিন্ন অ্যাকাউন্টের ব্যাকআপ ফাইল রিস্টোর করা সম্ভব নয়।"
              else "Cannot restore backup from a different account."
            )
          }
        } catch (_: Exception) {}
      }
    }

    var restoredTrips = 0
    var restoredBookings = 0

    // Priority 1: Restore SQLite database file
    if (dbCandidate != null && dbCandidate!!.length() > 0) {
      try {
        AppDatabase.closeDatabase()
        val targetDb = context.getDatabasePath(DB_NAME)
        targetDb.parentFile?.mkdirs()

        File(targetDb.path + "-shm").delete()
        File(targetDb.path + "-wal").delete()

        FileInputStream(dbCandidate!!).use { input ->
          FileOutputStream(targetDb).use { output ->
            input.copyTo(output)
          }
        }

        val restoredDb = AppDatabase.getDatabase(context)
        val allTrips = restoredDb.tripDao().getAllTripsSnapshot()
        if (allTrips.isNotEmpty()) {
          val auditedTrips = allTrips.map { ensureValidTripDate(it) }
          restoredDb.tripDao().insertTrips(auditedTrips)

          restoredTrips = auditedTrips.size
          restoredBookings = restoredDb.bookingDao().getAllBookingsSnapshot().size
          Log.d(TAG, "Restored directly from DB file: trips=$restoredTrips, bookings=$restoredBookings")
        }
      } catch (e: Exception) {
        Log.w(TAG, "Failed SQLite DB restore attempt: ${e.message}")
      }
    }

    // Priority 2: Restore from month-wise JSON files if DB restore didn't yield trips
    if (restoredTrips == 0 && jsonCandidates.isNotEmpty()) {
      val tripsToInsert = mutableListOf<TripEntity>()
      val bookingsToInsert = mutableListOf<BookingEntity>()
      for (jf in jsonCandidates) {
        try {
          val content = jf.readText()
          val root = JSONObject(content)
          val tripsArr = root.optJSONArray("trips")
          if (tripsArr != null) {
            for (i in 0 until tripsArr.length()) {
              val obj = tripsArr.getJSONObject(i)
              val tripId = obj.optLong("id", 0L)
              val trip = TripEntity(
                id = if (tripId > 0L) tripId else 0L,
                userId = obj.optString("user_id", ""),
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
          }

          val bookingsArr = root.optJSONArray("bookings")
          if (bookingsArr != null) {
            for (i in 0 until bookingsArr.length()) {
              val bObj = bookingsArr.getJSONObject(i)
              val bId = bObj.optLong("id", 0L)
              val booking = BookingEntity(
                id = if (bId > 0L) bId else 0L,
                passengerName = bObj.optString("passenger_name", ""),
                passengerPhone = bObj.optString("passenger_phone", ""),
                pickupLocation = bObj.optString("pickup_location", ""),
                dropLocation = bObj.optString("drop_location", ""),
                tripDateMillis = bObj.optLong("trip_date_millis", 0L),
                tripDateString = bObj.optString("trip_date_string", ""),
                tripTimeString = bObj.optString("trip_time_string", ""),
                totalFare = bObj.optDouble("total_fare", 0.0),
                advancePaid = bObj.optDouble("advance_paid", 0.0),
                dueFare = bObj.optDouble("due_fare", 0.0),
                status = bObj.optString("status", "CONFIRMED"),
                notes = bObj.optString("notes", "")
              )
              bookingsToInsert.add(booking)
            }
          }
        } catch (je: Exception) {
          Log.w(TAG, "Error reading json ${jf.name}: ${je.message}")
        }
      }

      if (tripsToInsert.isNotEmpty()) {
        val db = AppDatabase.getDatabase(context)
        db.tripDao().deleteAllTrips()
        db.tripDao().insertTrips(tripsToInsert)
        restoredTrips = db.tripDao().getAllTripsSnapshot().size
        Log.d(TAG, "Restored trips from JSON archive: count=$restoredTrips")
      }

      if (bookingsToInsert.isNotEmpty()) {
        val db = AppDatabase.getDatabase(context)
        db.bookingDao().deleteAllBookings()
        db.bookingDao().insertBookings(bookingsToInsert)
        restoredBookings = db.bookingDao().getAllBookingsSnapshot().size
        Log.d(TAG, "Restored bookings from JSON archive: count=$restoredBookings")
      }
    }

    if (restoredTrips == 0 && restoredBookings == 0) {
      val err = if (isBangla) "ফাইলটিতে রিস্টোর করার মতো কোনো ট্রিপ বা বুকিং তথ্য পাওয়া যায়নি।"
      else "No valid trip or booking records found in the backup file."
      return@withContext RestoreResult.Error(err)
    }

    // Build distinct month breakdown
    val finalDb = AppDatabase.getDatabase(context)
    val finalTrips = finalDb.tripDao().getAllTripsSnapshot()
    val breakdown = mutableMapOf<String, Int>()
    for (t in finalTrips) {
      val label = getMonthDisplay(t.dateMillis, isBangla)
      breakdown[label] = (breakdown[label] ?: 0) + 1
    }

    val userPrefs = UserPreferencesRepository(context)
    userPrefs.setLastDriveBackupTime(System.currentTimeMillis())
    userPrefs.setLastBackupTripCount(finalTrips.size)

    RestoreResult.Success(
      tripsCount = finalTrips.size,
      bookingsCount = restoredBookings,
      monthlyBreakdown = breakdown
    )
  }

  private fun isZipFile(file: File): Boolean {
    return try {
      FileInputStream(file).use { fis ->
        val header = ByteArray(4)
        val read = fis.read(header)
        read == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
            header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
      }
    } catch (_: Exception) {
      false
    }
  }

  private fun isSqliteDb(file: File): Boolean {
    return try {
      FileInputStream(file).use { fis ->
        val header = ByteArray(16)
        val read = fis.read(header)
        if (read >= 16) {
          String(header, 0, 16).startsWith("SQLite format 3")
        } else false
      }
    } catch (_: Exception) {
      false
    }
  }

  private fun isJsonFile(file: File): Boolean {
    return try {
      FileInputStream(file).use { fis ->
        val firstChar = fis.read()
        firstChar == '{'.code || firstChar == '['.code
      }
    } catch (_: Exception) {
      false
    }
  }

  private fun unzipArchive(zipFile: File, destinationDir: File) {
    ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
      var entry: ZipEntry? = zis.nextEntry
      while (entry != null) {
        val targetFile = File(destinationDir, entry.name)
        if (entry.isDirectory) {
          targetFile.mkdirs()
        } else {
          targetFile.parentFile?.mkdirs()
          FileOutputStream(targetFile).use { fos ->
            zis.copyTo(fos)
          }
        }
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }
  }

  /**
   * Creates an Android Share Intent for direct 1-tap sharing to Google Drive, WhatsApp, Gmail, etc.
   */
  suspend fun createShareBackupIntent(context: Context): Intent? = withContext(Dispatchers.IO) {
    try {
      val zipFile = prepareBackupZip(context) ?: return@withContext null
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        zipFile
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "CarHisab Backup (${zipFile.name})")
        putExtra(
          Intent.EXTRA_TEXT,
          "CarHisab সম্পূর্ণ ট্রিপ ও গাড়ির হিসাব ব্যাকআপ ফাইল। গুগল ড্রাইভে সেভ করুন অথবা নিরাপদে সংরক্ষণ করুন।"
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      Intent.createChooser(shareIntent, "গুগল ড্রাইভ অথবা স্টোরেজে ব্যাকআপ সেভ করুন")
    } catch (e: Exception) {
      Log.e(TAG, "Error creating share intent: ${e.message}", e)
      null
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
      val backupDir = getBackupDirectory(context)
      val docDir = getDocumentsBackupDir(context)

      val files = mutableListOf<File>()
      backupDir.listFiles()?.filter { it.length() > 0 }?.let { files.addAll(it) }
      docDir?.listFiles()?.filter { it.length() > 0 }?.let { files.addAll(it) }

      val latest = files.maxByOrNull { it.lastModified() }
      if (latest != null) {
        val ts = latest.lastModified()
        val size = latest.length()
        return@withContext BackupMetadata(
          exists = true,
          dateString = formatDateString(ts, isBangla),
          sizeString = formatFileSize(size, isBangla),
          timestamp = ts,
          sizeBytes = size,
          fileId = latest.name
        )
      }

      val lastTime = userPrefs.getLastDriveBackupTime()
      if (lastTime > 0L) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val size = if (dbFile.exists()) dbFile.length() else 102400L
        return@withContext BackupMetadata(
          exists = true,
          dateString = formatDateString(lastTime, isBangla),
          sizeString = formatFileSize(size, isBangla),
          timestamp = lastTime,
          sizeBytes = size,
          fileId = "device_backup"
        )
      }

      BackupMetadata(exists = false)
    } catch (e: Exception) {
      BackupMetadata(exists = false)
    }
  }

  /**
   * Compatibility alias for performBackup
   */
  suspend fun performBackup(
    context: Context,
    accessToken: String? = null,
    currentAccountEmail: String = "",
    isBangla: Boolean = true
  ): BackupResult {
    return performDeviceAutoBackup(context, currentAccountEmail, isBangla)
  }

  /**
   * Compatibility alias for restoreBackup
   */
  suspend fun restoreBackup(
    context: Context,
    accessToken: String? = null,
    currentAccountEmail: String = "",
    isBangla: Boolean = true
  ): RestoreResult {
    return restoreFromDeviceAutoBackup(context, currentAccountEmail, isBangla)
  }
}
