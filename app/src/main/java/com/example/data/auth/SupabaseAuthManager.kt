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
  val accessToken: String? = null,
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
        val userObj = json.optJSONObject("user")
        val returnEmail = userObj?.optString("email", "") ?: ""

        SupabaseAuthResult(
          success = true,
          email = returnEmail,
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
        msg.isNotBlank() -> "Supabase Error: $msg"
        else -> "Supabase Error (HTTP $statusCode)"
      }
    } catch (_: Exception) {
      "Supabase ত্রুটি (HTTP $statusCode)"
    }
  }
}
