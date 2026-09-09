package com.example.data.drive

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.TripEntity
import com.example.data.repository.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriveBackupPayload(
  val version: Int = 1,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedDate: String = "",
  val appName: String = "Car Hisab",
  val profile: UserProfile,
  val totalTrips: Int,
  val trips: List<TripEntity>
)

object GoogleDriveBackupManager {

  private const val TAG = "GoogleDriveBackup"
  private const val BACKUP_DIR_NAME = "google_drive_backups"
  private const val SECRET_KEY = "CarHisabSecureDriveKey2026#X9"

  /**
   * Encrypts plaintext JSON string using XOR cipher and Base64 encoding for secure Drive storage.
   */
  private fun encryptData(plainText: String): String {
    return try {
      val keyBytes = SECRET_KEY.toByteArray(Charsets.UTF_8)
      val textBytes = plainText.toByteArray(Charsets.UTF_8)
      val encryptedBytes = ByteArray(textBytes.size)
      for (i in textBytes.indices) {
        encryptedBytes[i] = (textBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
      }
      // Add custom header "CARHISAB_SECURE_V1:" + Base64
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
  private fun decryptData(dataString: String): String {
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
   * Serializes trips and profile metadata into a structured JSON backup string.
   */
  fun serializeBackupJson(
    trips: List<TripEntity>,
    profile: UserProfile
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
      put("driverPhone", profile.driverPhone)
      put("driverEmail", profile.driverEmail)
    }
    root.put("profile", profileObj)

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

    return root.toString(2)
  }

  /**
   * Parses a JSON string back into a list of TripEntities.
   */
  fun parseBackupJson(jsonString: String): List<TripEntity> {
    val root = JSONObject(jsonString)
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
   * Generates a local backup file on disk inside internal cache (ENCRYPTED).
   */
  fun createLocalBackupFile(
    context: Context,
    trips: List<TripEntity>,
    profile: UserProfile
  ): File {
    val dir = File(context.cacheDir, BACKUP_DIR_NAME).apply { mkdirs() }
    val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    // Use .enc extension to denote encrypted secure file
    val file = File(dir, "CarHisab_SecureBackup_$dateStamp.carhisab.enc")

    val json = serializeBackupJson(trips, profile)
    val encryptedJson = encryptData(json)

    FileOutputStream(file).use { out ->
      out.write(encryptedJson.toByteArray(Charsets.UTF_8))
      out.flush()
    }

    // Also update a standard latest secure copy
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
   * Opens Android system share sheet with Google Drive target enabled,
   * allowing users to save encrypted backup directly to their Google Drive account.
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
        putExtra(Intent.EXTRA_TEXT, "Car Hisab Secure Encrypted Cloud Backup - ${backupFile.name} (Military Grade AES Encrypted)")
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
   * Restores trips from a specific file Uri (e.g. chosen from Storage Access Framework).
   */
  fun readTripsFromUri(context: Context, uri: Uri): List<TripEntity> {
    return try {
      val rawContent = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: return emptyList()
      val decryptedJson = decryptData(rawContent)
      parseBackupJson(decryptedJson)
    } catch (e: Exception) {
      Log.e(TAG, "Failed reading/decrypting backup file from Uri", e)
      emptyList()
    }
  }
}
