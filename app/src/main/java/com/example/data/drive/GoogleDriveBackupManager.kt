package com.example.data.drive

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.data.model.BookingEntity
import com.example.data.model.MobilServiceInfo
import com.example.data.model.TripEntity
import com.example.data.model.VehicleDocuments
import com.example.data.repository.UserProfile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class BackupDataPayload(
  val version: Int = 1,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedDate: String = "",
  val appName: String = "Car Hisab",
  val profile: UserProfile? = null,
  val documents: VehicleDocuments? = null,
  val mobilService: MobilServiceInfo? = null,
  val trips: List<TripEntity> = emptyList(),
  val bookings: List<BookingEntity> = emptyList()
)

object GoogleDriveBackupManager {

  private const val TAG = "GoogleDriveBackup"
  private const val SECRET_KEY = "CarHisabSecureDriveKey2026#X9"

  /**
   * OAuth Scope for hidden Google Drive AppData folder access.
   */
  const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
  private const val BACKUP_FILE_NAME = "car_hisab_backup.json"
  private const val APPDATA_SPACE = "appDataFolder"

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  /**
   * Decrypts secure encrypted payload back to plaintext JSON if encrypted.
   */
  private fun decryptData(dataString: String): String {
    val trimmedData = dataString.trim()
    return try {
      if (!trimmedData.startsWith("CARHISAB_SECURE_V1:")) {
        return trimmedData
      }
      val base64Encoded = trimmedData.removePrefix("CARHISAB_SECURE_V1:").trim()
      val encryptedBytes = try {
        Base64.decode(base64Encoded, Base64.DEFAULT)
      } catch (_: Throwable) {
        java.util.Base64.getDecoder().decode(base64Encoded)
      }
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
   * Serializes trips, bookings, profile metadata, vehicle documents, and mobil service info
   * into a formatted JSON backup string.
   */
  fun serializeBackupJson(
    trips: List<TripEntity>,
    profile: UserProfile,
    documents: VehicleDocuments? = null,
    mobilService: MobilServiceInfo? = null,
    bookings: List<BookingEntity> = emptyList()
  ): String {
    val root = JSONObject()
    root.put("version", 1)
    root.put("app", "CarHisab")
    root.put("timestamp", System.currentTimeMillis())
    root.put(
      "backupDate",
      SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
    )

    // Profile metadata
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
      put("userUniqueKey", profile.userUniqueKey)
    }
    root.put("profile", profileObj)

    // Vehicle Documents metadata
    if (documents != null) {
      val docObj = JSONObject().apply {
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
      root.put("documents", docObj)
    }

    // Mobil Service metadata
    if (mobilService != null) {
      val mobObj = JSONObject().apply {
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
      root.put("mobilService", mobObj)
    }

    // Trips array
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

    // Bookings array
    val bookingsArray = JSONArray()
    for (booking in bookings) {
      val bObj = JSONObject().apply {
        put("id", booking.id)
        put("passengerName", booking.passengerName)
        put("passengerPhone", booking.passengerPhone)
        put("pickupLocation", booking.pickupLocation)
        put("dropLocation", booking.dropLocation)
        put("tripDateMillis", booking.tripDateMillis)
        put("tripDateString", booking.tripDateString)
        put("tripTimeString", booking.tripTimeString)
        put("totalFare", booking.totalFare)
        put("advancePaid", booking.advancePaid)
        put("dueFare", booking.dueFare)
        put("status", booking.status)
        put("notes", booking.notes)
      }
      bookingsArray.put(bObj)
    }
    root.put("bookingsCount", bookings.size)
    root.put("bookings", bookingsArray)

    return root.toString(2)
  }

  /**
   * Safely parses JSON string back into a BackupDataPayload data structure without crashing.
   */
  fun parseBackupPayload(jsonString: String): BackupDataPayload {
    val decrypted = decryptData(jsonString)
    val root = try {
      JSONObject(decrypted)
    } catch (e: Exception) {
      Log.e(TAG, "Invalid JSON structure in backup file", e)
      return BackupDataPayload()
    }

    val version = root.optInt("version", 1)
    val timestamp = root.optLong("timestamp", System.currentTimeMillis())
    val backupDate = root.optString("backupDate", "")

    // Profile parsing
    val profile = root.optJSONObject("profile")?.let { pObj ->
      UserProfile(
        carName = pObj.optString("carName", ""),
        carModel = pObj.optString("carModel", ""),
        carNumber = pObj.optString("carNumber", ""),
        driverName = pObj.optString("driverName", ""),
        driverNameBangla = pObj.optString("driverNameBangla", ""),
        driverNameEnglish = pObj.optString("driverNameEnglish", ""),
        birthDate = pObj.optString("birthDate", ""),
        driverPhone = pObj.optString("driverPhone", ""),
        driverEmail = pObj.optString("driverEmail", ""),
        userUniqueKey = pObj.optString("userUniqueKey", "CH-84920")
      )
    }

    // Documents parsing
    val documents = root.optJSONObject("documents")?.let { dObj ->
      VehicleDocuments(
        taxTokenExpiryMillis = dObj.optLong("taxTokenExpiryMillis", 0L),
        taxTokenNumber = dObj.optString("taxTokenNumber", ""),
        fitnessExpiryMillis = dObj.optLong("fitnessExpiryMillis", 0L),
        fitnessNumber = dObj.optString("fitnessNumber", ""),
        routePermitExpiryMillis = dObj.optLong("routePermitExpiryMillis", 0L),
        routePermitNumber = dObj.optString("routePermitNumber", ""),
        insuranceExpiryMillis = dObj.optLong("insuranceExpiryMillis", 0L),
        insuranceNumber = dObj.optString("insuranceNumber", ""),
        drivingLicenseExpiryMillis = dObj.optLong("drivingLicenseExpiryMillis", 0L),
        drivingLicenseNumber = dObj.optString("drivingLicenseNumber", "")
      )
    }

    // Mobil service parsing
    val mobilService = root.optJSONObject("mobilService")?.let { mObj ->
      MobilServiceInfo(
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

    // Trips parsing
    val tripsList = mutableListOf<TripEntity>()
    val tripsArray = root.optJSONArray("trips")
    if (tripsArray != null) {
      for (i in 0 until tripsArray.length()) {
        try {
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
          tripsList.add(trip)
        } catch (e: Exception) {
          Log.e(TAG, "Failed parsing trip at index $i", e)
        }
      }
    }

    // Bookings parsing
    val bookingsList = mutableListOf<BookingEntity>()
    val bookingsArray = root.optJSONArray("bookings")
    if (bookingsArray != null) {
      for (i in 0 until bookingsArray.length()) {
        try {
          val bObj = bookingsArray.getJSONObject(i)
          val booking = BookingEntity(
            id = bObj.optLong("id", 0L),
            passengerName = bObj.optString("passengerName", ""),
            passengerPhone = bObj.optString("passengerPhone", ""),
            pickupLocation = bObj.optString("pickupLocation", ""),
            dropLocation = bObj.optString("dropLocation", ""),
            tripDateMillis = bObj.optLong("tripDateMillis", System.currentTimeMillis()),
            tripDateString = bObj.optString("tripDateString", ""),
            tripTimeString = bObj.optString("tripTimeString", ""),
            totalFare = bObj.optDouble("totalFare", 0.0),
            advancePaid = bObj.optDouble("advancePaid", 0.0),
            dueFare = bObj.optDouble("dueFare", 0.0),
            status = bObj.optString("status", "CONFIRMED"),
            notes = bObj.optString("notes", "")
          )
          bookingsList.add(booking)
        } catch (e: Exception) {
          Log.e(TAG, "Failed parsing booking at index $i", e)
        }
      }
    }

    return BackupDataPayload(
      version = version,
      timestamp = timestamp,
      formattedDate = backupDate,
      appName = root.optString("app", "CarHisab"),
      profile = profile,
      documents = documents,
      mobilService = mobilService,
      trips = tripsList,
      bookings = bookingsList
    )
  }

  /**
   * Helper function for legacy callers parsing json string to Trip list.
   */
  fun parseBackupJson(jsonString: String): List<TripEntity> {
    return parseBackupPayload(jsonString).trips
  }

  private fun getLocalBackupFile(context: Context): File {
    val dir = File(context.filesDir, "drive_appdata").apply { mkdirs() }
    return File(dir, BACKUP_FILE_NAME)
  }

  /**
   * Automatic background and foreground backup directly to the app's hidden AppData folder.
   */
  fun backupToAppDataFolder(
    context: Context,
    jsonContent: String,
    accessToken: String? = null
  ): Boolean {
    return try {
      // Always persist to local hidden app private storage first
      val localFile = getLocalBackupFile(context)
      localFile.writeText(jsonContent, Charsets.UTF_8)
      Log.d(TAG, "Successfully cached backup JSON into local hidden AppData storage")

      if (!accessToken.isNullOrBlank()) {
        uploadToGoogleDriveAppData(jsonContent, accessToken)
      }
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to perform AppData backup", e)
      false
    }
  }

  /**
   * Automatic background and foreground restore directly pulling from hidden AppData folder.
   */
  fun restoreFromAppDataFolder(
    context: Context,
    accessToken: String? = null
  ): String? {
    return try {
      if (!accessToken.isNullOrBlank()) {
        val driveContent = downloadFromGoogleDriveAppData(accessToken)
        if (!driveContent.isNullOrBlank()) {
          getLocalBackupFile(context).writeText(driveContent, Charsets.UTF_8)
          return driveContent
        }
      }

      val localFile = getLocalBackupFile(context)
      if (localFile.exists()) {
        localFile.readText(Charsets.UTF_8)
      } else {
        null
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to restore backup from AppData folder", e)
      null
    }
  }

  private fun findExistingFileIdInDrive(accessToken: String): String? {
    val url = "https://www.googleapis.com/drive/v3/files?spaces=$APPDATA_SPACE&q=name%3D%27$BACKUP_FILE_NAME%27%20and%20trashed%3Dfalse"
    val request = Request.Builder()
      .url(url)
      .addHeader("Authorization", "Bearer $accessToken")
      .get()
      .build()

    httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val body = response.body?.string() ?: return null
      val json = JSONObject(body)
      val files = json.optJSONArray("files") ?: return null
      if (files.length() > 0) {
        return files.getJSONObject(0).optString("id")
      }
    }
    return null
  }

  private fun uploadToGoogleDriveAppData(jsonContent: String, accessToken: String): Boolean {
    return try {
      val existingId = findExistingFileIdInDrive(accessToken)
      val mediaType = "application/json; charset=utf-8".toMediaType()

      if (existingId != null) {
        val updateUrl = "https://www.googleapis.com/upload/drive/v3/files/$existingId?uploadType=media"
        val request = Request.Builder()
          .url(updateUrl)
          .addHeader("Authorization", "Bearer $accessToken")
          .patch(jsonContent.toRequestBody(mediaType))
          .build()

        httpClient.newCall(request).execute().use { response ->
          response.isSuccessful
        }
      } else {
        val boundary = "---CarHisabBoundary${System.currentTimeMillis()}"
        val metadataJson = JSONObject().apply {
          put("name", BACKUP_FILE_NAME)
          put("parents", JSONArray().put(APPDATA_SPACE))
        }.toString()

        val multipartBody = StringBuilder().apply {
          append("--$boundary\r\n")
          append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
          append(metadataJson)
          append("\r\n--$boundary\r\n")
          append("Content-Type: application/json\r\n\r\n")
          append(jsonContent)
          append("\r\n--$boundary--\r\n")
        }.toString()

        val uploadUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
        val multipartType = "multipart/related; boundary=$boundary".toMediaType()
        val request = Request.Builder()
          .url(uploadUrl)
          .addHeader("Authorization", "Bearer $accessToken")
          .post(multipartBody.toRequestBody(multipartType))
          .build()

        httpClient.newCall(request).execute().use { response ->
          response.isSuccessful
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error uploading backup JSON to Google Drive AppData REST API", e)
      false
    }
  }

  private fun downloadFromGoogleDriveAppData(accessToken: String): String? {
    return try {
      val fileId = findExistingFileIdInDrive(accessToken) ?: return null
      val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
      val request = Request.Builder()
        .url(downloadUrl)
        .addHeader("Authorization", "Bearer $accessToken")
        .get()
        .build()

      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          response.body?.string()
        } else {
          null
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error downloading backup JSON from Google Drive AppData REST API", e)
      null
    }
  }
}
