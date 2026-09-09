package com.example

import com.example.data.auth.EmailOtpManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmailOtpManagerTest {

  @Test
  fun testOtpSendWithoutApiKeyFails() = runBlocking {
    val email = "driver123@gmail.com"
    val result = EmailOtpManager.sendOtp(
      recipientEmail = email,
      provider = "BREVO",
      apiKey = "", // No API key configured
      senderEmail = ""
    )

    // No sandbox mode: must fail cleanly and require real API key
    assertFalse(result.success)
    assertFalse(result.isRealEmailSent)
    assertEquals("", result.code)
    assertTrue(result.message.contains("API Key") || result.message.contains("Brevo"))
  }

  @Test
  fun testOtpVerificationFlow() {
    val email = "driver123@gmail.com"
    val testCode = "849201"

    EmailOtpManager.setActiveCodeForTesting(testCode, email)

    // Test invalid OTP
    val (wrongVerify, wrongMsg) = EmailOtpManager.verifyOtp("000000", email)
    assertFalse(wrongVerify)
    assertNotNull(wrongMsg)

    // Test correct OTP
    val (correctVerify, correctMsg) = EmailOtpManager.verifyOtp(testCode, email)
    assertTrue(correctVerify)
    assertNotNull(correctMsg)

    // Test OTP is consumed (single-use)
    val (replayVerify, _) = EmailOtpManager.verifyOtp(testCode, email)
    assertFalse(replayVerify)
  }

  @Test
  fun testOtpVerificationWithWrongEmail() {
    val email = "driver_a@gmail.com"
    val testCode = "135790"

    EmailOtpManager.setActiveCodeForTesting(testCode, email)

    val (verifyWrongEmail, _) = EmailOtpManager.verifyOtp(testCode, "driver_b@gmail.com")
    assertFalse(verifyWrongEmail)
  }

  @Test
  fun testOtpExpiration() {
    val email = "driver_exp@gmail.com"
    val testCode = "246801"

    // Set code expired 1 second ago
    EmailOtpManager.setActiveCodeForTesting(testCode, email, expiryMillis = System.currentTimeMillis() - 1000L)

    val (expiredVerify, msg) = EmailOtpManager.verifyOtp(testCode, email)
    assertFalse(expiredVerify)
    assertTrue(msg.contains("মেয়াদ শেষ") || msg.contains("expired"))
  }
}
