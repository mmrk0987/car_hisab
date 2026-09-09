package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MinorCrash
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MobilServiceInfo
import com.example.data.model.VehicleDocuments
import com.example.data.repository.UserProfile
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedContainerLight
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun VehicleDocumentsScreen(
  language: AppLanguage,
  profile: UserProfile,
  documents: VehicleDocuments,
  mobilService: MobilServiceInfo,
  onUpdateDocuments: (VehicleDocuments) -> Unit,
  onUpdateMobilService: (MobilServiceInfo) -> Unit,
  onLogMobilChange: (newKm: Double, brand: String, cost: Double) -> Unit,
  onUpdateOdometer: (Double) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val context = LocalContext.current

  var showDocEditDialog by remember { mutableStateOf<String?>(null) }
  var showMobilChangeDialog by remember { mutableStateOf(false) }
  var showOdometerDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("vehicle_docs_screen_root")
  ) {
    // Top Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text(
            text = AppStrings.vehicleDocsTab(language),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        },
        modifier = Modifier.testTag("tab_vehicle_docs")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = AppStrings.mobilServiceTab(language),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        },
        modifier = Modifier.testTag("tab_mobil_service")
      )
    }

    if (selectedTab == 0) {
      // Documents List Tab
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Vehicle Header Info Card
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.DirectionsCar,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(26.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "${profile.carName} (${profile.carModel})",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = profile.carNumber,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
              }
            }
          }
        }

        // Expiry Summary Notice
        item {
          val expiringCount = countExpiringSoon(documents)
          val expiredCount = countExpired(documents)
          if (expiredCount > 0 || expiringCount > 0) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (expiredCount > 0) LossRed.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.15f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (expiredCount > 0) Icons.Default.ErrorOutline else Icons.Default.Warning,
                  contentDescription = null,
                  tint = if (expiredCount > 0) LossRed else WarningAmber,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = if (language == AppLanguage.BANGLA) {
                    if (expiredCount > 0) "$expiredCount টি কাগজের মেয়াদ ইতিমধ্যে শেষ হয়েছে! দ্রুত নবায়ন করুন।"
                    else "$expiringCount টি কাগজের মেয়াদ ৩০ দিনের মধ্যে শেষ হবে।"
                  } else {
                    if (expiredCount > 0) "$expiredCount document(s) have expired! Renew urgently."
                    else "$expiringCount document(s) expiring within 30 days."
                  },
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (expiredCount > 0) LossRed else WarningAmber
                )
              }
            }
          }
        }

        // 1. Tax Token
        item {
          DocumentCard(
            title = AppStrings.taxToken(language),
            docNumber = documents.taxTokenNumber,
            expiryMillis = documents.taxTokenExpiryMillis,
            icon = Icons.Default.Security,
            language = language,
            onEditClick = { showDocEditDialog = "TAX_TOKEN" }
          )
        }

        // 2. Fitness Certificate
        item {
          DocumentCard(
            title = AppStrings.fitnessCertificate(language),
            docNumber = documents.fitnessNumber,
            expiryMillis = documents.fitnessExpiryMillis,
            icon = Icons.Default.Build,
            language = language,
            onEditClick = { showDocEditDialog = "FITNESS" }
          )
        }

        // 3. Route Permit
        item {
          DocumentCard(
            title = AppStrings.routePermit(language),
            docNumber = documents.routePermitNumber,
            expiryMillis = documents.routePermitExpiryMillis,
            icon = Icons.Default.DirectionsCar,
            language = language,
            onEditClick = { showDocEditDialog = "ROUTE_PERMIT" }
          )
        }

        // 4. Insurance
        item {
          DocumentCard(
            title = AppStrings.insurancePolicy(language),
            docNumber = documents.insuranceNumber,
            expiryMillis = documents.insuranceExpiryMillis,
            icon = Icons.Default.MinorCrash,
            language = language,
            onEditClick = { showDocEditDialog = "INSURANCE" }
          )
        }

        // 5. Driving License
        item {
          DocumentCard(
            title = AppStrings.drivingLicense(language),
            docNumber = documents.drivingLicenseNumber,
            expiryMillis = documents.drivingLicenseExpiryMillis,
            icon = Icons.Default.CheckCircle,
            language = language,
            onEditClick = { showDocEditDialog = "LICENSE" }
          )
        }
      }
    } else {
      // Mobil & Maintenance Tab
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Current Odometer & Quick Update
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryEmerald.copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = PrimaryEmerald,
                    modifier = Modifier.size(26.dp)
                  )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "বর্তমান ওডোমিটার" else "Current Odometer",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                  )
                  Text(
                    text = String.format(Locale.US, "%,.0f KM", mobilService.currentOdometerKm),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }

              Button(
                onClick = { showOdometerDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
              ) {
                Text(
                  text = if (language == AppLanguage.BANGLA) "আপডেট" else "Update",
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }

        // Mobil Health Card
        item {
          val kmDrivenSince = maxOf(0.0, mobilService.currentOdometerKm - mobilService.lastMobilChangeKm)
          val kmRemaining = maxOf(0.0, mobilService.mobilChangeIntervalKm - kmDrivenSince)
          val progress = (kmDrivenSince / mobilService.mobilChangeIntervalKm).toFloat().coerceIn(0f, 1f)
          val isDue = kmRemaining <= 300.0

          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(CircleShape)
                      .background(if (isDue) LossRed.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.OilBarrel,
                      contentDescription = null,
                      tint = if (isDue) LossRed else WarningAmber,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = AppStrings.mobilStatus(language),
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = mobilService.mobilBrandGrade,
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isDue) LossRed.copy(alpha = 0.12f) else ProfitGreen.copy(alpha = 0.12f)
                ) {
                  Text(
                    text = if (isDue) {
                      if (language == AppLanguage.BANGLA) "পরিবর্তন জরুরি!" else "Change Due!"
                    } else {
                      if (language == AppLanguage.BANGLA) "ভালো অবস্থা" else "Good"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDue) LossRed else ProfitGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Progress Bar
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(10.dp)
                  .clip(RoundedCornerShape(5.dp)),
                color = if (isDue) LossRed else if (progress > 0.75f) WarningAmber else PrimaryEmerald,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (language == AppLanguage.BANGLA) {
                    "ব্যবহৃত: ${kmDrivenSince.toInt()} কিমি"
                  } else {
                    "Used: ${kmDrivenSince.toInt()} km"
                  },
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                  text = if (language == AppLanguage.BANGLA) {
                    "বাকি আছে: ${kmRemaining.toInt()} কিমি"
                  } else {
                    "Remaining: ${kmRemaining.toInt()} km"
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isDue) LossRed else MaterialTheme.colorScheme.primary
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Log Mobil Change Button
              Button(
                onClick = { showMobilChangeDialog = true },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("btn_log_mobil_change"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(Icons.Default.OilBarrel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = AppStrings.logMobilChange(language),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // Service Records & Parts Checklist
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = if (language == AppLanguage.BANGLA) "অন্যান্য পার্টস ও সার্ভিসিং ট্র্যাকার" else "Other Parts & Periodic Maintenance",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              Spacer(modifier = Modifier.height(12.dp))

              ServiceItemRow(
                title = if (language == AppLanguage.BANGLA) "ব্রেক প্যাড চেক" else "Brake Pad Check",
                lastKm = mobilService.lastBrakeCheckKm,
                currentKm = mobilService.currentOdometerKm,
                recommendedIntervalKm = 10000.0,
                language = language
              )

              Spacer(modifier = Modifier.height(8.dp))

              ServiceItemRow(
                title = if (language == AppLanguage.BANGLA) "এয়ার ও এসি ফিল্টার" else "Air & AC Filter",
                lastKm = mobilService.lastAirFilterKm,
                currentKm = mobilService.currentOdometerKm,
                recommendedIntervalKm = 5000.0,
                language = language
              )

              Spacer(modifier = Modifier.height(8.dp))

              ServiceItemRow(
                title = if (language == AppLanguage.BANGLA) "গিয়ার অয়েল পরিবর্তন" else "Gear Oil Change",
                lastKm = mobilService.lastGearOilKm,
                currentKm = mobilService.currentOdometerKm,
                recommendedIntervalKm = 20000.0,
                language = language
              )
            }
          }
        }
      }
    }
  }

  // Edit Document Expiry Dialog
  showDocEditDialog?.let { docKey ->
    val (curName, curNumber, curExpiry) = when (docKey) {
      "TAX_TOKEN" -> Triple(AppStrings.taxToken(language), documents.taxTokenNumber, documents.taxTokenExpiryMillis)
      "FITNESS" -> Triple(AppStrings.fitnessCertificate(language), documents.fitnessNumber, documents.fitnessExpiryMillis)
      "ROUTE_PERMIT" -> Triple(AppStrings.routePermit(language), documents.routePermitNumber, documents.routePermitExpiryMillis)
      "INSURANCE" -> Triple(AppStrings.insurancePolicy(language), documents.insuranceNumber, documents.insuranceExpiryMillis)
      "LICENSE" -> Triple(AppStrings.drivingLicense(language), documents.drivingLicenseNumber, documents.drivingLicenseExpiryMillis)
      else -> Triple("", "", System.currentTimeMillis())
    }

    var editNum by remember { mutableStateOf(curNumber) }
    var editExpiryMillis by remember { mutableStateOf(curExpiry) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val dateDisplay = dateFormat.format(Date(editExpiryMillis))

    AlertDialog(
      onDismissRequest = { showDocEditDialog = null },
      title = {
        Text(
          text = if (language == AppLanguage.BANGLA) "$curName এর মেয়াদ আপডেট" else "Update $curName",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = editNum,
            onValueChange = { editNum = it },
            label = { Text(if (language == AppLanguage.BANGLA) "কাগজের নম্বর / সার্টিফিকেট নং" else "Doc / Certificate No") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = dateDisplay,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (language == AppLanguage.BANGLA) "মেয়াদ উত্তীর্ণের তারিখ" else "Expiry Date") },
            trailingIcon = {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.clickable {
                  val cal = Calendar.getInstance().apply { timeInMillis = editExpiryMillis }
                  DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      val selectedCal = Calendar.getInstance().apply {
                        set(y, m, d, 23, 59, 59)
                      }
                      editExpiryMillis = selectedCal.timeInMillis
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                  ).show()
                }
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = editExpiryMillis }
                DatePickerDialog(
                  context,
                  { _, y, m, d ->
                    val selectedCal = Calendar.getInstance().apply {
                      set(y, m, d, 23, 59, 59)
                    }
                    editExpiryMillis = selectedCal.timeInMillis
                  },
                  cal.get(Calendar.YEAR),
                  cal.get(Calendar.MONTH),
                  cal.get(Calendar.DAY_OF_MONTH)
                ).show()
              }
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val updated = when (docKey) {
              "TAX_TOKEN" -> documents.copy(taxTokenNumber = editNum, taxTokenExpiryMillis = editExpiryMillis)
              "FITNESS" -> documents.copy(fitnessNumber = editNum, fitnessExpiryMillis = editExpiryMillis)
              "ROUTE_PERMIT" -> documents.copy(routePermitNumber = editNum, routePermitExpiryMillis = editExpiryMillis)
              "INSURANCE" -> documents.copy(insuranceNumber = editNum, insuranceExpiryMillis = editExpiryMillis)
              "LICENSE" -> documents.copy(drivingLicenseNumber = editNum, drivingLicenseExpiryMillis = editExpiryMillis)
              else -> documents
            }
            onUpdateDocuments(updated)
            showDocEditDialog = null
          }
        ) {
          Text(if (language == AppLanguage.BANGLA) "সংরক্ষণ" else "Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDocEditDialog = null }) {
          Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
        }
      }
    )
  }

  // Log Mobil Change Dialog
  if (showMobilChangeDialog) {
    var newKmInput by remember { mutableStateOf(mobilService.currentOdometerKm.toInt().toString()) }
    var brandInput by remember { mutableStateOf(mobilService.mobilBrandGrade) }
    var costInput by remember { mutableStateOf("2500") }

    AlertDialog(
      onDismissRequest = { showMobilChangeDialog = false },
      title = {
        Text(
          text = if (language == AppLanguage.BANGLA) "মবিল পরিবর্তন এন্ট্রি" else "Log Mobil Change",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = newKmInput,
            onValueChange = { newKmInput = it },
            label = { Text(if (language == AppLanguage.BANGLA) "বর্তমান ওডোমিটার রিডিং (কিমি)" else "Current Odometer (KM)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = brandInput,
            onValueChange = { brandInput = it },
            label = { Text(if (language == AppLanguage.BANGLA) "মবিল ব্র্যান্ড / গ্রেড" else "Mobil Brand / Grade") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = costInput,
            onValueChange = { costInput = it },
            label = { Text(if (language == AppLanguage.BANGLA) "মোট মবিল ও ফিল্টার খরচ (৳)" else "Total Cost (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Text(
            text = if (language == AppLanguage.BANGLA) {
              "* এই খরচটি স্বয়ংক্রিয়ভাবে আজকের খরচের হিসাবে যুক্ত হবে।"
            } else {
              "* This cost will automatically be recorded in today's expenses."
            },
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val km = newKmInput.toDoubleOrNull() ?: mobilService.currentOdometerKm
            val cost = costInput.toDoubleOrNull() ?: 0.0
            onLogMobilChange(km, brandInput, cost)
            showMobilChangeDialog = false
          }
        ) {
          Text(if (language == AppLanguage.BANGLA) "এন্ট্রি করুন" else "Log Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { showMobilChangeDialog = false }) {
          Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
        }
      }
    )
  }

  // Quick Odometer Update Dialog
  if (showOdometerDialog) {
    var odoInput by remember { mutableStateOf(mobilService.currentOdometerKm.toInt().toString()) }

    AlertDialog(
      onDismissRequest = { showOdometerDialog = false },
      title = {
        Text(
          text = if (language == AppLanguage.BANGLA) "ওডোমিটার রিডিং আপডেট" else "Update Odometer",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      },
      text = {
        OutlinedTextField(
          value = odoInput,
          onValueChange = { odoInput = it },
          label = { Text(if (language == AppLanguage.BANGLA) "বর্তমান কিমি" else "Current KM") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val km = odoInput.toDoubleOrNull() ?: mobilService.currentOdometerKm
            onUpdateOdometer(km)
            showOdometerDialog = false
          }
        ) {
          Text(if (language == AppLanguage.BANGLA) "সংরক্ষণ" else "Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showOdometerDialog = false }) {
          Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
        }
      }
    )
  }
}

@Composable
private fun DocumentCard(
  title: String,
  docNumber: String,
  expiryMillis: Long,
  icon: ImageVector,
  language: AppLanguage,
  onEditClick: () -> Unit
) {
  val now = System.currentTimeMillis()
  val diffDays = (expiryMillis - now) / (24 * 60 * 60 * 1000L)
  val isExpired = diffDays <= 0
  val isExpiringSoon = diffDays in 1..30

  val statusColor = when {
    isExpired -> LossRed
    isExpiringSoon -> WarningAmber
    else -> ProfitGreen
  }

  val statusContainer = when {
    isExpired -> LossRed.copy(alpha = 0.12f)
    isExpiringSoon -> WarningAmber.copy(alpha = 0.15f)
    else -> ProfitGreen.copy(alpha = 0.12f)
  }

  val statusText = when {
    isExpired -> if (language == AppLanguage.BANGLA) "মেয়াদ শেষ (${-diffDays} দিন আগে)" else "Expired (${-diffDays} days ago)"
    isExpiringSoon -> if (language == AppLanguage.BANGLA) "$diffDays দিন বাকি (সতর্কতা)" else "$diffDays days left (Expiring soon)"
    else -> if (language == AppLanguage.BANGLA) "$diffDays দিন বাকি (সক্রিয়)" else "$diffDays days left (Active)"
  }

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
  val dateStr = dateFormat.format(Date(expiryMillis))

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
          .background(statusContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = statusColor,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (docNumber.isNotBlank()) "নং: $docNumber" else "নং যোগ করা হয়নি",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "মেয়াদ: $dateStr",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusContainer
          ) {
            Text(
              text = statusText,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = statusColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      IconButtonWithBg(
        onClick = onEditClick,
        icon = Icons.Default.Edit,
        tint = MaterialTheme.colorScheme.primary
      )
    }
  }
}

@Composable
private fun ServiceItemRow(
  title: String,
  lastKm: Double,
  currentKm: Double,
  recommendedIntervalKm: Double,
  language: AppLanguage
) {
  val kmSince = maxOf(0.0, currentKm - lastKm)
  val isOverdue = kmSince >= recommendedIntervalKm

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = if (language == AppLanguage.BANGLA) {
          "সর্বশেষ: ${lastKm.toInt()} কিমি (${kmSince.toInt()} কিমি পূর্বে)"
        } else {
          "Last: ${lastKm.toInt()} km (${kmSince.toInt()} km ago)"
        },
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      )
    }

    Surface(
      shape = RoundedCornerShape(6.dp),
      color = if (isOverdue) LossRed.copy(alpha = 0.12f) else ProfitGreen.copy(alpha = 0.12f)
    ) {
      Text(
        text = if (isOverdue) {
          if (language == AppLanguage.BANGLA) "চেক করুন" else "Check Now"
        } else {
          if (language == AppLanguage.BANGLA) "ঠিক আছে" else "OK"
        },
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (isOverdue) LossRed else ProfitGreen,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}

@Composable
private fun IconButtonWithBg(
  onClick: () -> Unit,
  icon: ImageVector,
  tint: Color
) {
  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(tint.copy(alpha = 0.1f))
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(18.dp)
    )
  }
}

private fun countExpiringSoon(docs: VehicleDocuments): Int {
  val now = System.currentTimeMillis()
  val thirtyDays = 30L * 24 * 60 * 60 * 1000L
  var count = 0
  val expiries = listOf(
    docs.taxTokenExpiryMillis,
    docs.fitnessExpiryMillis,
    docs.routePermitExpiryMillis,
    docs.insuranceExpiryMillis,
    docs.drivingLicenseExpiryMillis
  )
  for (exp in expiries) {
    val diff = exp - now
    if (diff in 1..thirtyDays) count++
  }
  return count
}

private fun countExpired(docs: VehicleDocuments): Int {
  val now = System.currentTimeMillis()
  var count = 0
  val expiries = listOf(
    docs.taxTokenExpiryMillis,
    docs.fitnessExpiryMillis,
    docs.routePermitExpiryMillis,
    docs.insuranceExpiryMillis,
    docs.drivingLicenseExpiryMillis
  )
  for (exp in expiries) {
    if (exp <= now) count++
  }
  return count
}
