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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.ProfitGreen

@Composable
fun NidVerifyScreen(
  language: AppLanguage,
  initialFront: Boolean = true,
  initialBack: Boolean = true,
  initialSelfie: Boolean = true,
  onVerificationSubmitted: (front: Boolean, back: Boolean, selfie: Boolean) -> Unit,
  onSkip: () -> Unit
) {
  var frontAttached by remember { mutableStateOf(initialFront) }
  var backAttached by remember { mutableStateOf(initialBack) }
  var selfieAttached by remember { mutableStateOf(initialSelfie) }

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("nid_verify_screen_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header Icon
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Badge,
          contentDescription = "NID",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = AppStrings.nidTitle(language),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
      )

      Text(
        text = AppStrings.nidSubtitle(language),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
      )

      // Card 1: NID Front
      UploadItemCard(
        title = AppStrings.nidFront(language),
        subtitle = if (language == AppLanguage.BANGLA) "নাম, ছবি ও জন্মতারিখ পরিষ্কার দেখা যেতে হবে" else "Ensure name, photo and DOB are clearly visible",
        icon = Icons.Default.Badge,
        isAttached = frontAttached,
        testTag = "nid_front_upload_card",
        onToggle = { frontAttached = !frontAttached }
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Card 2: NID Back
      UploadItemCard(
        title = AppStrings.nidBack(language),
        subtitle = if (language == AppLanguage.BANGLA) "ঠিকানা ও বারকোড সম্বলিত অংশ" else "Address and barcode section",
        icon = Icons.Default.Badge,
        isAttached = backAttached,
        testTag = "nid_back_upload_card",
        onToggle = { backAttached = !backAttached }
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Card 3: Selfie
      UploadItemCard(
        title = AppStrings.driverSelfie(language),
        subtitle = if (language == AppLanguage.BANGLA) "ক্যাপ বা সানগ্লাস ছাড়া স্বাভাবিক আলোর সেলফি" else "Clear face photo without hat or sunglasses",
        icon = Icons.Default.Face,
        isAttached = selfieAttached,
        testTag = "driver_selfie_upload_card",
        onToggle = { selfieAttached = !selfieAttached }
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Submit Button
      Button(
        onClick = { onVerificationSubmitted(frontAttached, backAttached, selfieAttached) },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("nid_submit_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Text(
          text = AppStrings.submitNid(language),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Skip Button
      OutlinedButton(
        onClick = onSkip,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("nid_skip_button"),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = AppStrings.skipNid(language),
          fontSize = 14.sp
        )
      }
    }
  }
}

@Composable
private fun UploadItemCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  isAttached: Boolean,
  testTag: String,
  onToggle: () -> Unit
) {
  val borderColor = if (isAttached) ProfitGreen else MaterialTheme.colorScheme.outlineVariant
  val containerColor = if (isAttached) ProfitGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
      .clickable { onToggle() }
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(if (isAttached) ProfitGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isAttached) Icons.Default.CheckCircle else icon,
          contentDescription = null,
          tint = if (isAttached) ProfitGreen else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (isAttached) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = ProfitGreen.copy(alpha = 0.2f)
            ) {
              Text(
                text = "✓ সংযুক্ত",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ProfitGreen,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Icon(
        imageVector = if (isAttached) Icons.Default.CameraAlt else Icons.Default.AddPhotoAlternate,
        contentDescription = "Upload",
        tint = if (isAttached) ProfitGreen else MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}
