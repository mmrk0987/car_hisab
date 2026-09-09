package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserProfile
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.ProfitGreen
import java.text.SimpleDateFormat
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveBackupScreen(
  language: AppLanguage,
  profile: UserProfile,
  lastBackupTime: Long,
  lastBackupCount: Int,
  isBackingUp: Boolean,
  isRestoring: Boolean,
  backupStatusMessage: String?,
  isWeeklyReminderEnabled: Boolean = true,
  onToggleWeeklyReminder: (Boolean) -> Unit = {},
  onGoogleDriveBackup: () -> Unit,
  onGoogleDriveRestore: (Uri?) -> Unit,
  onBack: () -> Unit
) {
  var showRestoreConfirmDialog by remember { mutableStateOf(false) }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      onGoogleDriveRestore(uri)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "ড্রাইভ ব্যাকআপ ও রিস্টোর" else "Google Drive Backup",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("drive_backup_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
        .testTag("drive_backup_screen")
    ) {
      // Header Info Banner
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.35f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MintGreenAccent.copy(alpha = 0.15f))
                .border(1.dp, MintGreenAccent.copy(alpha = 0.3f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = MintGreenAccent,
                modifier = Modifier.size(28.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (language == AppLanguage.BANGLA) "গুগল ড্রাইভ ক্লাউড ব্যাকআপ" else "Google Drive Cloud Backup",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = if (language == AppLanguage.BANGLA) "১০০% নিরাপদ ও আজীবন ফ্রি ডাটা সংরক্ষণ" else "100% Secure & Free Cloud Storage",
                fontSize = 12.sp,
                color = MintGreenAccent,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Explanatory note
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MintGreenAccent.copy(alpha = 0.10f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.Top
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MintGreenAccent,
                modifier = Modifier
                  .size(18.dp)
                  .padding(top = 2.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = if (language == AppLanguage.BANGLA)
                  "আপনার ব্যক্তিগত জিমেইলের ১৫ জিবি ফ্রি গুগল ড্রাইভ ব্যবহার করা হয়। ফোন হারালেও বা নতুন ডিভাইসে গেলে কেবল 'রিস্টোর' দিলেই সব আয়-ব্যয় ও ট্রিপের ডাটা ফিরে পাবেন।"
                else
                  "Utilizes your personal Gmail's free 15 GB Google Drive storage. Even if you switch or lose your phone, simply tap 'Restore' to recover all your financial trip records.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0xFFCBD5E1)
              )
            }
          }
        }
      }

      // Account & Status Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = if (language == AppLanguage.BANGLA) "সংযুক্ত অ্যাকাউন্ট ও স্ট্যাটাস" else "Connected Account & Status",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Connected Gmail row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkGreenCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = null,
                tint = MintGreenAccent,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (language == AppLanguage.BANGLA) "গুগল অ্যাকাউন্ট" else "Google Account",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = profile.driverEmail.ifEmpty { "driver@gmail.com" },
                fontSize = 12.sp,
                color = MintGreenAccent,
                fontWeight = FontWeight.SemiBold
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ProfitGreen.copy(alpha = 0.15f)
            ) {
              Text(
                text = "✓ সক্রিয়",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ProfitGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Last backup details
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkGreenCard,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (language == AppLanguage.BANGLA) "সর্বশেষ ক্লাউড ব্যাকআপ:" else "Last Cloud Backup:",
                  fontSize = 12.sp,
                  color = Color(0xFF94A3B8)
                )
                Text(
                  text = if (lastBackupTime > 0) {
                    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(lastBackupTime))
                    "$dateStr ($lastBackupCount টি ট্রিপ)"
                  } else {
                    if (language == AppLanguage.BANGLA) "এখনো ব্যাকআপ নেওয়া হয়নি" else "No backup taken yet"
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
              }

              if (backupStatusMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "স্ট্যাটাস: $backupStatusMessage",
                  fontSize = 11.sp,
                  color = MintGreenAccent,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }

      // Weekly Automatic Reminder Card (বৃহস্পতিবার সন্ধ্যা ৬টা)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MintGreenAccent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MintGreenAccent,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (language == AppLanguage.BANGLA) "সাপ্তাহিক অটো রিমাইন্ডার" else "Weekly Auto Reminder",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = if (language == AppLanguage.BANGLA) "প্রতি বৃহস্পতিবার সন্ধ্যা ৬:০০ টা" else "Every Thursday at 6:00 PM",
                fontSize = 12.sp,
                color = MintGreenAccent,
                fontWeight = FontWeight.SemiBold
              )
            }

            Switch(
              checked = isWeeklyReminderEnabled,
              onCheckedChange = { onToggleWeeklyReminder(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DarkGreenPrimary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = DarkGreenCard
              ),
              modifier = Modifier.testTag("switch_weekly_backup_reminder")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkGreenCard,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = if (isWeeklyReminderEnabled) ProfitGreen else Color.Gray,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isWeeklyReminderEnabled) {
                  if (language == AppLanguage.BANGLA)
                    "✓ প্রতি বৃহস্পতিবার ঠিক সন্ধ্যা ৬:০০ টায় ১-ক্লিক ড্রাইভ ব্যাকআপের নোটিফিকেশন পাবেন।"
                  else
                    "✓ You'll get a 1-tap backup notification every Thursday at 6:00 PM."
                } else {
                  if (language == AppLanguage.BANGLA)
                    "সাপ্তাহিক নোটিফিকেশন বন্ধ রয়েছে।"
                  else
                    "Weekly reminder notifications are turned off."
                },
                fontSize = 11.5.sp,
                color = if (isWeeklyReminderEnabled) Color(0xFFCBD5E1) else Color(0xFF94A3B8),
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      // Action Buttons Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = if (language == AppLanguage.BANGLA) "ব্যাকআপ ও রিস্টোর একশন" else "Backup & Restore Actions",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Backup Now
          Button(
            onClick = { onGoogleDriveBackup() },
            enabled = !isBackingUp && !isRestoring,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("btn_drive_backup"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
          ) {
            if (isBackingUp) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color(0xFF022B1E),
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "গুগল ড্রাইভে আপলোড হচ্ছে..." else "Backing up to Drive...",
                fontSize = 13.sp,
                color = Color(0xFF022B1E),
                fontWeight = FontWeight.Bold
              )
            } else {
              Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = Color(0xFF022B1E),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "গুগল ড্রাইভে এখনই ব্যাকআপ নিন" else "Backup Now to Google Drive",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF022B1E)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Restore
          OutlinedButton(
            onClick = { showRestoreConfirmDialog = true },
            enabled = !isBackingUp && !isRestoring,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("btn_drive_restore"),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.6f))
          ) {
            if (isRestoring) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MintGreenAccent,
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "ড্রাইভ থেকে ডাটা আনা হচ্ছে..." else "Restoring from Drive...",
                fontSize = 13.sp,
                color = MintGreenAccent
              )
            } else {
              Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = MintGreenAccent,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "ড্রাইভ থেকে ডাটা রিস্টোর করুন" else "Restore Data from Drive",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MintGreenAccent
              )
            }
          }
        }
      }

      // Security Guarantee
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkGreenSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MintGreenAccent,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = if (language == AppLanguage.BANGLA)
              "আপনার ব্যাকআপ ফাইলটি শুধুমাত্র আপনার জিমেইলের প্রাইভেট অ্যাপ-ফোল্ডারে এনক্রিপ্টেড থাকে। অন্য কেউ এই ডাটা দেখতে পারবে না।"
            else
              "Your backup is stored encrypted in your personal Gmail's private app folder. No third-party or unauthorized person can access it.",
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
            color = Color(0xFF94A3B8)
          )
        }
      }
    }

    // Restore Confirm Dialog
    if (showRestoreConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showRestoreConfirmDialog = false },
        containerColor = DarkGreenSurface,
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "ড্রাইভ থেকে রিস্টোর করতে চান?" else "Restore from Google Drive?",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Text(
            text = if (language == AppLanguage.BANGLA)
              "গুগল ড্রাইভে সংরক্ষিত ট্রিপ ও হিসাবের ডাটা বর্তমান অ্যাপে ইমপোর্ট হবে। আপনি কি নিশ্চিত?"
            else
              "Trip and financial data saved on Google Drive will be restored to this app. Are you sure you want to proceed?",
            color = Color(0xFFCBD5E1),
            fontSize = 13.sp
          )
        },
        confirmButton = {
          Button(
            onClick = {
              showRestoreConfirmDialog = false
              filePickerLauncher.launch("*/*")
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
          ) {
            Text(
              text = if (language == AppLanguage.BANGLA) "হ্যাঁ, রিস্টোর করুন" else "Yes, Restore",
              color = Color(0xFF022B1E),
              fontWeight = FontWeight.Bold
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { showRestoreConfirmDialog = false }) {
            Text(
              text = if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel",
              color = Color(0xFF94A3B8)
            )
          }
        }
      )
    }
  }
}
