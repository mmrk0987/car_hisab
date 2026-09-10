package com.example.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.EmailOtpManager
import com.example.data.auth.SupabaseAuthManager
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.LossRed
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.ProfitGreen
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  language: AppLanguage,
  savedEmail: String = "",
  savedRememberEmail: Boolean = true,
  otpProvider: String = "BREVO",
  otpApiKey: String = "",
  otpWebhookUrl: String = "",
  otpSenderEmail: String = "",
  supabaseUrl: String = SupabaseAuthManager.DEFAULT_SUPABASE_URL,
  supabaseAnonKey: String = SupabaseAuthManager.DEFAULT_ANON_KEY,
  onSaveOtpSettings: (provider: String, apiKey: String, webhook: String, sender: String) -> Unit = { _, _, _, _ -> },
  onLoginSuccess: (email: String, remember: Boolean) -> Unit = { _, _ -> },
  onSignUpSuccess: (email: String, phone: String) -> Unit = { _, _ -> }
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val coroutineScope = rememberCoroutineScope()
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Sign Up

  // Login States
  var loginEmail by remember { mutableStateOf(if (savedRememberEmail) savedEmail else "") }
  var loginPassword by remember { mutableStateOf("") }
  var rememberEmail by remember { mutableStateOf(savedRememberEmail) }
  var isLoginPasswordVisible by remember { mutableStateOf(false) }
  var loginError by remember { mutableStateOf<String?>(null) }
  var isLoggingIn by remember { mutableStateOf(false) }
  var showFingerprintDialog by remember { mutableStateOf(false) }

  // Sign Up States
  var signUpGmail by remember { mutableStateOf("") }
  var signUpPassword by remember { mutableStateOf("") }
  var signUpRetypePassword by remember { mutableStateOf("") }
  var signUpPhone by remember { mutableStateOf("") }
  var isSignUpPasswordVisible by remember { mutableStateOf(false) }
  var isRetypePasswordVisible by remember { mutableStateOf(false) }
  var signUpError by remember { mutableStateOf<String?>(null) }
  var isSigningUp by remember { mutableStateOf(false) }

  // Email OTP Modal & Dispatch States
  var showOtpModal by remember { mutableStateOf(false) }
  var otpTargetEmail by remember { mutableStateOf("") }
  var otpTargetPhone by remember { mutableStateOf("") }
  var otpIsForLogin by remember { mutableStateOf(false) }
  var otpInput by remember { mutableStateOf("") }
  var activeOtpCode by remember { mutableStateOf("") }
  var isSendingOtp by remember { mutableStateOf(false) }
  var isRealEmailSent by remember { mutableStateOf(false) }
  var otpSendStatusMessage by remember { mutableStateOf<String?>(null) }
  var otpModalError by remember { mutableStateOf<String?>(null) }
  var resendCountdown by remember { mutableIntStateOf(60) }
  var showGatewayConfig by remember { mutableStateOf(false) }
  var tempProvider by remember(otpProvider) { mutableStateOf(if (otpProvider.isBlank() || otpProvider.equals("RESEND", true)) "BREVO" else otpProvider) }
  var tempApiKey by remember(otpApiKey) { mutableStateOf(otpApiKey) }
  var tempSenderEmail by remember(otpSenderEmail) { mutableStateOf(otpSenderEmail) }

  // OTP Countdown timer
  LaunchedEffect(showOtpModal, resendCountdown) {
    if (showOtpModal && resendCountdown > 0) {
      kotlinx.coroutines.delay(1000L)
      resendCountdown -= 1
    }
  }

  // Automatic Google/Gmail Account Detection from Android Phone
  val detectedAccounts = remember(context) {
    try {
      val am = AccountManager.get(context)
      am.getAccountsByType("com.google")
        .mapNotNull { it.name }
        .filter { it.contains("@") && it.lowercase().endsWith("@gmail.com") }
    } catch (e: Exception) {
      emptyList()
    }
  }

  // System Google Account Picker
  val accountPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
      if (!accountName.isNullOrBlank()) {
        signUpGmail = accountName
        signUpError = null
      }
    }
  }

  val launchAccountPicker: () -> Unit = {
    try {
      val intent = AccountManager.newChooseAccountIntent(
        null,
        null,
        arrayOf("com.google"),
        false,
        null,
        null,
        null,
        null
      )
      accountPickerLauncher.launch(intent)
    } catch (e: Exception) {
      if (detectedAccounts.isNotEmpty()) {
        signUpGmail = detectedAccounts.first()
        signUpError = null
      }
    }
  }

  // Auto pre-populate device Gmail upon switching to Sign Up tab
  LaunchedEffect(selectedTab) {
    if (selectedTab == 1 && signUpGmail.isBlank()) {
      if (detectedAccounts.isNotEmpty()) {
        signUpGmail = detectedAccounts.first()
        signUpError = null
      } else {
        try {
          val intent = AccountManager.newChooseAccountIntent(
            null,
            null,
            arrayOf("com.google"),
            false,
            null,
            null,
            null,
            null
          )
          accountPickerLauncher.launch(intent)
        } catch (_: Exception) {}
      }
    }
  }

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF07120D),
            Color(0xFF0C1D15),
            Color(0xFF091410)
          )
        )
      )
      .testTag("login_screen_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // App Brand Emblem
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(CircleShape)
          .background(DarkGreenCard)
          .border(2.dp, MintGreenAccent.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.DirectionsCar,
          contentDescription = "Car Hisab",
          tint = MintGreenAccent,
          modifier = Modifier.size(42.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Car Hisab",
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFF1F5F9),
        letterSpacing = 0.5.sp
      )

      Text(
        text = if (language == AppLanguage.BANGLA) "ড্রাইভার ও গাড়ির হিসাবের আধুনিক অ্যাপ (Supabase Auth)" else "Modern Fleet & Driver Financial System (Supabase Auth)",
        fontSize = 13.sp,
        color = Color(0xFF94A3B8),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
      )

      // Tabs: লগইন (Sign In) / নতুন একাউন্ট (Sign Up)
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = DarkGreenSurface,
        contentColor = MintGreenAccent,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = MintGreenAccent,
            height = 3.dp
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, DarkGreenBorder, RoundedCornerShape(16.dp))
          .testTag("auth_tab_row")
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Text(
              text = if (language == AppLanguage.BANGLA) "লগইন" else "Sign In",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              fontSize = 15.sp,
              color = if (selectedTab == 0) MintGreenAccent else Color(0xFF94A3B8)
            )
          },
          modifier = Modifier.testTag("tab_login")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              text = if (language == AppLanguage.BANGLA) "নতুন একাউন্ট" else "Sign Up",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
              fontSize = 15.sp,
              color = if (selectedTab == 1) MintGreenAccent else Color(0xFF94A3B8)
            )
          },
          modifier = Modifier.testTag("tab_signup")
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // TAB 0: LOGIN CONTENT
      if (selectedTab == 0) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Email Field
          OutlinedTextField(
            value = loginEmail,
            onValueChange = {
              loginEmail = it
              loginError = null
            },
            label = { Text(if (language == AppLanguage.BANGLA) "ইমেইল / জিমেইল" else "Email / Gmail") },
            placeholder = { Text("driver@gmail.com") },
            leadingIcon = {
              Icon(Icons.Default.Email, contentDescription = null, tint = MintGreenAccent)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkGreenSurface,
              unfocusedContainerColor = DarkGreenSurface,
              focusedBorderColor = MintGreenAccent,
              unfocusedBorderColor = DarkGreenBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_email_input")
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Password Field
          OutlinedTextField(
            value = loginPassword,
            onValueChange = {
              loginPassword = it
              loginError = null
            },
            label = { Text(if (language == AppLanguage.BANGLA) "পাসওয়ার্ড" else "Password") },
            placeholder = { Text("••••••") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null, tint = MintGreenAccent)
            },
            trailingIcon = {
              IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                Icon(
                  imageVector = if (isLoginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = "Toggle password",
                  tint = Color(0xFF94A3B8)
                )
              }
            },
            visualTransformation = if (isLoginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkGreenSurface,
              unfocusedContainerColor = DarkGreenSurface,
              focusedBorderColor = MintGreenAccent,
              unfocusedBorderColor = DarkGreenBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input")
          )

          Spacer(modifier = Modifier.height(8.dp))

          // "Remember user Email" Checkbox
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { rememberEmail = !rememberEmail }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = rememberEmail,
              onCheckedChange = { rememberEmail = it },
              colors = CheckboxDefaults.colors(
                checkedColor = DarkGreenPrimary,
                checkmarkColor = Color.White,
                uncheckedColor = Color(0xFF94A3B8)
              ),
              modifier = Modifier.testTag("remember_email_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "আমার ইমেইল মনে রাখুন" else "Remember user Email",
              fontSize = 13.sp,
              color = Color(0xFFCBD5E1),
              fontWeight = FontWeight.Medium
            )
          }

          if (loginError != null) {
            Text(
              text = loginError ?: "",
              color = LossRed,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Primary Login Button (Integrated with Supabase Auth)
          Button(
            onClick = {
              val email = loginEmail.trim()
              val password = loginPassword.trim()
              if (email.isBlank()) {
                loginError = if (language == AppLanguage.BANGLA) "দয়া করে ইমেইল দিন" else "Please enter your email"
                return@Button
              }
              if (password.isBlank()) {
                loginError = if (language == AppLanguage.BANGLA) "পাসওয়ার্ড দিন" else "Please enter password"
                return@Button
              }

              isLoggingIn = true
              loginError = null

              coroutineScope.launch {
                val authResult = SupabaseAuthManager.loginWithEmail(
                  email = email,
                  password = password,
                  baseUrl = supabaseUrl,
                  anonKey = supabaseAnonKey
                )
                isLoggingIn = false

                if (authResult.success) {
                  onLoginSuccess(authResult.email ?: email, rememberEmail)
                } else {
                  loginError = authResult.message
                }
              }
            },
            enabled = !isLoggingIn,
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("login_submit_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
          ) {
            if (isLoggingIn) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF022B1E),
                strokeWidth = 2.5.dp
              )
            } else {
              Text(
                text = if (language == AppLanguage.BANGLA) "Supabase-এ লগইন করুন" else "Log In with Supabase",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF022B1E)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Supabase Email OTP Direct Login Button
          OutlinedButton(
            onClick = {
              val email = loginEmail.trim().lowercase()
              if (email.isBlank() || !email.contains("@")) {
                loginError = if (language == AppLanguage.BANGLA) "ওটিপি পেতে প্রথমে আপনার জিমেইল ঠিকানাটি লিখুন" else "Enter your Gmail address to receive OTP"
                return@OutlinedButton
              }
              loginError = null
              otpTargetEmail = email
              otpTargetPhone = ""
              otpIsForLogin = true
              otpInput = ""
              otpModalError = null
              isSendingOtp = true
              showOtpModal = true
              resendCountdown = 60

              coroutineScope.launch {
                // Try Supabase OTP dispatch
                val supabaseResult = SupabaseAuthManager.sendEmailOtp(
                  email = email,
                  baseUrl = supabaseUrl,
                  anonKey = supabaseAnonKey
                )

                if (supabaseResult.success) {
                  isSendingOtp = false
                  isRealEmailSent = true
                  activeOtpCode = ""
                  otpSendStatusMessage = supabaseResult.message
                } else {
                  // Fallback to Brevo/configured provider if Supabase OTP email isn't configured in project settings
                  val result = EmailOtpManager.sendOtp(
                    recipientEmail = email,
                    provider = otpProvider,
                    apiKey = otpApiKey,
                    webhookUrl = otpWebhookUrl,
                    senderEmail = otpSenderEmail
                  )
                  isSendingOtp = false
                  isRealEmailSent = result.isRealEmailSent
                  activeOtpCode = result.code
                  otpSendStatusMessage = result.message
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("login_with_email_otp_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DarkGreenSurface,
              contentColor = MintGreenAccent
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.6f))
          ) {
            Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = MintGreenAccent)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "📧 ওটিপি দিয়ে সরাসরি লগইন" else "Login with Email OTP",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = MintGreenAccent
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Fingerprint Login Option Button
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .clip(RoundedCornerShape(16.dp))
              .clickable {
                if (savedEmail.isNotBlank() && savedRememberEmail) {
                  authenticateWithBiometric(context, language) {
                    onLoginSuccess(savedEmail, savedRememberEmail)
                  }
                } else {
                  Toast.makeText(context, if (language == AppLanguage.BANGLA) "প্রথমে পাসওয়ার্ড দিয়ে লগইন করে 'Remember Me' সেভ করুন।" else "Login with password first to enable fingerprint.", Toast.LENGTH_LONG).show()
                }
              }
              .testTag("fingerprint_login_option_button"),
            colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Fingerprint",
                tint = MintGreenAccent,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে লগইন করুন" else "Login with Fingerprint",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF1F5F9)
              )
            }
          }
        }
      }

      // TAB 1: SIGN UP CONTENT
      if (selectedTab == 1) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Device Gmail Auto-Detection Banner
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { launchAccountPicker() }
              .testTag("auto_gmail_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGreenCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.35f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(MintGreenAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.PhoneAndroid,
                      contentDescription = null,
                      tint = MintGreenAccent,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = if (language == AppLanguage.BANGLA) "মোবাইলের জিমেইল দিয়ে সাইনআপ" else "Auto-fill Phone Gmail",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                    Text(
                      text = if (signUpGmail.isNotBlank())
                        (if (language == AppLanguage.BANGLA) "জিমেইল প্রস্তুত আছে ✓" else "Gmail is ready ✓")
                      else
                        (if (language == AppLanguage.BANGLA) "মোবাইলের অ্যাকাউন্টটি নির্বাচন করতে ট্যাপ করুন" else "Tap to choose Google Account"),
                      fontSize = 11.sp,
                      color = if (signUpGmail.isNotBlank()) ProfitGreen else Color(0xFF94A3B8)
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MintGreenAccent.copy(alpha = 0.18f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.4f)),
                  modifier = Modifier.clickable { launchAccountPicker() }
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = MintGreenAccent,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (language == AppLanguage.BANGLA) "অটো সিলেক্ট" else "Auto Select",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = MintGreenAccent
                    )
                  }
                }
              }

              // Detected Accounts Chips if multiple exist
              if (detectedAccounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  detectedAccounts.take(3).forEach { acc ->
                    val isSelected = (signUpGmail.trim().lowercase() == acc.trim().lowercase())
                    Surface(
                      shape = RoundedCornerShape(20.dp),
                      color = if (isSelected) MintGreenAccent.copy(alpha = 0.25f) else DarkGreenSurface,
                      border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MintGreenAccent else DarkGreenBorder
                      ),
                      modifier = Modifier
                        .clickable {
                          signUpGmail = acc
                          signUpError = null
                        }
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      ) {
                        Icon(
                          imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                          contentDescription = null,
                          tint = if (isSelected) MintGreenAccent else Color(0xFF94A3B8),
                          modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = acc,
                          fontSize = 11.sp,
                          color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Gmail Only Field
          OutlinedTextField(
            value = signUpGmail,
            onValueChange = {
              signUpGmail = it
              signUpError = null
            },
            label = { Text(if (language == AppLanguage.BANGLA) "জিমেইল ঠিকানা (Gmail Only)" else "Gmail Address (Only @gmail.com)") },
            placeholder = { Text("username@gmail.com") },
            leadingIcon = {
              Icon(Icons.Default.Email, contentDescription = null, tint = MintGreenAccent)
            },
            trailingIcon = {
              IconButton(
                onClick = { launchAccountPicker() },
                modifier = Modifier.testTag("btn_auto_pick_gmail")
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = "Auto Pick Gmail",
                  tint = MintGreenAccent,
                  modifier = Modifier.size(20.dp)
                )
              }
            },
            supportingText = {
              Text(
                text = if (signUpGmail.isNotEmpty() && signUpGmail.trim().lowercase().endsWith("@gmail.com"))
                  (if (language == AppLanguage.BANGLA) "✓ জিমেইল প্রস্তুত রয়েছে" else "✓ Gmail is ready for sign up")
                else if (signUpGmail.isNotEmpty())
                  (if (language == AppLanguage.BANGLA) "শুধুমাত্র @gmail.com অ্যাকাউন্ট গ্রহণযোগ্য" else "Only @gmail.com accounts accepted")
                else
                  (if (language == AppLanguage.BANGLA) "মোবাইল থেকে জিমেইল আনতে অটো-সিলেক্ট বাটন চাপুন" else "Tap auto-select to fetch from phone"),
                fontSize = 11.sp,
                color = if (signUpGmail.isNotEmpty() && !signUpGmail.trim().lowercase().endsWith("@gmail.com")) LossRed else if (signUpGmail.isNotEmpty()) ProfitGreen else Color(0xFF94A3B8)
              )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkGreenSurface,
              unfocusedContainerColor = DarkGreenSurface,
              focusedBorderColor = MintGreenAccent,
              unfocusedBorderColor = DarkGreenBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_gmail_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Phone Number
          OutlinedTextField(
            value = signUpPhone,
            onValueChange = {
              signUpPhone = it
              signUpError = null
            },
            label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর" else "Phone Number") },
            placeholder = { Text("01712345678") },
            leadingIcon = {
              Icon(Icons.Default.Phone, contentDescription = null, tint = MintGreenAccent)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkGreenSurface,
              unfocusedContainerColor = DarkGreenSurface,
              focusedBorderColor = MintGreenAccent,
              unfocusedBorderColor = DarkGreenBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_phone_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Password
          OutlinedTextField(
            value = signUpPassword,
            onValueChange = {
              signUpPassword = it
              signUpError = null
            },
            label = { Text(if (language == AppLanguage.BANGLA) "পাসওয়ার্ড দিন" else "Password") },
            placeholder = { Text("কমপক্ষে ৬ অক্ষর") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null, tint = MintGreenAccent)
            },
            trailingIcon = {
              IconButton(onClick = { isSignUpPasswordVisible = !isSignUpPasswordVisible }) {
                Icon(
                  imageVector = if (isSignUpPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = null,
                  tint = Color(0xFF94A3B8)
                )
              }
            },
            visualTransformation = if (isSignUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkGreenSurface,
              unfocusedContainerColor = DarkGreenSurface,
              focusedBorderColor = MintGreenAccent,
              unfocusedBorderColor = DarkGreenBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_password_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Retype Password
          OutlinedTextField(
            value = signUpRetypePassword,
            onValueChange = {
              signUpRetypePassword = it
              signUpError = null
            },
            label = { Text(if (language == AppLanguage.BANGLA) "পুনরায় পাসওয়ার্ড দিন" else "Retype Password") },
            placeholder = { Text("একই পাসওয়ার্ড পুনরায় লিখুন") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null, tint = MintGreenAccent)
            },
            trailingIcon = {
              IconButton(onClick = { isRetypePasswordVisible = !isRetypePasswordVisible }) {
                Icon(
                  imageVector = if (isRetypePasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = null,
                  tint = Color(0xFF94A3B8)
                )
              }
            },
            visualTransformation = if (isRetypePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkGreenSurface,
              unfocusedContainerColor = DarkGreenSurface,
              focusedBorderColor = MintGreenAccent,
              unfocusedBorderColor = DarkGreenBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_retype_password_input")
          )

          if (signUpError != null) {
            Text(
              text = signUpError ?: "",
              color = LossRed,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 6.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Sign Up / Supabase Register Button
          Button(
            onClick = {
              val email = signUpGmail.trim().lowercase()
              val phone = signUpPhone.trim()
              val password = signUpPassword
              if (!email.endsWith("@gmail.com") || email.length <= 10) {
                signUpError = if (language == AppLanguage.BANGLA) "শুধুমাত্র বৈধ Gmail (@gmail.com) দিয়ে সাইন আপ সম্ভব" else "Only valid Gmail (@gmail.com) allowed"
                return@Button
              }
              if (phone.length < 11) {
                signUpError = if (language == AppLanguage.BANGLA) "সঠিক ১১-সংখ্যার মোবাইল নম্বর দিন" else "Enter valid 11-digit phone number"
                return@Button
              }
              if (password.length < 6) {
                signUpError = if (language == AppLanguage.BANGLA) "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে" else "Password must be at least 6 characters"
                return@Button
              }
              if (password != signUpRetypePassword) {
                signUpError = if (language == AppLanguage.BANGLA) "উভয় পাসওয়ার্ড মিলছে না" else "Passwords do not match"
                return@Button
              }

              signUpError = null
              isSigningUp = true

              coroutineScope.launch {
                val supabaseResult = SupabaseAuthManager.signUpWithEmail(
                  email = email,
                  password = password,
                  phone = phone,
                  baseUrl = supabaseUrl,
                  anonKey = supabaseAnonKey
                )
                isSigningUp = false

                if (supabaseResult.success) {
                  onSignUpSuccess(email, phone)
                } else {
                  signUpError = supabaseResult.message
                }
              }
            },
            enabled = !isSigningUp,
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("signup_submit_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
          ) {
            if (isSigningUp) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF022B1E),
                strokeWidth = 2.5.dp
              )
            } else {
              Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = Color(0xFF022B1E))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "Supabase-এ একাউন্ট খুলুন" else "Register with Supabase",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF022B1E)
              )
            }
          }
        }
      }
    }

    // EMAIL OTP VERIFICATION MODAL
    if (showOtpModal) {
      AlertDialog(
        onDismissRequest = { showOtpModal = false },
        containerColor = DarkGreenSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.testTag("email_otp_dialog"),
        icon = {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(MintGreenAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MarkEmailRead,
              contentDescription = "Email OTP",
              tint = MintGreenAccent,
              modifier = Modifier.size(28.dp)
            )
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = if (language == AppLanguage.BANGLA) "জিমেইল ওটিপি যাচাইকরণ" else "Gmail OTP Verification",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "৬-সংখ্যার কোড পাঠানো হয়েছে:" else "6-digit OTP dispatched to:",
              fontSize = 12.sp,
              color = Color(0xFF94A3B8),
              textAlign = TextAlign.Center
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = DarkGreenCard,
              border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder),
              modifier = Modifier.padding(top = 4.dp)
            ) {
              Text(
                text = otpTargetEmail,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MintGreenAccent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Status Notification Card
            if (isSendingOtp) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGreenCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.3f))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MintGreenAccent,
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA) "ইমেইল সার্ভারে ওটিপি পাঠানো হচ্ছে..." else "Dispatching email OTP...",
                    fontSize = 12.sp,
                    color = Color(0xFFE2E8F0)
                  )
                }
              }
            } else if (isRealEmailSent) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF06331E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProfitGreen.copy(alpha = 0.5f))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ProfitGreen,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = otpSendStatusMessage ?: (if (language == AppLanguage.BANGLA)
                      "আপনার জিমেইলে আসল ওটিপি পাঠানো হয়েছে! ইনবক্স এবং স্প্যাম ফোল্ডার দেখুন।"
                    else
                      "Real OTP delivered to your inbox! Check your inbox or spam."),
                    fontSize = 12.sp,
                    color = Color(0xFFD1FAE5),
                    lineHeight = 16.sp
                  )
                }
              }
            } else if (otpSendStatusMessage != null) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1616)),
                border = androidx.compose.foundation.BorderStroke(1.dp, LossRed.copy(alpha = 0.5f))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = null,
                      tint = LossRed,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (language == AppLanguage.BANGLA) "ওটিপি পাঠানো যায়নি" else "OTP Delivery Failed",
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFFCA5A5)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = otpSendStatusMessage ?: "",
                    fontSize = 11.5.sp,
                    color = Color(0xFFFECACA),
                    lineHeight = 16.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 6-Digit OTP Input
            OutlinedTextField(
              value = otpInput,
              onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                  otpInput = it
                  otpModalError = null
                }
              },
              label = { Text(if (language == AppLanguage.BANGLA) "৬-সংখ্যার ওটিপি কোড" else "6-Digit OTP Code") },
              placeholder = { Text("••••••") },
              singleLine = true,
              textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
                color = Color.White
              ),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkGreenCard,
                unfocusedContainerColor = DarkGreenCard,
                focusedBorderColor = MintGreenAccent,
                unfocusedBorderColor = DarkGreenBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFE2E8F0)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_code_input")
            )

            if (otpModalError != null) {
              Text(
                text = otpModalError ?: "",
                color = LossRed,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timer & Resend OTP Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (resendCountdown > 0) {
                Icon(
                  imageVector = Icons.Default.Timer,
                  contentDescription = null,
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (language == AppLanguage.BANGLA)
                    "পুনরায় ওটিপি পাঠানোর সময়: 0:${String.format(java.util.Locale.US, "%02d", resendCountdown)}"
                  else
                    "Resend code in 0:${String.format(java.util.Locale.US, "%02d", resendCountdown)}",
                  fontSize = 12.sp,
                  color = Color(0xFF94A3B8)
                )
              } else {
                TextButton(
                  onClick = {
                    resendCountdown = 60
                    isSendingOtp = true
                    otpModalError = null
                    coroutineScope.launch {
                      val supabaseResult = SupabaseAuthManager.sendEmailOtp(
                        email = otpTargetEmail,
                        baseUrl = supabaseUrl,
                        anonKey = supabaseAnonKey
                      )
                      if (supabaseResult.success) {
                        isSendingOtp = false
                        isRealEmailSent = true
                        activeOtpCode = ""
                        otpSendStatusMessage = supabaseResult.message
                      } else {
                        val result = EmailOtpManager.sendOtp(
                          recipientEmail = otpTargetEmail,
                          provider = otpProvider,
                          apiKey = otpApiKey,
                          webhookUrl = otpWebhookUrl,
                          senderEmail = otpSenderEmail
                        )
                        isSendingOtp = false
                        isRealEmailSent = result.isRealEmailSent
                        activeOtpCode = result.code
                        otpSendStatusMessage = result.message
                      }
                    }
                  },
                  modifier = Modifier.testTag("otp_resend_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MintGreenAccent,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA) "পুনরায় কোড পাঠান" else "Resend OTP Code",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintGreenAccent
                  )
                }
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              coroutineScope.launch {
                // Try Supabase OTP verification first
                val supabaseResult = SupabaseAuthManager.verifyEmailOtp(
                  email = otpTargetEmail,
                  token = otpInput,
                  baseUrl = supabaseUrl,
                  anonKey = supabaseAnonKey
                )

                if (supabaseResult.success) {
                  showOtpModal = false
                  if (otpIsForLogin) {
                    onLoginSuccess(otpTargetEmail, rememberEmail)
                  } else {
                    onSignUpSuccess(otpTargetEmail, otpTargetPhone)
                  }
                } else {
                  // Fall back to EmailOtpManager verification if activeCode was used
                  val (verified, message) = EmailOtpManager.verifyOtp(otpInput, otpTargetEmail)
                  if (verified) {
                    showOtpModal = false
                    if (otpIsForLogin) {
                      onLoginSuccess(otpTargetEmail, rememberEmail)
                    } else {
                      onSignUpSuccess(otpTargetEmail, otpTargetPhone)
                    }
                  } else {
                    otpModalError = supabaseResult.message.ifBlank { message }
                  }
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("otp_confirm_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
          ) {
            Text(
              text = if (language == AppLanguage.BANGLA) "যাচাই করুন ও প্রবেশ করুন" else "Verify & Proceed",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF022B1E)
            )
          }
        },
        dismissButton = {
          TextButton(
            onClick = { showOtpModal = false },
            modifier = Modifier.testTag("otp_cancel_button")
          ) {
            Text(
              text = if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }
      )
    }

  }
}
