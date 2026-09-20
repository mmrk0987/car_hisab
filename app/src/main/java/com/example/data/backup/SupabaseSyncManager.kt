package com.example.data.backup

import android.content.Context
import android.util.Log
import com.example.data.auth.SupabaseAuthManager
import com.example.data.db.AppDatabase
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseSyncManager {
    private const val TAG = "SupabaseSync"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
        
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun pushToSupabase(context: Context, email: String): Boolean = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return@withContext false
        try {
            val db = AppDatabase.getDatabase(context)
            val trips = db.tripDao().getAllTripsSnapshot()
            val bookings = db.bookingDao().getAllBookingsSnapshot()
            val userPrefsRepo = UserPreferencesRepository(context)
            val profile = userPrefsRepo.profileFlow.value
            val docs = userPrefsRepo.documentsFlow.value
            val mobil = userPrefsRepo.mobilServiceFlow.value
            
            // 1. Profile information (saved once as default driver & car information)
            val profileObj = JSONObject().apply {
                put("driver_name", profile.driverName)
                put("driver_name_bangla", profile.driverNameBangla)
                put("driver_name_english", profile.driverNameEnglish)
                put("driver_phone", profile.driverPhone)
                put("driver_email", profile.driverEmail.ifBlank { cleanEmail })
                put("car_name", profile.carName)
                put("car_model", profile.carModel)
                put("car_number", profile.carNumber)
                put("birth_date", profile.birthDate)
                put("user_unique_key", profile.userUniqueKey)
                put("profile_image_uri", profile.profileImageUri ?: "")
                put("is_profile_completed", profile.isProfileCompleted)
            }

            // 2. Documents information
            val documentsObj = JSONObject().apply {
                put("tax_token_expiry_millis", docs.taxTokenExpiryMillis)
                put("tax_token_number", docs.taxTokenNumber)
                put("fitness_expiry_millis", docs.fitnessExpiryMillis)
                put("fitness_number", docs.fitnessNumber)
                put("route_permit_expiry_millis", docs.routePermitExpiryMillis)
                put("route_permit_number", docs.routePermitNumber)
                put("insurance_expiry_millis", docs.insuranceExpiryMillis)
                put("insurance_number", docs.insuranceNumber)
                put("driving_license_expiry_millis", docs.drivingLicenseExpiryMillis)
                put("driving_license_number", docs.drivingLicenseNumber)
            }

            // 3. Mobil & Service information
            val mobilObj = JSONObject().apply {
                put("current_odometer_km", mobil.currentOdometerKm)
                put("last_mobil_change_km", mobil.lastMobilChangeKm)
                put("mobil_change_interval_km", mobil.mobilChangeIntervalKm)
                put("last_mobil_change_date_millis", mobil.lastMobilChangeDateMillis)
                put("mobil_brand_grade", mobil.mobilBrandGrade)
                put("last_brake_check_km", mobil.lastBrakeCheckKm)
                put("last_air_filter_km", mobil.lastAirFilterKm)
                put("last_gear_oil_km", mobil.lastGearOilKm)
                put("general_notes", mobil.generalNotes)
            }
            
            // 4. Trips (newly added over time)
            val tripsArray = JSONArray()
            for (t in trips) {
                tripsArray.put(JSONObject().apply {
                    put("id", t.id)
                    put("user_id", t.userId)
                    put("vehicle_id", t.vehicleId)
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
                })
            }
            
            // 5. Bookings
            val bookingsArray = JSONArray()
            for (b in bookings) {
                bookingsArray.put(JSONObject().apply {
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
            
            val backupData = JSONObject().apply {
                put("profile", profileObj)
                put("documents", documentsObj)
                put("mobil_service", mobilObj)
                put("trips", tripsArray)
                put("bookings", bookingsArray)
            }
            
            val row = JSONObject().apply {
                put("user_email", cleanEmail)
                put("backup_data", backupData)
                put("created_at_millis", System.currentTimeMillis())
            }
            
            // Delete existing row for this email to avoid duplicate rows
            val deleteUrl = "${SupabaseAuthManager.DEFAULT_SUPABASE_URL}/rest/v1/car_hisab_sync?user_email=eq.$cleanEmail"
            val delReq = Request.Builder()
                .url(deleteUrl)
                .addHeader("apikey", SupabaseAuthManager.DEFAULT_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseAuthManager.DEFAULT_ANON_KEY}")
                .delete()
                .build()
            httpClient.newCall(delReq).execute() // ignore result
            
            val insertUrl = "${SupabaseAuthManager.DEFAULT_SUPABASE_URL}/rest/v1/car_hisab_sync"
            val request = Request.Builder()
                .url(insertUrl)
                .addHeader("apikey", SupabaseAuthManager.DEFAULT_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseAuthManager.DEFAULT_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(row.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
                
            val response = httpClient.newCall(request).execute()
            val code = response.code
            val body = response.body?.string()
            Log.d(TAG, "Push HTTP $code, Body: $body")
            if (response.isSuccessful) {
                userPrefsRepo.setLastDriveBackupTime(System.currentTimeMillis())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Push error: ${e.message}", e)
            false
        }
    }
    
    suspend fun pullFromSupabase(context: Context, email: String): Boolean = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return@withContext false
        try {
            val url = "${SupabaseAuthManager.DEFAULT_SUPABASE_URL}/rest/v1/car_hisab_sync?user_email=eq.$cleanEmail&select=*&order=created_at_millis.desc&limit=1"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseAuthManager.DEFAULT_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseAuthManager.DEFAULT_ANON_KEY}")
                .get()
                .build()
                
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false
            val bodyStr = response.body?.string() ?: return@withContext false
            val arr = JSONArray(bodyStr)
            if (arr.length() == 0) return@withContext false
            
            val row = arr.getJSONObject(0)
            val backupData = row.getJSONObject("backup_data")
            val userPrefsRepo = UserPreferencesRepository(context)

            // 1. Restore Profile Information as Default Driver & Vehicle Info
            val profileObj = backupData.optJSONObject("profile")
            if (profileObj != null) {
                val current = userPrefsRepo.profileFlow.value
                val bName = profileObj.optString("driver_name", "")
                val bNameBn = profileObj.optString("driver_name_bangla", "")
                val bNameEn = profileObj.optString("driver_name_english", "")
                val bPhone = profileObj.optString("driver_phone", "")
                val bEmail = profileObj.optString("driver_email", "")
                val bCarName = profileObj.optString("car_name", "")
                val bCarModel = profileObj.optString("car_model", "")
                val bCarNumber = profileObj.optString("car_number", "")
                val bBirthDate = profileObj.optString("birth_date", "")
                val bUniqueKey = profileObj.optString("user_unique_key", "")
                val bImageUri = profileObj.optString("profile_image_uri", "")

                val effectiveName = bName.ifBlank { bNameBn.ifBlank { bNameEn } }
                val updatedProfile = current.copy(
                    driverName = effectiveName.ifBlank { current.driverName },
                    driverNameBangla = bNameBn.ifBlank { effectiveName.ifBlank { current.driverNameBangla } },
                    driverNameEnglish = bNameEn.ifBlank { effectiveName.ifBlank { current.driverNameEnglish } },
                    driverPhone = bPhone.ifBlank { current.driverPhone },
                    driverEmail = bEmail.ifBlank { current.driverEmail.ifBlank { cleanEmail } },
                    savedEmail = if (current.savedEmail.isBlank()) (bEmail.ifBlank { cleanEmail }) else current.savedEmail,
                    carName = bCarName.ifBlank { current.carName },
                    carModel = bCarModel.ifBlank { current.carModel },
                    carNumber = bCarNumber.ifBlank { current.carNumber },
                    birthDate = bBirthDate.ifBlank { current.birthDate },
                    userUniqueKey = bUniqueKey.ifBlank { current.userUniqueKey },
                    profileImageUri = if (bImageUri.isNotBlank()) bImageUri else current.profileImageUri,
                    isProfileCompleted = profileObj.optBoolean("is_profile_completed", true),
                    isLoggedIn = true
                )
                userPrefsRepo.updateProfile(updatedProfile)
                Log.d(TAG, "Restored profile: ${updatedProfile.driverName}, phone: ${updatedProfile.driverPhone}, car: ${updatedProfile.carNumber}")
            }

            // 2. Restore Vehicle Documents
            val docObj = backupData.optJSONObject("documents")
            if (docObj != null) {
                val currentDocs = userPrefsRepo.documentsFlow.value
                val restoredDocs = currentDocs.copy(
                    taxTokenExpiryMillis = docObj.optLong("tax_token_expiry_millis", currentDocs.taxTokenExpiryMillis),
                    taxTokenNumber = docObj.optString("tax_token_number", currentDocs.taxTokenNumber),
                    fitnessExpiryMillis = docObj.optLong("fitness_expiry_millis", currentDocs.fitnessExpiryMillis),
                    fitnessNumber = docObj.optString("fitness_number", currentDocs.fitnessNumber),
                    routePermitExpiryMillis = docObj.optLong("route_permit_expiry_millis", currentDocs.routePermitExpiryMillis),
                    routePermitNumber = docObj.optString("route_permit_number", currentDocs.routePermitNumber),
                    insuranceExpiryMillis = docObj.optLong("insurance_expiry_millis", currentDocs.insuranceExpiryMillis),
                    insuranceNumber = docObj.optString("insurance_number", currentDocs.insuranceNumber),
                    drivingLicenseExpiryMillis = docObj.optLong("driving_license_expiry_millis", currentDocs.drivingLicenseExpiryMillis),
                    drivingLicenseNumber = docObj.optString("driving_license_number", currentDocs.drivingLicenseNumber)
                )
                userPrefsRepo.updateDocuments(restoredDocs)
            }

            // 3. Restore Mobil & Service Information
            val mobilObj = backupData.optJSONObject("mobil_service")
            if (mobilObj != null) {
                val currentMobil = userPrefsRepo.mobilServiceFlow.value
                val restoredMobil = currentMobil.copy(
                    currentOdometerKm = mobilObj.optDouble("current_odometer_km", currentMobil.currentOdometerKm),
                    lastMobilChangeKm = mobilObj.optDouble("last_mobil_change_km", currentMobil.lastMobilChangeKm),
                    mobilChangeIntervalKm = mobilObj.optDouble("mobil_change_interval_km", currentMobil.mobilChangeIntervalKm),
                    lastMobilChangeDateMillis = mobilObj.optLong("last_mobil_change_date_millis", currentMobil.lastMobilChangeDateMillis),
                    mobilBrandGrade = mobilObj.optString("mobil_brand_grade", currentMobil.mobilBrandGrade),
                    lastBrakeCheckKm = mobilObj.optDouble("last_brake_check_km", currentMobil.lastBrakeCheckKm),
                    lastAirFilterKm = mobilObj.optDouble("last_air_filter_km", currentMobil.lastAirFilterKm),
                    lastGearOilKm = mobilObj.optDouble("last_gear_oil_km", currentMobil.lastGearOilKm),
                    generalNotes = mobilObj.optString("general_notes", currentMobil.generalNotes)
                )
                userPrefsRepo.updateMobilService(restoredMobil)
            }
            
            // 4. Restore Trips
            val tripsArr = backupData.optJSONArray("trips")
            val tripsToInsert = mutableListOf<com.example.data.model.TripEntity>()
            if (tripsArr != null) {
                for (i in 0 until tripsArr.length()) {
                    val obj = tripsArr.getJSONObject(i)
                    val tripId = obj.optLong("id", 0L)
                    tripsToInsert.add(com.example.data.model.TripEntity(
                        id = if (tripId > 0) tripId else 0L,
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
                    ))
                }
            }
            
            // 5. Restore Bookings
            val bookingsArr = backupData.optJSONArray("bookings")
            val bookingsToInsert = mutableListOf<com.example.data.model.BookingEntity>()
            if (bookingsArr != null) {
                for (i in 0 until bookingsArr.length()) {
                    val bObj = bookingsArr.getJSONObject(i)
                    val bId = bObj.optLong("id", 0L)
                    bookingsToInsert.add(com.example.data.model.BookingEntity(
                        id = if (bId > 0) bId else 0L,
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
                    ))
                }
            }
            
            val db = AppDatabase.getDatabase(context)
            if (tripsToInsert.isNotEmpty()) {
                db.tripDao().deleteAllTrips()
                db.tripDao().insertTrips(tripsToInsert)
            }
            if (bookingsToInsert.isNotEmpty()) {
                db.bookingDao().deleteAllBookings()
                db.bookingDao().insertBookings(bookingsToInsert)
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Pull error: ${e.message}", e)
            false
        }
    }
}
