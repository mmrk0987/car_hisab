package com.example.data.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class EmailOtpSendResult(
  val success: Boolean,
  val isRealEmailSent: Boolean,
  val code: String,
  val recipientEmail: String,
  val message: String,
  val errorDetail: String? = null
)

object EmailOtpManager {
  private const val TAG = "EmailOtpManager"
  private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

  private fun logD(msg: String) {
    try {
      android.util.Log.d(TAG, msg)
    } catch (_: Throwable) {
      println("[$TAG] $msg")
    }
  }

  private fun logW(msg: String) {
    try {
      android.util.Log.w(TAG, msg)
    } catch (_: Throwable) {
      System.err.println("[$TAG] $msg")
    }
  }

  private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .build()
  }

  // In-memory active verification record
  @Volatile
  private var activeCode: String = ""
  @Volatile
  private var activeRecipient: String = ""
  @Volatile
  private var activeExpiryMillis: Long = 0L

  private fun resolveApiKey(provider: String, configuredKey: String): String {
    if (configuredKey.trim().isNotBlank()) return configuredKey.trim()
    return try {
      val fieldName = if (provider.equals("BREVO", ignoreCase = true)) "BREVO_API_KEY" else "RESEND_API_KEY"
      val field = com.example.BuildConfig::class.java.getField(fieldName)
      (field.get(null) as? String)?.trim() ?: ""
    } catch (_: Throwable) {
      ""
    }
  }

  private fun resolveSenderEmail(provider: String, configuredSender: String): String {
    if (configuredSender.trim().isNotBlank()) return configuredSender.trim()
    return try {
      val fieldName = if (provider.equals("BREVO", ignoreCase = true)) "BREVO_SENDER_EMAIL" else "RESEND_SENDER_EMAIL"
      val field = com.example.BuildConfig::class.java.getField(fieldName)
      (field.get(null) as? String)?.trim() ?: ""
    } catch (_: Throwable) {
      ""
    }
  }

  /**
   * Generates a 6-digit random code and dispatches it via Brevo (Sendinblue) or selected provider.
   * Brevo provides 300 free emails every day.
   */
  suspend fun sendOtp(
    recipientEmail: String,
    provider: String = "BREVO",
    apiKey: String = "",
    webhookUrl: String = "",
    senderEmail: String = ""
  ): EmailOtpSendResult = withContext(Dispatchers.IO) {
    val cleanEmail = recipientEmail.trim().lowercase()
    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      return@withContext EmailOtpSendResult(
        success = false,
        isRealEmailSent = false,
        code = "",
        recipientEmail = cleanEmail,
        message = "সঠিক ইমেইল ঠিকানা প্রদান করুন।"
      )
    }

    val otpCode = String.format(java.util.Locale.US, "%06d", Random.nextInt(100000, 999999))
    val effectiveProvider = if (provider.isBlank()) "BREVO" else provider.trim().uppercase()
    val cleanApiKey = resolveApiKey(effectiveProvider, apiKey)
    val cleanSenderEmail = resolveSenderEmail(effectiveProvider, senderEmail)
    val cleanWebhook = webhookUrl.trim()

    when (effectiveProvider) {
      "BREVO" -> {
        if (cleanApiKey.isBlank()) {
          return@withContext EmailOtpSendResult(
            success = false,
            isRealEmailSent = false,
            code = "",
            recipientEmail = cleanEmail,
            message = "Brevo (Sendinblue) API Key পাওয়া যায়নি! Settings অথবা ওটিপি ডায়ালগে আপনার Brevo API Key দিন (প্রতিদিন ৩০০টি ফ্রি ইমেইল)।",
            errorDetail = "Brevo API Key is missing"
          )
        }
        val brevoResult = sendViaBrevo(cleanEmail, otpCode, cleanApiKey, cleanSenderEmail)
        if (brevoResult.success) {
          activeCode = otpCode
          activeRecipient = cleanEmail
          activeExpiryMillis = System.currentTimeMillis() + (5 * 60 * 1000L)
        }
        return@withContext brevoResult
      }
      "RESEND" -> {
        if (cleanApiKey.isBlank()) {
          return@withContext EmailOtpSendResult(
            success = false,
            isRealEmailSent = false,
            code = "",
            recipientEmail = cleanEmail,
            message = "Resend API Key কনফিগার করা নেই। Settings থেকে API Key দিন।",
            errorDetail = "Resend API Key is missing"
          )
        }
        val resendResult = sendViaResend(cleanEmail, otpCode, cleanApiKey, cleanSenderEmail)
        if (resendResult.success) {
          activeCode = otpCode
          activeRecipient = cleanEmail
          activeExpiryMillis = System.currentTimeMillis() + (5 * 60 * 1000L)
        }
        return@withContext resendResult
      }
      "WEBHOOK" -> {
        if (cleanWebhook.isBlank()) {
          return@withContext EmailOtpSendResult(
            success = false,
            isRealEmailSent = false,
            code = "",
            recipientEmail = cleanEmail,
            message = "Webhook URL কনফিগার করা নেই। Settings থেকে Webhook দিন।",
            errorDetail = "Webhook URL is missing"
          )
        }
        val webhookResult = sendViaWebhook(cleanEmail, otpCode, cleanWebhook)
        if (webhookResult.success) {
          activeCode = otpCode
          activeRecipient = cleanEmail
          activeExpiryMillis = System.currentTimeMillis() + (5 * 60 * 1000L)
        }
        return@withContext webhookResult
      }
      else -> {
        return@withContext EmailOtpSendResult(
          success = false,
          isRealEmailSent = false,
          code = "",
          recipientEmail = cleanEmail,
          message = "অজানা ইমেইল প্রোভাইডার: $effectiveProvider"
        )
      }
    }
  }

  /**
   * Verifies an entered OTP code against the active generated code.
   * Real OTP strictly required - no demo/sandbox bypass codes.
   */
  fun verifyOtp(enteredCode: String, recipientEmail: String): Pair<Boolean, String> {
    val cleanEntered = enteredCode.trim()
    val cleanEmail = recipientEmail.trim().lowercase()

    if (activeCode.isBlank()) {
      return Pair(false, "কোনো সক্রিয় ওটিপি কোড পাওয়া যায়নি। পুনরায় কোড পাঠান।")
    }

    if (System.currentTimeMillis() > activeExpiryMillis) {
      activeCode = ""
      return Pair(false, "ওটিপি কোডের মেয়াদ শেষ হয়ে গেছে (৫ মিনিট)। পুনরায় কোড পাঠান।")
    }

    if (activeRecipient.isNotBlank() && cleanEmail.isNotBlank() && activeRecipient != cleanEmail) {
      return Pair(false, "ইমেইল ঠিকানায় অমিল পাওয়া গেছে।")
    }

    if (cleanEntered == activeCode) {
      activeCode = ""
      activeExpiryMillis = 0L
      return Pair(true, "সফলভাবে ওটিপি যাচাই করা হয়েছে!")
    }

    return Pair(false, "ভুল ওটিপি কোড! অনুগ্রহ করে আপনার ইমেইল ইনবক্সে আসা সঠিক ৬-সংখ্যার কোডটি লিখুন।")
  }

  fun getActiveCode(): String = activeCode

  fun setActiveCodeForTesting(code: String, recipientEmail: String, expiryMillis: Long = System.currentTimeMillis() + 300_000L) {
    activeCode = code
    activeRecipient = recipientEmail.trim().lowercase()
    activeExpiryMillis = expiryMillis
  }

  /**
   * Dispatches an email using Resend API (Free 3,000 emails/month).
   * https://resend.com
   */
  private fun sendViaResend(
    toEmail: String,
    otpCode: String,
    apiKey: String,
    senderEmail: String
  ): EmailOtpSendResult {
    return try {
      val payload = JSONObject().apply {
        put("from", if (senderEmail.isNotBlank() && senderEmail.contains("@")) senderEmail else "Car Hisab <onboarding@resend.dev>")
        put("to", JSONArray().put(toEmail))
        put("subject", "কার হিসাব (Car Hisab) - আপনার ভেরিফিকেশন ওটিপি কোড: $otpCode")
        put("html", generateHtmlBody(otpCode, toEmail))
      }

      val request = Request.Builder()
        .url("https://api.resend.com/emails")
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        EmailOtpSendResult(
          success = true,
          isRealEmailSent = true,
          code = otpCode,
          recipientEmail = toEmail,
          message = "আপনার জিমেইল ইনবক্সে ($toEmail) ৬-সংখ্যার আসল ওটিপি কোড পাঠানো হয়েছে!"
        )
      } else {
        EmailOtpSendResult(
          success = false,
          isRealEmailSent = false,
          code = otpCode,
          recipientEmail = toEmail,
          message = "Resend API ত্রুটি: কোড পাঠানো যায়নি।",
          errorDetail = "HTTP ${response.code}: $respBody"
        )
      }
    } catch (e: Exception) {
      EmailOtpSendResult(
        success = false,
        isRealEmailSent = false,
        code = otpCode,
        recipientEmail = toEmail,
        message = "ইমেইল নেটওয়ার্ক সংযোগে সমস্যা।",
        errorDetail = e.message
      )
    }
  }

  fun parseSenderNameAndEmail(raw: String, fallbackEmail: String = "carhisab.otp@gmail.com"): Pair<String, String> {
    val trimmed = raw.trim()
    val angleRegex = Regex("""^(.*?)\s*<([^>]+)>$""")
    val match = angleRegex.find(trimmed)
    if (match != null) {
      val name = match.groupValues[1].trim().ifEmpty { "Car Hisab" }
      val email = match.groupValues[2].trim()
      if (email.contains("@")) {
        return Pair(name, email)
      }
    }
    if (trimmed.contains("@")) {
      return Pair("Car Hisab", trimmed)
    }
    return Pair("Car Hisab", fallbackEmail)
  }

  /**
   * Dispatches an email using Brevo (Sendinblue) API (Free 300 emails/day).
   * https://www.brevo.com
   */
  private fun sendViaBrevo(
    toEmail: String,
    otpCode: String,
    apiKey: String,
    senderEmailInput: String = ""
  ): EmailOtpSendResult {
    return try {
      val (senderName, senderAddress) = parseSenderNameAndEmail(senderEmailInput)
      val senderObj = JSONObject().apply {
        put("name", senderName)
        put("email", senderAddress)
      }
      val toArray = JSONArray().apply {
        put(JSONObject().apply {
          put("email", toEmail)
          put("name", "Car Hisab User")
        })
      }
      val payload = JSONObject().apply {
        put("sender", senderObj)
        put("to", toArray)
        put("subject", "কার হিসাব (Car Hisab) - আপনার ভেরিফিকেশন ওটিপি কোড: $otpCode")
        put("htmlContent", generateHtmlBody(otpCode, toEmail))
      }

      val request = Request.Builder()
        .url("https://api.brevo.com/v3/smtp/email")
        .addHeader("api-key", apiKey)
        .addHeader("accept", "application/json")
        .addHeader("content-type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        EmailOtpSendResult(
          success = true,
          isRealEmailSent = true,
          code = otpCode,
          recipientEmail = toEmail,
          message = "Brevo (Sendinblue) API-র মাধ্যমে আপনার ইমেইলে ($toEmail) ৬-সংখ্যার আসল ওটিপি কোড পাঠানো হয়েছে! ইনবক্স এবং স্প্যাম ফোল্ডার চেক করুন।"
        )
      } else {
        val errorMsg = try {
          val json = JSONObject(respBody)
          val msg = json.optString("message", "")
          val code = json.optString("code", "")
          when {
            msg.contains("Sender email is not authorized", ignoreCase = true) || code.contains("invalid_parameter", true) ->
              "Brevo প্রেরকের ইমেইল অনুমোদন করা নেই। Brevo অ্যাকাউন্টে যে জিমেইল দিয়ে রেজিস্টার করেছেন (Verified Sender), সেটি সেটিংসের 'প্রেরকের ইমেইল'-এ দিন।"
            msg.contains("Key not found", ignoreCase = true) || code.contains("unauthorized", true) ->
              "Brevo API Key সঠিক নয়। অনুগ্রহ করে Brevo অ্যাকাউন্ট থেকে নতুন API Key (xkeysib-...) কপি করে দিন।"
            msg.isNotBlank() -> "Brevo API ত্রুটি: $msg"
            else -> "Brevo API রেসপন্স কোড: HTTP ${response.code}"
          }
        } catch (_: Exception) {
          "Brevo ত্রুটি: HTTP ${response.code}"
        }

        EmailOtpSendResult(
          success = false,
          isRealEmailSent = false,
          code = otpCode,
          recipientEmail = toEmail,
          message = errorMsg,
          errorDetail = "HTTP ${response.code}: $respBody"
        )
      }
    } catch (e: Exception) {
      EmailOtpSendResult(
        success = false,
        isRealEmailSent = false,
        code = otpCode,
        recipientEmail = toEmail,
        message = "ইমেইল নেটওয়ার্ক সংযোগে সমস্যা: ${e.localizedMessage ?: "Network error"}",
        errorDetail = e.message
      )
    }
  }

  /**
   * Dispatches an email using a custom Webhook or Google Apps Script Web App.
   */
  private fun sendViaWebhook(
    toEmail: String,
    otpCode: String,
    webhookUrl: String
  ): EmailOtpSendResult {
    return try {
      val payload = JSONObject().apply {
        put("email", toEmail)
        put("to", toEmail)
        put("otp", otpCode)
        put("code", otpCode)
        put("appName", "Car Hisab")
        put("subject", "কার হিসাব - ভেরিফিকেশন ওটিপি কোড")
      }

      val request = Request.Builder()
        .url(webhookUrl)
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = httpClient.newCall(request).execute()
      val respBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        EmailOtpSendResult(
          success = true,
          isRealEmailSent = true,
          code = otpCode,
          recipientEmail = toEmail,
          message = "ওয়েবহুকের মাধ্যমে আপনার ইমেইলে ($toEmail) ওটিপি কোড পাঠানো হয়েছে!"
        )
      } else {
        EmailOtpSendResult(
          success = false,
          isRealEmailSent = false,
          code = otpCode,
          recipientEmail = toEmail,
          message = "ওয়েবহুক সংযোগ ব্যর্থ হয়েছে।",
          errorDetail = "HTTP ${response.code}: $respBody"
        )
      }
    } catch (e: Exception) {
      EmailOtpSendResult(
        success = false,
        isRealEmailSent = false,
        code = otpCode,
        recipientEmail = toEmail,
        message = "ওয়েবহুক সংযোগে সমস্যা।",
        errorDetail = e.message
      )
    }
  }

  private fun generateHtmlBody(otpCode: String, recipientEmail: String): String {
    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #05180f; margin: 0; padding: 20px; color: #ffffff; }
          .card { max-width: 500px; margin: 0 auto; background: #0d2818; border: 1px solid #1e4d30; border-radius: 16px; padding: 28px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
          .header { text-align: center; border-bottom: 1px solid #1e4d30; padding-bottom: 16px; margin-bottom: 20px; }
          .title { font-size: 22px; font-weight: bold; color: #2ecc71; margin: 0; }
          .subtitle { font-size: 13px; color: #94a3b8; margin-top: 4px; }
          .greeting { font-size: 15px; color: #e2e8f0; line-height: 1.5; margin-bottom: 16px; }
          .otp-box { background: #05180f; border: 2px dashed #00ff88; border-radius: 12px; padding: 18px; text-align: center; margin: 24px 0; }
          .otp-code { font-size: 38px; font-weight: 800; letter-spacing: 8px; color: #00ff88; font-family: monospace; margin: 0; }
          .instruction { font-size: 13px; color: #cbd5e1; line-height: 1.6; }
          .footer { text-align: center; font-size: 12px; color: #64748b; margin-top: 24px; border-top: 1px solid #1e4d30; padding-top: 14px; }
        </style>
      </head>
      <body>
        <div class="card">
          <div class="header">
            <h1 class="title">কার হিসাব (Car Hisab)</h1>
            <div class="subtitle">ড্রাইভার ও গাড়ির হিসাবের নিরাপদ সমাধান</div>
          </div>
          <div class="greeting">
            প্রিয় ব্যবহারকারী (<strong style="color:#00ff88;">$recipientEmail</strong>),<br>
            কার হিসাব অ্যাপে ভেরিফিকেশনের জন্য আপনার ৬-সংখ্যার ওয়ান-টাইম পাসওয়ার্ড (OTP) নিচে দেওয়া হলো:
          </div>
          <div class="otp-box">
            <div class="otp-code">$otpCode</div>
          </div>
          <div class="instruction">
            ⏱️ এই ওটিপি কোডটি আগামী <strong>৫ মিনিট</strong> পর্যন্ত কার্যকর থাকবে।<br>
            🔒 আপনার একাউন্টের নিরাপত্তার স্বার্থে এই কোডটি অন্য কারও সাথে শেয়ার করবেন না।
          </div>
          <div class="footer">
            &copy; কার হিসাব (Car Hisab) • নিরাপদ ও বিশ্বস্ত হিসাব ব্যবস্থা
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }
}
