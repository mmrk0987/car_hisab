package com.example.data.backup

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.auth.SupabaseAuthManager
import com.example.data.db.AppDatabase
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
        if (email.isBlank()) return@withContext false
        try {
            val db = AppDatabase.getDatabase(context)
            val trips = db.tripDao().getAllTripsSnapshot()
            val bookings = db.bookingDao().getAllBookingsSnapshot()
            
            val payload = JSONObject()
            
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
                put("trips", tripsArray)
                put("bookings", bookingsArray)
            }
            
            val row = JSONObject().apply {
                put("user_email", email)
                put("backup_data", backupData)
                put("created_at_millis", System.currentTimeMillis())
            }
            
            // Delete existing row for this email to avoid duplicate rows, or just use upsert if PK was email. 
            // Since PK is id, we should delete the old one first.
            val deleteUrl = "${SupabaseAuthManager.DEFAULT_SUPABASE_URL}/rest/v1/car_hisab_sync?user_email=eq.$email"
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
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Push error: ${e.message}")
            false
        }
    }
    
    suspend fun pullFromSupabase(context: Context, email: String): Boolean = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext false
        try {
            val url = "${SupabaseAuthManager.DEFAULT_SUPABASE_URL}/rest/v1/car_hisab_sync?user_email=eq.$email&select=*&order=created_at_millis.desc&limit=1"
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
            Log.e(TAG, "Pull error: ${e.message}")
            false
        }
    }
}
