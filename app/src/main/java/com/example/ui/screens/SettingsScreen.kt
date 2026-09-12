package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.data.auth.EmailOtpManager
import com.example.data.export.ExportFormat
import com.example.data.export.ExportScope
import com.example.data.model.TripEntity
import com.example.data.repository.UserProfile
import com.example.ui.components.ExportDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.LossRed
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.ProfitGreen
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  language: AppLanguage,
  themeMode: AppThemeMode,
  profile: UserProfile,
  trips: List<TripEntity> = emptyList(),
  emailOtpProvider: String = "RESEND",
  emailOtpApiKey: String = "",
  emailOtpWebhookUrl: String = "",
  emailOtpSenderEmail: String = "Car Hisab <onboarding@resend.dev>",
  onSaveEmailOtpSettings: (provider: String, apiKey: String, webhook: String, sender: String) -> Unit = { _, _, _, _ -> },
  onLanguageChange: (AppLanguage) -> Unit = {},
  onOpenLanguageSettings: () -> Unit = {},
  onOpenDriveBackup: () -> Unit = {},
  onOpenThemeSettings: () -> Unit = {},
  onThemeModeChange: (AppThemeMode) -> Unit = {},
  onUpdateProfile: (
    carName: String,
    carModel: String,
    carNumber: String,
    driverName: String,
    phone: String,
    email: String
  ) -> Unit,
  onSaveProfilePhoto: (Uri) -> Unit = {},
  onRemoveProfilePhoto: () -> Unit = {},
  onOpenSubscription: () -> Unit = {},
  onOpenDocuments: () -> Unit = {},
  onExportTrips: (format: ExportFormat, tripsToExport: List<TripEntity>) -> Unit = { _, _ -> },
  lastBackupTime: Long = 0L,
  lastBackupCount: Int = 0,
  isBackingUp: Boolean = false,
  isRestoring: Boolean = false,
  backupStatusMessage: String? = null,
  onGoogleDriveBackup: () -> Unit = {},
  onGoogleDriveRestore: (Uri?) -> Unit = {},
  onLogout: () -> Unit = {}
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  var versionTapCount by remember { mutableStateOf(0) }
  var showAdminGeneratorDialog by remember { mutableStateOf(false) }
  var adminTargetIdInput by remember { mutableStateOf("") }
  var adminSelectedDurationDays by remember { mutableStateOf(30) }
  var generatedResultKey by remember { mutableStateOf("") }
  
  var showRestoreConfirmDialog by remember { mutableStateOf(false) }
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onSaveProfilePhoto(uri)
    }
  }

  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var exportInitialScope by remember { mutableStateOf(ExportScope.CURRENT_MONTH) }

  var showEmailOtpSettingsDialog by remember { mutableStateOf(false) }
  var selectedOtpProvider by remember(emailOtpProvider) {
    mutableStateOf(if (emailOtpProvider.isBlank() || emailOtpProvider.equals("RESEND", true)) "BREVO" else emailOtpProvider)
  }
  var enteredOtpApiKey by remember { mutableStateOf(emailOtpApiKey) }
  var enteredOtpWebhookUrl by remember { mutableStateOf(emailOtpWebhookUrl) }
  var enteredOtpSenderEmail by remember { mutableStateOf(emailOtpSenderEmail) }
  var isSendingTestOtp by remember { mutableStateOf(false) }
  var testOtpResult by remember { mutableStateOf<String?>(null) }
  var isTestSuccess by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  val currentCal = remember { java.util.Calendar.getInstance() }
  val curYear = currentCal.get(java.util.Calendar.YEAR)
  val curMonth = currentCal.get(java.util.Calendar.MONTH)

  val currentMonthTrips = remember(trips) {
    trips.filter { trip ->
      val tCal = java.util.Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      tCal.get(java.util.Calendar.YEAR) == curYear && tCal.get(java.util.Calendar.MONTH) == curMonth
    }
  }

  val curMonthProfit = currentMonthTrips.sumOf { it.profit }

  val bengaliMonths = listOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
  )
  val englishMonths = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  )
  val currentMonthName = if (language == AppLanguage.BANGLA)
    "${bengaliMonths[curMonth]} $curYear"
  else
    "${englishMonths[curMonth]} $curYear"

  // Editable fields state for dialog
  var editCarName by remember { mutableStateOf(profile.carName) }
  var editCarModel by remember { mutableStateOf(profile.carModel) }
  var editCarNumber by remember { mutableStateOf(profile.carNumber) }
  var editDriverName by remember { mutableStateOf(profile.driverNameBangla.ifEmpty { profile.driverName }) }
  var editPhone by remember { mutableStateOf(profile.driverPhone) }
  var editEmail by remember { mutableStateOf(profile.driverEmail) }

  val isDark = when (themeMode) {
    AppThemeMode.DARK -> true
    AppThemeMode.LIGHT -> false
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val backgroundBrush = if (isDark) {
    Brush.verticalGradient(
      colors = listOf(
        Color(0xFF07130E),
        Color(0xFF0C1D15),
        Color(0xFF091410)
      )
    )
  } else {
    Brush.verticalGradient(
      colors = listOf(
        Color(0xFFF1F8F5),
        Color(0xFFE8F4EE),
        Color(0xFFEDF6F1)
      )
    )
  }

  val accentColor = if (isDark) MintGreenAccent else Color(0xFF0F766E)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(backgroundBrush)
      .testTag("settings_screen_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
      Text(
        text = if (language == AppLanguage.BANGLA) "সেটিংস" else "Settings",
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = if (language == AppLanguage.BANGLA) "প্রোফাইল, থিম ও ব্যাকআপ ব্যবস্থাপনা" else "Profile, theme & backup management",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Card 1: DRIVER & VEHICLE PROFILE EDIT
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable {
            editCarName = profile.carName
            editCarModel = profile.carModel
            editCarNumber = profile.carNumber
            editDriverName = profile.driverNameBangla.ifEmpty { profile.driverName }
            editPhone = profile.driverPhone
            editEmail = profile.driverEmail
            showEditProfileDialog = true
          }
          .testTag("edit_car_info_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Driver Avatar with Camera icon overlay
          Box(
            modifier = Modifier
              .size(54.dp)
              .clickable {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }
              .testTag("settings_driver_avatar_container"),
            contentAlignment = Alignment.Center
          ) {
            if (!profile.profileImageUri.isNullOrBlank()) {
              AsyncImage(
                model = profile.profileImageUri,
                contentDescription = "Driver Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(50.dp)
                  .clip(CircleShape)
                  .border(2.dp, accentColor, CircleShape)
              )
            } else {
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(30.dp)
                )
              }
            }

            // Camera badge on bottom end
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(20.dp)
                .clip(CircleShape)
                .background(accentColor)
                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change Photo",
                tint = if (isDark) Color.Black else Color.White,
                modifier = Modifier.size(11.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            val driverDisplayName = profile.driverNameBangla.ifEmpty { profile.driverName }.trim()
            Text(
              text = driverDisplayName.ifEmpty { if (language == AppLanguage.BANGLA) "ড্রাইভার প্রোফাইল" else "Driver Profile" },
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            val vehicleInfo = listOfNotNull(
              profile.carNumber.trim().takeIf { it.isNotBlank() && it != "ঢাকা মেট্রো চ-১১-২২৩৩" },
              profile.carModel.trim().takeIf { it.isNotBlank() && it != "2020" }
            ).joinToString(" • ")
            if (vehicleInfo.isNotBlank()) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = vehicleInfo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            val contactInfo = listOfNotNull(
              profile.driverPhone.trim().takeIf { it.isNotBlank() && it != "01712345678" },
              profile.driverEmail.trim().takeIf { it.isNotBlank() && it != "driver@gmail.com" }
            ).joinToString(" • ")
            if (contactInfo.isNotBlank()) {
              Text(
                text = contactInfo,
                fontSize = 11.sp,
                color = accentColor.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.15f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit", tint = accentColor, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "এডিট" else "Edit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Card 2: THEME PREFERENCE
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onOpenThemeSettings() }
          .testTag("settings_theme_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              modifier = Modifier.size(44.dp),
              shape = CircleShape,
              color = accentColor.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = when (themeMode) {
                    AppThemeMode.DARK -> Icons.Default.DarkMode
                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                    AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                  },
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "থিম পরিবর্তন করুন" else "Change App Theme",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = when (themeMode) {
                  AppThemeMode.DARK -> if (language == AppLanguage.BANGLA) "ডার্ক মোড (Dark Green)" else "Dark Green Mode"
                  AppThemeMode.LIGHT -> if (language == AppLanguage.BANGLA) "লাইট মোড (Light)" else "Light Mode"
                  AppThemeMode.SYSTEM -> if (language == AppLanguage.BANGLA) "সিস্টেম ডিফল্ট (System)" else "System Default"
                },
                fontSize = 12.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open Theme Settings",
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Card 3: LANGUAGE SETTINGS
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onOpenLanguageSettings() }
          .testTag("settings_language_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              modifier = Modifier.size(44.dp),
              shape = CircleShape,
              color = accentColor.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = when (language) {
                    AppLanguage.BANGLA -> "বাং"
                    AppLanguage.ENGLISH -> "EN"
                    AppLanguage.HINDI -> "हि"
                    AppLanguage.TAMIL -> "த"
                    AppLanguage.URDU -> "ارد"
                  },
                  color = accentColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = when (language) {
                  AppLanguage.BANGLA -> "অ্যাপের ভাষা পরিবর্তন করুন"
                  AppLanguage.HINDI -> "ऐप की भाषा बदलें"
                  AppLanguage.TAMIL -> "பயன்பாட்டின் மொழியை மாற்றவும்"
                  AppLanguage.URDU -> "ایپ کی زبان تبدیل کریں"
                  else -> "Change App Language"
                },
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "${language.nativeName} (${language.englishName})",
                fontSize = 12.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open Language Settings",
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Card 4: ACCOUNTING EXPORT
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable {
            exportInitialScope = ExportScope.CURRENT_MONTH
            showExportDialog = true
          }
          .testTag("settings_export_current_month_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (language == AppLanguage.BANGLA) "চলতি মাসের ট্রিপ হিস্ট্রি ডাউনলোড" else "Current Month Trip History",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = accentColor.copy(alpha = 0.2f)
              ) {
                Text(
                  text = if (language == AppLanguage.BANGLA) "চলতি মাস" else "This Month",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = accentColor,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = if (language == AppLanguage.BANGLA)
                "$currentMonthName • ${currentMonthTrips.size}টি ট্রিপ • লাভ: ৳${String.format(java.util.Locale.US, "%,.0f", curMonthProfit)}"
              else
                "$currentMonthName • ${currentMonthTrips.size} trips • Profit: ৳${String.format(java.util.Locale.US, "%,.0f", curMonthProfit)}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Card 5: VEHICLE DOCUMENTS & SERVICE SHORTCUT
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onOpenDocuments() }
          .testTag("settings_documents_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (language == AppLanguage.BANGLA) "ট্যাক্স টোকেন, ফিটনেস ও সার্ভিস রেকর্ড" else "Tax Token, Fitness & Mobil Service",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (language == AppLanguage.BANGLA) "কাগজপত্রের মেয়াদ উত্তীর্ণ অ্যালার্ট ও মবিল চেঞ্জ ট্র্যাকার" else "Expiry reminder alerts & mobil change mileage logs",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Card 6: GOOGLE DRIVE CLOUD BACKUP
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onOpenDriveBackup() }
          .testTag("settings_google_drive_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              modifier = Modifier.size(44.dp),
              shape = CircleShape,
              color = accentColor.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "ড্রাইভ ব্যাকআপ ও রিস্টোর" else "Drive Backup & Restore",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = if (lastBackupTime > 0) {
                  val dateStr = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.US).format(java.util.Date(lastBackupTime))
                  if (language == AppLanguage.BANGLA) "সর্বশেষ ব্যাকআপ: $dateStr" else "Last backup: $dateStr"
                } else {
                  if (language == AppLanguage.BANGLA) "গুগল ড্রাইভে ১০০% নিরাপদ ক্লাউড সংরক্ষণ" else "100% Secure cloud data sync"
                },
                fontSize = 12.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open Drive Backup",
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Card: EMAIL OTP GATEWAY CONFIGURATION
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable {
            selectedOtpProvider = if (emailOtpProvider.isBlank() || emailOtpProvider.equals("RESEND", true)) "BREVO" else emailOtpProvider
            enteredOtpApiKey = emailOtpApiKey
            enteredOtpWebhookUrl = emailOtpWebhookUrl
            enteredOtpSenderEmail = emailOtpSenderEmail
            testOtpResult = null
            showEmailOtpSettingsDialog = true
          }
          .testTag("settings_email_otp_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              modifier = Modifier.size(44.dp),
              shape = CircleShape,
              color = accentColor.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.MarkEmailRead,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "ইমেইল ওটিপি গেটওয়ে সেটিংস" else "Email OTP Gateway Settings",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = if (emailOtpApiKey.isNotBlank()) {
                  if (language == AppLanguage.BANGLA) "Brevo API সক্রিয় (রিয়েল ওটিপি চালু - দিনে ৩০০ ইমেইল)" else "Active: Brevo API (Real OTP - 300/day)"
                } else {
                  if (language == AppLanguage.BANGLA) "Brevo API Key কনফিগার করুন (দিনে ৩০০ ফ্রি ইমেইল)" else "Configure Brevo API Key (300 free/day)"
                },
                fontSize = 12.sp,
                color = if (emailOtpApiKey.isNotBlank()) ProfitGreen else LossRed,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open Email OTP Settings",
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // LOGOUT CARD
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onLogout() }
          .testTag("settings_logout_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, LossRed.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Logout",
            tint = LossRed,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (language == AppLanguage.BANGLA) "একাউন্ট থেকে লগআউট করুন" else "Log Out of Account",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = LossRed
          )
        }
      }

      Spacer(modifier = Modifier.height(30.dp))

      // Footer - Information/Branding badge
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(if (isDark) Color(0xFF334155) else Color(0xFF94A3B8))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "CAR HISAB",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(if (isDark) Color(0xFF334155) else Color(0xFF94A3B8))
          )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
          text = if (language == AppLanguage.BANGLA) "সংস্করণ v${BuildConfig.VERSION_NAME} • নিরাপদ ও গুগল ড্রাইভে সংরক্ষিত" else "Version ${BuildConfig.VERSION_NAME} • Cloud synced with Google Drive",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier
            .clickable {
              versionTapCount++
              if (versionTapCount >= 7) {
                versionTapCount = 0
                showAdminGeneratorDialog = true
              }
            }
            .padding(8.dp)
            .testTag("version_text_admin_trigger")
        )
      }

      // Admin Key Generator Dialog
      if (showAdminGeneratorDialog) {
        var adminPasswordInput by remember { mutableStateOf("") }
        var isAdminAuthenticated by remember { mutableStateOf(false) }

        AlertDialog(
          onDismissRequest = { showAdminGeneratorDialog = false },
          containerColor = MaterialTheme.colorScheme.surface,
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isAdminAuthenticated) "অ্যাডমিন কি জেনারেটর" else "মাস্টার অ্যাডমিন লগইন",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            }
          },
          text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
              if (!isAdminAuthenticated) {
                Text(
                  text = "গোপন অ্যাডমিন প্যানেল ওপেন করতে মাস্টার পাসওয়ার্ড লিখুন (ডিফল্ট: 9999):",
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                  value = adminPasswordInput,
                  onValueChange = { adminPasswordInput = it },
                  label = { Text("মাস্টার পাসওয়ার্ড") },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                )
              } else {
                Text(
                  text = "ড্রাইভারের ইউনিক আইডি এবং মেয়াদ সিলেক্ট করে অ্যাক্টিভেশন কোড তৈরি করুন:",
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                  value = adminTargetIdInput,
                  onValueChange = { adminTargetIdInput = it },
                  label = { Text("ড্রাইভার আইডি (যেমন: CH-84920)") },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "মেয়াদের সময়কাল নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = { adminSelectedDurationDays = 30 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (adminSelectedDurationDays == 30) accentColor else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("১ মাস", fontSize = 11.sp, color = if (adminSelectedDurationDays == 30) Color.White else MaterialTheme.colorScheme.onSurface)
                  }
                  Button(
                    onClick = { adminSelectedDurationDays = 90 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (adminSelectedDurationDays == 90) accentColor else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("৩ মাস", fontSize = 11.sp, color = if (adminSelectedDurationDays == 90) Color.White else MaterialTheme.colorScheme.onSurface)
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = { adminSelectedDurationDays = 180 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (adminSelectedDurationDays == 180) accentColor else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("৬ মাস", fontSize = 11.sp, color = if (adminSelectedDurationDays == 180) Color.White else MaterialTheme.colorScheme.onSurface)
                  }
                  Button(
                    onClick = { adminSelectedDurationDays = 365 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (adminSelectedDurationDays == 365) accentColor else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("১ বছর", fontSize = 11.sp, color = if (adminSelectedDurationDays == 365) Color.White else MaterialTheme.colorScheme.onSurface)
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                  onClick = {
                    val days = adminSelectedDurationDays
                    generatedResultKey = when(days) {
                      30 -> "CH-30-M100"
                      90 -> "CH-90-VIP"
                      180 -> "CH-180-H500"
                      365 -> "CH-365-Y950"
                      else -> "CH-$days-VIP"
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("কোড জেনারেট করুন", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF022B1E) else Color.White)
                }

                if (generatedResultKey.isNotBlank()) {
                  Spacer(modifier = Modifier.height(12.dp))
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(text = "জেনারেটেড সিক্রেট কোড:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(text = generatedResultKey, fontSize = 18.sp, fontWeight = FontWeight.Black, color = accentColor)
                      Spacer(modifier = Modifier.height(6.dp))
                      TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Admin Key", generatedResultKey))
                        Toast.makeText(context, "কোড কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                      }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কোড কপি করুন", fontSize = 12.sp)
                      }
                    }
                  }
                }
              }
            }
          },
          confirmButton = {
            if (!isAdminAuthenticated) {
              Button(
                onClick = {
                  if (adminPasswordInput == "9999" || adminPasswordInput == "MMRK2026") {
                    isAdminAuthenticated = true
                  } else {
                    Toast.makeText(context, "ভুল পাসওয়ার্ড!", Toast.LENGTH_SHORT).show()
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("লগইন", color = if (isDark) Color(0xFF022B1E) else Color.White, fontWeight = FontWeight.Bold)
              }
            } else {
              TextButton(onClick = { showAdminGeneratorDialog = false }) {
                Text("বন্ধ করুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          },
          dismissButton = {
            TextButton(onClick = { showAdminGeneratorDialog = false }) {
              Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        )
      }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
      AlertDialog(
        onDismissRequest = { showEditProfileDialog = false },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "চালক ও গাড়ির প্রোফাইল সম্পাদন" else "Edit Profile",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clickable {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  }
                  .testTag("dialog_profile_avatar"),
                contentAlignment = Alignment.Center
              ) {
                if (!profile.profileImageUri.isNullOrBlank()) {
                  AsyncImage(
                    model = profile.profileImageUri,
                    contentDescription = "Driver Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .size(64.dp)
                      .clip(CircleShape)
                      .border(2.dp, accentColor, CircleShape)
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .size(64.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                      .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = null,
                      tint = accentColor,
                      modifier = Modifier.size(36.dp)
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = if (isDark) Color.Black else Color.White,
                    modifier = Modifier.size(12.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column {
                Button(
                  onClick = {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.18f)),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("btn_dialog_change_photo")
                ) {
                  Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA)
                      if (!profile.profileImageUri.isNullOrBlank()) "ছবি পরিবর্তন" else "ছবি যুক্ত করুন"
                    else
                      if (!profile.profileImageUri.isNullOrBlank()) "Change Photo" else "Add Photo",
                    color = accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                if (!profile.profileImageUri.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  TextButton(
                    onClick = { onRemoveProfilePhoto() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("btn_dialog_remove_photo")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = null,
                      tint = LossRed,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (language == AppLanguage.BANGLA) "ছবি মুছুন" else "Remove",
                      color = LossRed,
                      fontSize = 10.5.sp
                    )
                  }
                }
              }
            }

            OutlinedTextField(
              value = editDriverName,
              onValueChange = { editDriverName = it },
              label = { Text(if (language == AppLanguage.BANGLA) "চালকের নাম" else "Driver Name") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = editCarModel,
              onValueChange = { editCarModel = it },
              label = { Text(if (language == AppLanguage.BANGLA) "গাড়ির মডেল" else "Car Model") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = editCarNumber,
              onValueChange = { editCarNumber = it },
              label = { Text(if (language == AppLanguage.BANGLA) "গাড়ির নম্বর প্লেট" else "License Plate") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = editPhone,
              onValueChange = { editPhone = it },
              label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর" else "Phone Number") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = editEmail,
              onValueChange = { editEmail = it },
              label = { Text(if (language == AppLanguage.BANGLA) "জিমেইল ঠিকানা" else "Gmail Address") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              showEditProfileDialog = false
              onUpdateProfile(editCarName, editCarModel, editCarNumber, editDriverName, editPhone, editEmail)
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(if (language == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Save", color = if (isDark) Color(0xFF022B1E) else Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showEditProfileDialog = false }) {
            Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      )
    }

    // Export Dialog
    if (showExportDialog) {
      ExportDialog(
        language = language,
        allTrips = trips,
        filteredTrips = currentMonthTrips,
        initialScope = exportInitialScope,
        onDismiss = { showExportDialog = false },
        onPerformExport = { format, tripsToExport ->
          showExportDialog = false
          onExportTrips(format, tripsToExport)
        }
      )
    }

    // Restore Confirm Dialog
    if (showRestoreConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showRestoreConfirmDialog = false },
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
          Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(32.dp)
          )
        },
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "গুগল ড্রাইভ থেকে রিস্টোর করবেন?" else "Restore from Google Drive?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        text = {
          Text(
            text = if (language == AppLanguage.BANGLA)
              "আপনার গুগল ড্রাইভে সংরক্ষিত ট্রিপ ও হিসাবের ফাইল থেকে সমস্ত ডাটা অ্যাপে রিস্টোর করা হবে। আপনি কি এগিয়ে যেতে চান?"
            else
              "Your accounting records from Google Drive backup will be restored into the app. Do you want to proceed?",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        confirmButton = {
          Button(
            onClick = {
              showRestoreConfirmDialog = false
              onGoogleDriveRestore(null)
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              if (language == AppLanguage.BANGLA) "হ্যাঁ, রিস্টোর করুন" else "Yes, Restore",
              color = if (isDark) Color(0xFF022B1E) else Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { showRestoreConfirmDialog = false }) {
            Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      )
    }

    if (showEmailOtpSettingsDialog) {
      AlertDialog(
        onDismissRequest = { showEmailOtpSettingsDialog = false },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.testTag("email_otp_settings_dialog"),
        icon = {
          Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(32.dp)
          )
        },
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "ইমেইল ওটিপি গেটওয়ে কনফিগারেশন" else "Email OTP Gateway Setup",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = if (language == AppLanguage.BANGLA)
                "লগইন ও সাইন আপে চালকদের জিমেইল ইনবক্সে আসল ওটিপি পাঠাতে নিচের যেকোনো ফ্রি গেটওয়ে ব্যবহার করতে পারেন:"
              else
                "To deliver real OTPs to Gmail inboxes for login/signup, configure any free provider below:",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = if (language == AppLanguage.BANGLA) "ইমেইল সার্ভিস প্রোভাইডার:" else "Email Service Provider:",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              FilterChip(
                selected = selectedOtpProvider.equals("BREVO", ignoreCase = true),
                onClick = { selectedOtpProvider = "BREVO" },
                label = { Text("Brevo (Free 300/day)", fontSize = 11.sp) }
              )
              FilterChip(
                selected = selectedOtpProvider.equals("RESEND", ignoreCase = true),
                onClick = { selectedOtpProvider = "RESEND" },
                label = { Text("Resend (Free 3k/mo)", fontSize = 11.sp) }
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = if (selectedOtpProvider.equals("BREVO", true)) "Brevo API Key (xkeysib-...):" else "Resend API Key (re_...):",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = enteredOtpApiKey,
              onValueChange = { enteredOtpApiKey = it },
              placeholder = { Text(if (selectedOtpProvider.equals("BREVO", true)) "xkeysib-..." else "re_123456789...") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_api_key_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = if (language == AppLanguage.BANGLA) "প্রেরকের নাম ও ইমেইল (Sender Email):" else "Sender Name & Email:",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = enteredOtpSenderEmail,
              onValueChange = { enteredOtpSenderEmail = it },
              placeholder = { Text(if (selectedOtpProvider.equals("BREVO", true)) "your-brevo-email@gmail.com" else "Car Hisab <onboarding@resend.dev>") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (selectedOtpProvider.equals("BREVO", true)) {
                if (language == AppLanguage.BANGLA)
                  "💡 Brevo টিপস: Brevo একাউন্টে সাইন আপ করার সময় যে ইমেইল ব্যবহার করেছেন (অথবা Senders এ ভেরিফাই করা ইমেইল) সেটি এখানে দিন। প্রতিদিন ৩০০টি ইমেইল সম্পূর্ণ ফ্রি।"
                else
                  "💡 Brevo tip: Use the email registered in your Brevo account (or a verified sender). 300 emails/day are 100% free."
              } else {
                if (language == AppLanguage.BANGLA)
                  "💡 Resend টিপস: টেস্টিংয়ের জন্য onboarding@resend.dev ব্যবহার করতে পারেন।"
                else
                  "💡 Resend tip: You can use onboarding@resend.dev for testing."
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val testTarget = profile.driverEmail.ifBlank { "mdmahfuj0987@gmail.com" }
            OutlinedButton(
              onClick = {
                isSendingTestOtp = true
                testOtpResult = null
                coroutineScope.launch {
                  val result = EmailOtpManager.sendOtp(
                    recipientEmail = testTarget,
                    provider = selectedOtpProvider,
                    apiKey = enteredOtpApiKey,
                    webhookUrl = enteredOtpWebhookUrl,
                    senderEmail = enteredOtpSenderEmail
                  )
                  isSendingTestOtp = false
                  isTestSuccess = result.isRealEmailSent
                  testOtpResult = result.message + (if (result.errorDetail != null) "\n(${result.errorDetail})" else "")
                }
              },
              enabled = !isSendingTestOtp,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("send_test_otp_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) {
              if (isSendingTestOtp) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accentColor, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (language == AppLanguage.BANGLA) "ওটিপি পাঠানো হচ্ছে..." else "Sending OTP...", fontSize = 12.sp)
              } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (language == AppLanguage.BANGLA) "যাচাই ইমেইল ওটিপি পাঠান ($testTarget)" else "Send Verification OTP ($testTarget)",
                  fontSize = 12.sp
                )
              }
            }

            if (testOtpResult != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isTestSuccess) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isTestSuccess) ProfitGreen else LossRed.copy(alpha = 0.5f)
                )
              ) {
                Text(
                  text = testOtpResult ?: "",
                  fontSize = 11.5.sp,
                  color = if (isTestSuccess) ProfitGreen else LossRed,
                  modifier = Modifier.padding(10.dp)
                )
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              onSaveEmailOtpSettings(
                selectedOtpProvider,
                enteredOtpApiKey.trim(),
                enteredOtpWebhookUrl.trim(),
                enteredOtpSenderEmail.trim()
              )
              showEmailOtpSettingsDialog = false
              android.widget.Toast.makeText(
                context,
                if (language == AppLanguage.BANGLA) "ইমেইল ওটিপি গেটওয়ে সংরক্ষিত হয়েছে!" else "Email OTP Gateway settings saved!",
                android.widget.Toast.LENGTH_SHORT
              ).show()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier.testTag("save_email_otp_settings_button")
          ) {
            Text(
              text = if (language == AppLanguage.BANGLA) "সেটিংস সংরক্ষণ করুন" else "Save Settings",
              fontWeight = FontWeight.Bold,
              color = Color(0xFF022B1E)
            )
          }
        },
        dismissButton = {
          TextButton(
            onClick = { showEmailOtpSettingsDialog = false }
          ) {
            Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
          }
        }
      )
    }
  }
}
}

@Composable
fun LanguageToggleTab(
  text: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier
      .height(44.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) DarkGreenPrimary else DarkGreenCard,
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DarkGreenPrimary else DarkGreenBorder)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (isSelected) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color(0xFF022B1E),
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
      }
      Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color(0xFF022B1E) else Color(0xFF94A3B8),
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun ThemeToggleTab(
  text: String,
  icon: ImageVector,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier
      .height(48.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) DarkGreenPrimary else DarkGreenCard,
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DarkGreenPrimary else DarkGreenBorder)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Color(0xFF022B1E) else Color(0xFF94A3B8),
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color(0xFF022B1E) else Color(0xFF94A3B8)
      )
    }
  }
}
