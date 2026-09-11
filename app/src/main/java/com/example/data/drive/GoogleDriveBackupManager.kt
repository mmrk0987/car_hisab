package com.example.data.drive

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.data.model.BookingEntity
import com.example.data.model.MobilServiceInfo
import com.example.data.model.TripEntity
import com.example.data.model.VehicleDocuments
import com.example.data.repository.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

  /**
   * Writes backup JSON string to an SAF Uri stream.
   */
  fun writeBackupToUri(context: Context, uri: Uri, jsonContent: String): Boolean {
    return try {
      context.contentResolver.openOutputStream(uri, "w")?.use { out ->
        out.write(jsonContent.toByteArray(Charsets.UTF_8))
        out.flush()
      }
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed writing backup to SAF URI", e)
      false
    }
  }

  /**
   * Reads backup JSON string from an SAF Uri stream.
   */
  fun readBackupFromUri(context: Context, uri: Uri): String? {
    return try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed reading backup from SAF URI", e)
      null
    }
  }

  /**
   * Restores trips from a specific file Uri (chosen from Storage Access Framework).
   */
  fun readTripsFromUri(context: Context, uri: Uri): List<TripEntity> {
    val content = readBackupFromUri(context, uri) ?: return emptyList()
    return parseBackupJson(content)
  }
}
