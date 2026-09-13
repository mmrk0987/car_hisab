package com.example.data.auth

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

object SupabaseAuthManager {
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
      return@withContext SupabaseAuthResult(
        success = false,
        message = "সঠিক ইমেইল ঠিকানা দিন।"
      )
    }
    if (password.isBlank()) {
      return@withContext SupabaseAuthResult(
        success = false,
        message = "পাসওয়ার্ড দেওয়া আবশ্যক।"
      )
    }

    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/auth/v1/token?grant_type=password"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }

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

      if (response.isSuccessful) {
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
        val errorMsg = parseErrorMessage(respBody, response.code)
        SupabaseAuthResult(
          success = false,
          message = errorMsg,
          errorDetail = "HTTP ${response.code}: $respBody"
        )
      }
    } catch (e: Exception) {
      SupabaseAuthResult(
        success = false,
        message = "Supabase সংযোগে সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = e.message
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
      return@withContext SupabaseAuthResult(
        success = false,
        message = "সঠিক ইমেইল ঠিকানা প্রদান করুন।"
      )
    }
    if (password.length < 6) {
      return@withContext SupabaseAuthResult(
        success = false,
        message = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
      )
    }

    try {
      val rootUrl = cleanBaseUrl(baseUrl)
      val endpoint = "$rootUrl/auth/v1/signup"
      val key = anonKey.ifBlank { DEFAULT_ANON_KEY }

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

      if (response.isSuccessful) {
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
        val errorMsg = parseErrorMessage(respBody, response.code)
        SupabaseAuthResult(
          success = false,
          message = errorMsg,
          errorDetail = "HTTP ${response.code}: $respBody"
        )
      }
    } catch (e: Exception) {
      SupabaseAuthResult(
        success = false,
        message = "Supabase সাইনআপে সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = e.message
      )
    }
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
