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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.PrimaryEmeraldDark

@Composable
fun LanguageSelectScreen(
  currentLanguage: AppLanguage,
  onLanguageSelected: (AppLanguage) -> Unit,
  onContinue: () -> Unit,
  isFromSettings: Boolean = false,
  onBack: () -> Unit = {}
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp)
      .testTag("language_select_screen"),
    contentAlignment = Alignment.Center
  ) {
    if (isFromSettings) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .align(Alignment.TopStart)
          .testTag("language_screen_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onBackground
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(if (isFromSettings) 36.dp else 16.dp))

      // Header Icon
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Language,
          contentDescription = "Language",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(38.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = AppStrings.selectLanguage(currentLanguage),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
      )

      Text(
        text = AppStrings.languageSubtitle(currentLanguage),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
      )

      // 1. Bangla Option (Default)
      LanguageOptionCard(
        title = "বাংলা",
        subtitle = "ড্রাইভারদের জন্য সহজ ও সম্পূর্ণ হিসাব",
        tag = "ডিফল্ট / মূল ভাষা",
        isSelected = currentLanguage == AppLanguage.BANGLA,
        testTag = "lang_opt_bangla",
        onClick = { onLanguageSelected(AppLanguage.BANGLA) }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 2. English Option
      LanguageOptionCard(
        title = "English",
        subtitle = "Standard Car Income & Expense Tracking",
        tag = "Global",
        isSelected = currentLanguage == AppLanguage.ENGLISH,
        testTag = "lang_opt_english",
        onClick = { onLanguageSelected(AppLanguage.ENGLISH) }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Hindi Option
      LanguageOptionCard(
        title = "हिन्दी (Hindi)",
        subtitle = "ड्राइवरों के लिए आसान कार हिसाब",
        tag = "हिन्दी",
        isSelected = currentLanguage == AppLanguage.HINDI,
        testTag = "lang_opt_hindi",
        onClick = { onLanguageSelected(AppLanguage.HINDI) }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 4. Tamil Option
      LanguageOptionCard(
        title = "தமிழ் (Tamil)",
        subtitle = "ஓட்டுநர்களுக்கான எளிய கார் கணக்கு",
        tag = "தமிழ்",
        isSelected = currentLanguage == AppLanguage.TAMIL,
        testTag = "lang_opt_tamil",
        onClick = { onLanguageSelected(AppLanguage.TAMIL) }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 5. Urdu Option
      LanguageOptionCard(
        title = "اردو (Urdu)",
        subtitle = "ڈرائیوروں کے لیے کار کا آسان حساب",
        tag = "اردو",
        isSelected = currentLanguage == AppLanguage.URDU,
        testTag = "lang_opt_urdu",
        onClick = { onLanguageSelected(AppLanguage.URDU) }
      )

      Spacer(modifier = Modifier.height(28.dp))

      Button(
        onClick = onContinue,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("language_continue_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        if (isFromSettings) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
          text = if (isFromSettings) {
            when (currentLanguage) {
              AppLanguage.BANGLA -> "সংরক্ষণ করুন (Save)"
              AppLanguage.HINDI -> "सहेजें (Save)"
              AppLanguage.TAMIL -> "சேமி (Save)"
              AppLanguage.URDU -> "محفوظ کریں (Save)"
              else -> "Save Selection"
            }
          } else {
            AppStrings.continueBtn(currentLanguage)
          },
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun LanguageOptionCard(
  title: String,
  subtitle: String,
  tag: String,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
  val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .testTag(testTag),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    shape = RoundedCornerShape(18.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
              if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = when {
              title.contains("বাংলা") -> "বাং"
              title.contains("English") -> "EN"
              title.contains("हिन्दी") -> "हि"
              title.contains("தமிழ்") -> "த"
              title.contains("اردو") -> "ارد"
              else -> "LG"
            },
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0x11000000),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Icon(
        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = if (isSelected) "Selected" else "Unselected",
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}
