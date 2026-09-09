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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.MintGreenAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
  language: AppLanguage,
  themeMode: AppThemeMode,
  onThemeModeChange: (AppThemeMode) -> Unit,
  onBack: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "অ্যাপ থিম (Theme)" else "App Theme",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("theme_settings_back_button")
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
        .testTag("theme_settings_screen")
    ) {
      // Header Description
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.3f))
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(MintGreenAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SettingsBrightness,
              contentDescription = null,
              tint = MintGreenAccent,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = if (language == AppLanguage.BANGLA) "ডিসপ্লে ও রঙ পছন্দ" else "Display & Color Appearance",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (language == AppLanguage.BANGLA)
                "চোখের আরাম ও ব্যাটারি সাশ্রয়ের জন্য আপনার সুবিধাজনক থিম নির্বাচন করুন।"
              else
                "Choose your preferred theme for visual comfort and battery optimization.",
              fontSize = 11.5.sp,
              lineHeight = 16.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }
      }

      // Theme Option 1: Dark (Emerald Green)
      ThemeOptionCard(
        title = if (language == AppLanguage.BANGLA) "ডার্ক মোড (Dark Green)" else "Dark Green Mode",
        subtitle = if (language == AppLanguage.BANGLA) "চোখের সুরক্ষা ও ব্যাটারি সেভিং (প্রস্তাবিত)" else "Recommended for eye protection and battery saving",
        icon = Icons.Default.DarkMode,
        tag = if (language == AppLanguage.BANGLA) "ডিফল্ট / প্রস্তাবিত" else "Default / Recommended",
        isSelected = themeMode == AppThemeMode.DARK || themeMode == AppThemeMode.LIGHT,
        testTag = "theme_opt_dark",
        onClick = { onThemeModeChange(AppThemeMode.DARK) }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Theme Option 2: System Default
      ThemeOptionCard(
        title = if (language == AppLanguage.BANGLA) "সিস্টেম ডিফল্ট (System)" else "System Default",
        subtitle = if (language == AppLanguage.BANGLA) "আপনার মোবাইলের সিস্টেম সেটিংসের সাথে স্বয়ংক্রিয় মিলবে" else "Automatically matches your Android system settings",
        icon = Icons.Default.SettingsBrightness,
        tag = if (language == AppLanguage.BANGLA) "স্বয়ংক্রিয়" else "Auto",
        isSelected = themeMode == AppThemeMode.SYSTEM,
        testTag = "theme_opt_system",
        onClick = { onThemeModeChange(AppThemeMode.SYSTEM) }
      )
    }
  }
}

@Composable
private fun ThemeOptionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  tag: String,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) DarkGreenCard else DarkGreenSurface
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) MintGreenAccent else DarkGreenBorder
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(
            if (isSelected) MintGreenAccent.copy(alpha = 0.2f) else DarkGreenCard
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isSelected) MintGreenAccent else Color(0xFF94A3B8),
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (isSelected) MintGreenAccent.copy(alpha = 0.2f) else Color(0xFF1E293B)
          ) {
            Text(
              text = tag,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) MintGreenAccent else Color(0xFF94A3B8),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = subtitle,
          fontSize = 11.5.sp,
          color = Color(0xFF94A3B8),
          lineHeight = 16.sp
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Icon(
        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = if (isSelected) "Selected" else "Not Selected",
        tint = if (isSelected) MintGreenAccent else Color(0xFF475569),
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
