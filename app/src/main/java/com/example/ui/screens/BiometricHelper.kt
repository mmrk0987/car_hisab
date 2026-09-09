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
  when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
    BiometricManager.BIOMETRIC_SUCCESS -> {
      val executor = ContextCompat.getMainExecutor(context)
      val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(context, "Authentication error: \$errString", Toast.LENGTH_SHORT).show()
          }

          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(context, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
            onSuccess()
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
          }
        })

      val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(if (language == AppLanguage.BANGLA) "ফিঙ্গারপ্রিন্ট লগইন" else "Fingerprint Login")
        .setSubtitle(if (language == AppLanguage.BANGLA) "আপনার একাউন্টে প্রবেশ করতে আঙুল স্পর্শ করুন" else "Touch sensor to log in to your account")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

      biometricPrompt.authenticate(promptInfo)
    }
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
      Toast.makeText(context, "No biometric hardware found.", Toast.LENGTH_SHORT).show()
    }
    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
      Toast.makeText(context, "Biometric hardware is currently unavailable.", Toast.LENGTH_SHORT).show()
    }
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
      Toast.makeText(context, "No fingerprints enrolled on this device.", Toast.LENGTH_SHORT).show()
    }
    else -> {
      Toast.makeText(context, "Biometric authentication error.", Toast.LENGTH_SHORT).show()
    }
  }
}
