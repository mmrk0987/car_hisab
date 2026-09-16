package com.example.data.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseAuthResult(
  val success: Boolean,
  val email: String? = null,
  val name: String? = null,
  val provider: String? = null,
  val accessToken: String? = null,
  val refreshToken: String? = null,
  val message: String,
  val errorDetail: String? = null
)

data class SupabaseParsedError(
  val errorCode: String,
  val errorMessage: String,
  val userFriendlyMsg: String
)

object SupabaseAuthManager {
  const val TAG = "AuthDebug"
  const val DEFAULT_SUPABASE_URL = "https://vuncvrkrjrqrmwusumdb.supabase.co"
  const val DEFAULT_ANON_KEY = "sb_publishable_uVfiX9pfEWjLEdJESpLizA_JSvb1OIM"
  const val DEFAULT_SENDER_EMAIL = "mdmahfuj0987@gmail.com"

  private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

  private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .build()
  }

  fun cleanBaseUrl(url: String): String {
    var cleaned = url.trim()
    if (cleaned.endsWith("/")) {
      cleaned = cleaned.substring(0, cleaned.length - 1)
    }
    if (cleaned.endsWith("/rest/v1")) {
      cleaned = cleaned.substring(0, cleaned.length - 8)
    }
    if (cleaned.endsWith("/")) {
      cleaned = cleaned.substring(0, cleaned.length - 1)
    }
    return if (cleaned.isBlank()) DEFAULT_SUPABASE_URL else cleaned
  }

  /**
   * Log in user with Email and Password using Supabase Auth endpoint
   */
  suspend fun loginWithEmail(
    email: String,
    password: String,
    baseUrl: String = DEFAULT_SUPABASE_URL,
    anonKey: String = DEFAULT_ANON_KEY
  ): SupabaseAuthResult = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      val msg = "সঠিক ইমেইল ঠিকানা দিন।"
      Log.w(TAG, "loginWithEmail validation failed: Email is invalid ($cleanEmail)")
      return@withContext SupabaseAuthResult(
        success = false,
        message = msg,
        errorDetail = "Validation error: Invalid email address"
      )
    }
    if (password.isBlank()) {
      val msg = "পাসওয়ার্ড দেওয়া আবশ্যক।"
      Log.w(TAG, "loginWithEmail validation failed: Password is blank")
      return@withContext SupabaseAuthResult(
        success = false,
        message = msg,
        errorDetail = "Validation error: Blank password"
      )
    }

    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/auth/v1/token?grant_type=password"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }

      Log.d(TAG, "loginWithEmail initiating request to: $endpoint for email: $cleanEmail")

      val payload = JSONObject().apply {
        put("email", cleanEmail)
        put("password", password)
      }

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", key)
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      val httpCode = response.code

      if (response.isSuccessful) {
        Log.d(TAG, "loginWithEmail SUCCESS. HTTP status: $httpCode")
        val json = JSONObject(respBody)
        val accessToken = json.optString("access_token", "")
        val refreshToken = json.optString("refresh_token", "")
        val userObj = json.optJSONObject("user")
        val returnEmail = userObj?.optString("email", cleanEmail) ?: cleanEmail

        SupabaseAuthResult(
          success = true,
          email = returnEmail,
          accessToken = accessToken,
          message = "সফলভাবে Supabase-এ লগইন হয়েছে!"
        )
      } else {
        val parsed = parseSupabaseError(respBody, httpCode)
        Log.e(
          TAG,
          "loginWithEmail FAILED. HTTP status: $httpCode, Error Code: ${parsed.errorCode}, Message: ${parsed.errorMessage}, Raw Body: $respBody"
        )
        SupabaseAuthResult(
          success = false,
          message = parsed.userFriendlyMsg,
          errorDetail = "HTTP $httpCode [Code: ${parsed.errorCode}]: ${parsed.errorMessage}"
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "loginWithEmail EXCEPTION: ${e.javaClass.simpleName} - ${e.message}", e)
      SupabaseAuthResult(
        success = false,
        message = "Supabase সংযোগে সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = "Exception: ${e.javaClass.simpleName}: ${e.message}"
      )
    }
  }

  /**
   * Sign up user with Email, Password and Metadata (phone)
   */
  suspend fun signUpWithEmail(
    email: String,
    password: String,
    phone: String = "",
    baseUrl: String = DEFAULT_SUPABASE_URL,
    anonKey: String = DEFAULT_ANON_KEY
  ): SupabaseAuthResult = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      val msg = "সঠিক ইমেইল ঠিকানা প্রদান করুন।"
      Log.w(TAG, "signUpWithEmail validation failed: Email is invalid ($cleanEmail)")
      return@withContext SupabaseAuthResult(
        success = false,
        message = msg,
        errorDetail = "Validation error: Invalid email address"
      )
    }
    if (password.length < 6) {
      val msg = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
      Log.w(TAG, "signUpWithEmail validation failed: Password too short (${password.length} chars)")
      return@withContext SupabaseAuthResult(
        success = false,
        message = msg,
        errorDetail = "Validation error: Password length must be at least 6 characters"
      )
    }

    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/auth/v1/signup"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }

      Log.d(TAG, "signUpWithEmail initiating request to: $endpoint for email: $cleanEmail")

      val payload = JSONObject().apply {
        put("email", cleanEmail)
        put("password", password)
        if (phone.isNotBlank()) {
          val dataObj = JSONObject().apply {
            put("phone", phone.trim())
          }
          put("data", dataObj)
        }
      }

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", key)
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      val httpCode = response.code

      if (response.isSuccessful) {
        Log.d(TAG, "signUpWithEmail SUCCESS. HTTP status: $httpCode")
        val json = JSONObject(respBody)
        val accessToken = if (json.has("access_token")) json.optString("access_token") else null
        val userObj = json.optJSONObject("user")
        val returnEmail = userObj?.optString("email", cleanEmail) ?: cleanEmail

        SupabaseAuthResult(
          success = true,
          email = returnEmail,
          accessToken = accessToken,
          message = "Supabase-এ একাউন্ট তৈরি সফল হয়েছে!"
        )
      } else {
        val parsed = parseSupabaseError(respBody, httpCode)
        Log.e(
          TAG,
          "signUpWithEmail FAILED. HTTP status: $httpCode, Error Code: ${parsed.errorCode}, Message: ${parsed.errorMessage}, Raw Body: $respBody"
        )
        SupabaseAuthResult(
          success = false,
          message = parsed.userFriendlyMsg,
          errorDetail = "HTTP $httpCode [Code: ${parsed.errorCode}]: ${parsed.errorMessage}"
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "signUpWithEmail EXCEPTION: ${e.javaClass.simpleName} - ${e.message}", e)
      SupabaseAuthResult(
        success = false,
        message = "Supabase সাইনআপে সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = "Exception: ${e.javaClass.simpleName}: ${e.message}"
      )
    }
  }

  /**
   * Sign in / Authenticate with Google ID Token using Supabase Auth endpoint
   */
  suspend fun signInWithGoogle(
    idToken: String?,
    baseUrl: String = DEFAULT_SUPABASE_URL,
    anonKey: String = DEFAULT_ANON_KEY
  ): SupabaseAuthResult = withContext(Dispatchers.IO) {
    if (idToken == null) {
      Log.e(TAG, "Google ID Token inspection: Token is NULL before passing to Supabase.")
      return@withContext SupabaseAuthResult(
        success = false,
        message = "Google ID Token পাওয়া যায়নি (null)",
        errorDetail = "Google ID Token is null"
      )
    }

    val cleanToken = idToken.trim()
    if (cleanToken.isBlank()) {
      Log.e(TAG, "Google ID Token inspection: Token is EMPTY/BLANK before passing to Supabase.")
      return@withContext SupabaseAuthResult(
        success = false,
        message = "Google ID Token খালি বা অকার্যকর (blank)",
        errorDetail = "Google ID Token is blank"
      )
    }

    if (!cleanToken.contains(".") || cleanToken.length < 20) {
      Log.w(
        TAG,
        "Google ID Token inspection: Token format appears INVALID before passing to Supabase. Length: ${cleanToken.length}"
      )
    } else {
      Log.i(
        TAG,
        "Google ID Token inspection: Token SUCCESSFULLY RETRIEVED! Length: ${cleanToken.length}, Prefix: ${cleanToken.take(12)}..."
      )
    }

    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/auth/v1/token?grant_type=id_token"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }

      Log.d(TAG, "signInWithGoogle initiating request to: $endpoint")

      val payload = JSONObject().apply {
        put("provider", "google")
        put("id_token", cleanToken)
      }

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", key)
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      val httpCode = response.code

      if (response.isSuccessful) {
        Log.d(TAG, "signInWithGoogle SUCCESS. HTTP status: $httpCode")
        val json = JSONObject(respBody)
        val accessToken = json.optString("access_token", "")
        val refreshToken = json.optString("refresh_token", "")
        val userObj = json.optJSONObject("user")
        val returnEmail = userObj?.optString("email", "") ?: ""
        val userMetadata = userObj?.optJSONObject("user_metadata")
        val returnName = userMetadata?.optString("full_name", userMetadata.optString("name", "")) ?: ""
        val appMetadata = userObj?.optJSONObject("app_metadata")
        val provider = appMetadata?.optString("provider", "google") ?: "google"

        SupabaseAuthResult(
          success = true,
          email = returnEmail,
          name = returnName,
          provider = provider,
          accessToken = accessToken,
          message = "Google দিয়ে সফলভাবে Supabase-এ লগইন হয়েছে!"
        )
      } else {
        val parsed = parseSupabaseError(respBody, httpCode)
        Log.e(
          TAG,
          "signInWithGoogle FAILED. HTTP status: $httpCode, Error Code: ${parsed.errorCode}, Message: ${parsed.errorMessage}, Raw Body: $respBody"
        )
        SupabaseAuthResult(
          success = false,
          message = parsed.userFriendlyMsg,
          errorDetail = "HTTP $httpCode [Code: ${parsed.errorCode}]: ${parsed.errorMessage}"
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "signInWithGoogle EXCEPTION: ${e.javaClass.simpleName} - ${e.message}", e)
      SupabaseAuthResult(
        success = false,
        message = "Supabase Google সাইন-ইনে সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = "Exception: ${e.javaClass.simpleName}: ${e.message}"
      )
    }
  }

  /**
   * Refresh session using Supabase refresh_token endpoint
   */
  suspend fun refreshSession(
    refreshToken: String,
    baseUrl: String = DEFAULT_SUPABASE_URL,
    anonKey: String = DEFAULT_ANON_KEY
  ): SupabaseAuthResult = withContext(Dispatchers.IO) {
    if (refreshToken.isBlank()) {
      return@withContext SupabaseAuthResult(
        success = false,
        message = "রিফ্রেশ টোকেন খালি।",
        errorDetail = "Refresh token is empty"
      )
    }

    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/auth/v1/token?grant_type=refresh_token"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }

      val payload = JSONObject().apply {
        put("refresh_token", refreshToken.trim())
      }

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", key)
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      val httpCode = response.code

      if (response.isSuccessful) {
        val json = JSONObject(respBody)
        val accessToken = json.optString("access_token", "")
        val newRefreshToken = json.optString("refresh_token", refreshToken)
        val userObj = json.optJSONObject("user")
        val returnEmail = userObj?.optString("email", "") ?: ""

        SupabaseAuthResult(
          success = true,
          email = returnEmail,
          accessToken = accessToken,
          refreshToken = newRefreshToken,
          message = "সেশন সফলভাবে রিনিউ হয়েছে।"
        )
      } else {
        val parsed = parseSupabaseError(respBody, httpCode)
        SupabaseAuthResult(
          success = false,
          message = if (httpCode == 400 || httpCode == 401) "সেশন মেয়াদোত্তীর্ণ হয়েছে, দয়া করে পুনরায় লগইন করুন।" else parsed.userFriendlyMsg,
          errorDetail = "HTTP $httpCode [Code: ${parsed.errorCode}]: ${parsed.errorMessage}"
        )
      }
    } catch (e: Exception) {
      SupabaseAuthResult(
        success = false,
        message = "সেশন রিনিউ করতে ব্যর্থ: ${e.localizedMessage ?: "Network error"}",
        errorDetail = "Exception: ${e.javaClass.simpleName}: ${e.message}"
      )
    }
  }

  /**
   * Reset Password by DOB via Supabase Edge Function reset-password-by-dob
   */
  suspend fun resetPasswordByDob(
    email: String,
    dob: String,
    newPassword: String,
    baseUrl: String = DEFAULT_SUPABASE_URL,
    anonKey: String = DEFAULT_ANON_KEY
  ): SupabaseAuthResult = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      return@withContext SupabaseAuthResult(success = false, message = "সঠিক ইমেইল ঠিকানা দিন।")
    }
    if (dob.isBlank()) {
      return@withContext SupabaseAuthResult(success = false, message = "জন্ম তারিখ দেওয়া আবশ্যক।")
    }
    if (newPassword.length < 6) {
      return@withContext SupabaseAuthResult(success = false, message = "নতুন পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।")
    }

    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/functions/v1/reset-password-by-dob"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }

      val payload = JSONObject().apply {
        put("email", cleanEmail)
        put("dob", dob.trim())
        put("new_password", newPassword)
      }

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", key)
        .addHeader("Authorization", "Bearer $key")
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      val httpCode = response.code

      if (response.isSuccessful) {
        val json = JSONObject(respBody)
        val msg = json.optString("message", "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে।")
        SupabaseAuthResult(success = true, message = msg)
      } else {
        val parsed = parseSupabaseError(respBody, httpCode)
        val userMsg = if (httpCode == 400 || respBody.contains("প্রদত্ত তথ্য মিলছে না") || respBody.contains("not match") || respBody.contains("Mismatch")) {
          "প্রদত্ত তথ্য মিলছে না"
        } else parsed.userFriendlyMsg
        SupabaseAuthResult(success = false, message = userMsg, errorDetail = "HTTP $httpCode: $respBody")
      }
    } catch (e: Exception) {
      SupabaseAuthResult(
        success = false,
        message = "সংযোগের সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = e.message
      )
    }
  }

  /**
   * Insert trip record into remote Supabase database `trips` table.
   * Catches network errors or Row Level Security (RLS) violations.
   */
  suspend fun insertTripToSupabase(
    trip: com.example.data.model.TripEntity,
    baseUrl: String = DEFAULT_SUPABASE_URL,
    anonKey: String = DEFAULT_ANON_KEY,
    accessToken: String? = null
  ): SupabaseAuthResult = withContext(Dispatchers.IO) {
    val tripSaveTag = "TripSaveError"
    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/rest/v1/trips"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }
      val authHeader = if (!accessToken.isNullOrBlank()) "Bearer $accessToken" else "Bearer $key"

      val payload = JSONObject().apply {
        put("user_id", trip.userId)
        put("vehicle_id", trip.vehicleId)
        put("date_string", trip.dateString)
        put("date_millis", trip.dateMillis)
        put("place", trip.place)
        put("rent", trip.rent)
        put("gratuity", trip.gratuity)
        put("maintenance_cost", trip.maintenanceCost)
        put("km_driven", trip.kmDriven)
        put("description", trip.description)
        put("passenger_name", trip.passengerName)
        put("passenger_phone", trip.passengerPhone)
        put("income", trip.income)
        put("profit", trip.profit)
      }

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", key)
        .addHeader("Authorization", authHeader)
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "return=representation")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      val httpCode = response.code

      if (response.isSuccessful) {
        Log.d("TripSaveSuccess", "Trip inserted to Supabase successfully. HTTP $httpCode")
        SupabaseAuthResult(
          success = true,
          message = "Supabase-এ ট্রিপ সফলভাবে সিঙ্ক হয়েছে!"
        )
      } else {
        val parsed = parseSupabaseError(respBody, httpCode)
        val isRlsError = respBody.contains("row-level security", ignoreCase = true) ||
            respBody.contains("RLS", ignoreCase = true) ||
            httpCode == 401 || httpCode == 403
        val errorMsg = if (isRlsError) {
          "Supabase Row Level Security (RLS) সিকিউরিটি পলিসি ত্রুটি: user_id বা পারমিশন সঠিক নয়।"
        } else {
          parsed.userFriendlyMsg
        }

        Log.e(
          tripSaveTag,
          "Failed to insert trip to Supabase. HTTP status: $httpCode, Error: ${parsed.errorMessage}, Body: $respBody"
        )
        SupabaseAuthResult(
          success = false,
          message = errorMsg,
          errorDetail = "HTTP $httpCode: ${parsed.errorMessage}"
        )
      }
    } catch (e: Exception) {
      Log.e(tripSaveTag, "Exception inserting trip to Supabase: ${e.javaClass.simpleName} - ${e.message}", e)
      SupabaseAuthResult(
        success = false,
        message = "Supabase সিঙ্ক নেটওয়ার্ক সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = "Exception: ${e.javaClass.simpleName}: ${e.message}"
      )
    }
  }

  fun parseSupabaseError(jsonStr: String, statusCode: Int): SupabaseParsedError {
    var errorCode = ""
    var errorMessage = ""
    try {
      val json = JSONObject(jsonStr)
      errorMessage = json.optString(
        "msg",
        json.optString("error_description", json.optString("message", jsonStr))
      )
      errorCode = json.optString(
        "error_code",
        json.optString("code", json.optString("error", "HTTP_$statusCode"))
      )
    } catch (_: Exception) {
      errorMessage = jsonStr.ifBlank { "HTTP $statusCode" }
    }

    val userFriendly = parseErrorMessage(jsonStr, statusCode)
    return SupabaseParsedError(
      errorCode = if (errorCode.isBlank()) "HTTP_$statusCode" else errorCode,
      errorMessage = if (errorMessage.isBlank()) "HTTP $statusCode" else errorMessage,
      userFriendlyMsg = userFriendly
    )
  }

  private fun parseErrorMessage(jsonStr: String, statusCode: Int): String {
    return try {
      val json = JSONObject(jsonStr)
      val msg = json.optString("msg", json.optString("error_description", json.optString("message", "")))
      val errorCode = json.optString("error_code", json.optString("code", ""))

      when {
        msg.contains("Invalid login credentials", ignoreCase = true) ->
          "ইমেইল অথবা পাসওয়ার্ড সঠিক নয়।"
        msg.contains("User already registered", ignoreCase = true) || errorCode.contains("user_already_exists", true) ->
          "এই ইমেইল দিয়ে ইতোমধ্যে একাউন্ট খোলা হয়েছে। লগইন করতে সাইন-ইন ট্যাবে যান।"
        msg.contains("Password should be at least", ignoreCase = true) ->
          "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
        msg.contains("Error sending confirmation mail", ignoreCase = true) || msg.contains("sending configuration", ignoreCase = true) || errorCode.contains("over_email_send_rate_limit", ignoreCase = true) ->
          "কনফার্মেশন ইমেইল পাঠাতে সমস্যা হচ্ছে (Supabase SMTP / Rate limit সমস্যা)। Supabase ড্যাশবোর্ডে 'Confirm Email' অপশনটি ডিজেবল করুন অথবা Custom SMTP সেটআপ করুন।"
        msg.isNotBlank() -> "Supabase Error: $msg"
        else -> "Supabase Error (HTTP $statusCode)"
      }
    } catch (_: Exception) {
      "Supabase ত্রুটি (HTTP $statusCode)"
    }
  }

  /**
   * Fetches trips from Supabase cloud database to restore locally.
   */
  suspend fun fetchTripsFromSupabase(
    userId: String = "",
    baseUrl: String = DEFAULT_SUPABASE_URL,
    anonKey: String = DEFAULT_ANON_KEY,
    accessToken: String? = null
  ): List<com.example.data.model.TripEntity> = withContext(Dispatchers.IO) {
    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }
      val authHeader = if (!accessToken.isNullOrBlank()) "Bearer $accessToken" else "Bearer $key"

      val queryParam = if (userId.isNotBlank()) {
        "user_id=eq.$userId&select=*&order=date_millis.desc"
      } else {
        "select=*&order=date_millis.desc"
      }
      val endpoint = "$rootUrl/rest/v1/trips?$queryParam"

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", key)
        .addHeader("Authorization", authHeader)
        .get()
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""
      if (response.isSuccessful) {
        val jsonArray = org.json.JSONArray(respBody)
        val list = mutableListOf<com.example.data.model.TripEntity>()
        for (i in 0 until jsonArray.length()) {
          val obj = jsonArray.getJSONObject(i)
          list.add(
            com.example.data.model.TripEntity(
              id = obj.optLong("id", 0L),
              userId = obj.optString("user_id", userId),
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
          )
        }
        Log.d("SupabaseRestore", "Successfully fetched ${list.size} trips from Supabase")
        list
      } else {
        Log.w("SupabaseRestore", "Failed to fetch trips from Supabase: HTTP ${response.code}")
        emptyList()
      }
    } catch (e: Exception) {
      Log.e("SupabaseRestore", "Exception fetching trips from Supabase: ${e.message}", e)
      emptyList()
    }
  }
}
