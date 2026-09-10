package com.example.data.drive

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.BookingEntity
import com.example.data.model.MobilServiceInfo
import com.example.data.model.TripEntity
import com.example.data.model.VehicleDocuments
import com.example.data.repository.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriveBackupPayload(
  val version: Int = 2,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedDate: String = "",
  val appName: String = "Car Hisab",
  val profile: UserProfile,
  val totalTrips: Int,
  val trips: List<TripEntity>
)

data class RestoredDataPayload(
  val trips: List<TripEntity> = emptyList(),
  val bookings: List<BookingEntity> = emptyList(),
  val profile: UserProfile? = null,
  val vehicleDocuments: VehicleDocuments? = null,
  val mobilServiceInfo: MobilServiceInfo? = null
)

object GoogleDriveBackupManager {

  private const val TAG = "GoogleDriveBackup"
  private const val BACKUP_DIR_NAME = "google_drive_backups"
  private const val SECRET_KEY = "CarHisabSecureDriveKey2026#X9"
  const val BACKUP_FILENAME = "carhisab_backup.enc"
  const val MIME_TYPE_JSON = "application/json"

  /**
   * Builds GoogleSignInOptions configured with proper Google Drive scopes:
   * DriveScopes.DRIVE_APPDATA and DriveScopes.DRIVE_FILE.
   */
  fun getGoogleSignInOptions(): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .requestScopes(
        Scope(DriveScopes.DRIVE_APPDATA),
        Scope(DriveScopes.DRIVE_FILE)
      )
      .build()
  }

  /**
   * Returns GoogleSignInClient configured for Drive scope authentication.
   */
  fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    return GoogleSignIn.getClient(context, getGoogleSignInOptions())
  }

  /**
   * Gets signed-in Google account if available and permissions for Drive scopes are granted.
   */
  fun getSignedInAccount(context: Context): GoogleSignInAccount? {
    val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
    val appDataScope = Scope(DriveScopes.DRIVE_APPDATA)
    val fileScope = Scope(DriveScopes.DRIVE_FILE)
    if (GoogleSignIn.hasPermissions(account, appDataScope) || GoogleSignIn.hasPermissions(account, fileScope)) {
      return account
    }
    return null
  }

  /**
   * Builds an authenticated Google Drive API service instance.
   */
  fun getDriveService(context: Context, account: GoogleSignInAccount): Drive {
    val credential = GoogleAccountCredential.usingOAuth2(
      context,
      listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
    ).apply {
      selectedAccount = account.account
    }

    return Drive.Builder(
      NetHttpTransport(),
      GsonFactory.getDefaultInstance(),
      credential
    )
      .setApplicationName("Car Hisab")
      .build()
  }

  /**
   * Encrypts plaintext JSON string using XOR cipher and Base64 encoding for secure Drive storage.
   */
  fun encryptData(plainText: String): String {
    return try {
      val keyBytes = SECRET_KEY.toByteArray(Charsets.UTF_8)
      val textBytes = plainText.toByteArray(Charsets.UTF_8)
      val encryptedBytes = ByteArray(textBytes.size)
      for (i in textBytes.indices) {
        encryptedBytes[i] = (textBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
      }
      val base64Encoded = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
      "CARHISAB_SECURE_V1:$base64Encoded"
    } catch (e: Exception) {
      Log.e(TAG, "Encryption failed", e)
      plainText
    }
  }

  /**
   * Decrypts secure encrypted payload back to plaintext JSON.
   */
  fun decryptData(dataString: String): String {
    val trimmedData = dataString.trim()
    return try {
      if (!trimmedData.startsWith("CARHISAB_SECURE_V1:")) {
        // Fallback for legacy unencrypted JSON backups
        return trimmedData
      }
      val base64Encoded = trimmedData.removePrefix("CARHISAB_SECURE_V1:").trim()
      val encryptedBytes = Base64.decode(base64Encoded, Base64.DEFAULT)
      val keyBytes = SECRET_KEY.toByteArray(Charsets.UTF_8)
      val decryptedBytes = ByteArray(encryptedBytes.size)
      for (i in encryptedBytes.indices) {
        decryptedBytes[i] = (encryptedBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
      }
      String(decryptedBytes, Charsets.UTF_8)
    } catch (e: Exception) {
      Log.e(TAG, "Decryption failed", e)
      trimmedData
    }
  }

  /**
   * Serializes ALL app data (trips, bookings, profile, vehicle documents, mobil service info) into JSON.
   */
  fun serializeFullBackupJson(
    trips: List<TripEntity>,
    bookings: List<BookingEntity>,
    profile: UserProfile,
    documents: VehicleDocuments,
    mobilService: MobilServiceInfo
  ): String {
    val root = JSONObject()
    root.put("version", 2)
    root.put("app", "CarHisab")
    root.put("timestamp", System.currentTimeMillis())
    root.put(
      "backupDate",
      SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
    )

    // 1. Profile metadata
    val profileObj = JSONObject().apply {
      put("carName", profile.carName)
      put("carModel", profile.carModel)
      put("carNumber", profile.carNumber)
      put("driverName", profile.driverName)
      put("driverNameBangla", profile.driverNameBangla)
      put("driverNameEnglish", profile.driverNameEnglish)
      put("birthDate", profile.birthDate)
      put("driverPhone", profile.driverPhone)
      put("driverEmail", profile.driverEmail)
      put("profileImageUri", profile.profileImageUri ?: "")
      put("rememberEmail", profile.rememberEmail)
      put("savedEmail", profile.savedEmail)
      put("nidFrontAttached", profile.nidFrontAttached)
      put("nidBackAttached", profile.nidBackAttached)
      put("selfieAttached", profile.selfieAttached)
      put("isProfileCompleted", profile.isProfileCompleted)
      put("isLoggedIn", profile.isLoggedIn)
      put("isFreeTrialActive", profile.isFreeTrialActive)
      put("trialMonthsRemaining", profile.trialMonthsRemaining)
      put("registrationDateMillis", profile.registrationDateMillis)
      put("subscriptionExpiryMillis", profile.subscriptionExpiryMillis)
      put("userUniqueKey", profile.userUniqueKey)
      put("currentPlanName", profile.currentPlanName)
    }
    root.put("profile", profileObj)

    // 2. Trips array
    val tripsArray = JSONArray()
    for (trip in trips) {
      val tObj = JSONObject().apply {
        put("id", trip.id)
        put("dateMillis", trip.dateMillis)
        put("dateString", trip.dateString)
        put("place", trip.place)
        put("rent", trip.rent)
        put("gratuity", trip.gratuity)
        put("maintenanceCost", trip.maintenanceCost)
        put("kmDriven", trip.kmDriven)
        put("description", trip.description)
        put("passengerName", trip.passengerName)
        put("passengerPhone", trip.passengerPhone)
        put("income", trip.income)
        put("profit", trip.profit)
      }
      tripsArray.put(tObj)
    }
    root.put("tripsCount", trips.size)
    root.put("trips", tripsArray)

    // 3. Bookings array
    val bookingsArray = JSONArray()
    for (b in bookings) {
      val bObj = JSONObject().apply {
        put("id", b.id)
        put("passengerName", b.passengerName)
        put("passengerPhone", b.passengerPhone)
        put("pickupLocation", b.pickupLocation)
        put("dropLocation", b.dropLocation)
        put("tripDateMillis", b.tripDateMillis)
        put("tripDateString", b.tripDateString)
        put("tripTimeString", b.tripTimeString)
        put("totalFare", b.totalFare)
        put("advancePaid", b.advancePaid)
        put("dueFare", b.dueFare)
        put("status", b.status)
        put("notes", b.notes)
        put("createdAtMillis", b.createdAtMillis)
      }
      bookingsArray.put(bObj)
    }
    root.put("bookingsCount", bookings.size)
    root.put("bookings", bookingsArray)

    // 4. Vehicle Documents
    val docsObj = JSONObject().apply {
      put("taxTokenExpiryMillis", documents.taxTokenExpiryMillis)
      put("taxTokenNumber", documents.taxTokenNumber)
      put("fitnessExpiryMillis", documents.fitnessExpiryMillis)
      put("fitnessNumber", documents.fitnessNumber)
      put("routePermitExpiryMillis", documents.routePermitExpiryMillis)
      put("routePermitNumber", documents.routePermitNumber)
      put("insuranceExpiryMillis", documents.insuranceExpiryMillis)
      put("insuranceNumber", documents.insuranceNumber)
      put("drivingLicenseExpiryMillis", documents.drivingLicenseExpiryMillis)
      put("drivingLicenseNumber", documents.drivingLicenseNumber)
    }
    root.put("vehicleDocuments", docsObj)

    // 5. Mobil Service Info
    val mobilObj = JSONObject().apply {
      put("currentOdometerKm", mobilService.currentOdometerKm)
      put("lastMobilChangeKm", mobilService.lastMobilChangeKm)
      put("mobilChangeIntervalKm", mobilService.mobilChangeIntervalKm)
      put("lastMobilChangeDateMillis", mobilService.lastMobilChangeDateMillis)
      put("mobilBrandGrade", mobilService.mobilBrandGrade)
      put("lastBrakeCheckKm", mobilService.lastBrakeCheckKm)
      put("lastAirFilterKm", mobilService.lastAirFilterKm)
      put("lastGearOilKm", mobilService.lastGearOilKm)
      put("generalNotes", mobilService.generalNotes)
    }
    root.put("mobilServiceInfo", mobilObj)

    return root.toString(2)
  }

  /**
   * Legacy method for serializing trips and profile.
   */
  fun serializeBackupJson(
    trips: List<TripEntity>,
    profile: UserProfile
  ): String {
    return serializeFullBackupJson(
      trips = trips,
      bookings = emptyList(),
      profile = profile,
      documents = VehicleDocuments(),
      mobilService = MobilServiceInfo()
    )
  }

  /**
   * Parses a JSON string back into a list of TripEntities.
   */
  fun parseBackupJson(jsonString: String): List<TripEntity> {
    if (jsonString.isBlank()) return emptyList()
    val root = try {
      JSONObject(jsonString)
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing trips JSON", e)
      return emptyList()
    }
    val tripsArray = root.optJSONArray("trips") ?: return emptyList()

    val list = mutableListOf<TripEntity>()
    for (i in 0 until tripsArray.length()) {
      val tObj = tripsArray.getJSONObject(i)
      val trip = TripEntity(
        id = tObj.optLong("id", 0L),
        dateMillis = tObj.optLong("dateMillis", System.currentTimeMillis()),
        dateString = tObj.optString("dateString", ""),
        place = tObj.optString("place", ""),
        rent = tObj.optDouble("rent", 0.0),
        gratuity = tObj.optDouble("gratuity", 0.0),
        maintenanceCost = tObj.optDouble("maintenanceCost", 0.0),
        kmDriven = tObj.optDouble("kmDriven", 0.0),
        description = tObj.optString("description", ""),
        passengerName = tObj.optString("passengerName", ""),
        passengerPhone = tObj.optString("passengerPhone", ""),
        income = tObj.optDouble("income", 0.0),
        profit = tObj.optDouble("profit", 0.0)
      )
      list.add(trip)
    }
    return list
  }

  /**
   * Parses JSON backup content into RestoredDataPayload containing all app data.
   */
  fun parseFullBackupJson(jsonString: String): RestoredDataPayload {
    if (jsonString.isBlank()) return RestoredDataPayload()

    val root = try {
      JSONObject(jsonString)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse JSON string", e)
      return RestoredDataPayload()
    }

    // Trips
    val tripsList = parseBackupJson(jsonString)

    // Bookings
    val bookingsList = mutableListOf<BookingEntity>()
    val bookingsArray = root.optJSONArray("bookings")
    if (bookingsArray != null) {
      for (i in 0 until bookingsArray.length()) {
        val bObj = bookingsArray.getJSONObject(i)
        val booking = BookingEntity(
          id = bObj.optLong("id", 0L),
          passengerName = bObj.optString("passengerName", ""),
          passengerPhone = bObj.optString("passengerPhone", ""),
          pickupLocation = bObj.optString("pickupLocation", ""),
          dropLocation = bObj.optString("dropLocation", ""),
          tripDateMillis = bObj.optLong("tripDateMillis", System.currentTimeMillis()),
          tripDateString = bObj.optString("tripDateString", ""),
          tripTimeString = bObj.optString("tripTimeString", "08:00 AM"),
          totalFare = bObj.optDouble("totalFare", 0.0),
          advancePaid = bObj.optDouble("advancePaid", 0.0),
          dueFare = bObj.optDouble("dueFare", 0.0),
          status = bObj.optString("status", "CONFIRMED"),
          notes = bObj.optString("notes", ""),
          createdAtMillis = bObj.optLong("createdAtMillis", System.currentTimeMillis())
        )
        bookingsList.add(booking)
      }
    }

    // Profile
    var restoredProfile: UserProfile? = null
    val pObj = root.optJSONObject("profile")
    if (pObj != null) {
      val photoUri = pObj.optString("profileImageUri", "").ifEmpty { null }
      restoredProfile = UserProfile(
        carName = pObj.optString("carName", ""),
        carModel = pObj.optString("carModel", ""),
        carNumber = pObj.optString("carNumber", ""),
        driverName = pObj.optString("driverName", ""),
        driverNameBangla = pObj.optString("driverNameBangla", ""),
        driverNameEnglish = pObj.optString("driverNameEnglish", ""),
        birthDate = pObj.optString("birthDate", ""),
        driverPhone = pObj.optString("driverPhone", ""),
        driverEmail = pObj.optString("driverEmail", ""),
        profileImageUri = photoUri,
        rememberEmail = pObj.optBoolean("rememberEmail", false),
        savedEmail = pObj.optString("savedEmail", ""),
        nidFrontAttached = pObj.optBoolean("nidFrontAttached", false),
        nidBackAttached = pObj.optBoolean("nidBackAttached", false),
        selfieAttached = pObj.optBoolean("selfieAttached", false),
        isProfileCompleted = pObj.optBoolean("isProfileCompleted", true),
        isLoggedIn = pObj.optBoolean("isLoggedIn", true),
        isFreeTrialActive = pObj.optBoolean("isFreeTrialActive", true),
        trialMonthsRemaining = pObj.optInt("trialMonthsRemaining", 6),
        registrationDateMillis = pObj.optLong("registrationDateMillis", System.currentTimeMillis()),
        subscriptionExpiryMillis = pObj.optLong("subscriptionExpiryMillis", Long.MAX_VALUE),
        userUniqueKey = pObj.optString("userUniqueKey", "CH-84920"),
        currentPlanName = pObj.optString("currentPlanName", "লাইফটাইম আনলিমিটেড")
      )
    }

    // Vehicle Documents
    var restoredDocs: VehicleDocuments? = null
    val dObj = root.optJSONObject("vehicleDocuments")
    if (dObj != null) {
      restoredDocs = VehicleDocuments(
        taxTokenExpiryMillis = dObj.optLong("taxTokenExpiryMillis", System.currentTimeMillis()),
        taxTokenNumber = dObj.optString("taxTokenNumber", ""),
        fitnessExpiryMillis = dObj.optLong("fitnessExpiryMillis", System.currentTimeMillis()),
        fitnessNumber = dObj.optString("fitnessNumber", ""),
        routePermitExpiryMillis = dObj.optLong("routePermitExpiryMillis", System.currentTimeMillis()),
        routePermitNumber = dObj.optString("routePermitNumber", ""),
        insuranceExpiryMillis = dObj.optLong("insuranceExpiryMillis", System.currentTimeMillis()),
        insuranceNumber = dObj.optString("insuranceNumber", ""),
        drivingLicenseExpiryMillis = dObj.optLong("drivingLicenseExpiryMillis", System.currentTimeMillis()),
        drivingLicenseNumber = dObj.optString("drivingLicenseNumber", "")
      )
    }

    // Mobil Service Info
    var restoredMobil: MobilServiceInfo? = null
    val mObj = root.optJSONObject("mobilServiceInfo")
    if (mObj != null) {
      restoredMobil = MobilServiceInfo(
        currentOdometerKm = mObj.optDouble("currentOdometerKm", 0.0),
        lastMobilChangeKm = mObj.optDouble("lastMobilChangeKm", 0.0),
        mobilChangeIntervalKm = mObj.optDouble("mobilChangeIntervalKm", 3000.0),
        lastMobilChangeDateMillis = mObj.optLong("lastMobilChangeDateMillis", System.currentTimeMillis()),
        mobilBrandGrade = mObj.optString("mobilBrandGrade", ""),
        lastBrakeCheckKm = mObj.optDouble("lastBrakeCheckKm", 0.0),
        lastAirFilterKm = mObj.optDouble("lastAirFilterKm", 0.0),
        lastGearOilKm = mObj.optDouble("lastGearOilKm", 0.0),
        generalNotes = mObj.optString("generalNotes", "")
      )
    }

    return RestoredDataPayload(
      trips = tripsList,
      bookings = bookingsList,
      profile = restoredProfile,
      vehicleDocuments = restoredDocs,
      mobilServiceInfo = restoredMobil
    )
  }

  /**
   * Uploads or overwrites encrypted full backup JSON in Google Drive.
   */
  suspend fun uploadBackupToDrive(
    context: Context,
    account: GoogleSignInAccount,
    jsonContent: String
  ): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val driveService = getDriveService(context, account)
      val encryptedData = encryptData(jsonContent)
      val mediaContent = ByteArrayContent(MIME_TYPE_JSON, encryptedData.toByteArray(Charsets.UTF_8))

      // Search in appDataFolder
      var query = "'appDataFolder' in parents and name = '$BACKUP_FILENAME' and trashed = false"
      var fileList = driveService.files().list()
        .setSpaces("appDataFolder")
        .setQ(query)
        .setFields("files(id, name)")
        .execute()

      var existingFile = fileList.files.firstOrNull()

      // Fallback: search in root drive space
      if (existingFile == null) {
        query = "name = '$BACKUP_FILENAME' and trashed = false"
        fileList = driveService.files().list()
          .setQ(query)
          .setFields("files(id, name)")
          .execute()
        existingFile = fileList.files.firstOrNull()
      }

      if (existingFile != null) {
        val fileMetadata = DriveFile().setName(BACKUP_FILENAME)
        driveService.files().update(existingFile.id, fileMetadata, mediaContent).execute()
        Log.d(TAG, "Successfully updated backup in Drive: ${existingFile.id}")
      } else {
        val fileMetadata = DriveFile()
          .setName(BACKUP_FILENAME)
          .setParents(listOf("appDataFolder"))

        val newFile = driveService.files().create(fileMetadata, mediaContent)
          .setFields("id")
          .execute()
        Log.d(TAG, "Successfully created backup in Drive: ${newFile.id}")
      }

      Result.success(1)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to upload backup to Google Drive", e)
      Result.failure(e)
    }
  }

  /**
   * Checks and downloads backup file automatically from Google Drive.
   */
  suspend fun downloadBackupFromDrive(
    context: Context,
    account: GoogleSignInAccount
  ): Result<RestoredDataPayload> = withContext(Dispatchers.IO) {
    try {
      val driveService = getDriveService(context, account)

      // Search in appDataFolder
      var query = "'appDataFolder' in parents and name = '$BACKUP_FILENAME' and trashed = false"
      var fileList = driveService.files().list()
        .setSpaces("appDataFolder")
        .setQ(query)
        .setFields("files(id, name, size)")
        .execute()

      var targetFile = fileList.files.firstOrNull()

      // Fallback: search in root drive space
      if (targetFile == null) {
        query = "name = '$BACKUP_FILENAME' and trashed = false"
        fileList = driveService.files().list()
          .setQ(query)
          .setFields("files(id, name, size)")
          .execute()
        targetFile = fileList.files.firstOrNull()
      }

      if (targetFile == null) {
        return@withContext Result.failure(
          NoSuchElementException("গুগল ড্রাইভে কোনো ব্যাকআপ ফাইল পাওয়া যায়নি। / No backup file found on Google Drive.")
        )
      }

      val fileId = targetFile.id
      val outputStream = java.io.ByteArrayOutputStream()
      driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
      val rawContent = outputStream.toString("UTF-8")

      if (rawContent.isBlank()) {
        return@withContext Result.failure(
          IllegalStateException("ড্রাইভের ব্যাকআপ ফাইলটি খালি। / Drive backup file is empty.")
        )
      }

      val decryptedJson = decryptData(rawContent)
      if (decryptedJson.isBlank()) {
        return@withContext Result.failure(
          IllegalStateException("ব্যাকআপ ফাইলের ডাটা পড়া যাচ্ছে না। / Failed to parse backup file content.")
        )
      }

      val payload = parseFullBackupJson(decryptedJson)
      Result.success(payload)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to download backup from Google Drive", e)
      Result.failure(e)
    }
  }

  /**
   * Generates a local backup file on disk inside internal cache (ENCRYPTED).
   */
  fun createLocalBackupFile(
    context: Context,
    trips: List<TripEntity>,
    bookings: List<BookingEntity> = emptyList(),
    profile: UserProfile,
    documents: VehicleDocuments? = null,
    mobilService: MobilServiceInfo? = null
  ): File {
    val dir = File(context.cacheDir, BACKUP_DIR_NAME).apply { mkdirs() }
    val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(dir, "CarHisab_SecureBackup_$dateStamp.carhisab.enc")

    val json = if (documents != null && mobilService != null) {
      serializeFullBackupJson(trips, bookings, profile, documents, mobilService)
    } else {
      serializeBackupJson(trips, profile)
    }
    val encryptedJson = encryptData(json)

    FileOutputStream(file).use { out ->
      out.write(encryptedJson.toByteArray(Charsets.UTF_8))
      out.flush()
    }

    val latestFile = File(dir, "CarHisab_Latest_SecureBackup.enc")
    FileOutputStream(latestFile).use { out ->
      out.write(encryptedJson.toByteArray(Charsets.UTF_8))
      out.flush()
    }

    return file
  }

  /**
   * Checks if a local backup file exists for instant restore.
   */
  fun getLatestLocalBackupFile(context: Context): File? {
    val dir = File(context.cacheDir, BACKUP_DIR_NAME)
    if (!dir.exists()) return null
    val latestFile = File(dir, "CarHisab_Latest_SecureBackup.enc")
    if (latestFile.exists() && latestFile.length() > 0) return latestFile
    val legacyLatest = File(dir, "CarHisab_Latest_Backup.json")
    if (legacyLatest.exists() && legacyLatest.length() > 0) return legacyLatest
    return dir.listFiles { f -> f.extension == "enc" || f.extension == "json" }?.maxByOrNull { it.lastModified() }
  }

  /**
   * Restores trips from the latest local backup file (DECRYPTED).
   */
  fun readTripsFromLatestBackup(context: Context): List<TripEntity> {
    val file = getLatestLocalBackupFile(context) ?: return emptyList()
    return try {
      val rawContent = FileInputStream(file).bufferedReader(Charsets.UTF_8).use { it.readText() }
      val decryptedJson = decryptData(rawContent)
      parseBackupJson(decryptedJson)
    } catch (e: Exception) {
      Log.e(TAG, "Failed reading/decrypting backup file", e)
      emptyList()
    }
  }

  /**
   * Opens Android system share sheet with Google Drive target enabled.
   */
  fun openDriveSaveIntent(
    context: Context,
    backupFile: File
  ) {
    try {
      val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        backupFile
      )

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Car Hisab Secure Encrypted Drive Backup")
        putExtra(Intent.EXTRA_TEXT, "Car Hisab Secure Encrypted Cloud Backup - ${backupFile.name}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      val chooser = Intent.createChooser(intent, "Save Secure Backup to Google Drive / ড্রাইভে সেভ করুন")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      Log.e(TAG, "Error initiating Google Drive share intent", e)
    }
  }

  /**
   * Restores full payload from Uri chosen via Storage Access Framework.
   */
  fun readFullPayloadFromUri(context: Context, uri: Uri): RestoredDataPayload {
    return try {
      val rawContent = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: return RestoredDataPayload()
      val decryptedJson = decryptData(rawContent)
      parseFullBackupJson(decryptedJson)
    } catch (e: Exception) {
      Log.e(TAG, "Failed reading/decrypting backup file from Uri", e)
      RestoredDataPayload()
    }
  }

  /**
   * Restores trips from a specific file Uri.
   */
  fun readTripsFromUri(context: Context, uri: Uri): List<TripEntity> {
    return readFullPayloadFromUri(context, uri).trips
  }
}
