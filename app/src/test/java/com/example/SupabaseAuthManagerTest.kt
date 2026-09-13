package com.example

import com.example.data.auth.SupabaseAuthManager
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
class SupabaseAuthManagerTest {

  @Test
  fun testCleanBaseUrl() {
    val rawRestUrl = "https://vuncvrkrjrqrmwusumdb.supabase.co/rest/v1/"
    val cleaned = SupabaseAuthManager.cleanBaseUrl(rawRestUrl)
    assertEquals("https://vuncvrkrjrqrmwusumdb.supabase.co", cleaned)
  }

  @Test
  fun testLoginWithEmptyEmailFails() = runBlocking {
    val result = SupabaseAuthManager.loginWithEmail(
      email = "",
      password = "password123"
    )
    assertFalse(result.success)
    assertTrue(result.message.contains("ইমেইল"))
    assertNotNull(result.errorDetail)
  }

  @Test
  fun testLoginWithEmptyPasswordFails() = runBlocking {
    val result = SupabaseAuthManager.loginWithEmail(
      email = "driver@gmail.com",
      password = ""
    )
    assertFalse(result.success)
    assertTrue(result.message.contains("পাসওয়ার্ড"))
    assertNotNull(result.errorDetail)
  }

  @Test
  fun testSignUpShortPasswordFails() = runBlocking {
    val result = SupabaseAuthManager.signUpWithEmail(
      email = "driver@gmail.com",
      password = "123"
    )
    assertFalse(result.success)
    assertTrue(result.message.contains("৬ অক্ষর"))
    assertNotNull(result.errorDetail)
  }

  @Test
  fun testSignInWithGoogleNullTokenFails() = runBlocking {
    val result = SupabaseAuthManager.signInWithGoogle(idToken = null)
    assertFalse(result.success)
    assertTrue(result.message.contains("null"))
    assertEquals("Google ID Token is null", result.errorDetail)
  }

  @Test
  fun testSignInWithGoogleBlankTokenFails() = runBlocking {
    val result = SupabaseAuthManager.signInWithGoogle(idToken = "   ")
    assertFalse(result.success)
    assertTrue(result.message.contains("blank"))
    assertEquals("Google ID Token is blank", result.errorDetail)
  }

  @Test
  fun testParseSupabaseError() {
    val rawJson = """{"error_code": "invalid_credentials", "msg": "Invalid login credentials"}"""
    val parsed = SupabaseAuthManager.parseSupabaseError(rawJson, 400)
    assertEquals("invalid_credentials", parsed.errorCode)
    assertEquals("Invalid login credentials", parsed.errorMessage)
    assertEquals("ইমেইল অথবা পাসওয়ার্ড সঠিক নয়।", parsed.userFriendlyMsg)
  }
}
