package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.BankAsiaBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.bKashPink

@Composable
fun SubscriptionScreen(
  language: AppLanguage,
  isFreeTrialActive: Boolean,
  monthsLeft: Int,
  userUniqueId: String = "CH-84920",
  currentPlanName: String = "৬ মাসের ফ্রি ট্রায়াল",
  expiryDateMillis: Long = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000),
  trxId: String,
  screenshotAttached: Boolean,
  onTrxIdChange: (String) -> Unit,
  onScreenshotToggle: () -> Unit,
  onSubmitVerification: () -> Unit,
  onActivateWithKey: (String, (Boolean, String) -> Unit) -> Unit,
  onToggleStatusDemo: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  var showSuccessBanner by remember { mutableStateOf(false) }
  var adminKeyInput by remember { mutableStateOf("") }
  var adminKeyFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

  fun copyToClipboard(label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, AppStrings.copiedToast(language), Toast.LENGTH_SHORT).show()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("subscription_screen_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = AppStrings.subscriptionTitle(language),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "MMRK Car Hisab Premium",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Demo state toggle button for reviewer/user
        OutlinedButton(
          onClick = onToggleStatusDemo,
          modifier = Modifier.testTag("toggle_trial_demo_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (language == AppLanguage.BANGLA) "স্ট্যাটাস টেস্ট" else "Test Status",
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Status Card (Free Trial vs Expired)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .border(
            2.dp,
            if (isFreeTrialActive) PrimaryEmerald else LossRed,
            RoundedCornerShape(20.dp)
          )
          .testTag("subscription_status_card"),
        colors = CardDefaults.cardColors(
          containerColor = if (isFreeTrialActive) PrimaryEmerald.copy(alpha = 0.08f) else LossRed.copy(alpha = 0.08f)
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(if (isFreeTrialActive) PrimaryEmerald.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isFreeTrialActive) Icons.Default.Star else Icons.Default.Warning,
                  contentDescription = null,
                  tint = if (isFreeTrialActive) PrimaryEmerald else LossRed,
                  modifier = Modifier.size(26.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = AppStrings.subStatus(language),
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                  text = if (isFreeTrialActive) AppStrings.freeTrialStatus(language) else AppStrings.expiredStatus(language),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isFreeTrialActive) PrimaryEmerald else LossRed
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = "ID: $userUniqueId",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          val expiryDateStr = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.US).format(java.util.Date(expiryDateMillis))

          Text(
            text = if (isFreeTrialActive) {
              if (language == AppLanguage.BANGLA)
                "আপনার একাউন্টে $currentPlanName সক্রিয় রয়েছে। ট্রায়াল বা প্ল্যানের মেয়াদ: $expiryDateStr পর্যন্ত। মেয়াদ শেষে হিসাব সচল রাখতে নিচের যেকোনো প্যাকেজ বেছে নিন।"
              else
                "Your plan ($currentPlanName) is active until $expiryDateStr. Once expired, keep tracking your fleet with the packages below."
            } else {
              if (language == AppLanguage.BANGLA)
                "আপনার সাবস্ক্রিপশন মেয়াদ শেষ হয়েছে! নতুন হিসাব যুক্ত করতে বা অডিট দেখতে নিচের প্যাকেজের ফি পাঠিয়ে এডমিনের থেকে অ্যাক্টিভেশন কোড সংগ্রহ করুন।"
              else
                "Your subscription has expired! Send payment to renew and enter the activation key from Admin."
            },
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Subscription Packages Section (1 Month, 6 Months, 1 Year)
      Text(
        text = AppStrings.packagesSectionTitle(language),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(12.dp))

      // Package 1: Monthly (৳100)
      PackageCard(
        title = AppStrings.packageMonthlyTitle(language),
        price = AppStrings.packageMonthlyPrice(language),
        badge = if (language == AppLanguage.BANGLA) "স্বল্পমেয়াদী" else "Starter",
        badgeColor = Color(0xFF2563EB),
        description = AppStrings.packageMonthlyDesc(language),
        testTag = "pkg_monthly"
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Package 2: 6 Months (৳500)
      PackageCard(
        title = AppStrings.packageHalfYearlyTitle(language),
        price = AppStrings.packageHalfYearlyPrice(language),
        badge = if (language == AppLanguage.BANGLA) "★ জনপ্রিয়" else "★ Popular",
        badgeColor = PrimaryEmerald,
        description = AppStrings.packageHalfYearlyDesc(language),
        isFeatured = true,
        testTag = "pkg_half_yearly"
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Package 3: 1 Year (৳950)
      PackageCard(
        title = AppStrings.packageYearlyTitle(language),
        price = AppStrings.packageYearlyPrice(language),
        badge = if (language == AppLanguage.BANGLA) "২৫% মেগা ছাড়" else "Mega 25% Off",
        badgeColor = Color(0xFF8B5CF6),
        description = AppStrings.packageYearlyDesc(language),
        testTag = "pkg_yearly"
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Admin Activation Key Unlock Card (DIRECT CONTROL)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("admin_activation_key_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryEmerald.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PrimaryEmerald.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = AppStrings.adminActivationTitle(language),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "ইউজার আইডি: $userUniqueId",
                fontSize = 11.sp,
                color = PrimaryEmerald,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = AppStrings.adminActivationSubtitle(language),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = adminKeyInput,
            onValueChange = { adminKeyInput = it },
            label = { Text(AppStrings.adminKeyLabel(language)) },
            placeholder = { Text("CH-180-H500 বা MMRK-YEAR-950") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryEmerald) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_admin_activation_key")
          )

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              if (adminKeyInput.isNotBlank()) {
                onActivateWithKey(adminKeyInput) { success, msg ->
                  adminKeyFeedback = Pair(success, msg)
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_verify_admin_key"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = AppStrings.activateKeyBtn(language),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          adminKeyFeedback?.let { (isSuccess, text) ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSuccess) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = text,
                color = if (isSuccess) ProfitGreen else LossRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Payment Methods Section
      Text(
        text = AppStrings.paymentMethodsTitle(language),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = AppStrings.paymentNotice(language),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
      )

      // Payment Method 1: bKash Personal
      PaymentMethodCard(
        title = "bKash Personal",
        number = "01606665209",
        badgeColor = bKashPink,
        badgeText = "bKash",
        icon = Icons.Default.Payments,
        onCopy = { copyToClipboard("bKash", "01606665209") },
        testTag = "payment_method_bkash"
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Payment Method 2: Nagad Personal
      PaymentMethodCard(
        title = "Nagad Personal",
        number = "01606665209",
        badgeColor = NagadOrange,
        badgeText = "নগদ",
        icon = Icons.Default.Payments,
        onCopy = { copyToClipboard("Nagad", "01606665209") },
        testTag = "payment_method_nagad"
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Payment Method 3: Bank Asia
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("payment_method_bank_asia"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(BankAsiaBlue),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalance,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Bank Asia Ltd",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            IconButton(
              onClick = { copyToClipboard("Bank Asia", "10234567890") },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = AppStrings.bankAsiaDetails(language),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Verification Input Form
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("verification_submission_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = if (language == AppLanguage.BANGLA) "পেমেন্ট তথ্য যাচাই" else "Verify Payment Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(14.dp))

          // TrxID Input
          OutlinedTextField(
            value = trxId,
            onValueChange = onTrxIdChange,
            label = { Text(AppStrings.trxIdLabel(language)) },
            placeholder = { Text(AppStrings.trxIdPlaceholder(language)) },
            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_trx_id")
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Upload Payment Screenshot Card
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(
                1.5.dp,
                if (screenshotAttached) ProfitGreen else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(14.dp)
              )
              .clickable { onScreenshotToggle() }
              .testTag("upload_screenshot_card"),
            colors = CardDefaults.cardColors(
              containerColor = if (screenshotAttached) ProfitGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(if (screenshotAttached) ProfitGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (screenshotAttached) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  tint = if (screenshotAttached) ProfitGreen else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (screenshotAttached) AppStrings.screenshotSelected(language) else AppStrings.uploadScreenshot(language),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (language == AppLanguage.BANGLA) "ট্যাপ করে স্ক্রিনশট সংযুক্ত করুন (UI প্রোটোটাইপ)"
                  else "Tap to attach screenshot (UI Prototype)",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Submit button
          Button(
            onClick = {
              onSubmitVerification()
              showSuccessBanner = true
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("submit_verification_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = AppStrings.submitVerificationBtn(language),
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          AnimatedVisibility(visible = showSuccessBanner) {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
              color = ProfitGreen.copy(alpha = 0.15f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = AppStrings.verificationSuccess(language),
                  color = ProfitGreen,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
private fun PaymentMethodCard(
  title: String,
  number: String,
  badgeColor: Color,
  badgeText: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onCopy: () -> Unit,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(badgeColor),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = badgeText,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = number,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onCopy() }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "কপি",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun PackageCard(
  title: String,
  price: String,
  badge: String,
  badgeColor: Color,
  description: String,
  isFeatured: Boolean = false,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isFeatured) PrimaryEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    border = if (isFeatured) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryEmerald) else null,
    elevation = CardDefaults.cardElevation(defaultElevation = if (isFeatured) 2.dp else 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = badgeColor.copy(alpha = 0.15f)
          ) {
            Text(
              text = badge,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = badgeColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = description,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = price,
        fontSize = 19.sp,
        fontWeight = FontWeight.Black,
        color = if (isFeatured) PrimaryEmerald else MaterialTheme.colorScheme.primary
      )
    }
  }
}
