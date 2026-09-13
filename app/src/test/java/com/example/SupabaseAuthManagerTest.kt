package com.example

import com.example.data.auth.SupabaseAuthManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
  }

  @Test
  fun testLoginWithEmptyPasswordFails() = runBlocking {
    val result = SupabaseAuthManager.loginWithEmail(
      email = "driver@gmail.com",
      password = ""
    )
    assertFalse(result.success)
    assertTrue(result.message.contains("পাসওয়ার্ড"))
  }

  @Test
  fun testSignUpShortPasswordFails() = runBlocking {
    val result = SupabaseAuthManager.signUpWithEmail(
      email = "driver@gmail.com",
      password = "123"
    )
    assertFalse(result.success)
    assertTrue(result.message.contains("৬ অক্ষর"))
  }
}
