package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.i18n.AppLanguage

fun authenticateWithBiometric(
  context: Context,
  language: AppLanguage,
  onSuccess: () -> Unit
) {
  val activity = context as? FragmentActivity
  if (activity == null) {
    Toast.makeText(context, "Biometric authentication not supported on this device context.", Toast.LENGTH_SHORT).show()
    return
  }

  val biometricManager = BiometricManager.from(context)
  val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
  when (canAuth) {
    BiometricManager.BIOMETRIC_SUCCESS -> {
      val executor = ContextCompat.getMainExecutor(context)
      val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            val msg = if (errorCode == BiometricPrompt.ERROR_LOCKOUT || errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT) {
              if (language == AppLanguage.BANGLA) "অত্যধিক ভুল চেষ্টার কারণে ফিঙ্গারপ্রিন্ট সাময়িকভাবে লক করা হয়েছে।" else "Biometric locked out due to too many attempts."
            } else {
              if (language == AppLanguage.BANGLA) "ফিঙ্গারপ্রিন্ট ত্রুটি: $errString" else "Biometric error: $errString"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
          }

          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            val msg = if (language == AppLanguage.BANGLA) "ফিঙ্গারপ্রিন্ট মেলেনি, আবার চেষ্টা করুন" else "Fingerprint did not match"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
          }
        })

      val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(if (language == AppLanguage.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে লগইন" else "Fingerprint Login")
        .setSubtitle(if (language == AppLanguage.BANGLA) "আপনার একাউন্টে প্রবেশ করতে আঙুল স্পর্শ করুন" else "Touch sensor to log in to your account")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

      biometricPrompt.authenticate(promptInfo)
    }
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
      val msg = if (language == AppLanguage.BANGLA) "এই ফোনে ফিঙ্গারপ্রিন্ট সেন্সর নেই।" else "No biometric hardware found."
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
      val msg = if (language == AppLanguage.BANGLA) "ফিঙ্গারপ্রিন্ট সেন্সর আপাতত অনুপলব্ধ।" else "Biometric hardware is currently unavailable."
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
      val msg = if (language == AppLanguage.BANGLA) "আপনার ফোনে ফিঙ্গারপ্রিন্ট সেটআপ করা নেই। ফোনের সেটিংসে গিয়ে ফিঙ্গারপ্রিন্ট সেটআপ করুন।" else "No fingerprint enrolled. Please set up fingerprint in phone settings."
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
    else -> {
      val msg = if (language == AppLanguage.BANGLA) "ফিঙ্গারপ্রিন্ট ব্যবহারে সমস্যা হচ্ছে।" else "Biometric authentication error."
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
  }
}
