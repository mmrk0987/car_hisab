package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.export.ExportFormat
import com.example.data.model.BookingEntity
import com.example.data.model.MobilServiceInfo
import com.example.data.model.MonthlySummaryData
import com.example.data.model.TripEntity
import com.example.data.model.VehicleDocuments
import com.example.data.repository.UserProfile
import com.example.ui.components.EditTripDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.LossRed
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.DashboardStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  language: AppLanguage,
  profile: UserProfile,
  stats: DashboardStats,
  themeMode: AppThemeMode = AppThemeMode.DARK,
  recentTrips: List<TripEntity> = emptyList(),
  documents: VehicleDocuments = VehicleDocuments(),
  mobilService: MobilServiceInfo = MobilServiceInfo(),
  upcomingBookings: List<BookingEntity> = emptyList(),
  monthlySummary: MonthlySummaryData = MonthlySummaryData(),
  selectedMonthOffset: Int = 0,
  onPrevMonth: () -> Unit = {},
  onNextMonth: () -> Unit = {},
  onResetToCurrentMonth: () -> Unit = {},
  onSelectMonthYear: (year: Int, month: Int) -> Unit = { _, _ -> },
  onAddTripClick: () -> Unit,
  onViewAllTripsClick: () -> Unit,
  onMonthTripsClick: (year: Int, month: Int) -> Unit = { _, _ -> },
  onSettingsClick: () -> Unit = {},
  onDocsClick: () -> Unit = {},
  onBookingsClick: () -> Unit = {},
  onSubscriptionClick: () -> Unit = {},
  onExportTrips: (format: ExportFormat, tripsToExport: List<TripEntity>) -> Unit = { _, _ -> },
  onUpdateTrip: (TripEntity) -> Unit = {},
  onUpdateProfileQuick: (nameBn: String, carModel: String, carPlate: String) -> Unit = { _, _, _ -> }
) {
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showServiceHistoryDialog by remember { mutableStateOf(false) }
  var showCalendarDialog by remember { mutableStateOf(false) }
  var showCalculatorDialog by remember { mutableStateOf(false) }
  var maintenanceTripToEdit by remember { mutableStateOf<TripEntity?>(null) }
  var editName by remember { mutableStateOf(profile.driverNameBangla.ifEmpty { profile.driverName }) }
  var editCarModel by remember { mutableStateOf(profile.carModel) }
  var editCarPlate by remember { mutableStateOf(profile.carNumber) }

  LaunchedEffect(profile) {
    editName = profile.driverNameBangla.ifEmpty { profile.driverName }
    editCarModel = profile.carModel
    editCarPlate = profile.carNumber
  }

  // Active calendar month according to selectedMonthOffset
  val activeCalendar = remember(selectedMonthOffset) {
    Calendar.getInstance().apply {
      add(Calendar.MONTH, selectedMonthOffset)
    }
  }
  val activeYear = remember(activeCalendar) { activeCalendar.get(Calendar.YEAR) }
  val activeMonth = remember(activeCalendar) { activeCalendar.get(Calendar.MONTH) }
  val isViewingCurrentMonth = (selectedMonthOffset == 0)

  // Filter trips strictly for the active calendar month
  val currentMonthTrips = remember(recentTrips, activeYear, activeMonth) {
    recentTrips.filter { trip ->
      val tripCal = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      tripCal.get(Calendar.YEAR) == activeYear && tripCal.get(Calendar.MONTH) == activeMonth
    }
  }

  // Active Month Income = (Active Month Gross Rent) - (Active Month Trip Expenses / Gratuity)
  val currentMonthIncome = remember(currentMonthTrips) {
    val totalRent = currentMonthTrips.sumOf { it.rent }
    val totalExpense = currentMonthTrips.sumOf { it.gratuity }
    totalRent - totalExpense
  }

  // Active Month Service History: strictly only this active month's maintenance services
  val serviceTrips = remember(currentMonthTrips) {
    currentMonthTrips.filter { it.maintenanceCost > 0 }
  }
  val totalServiceCost = remember(serviceTrips) {
    serviceTrips.sumOf { it.maintenanceCost }
  }

  val currentMonthYear = remember(activeCalendar, language) {
    try {
      val locale = if (language == AppLanguage.BANGLA) Locale("bn", "BD") else Locale.ENGLISH
      val sdf = SimpleDateFormat("MMMM yyyy", locale)
      sdf.format(activeCalendar.time)
    } catch (_: Exception) {
      val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
      sdf.format(activeCalendar.time)
    }
  }

  // Active Month profit and trip count
  val displayMonthProfit = remember(currentMonthTrips) {
    currentMonthTrips.sumOf { it.profit }
  }
  val displayMonthTrips = currentMonthTrips.size

  // Strictly Single Screen, Non-scrollable layout
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(backgroundBrush)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("dashboard_screen_root")
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // TOP SECTION: Header with Profile, Edit button, Settings, and dedicated Calendar Bar
      Column {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_header_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp)
          ) {
            // Tier 1: Driver Profile Information & Quick Settings
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // User Profile Photo
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .border(2.dp, MintGreenAccent.copy(alpha = 0.7f), CircleShape)
                  .testTag("dashboard_profile_avatar"),
                contentAlignment = Alignment.Center
              ) {
                if (!profile.profileImageUri.isNullOrBlank()) {
                  AsyncImage(
                    model = profile.profileImageUri,
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Photo",
                    tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Profile Name, Car Number, and Edit Profile Button
              Column(modifier = Modifier.weight(1f)) {
                val driverDisplayName = profile.driverNameBangla.ifEmpty { profile.driverName }.trim()
                Text(
                  text = driverDisplayName.ifEmpty {
                    if (language == AppLanguage.BANGLA) "ড্রাইভার প্রোফাইল" else "Driver Profile"
                  },
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.testTag("dashboard_profile_name")
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  val carPlateText = profile.carNumber.trim()
                  if (carPlateText.isNotEmpty() && carPlateText != "ঢাকা মেট্রো চ-১১-২২৩৩") {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                      Text(
                        text = carPlateText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                  }

                  // [Edit] Button
                  Surface(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .clickable {
                        editName = profile.driverNameBangla.ifEmpty { profile.driverName }
                        editCarModel = profile.carModel
                        editCarPlate = profile.carNumber
                        showEditProfileDialog = true
                      }
                      .testTag("dashboard_edit_profile_button"),
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) MintGreenAccent.copy(alpha = 0.15f) else Color(0xFF0F766E).copy(alpha = 0.12f)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                        modifier = Modifier.size(10.dp)
                      )
                      Spacer(modifier = Modifier.width(2.dp))
                      Text(
                        text = if (language == AppLanguage.BANGLA) "এডিট" else "Edit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MintGreenAccent else Color(0xFF0F766E)
                      )
                    }
                  }
                }
              }

              // Settings Icon Button
              IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                  .size(34.dp)
                  .testTag("dashboard_settings_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = "Settings",
                  tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                  modifier = Modifier.size(19.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
              modifier = Modifier.fillMaxWidth(),
              thickness = 0.8.dp,
              color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tier 2: Dedicated Month & Calendar Navigation Bar
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Previous Month Button (<)
              Surface(
                modifier = Modifier
                  .size(30.dp)
                  .clip(CircleShape)
                  .clickable { onPrevMonth() }
                  .testTag("dashboard_prev_month_btn"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                    modifier = Modifier.size(14.dp)
                  )
                }
              }

              // Interactive Month Badge -> Opens Full Calendar Dialog
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .padding(horizontal = 6.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { showCalendarDialog = true }
                  .testTag("dashboard_calendar_badge"),
                shape = RoundedCornerShape(10.dp),
                color = if (!isViewingCurrentMonth) {
                  if (isDark) MintGreenAccent.copy(alpha = 0.22f) else Color(0xFF0F766E).copy(alpha = 0.16f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (!isViewingCurrentMonth) (if (isDark) MintGreenAccent else Color(0xFF0F766E))
                  else MaterialTheme.colorScheme.outline
                )
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Open Calendar",
                    tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = currentMonthYear,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              // Next Month Button (>)
              Surface(
                modifier = Modifier
                  .size(30.dp)
                  .clip(CircleShape)
                  .clickable { onNextMonth() }
                  .testTag("dashboard_next_month_btn"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                    modifier = Modifier.size(14.dp)
                  )
                }
              }

              // Reset to Current Month Button (if browsing other months)
              if (!isViewingCurrentMonth) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                  modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable { onResetToCurrentMonth() }
                    .testTag("dashboard_reset_month_btn"),
                  shape = CircleShape,
                  color = if (isDark) MintGreenAccent.copy(alpha = 0.2f) else Color(0xFF0F766E).copy(alpha = 0.15f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Today,
                      contentDescription = "Current Month",
                      tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        
        // Backup Reminder Note
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
          border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Info",
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "আপনার ট্রিপ ডেটা ডাটাবেসে সেভ করতে সেটিংসে গিয়ে ব্যাকআপ বাটনে চাপুন।" else "Push Backup button in settings to save your trip data into database.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              lineHeight = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION: Core Metrics Cards (Custom Ordered & Styled Layout)
        // 1. প্রধান কার্ড: চলতি মাসের ইনকাম (Horizontal Full-Width Rectangular Card)
        val incomeBorderColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFF059669).copy(alpha = 0.4f)
        val incomeGradientColors = if (isDark) {
          listOf(Color(0xFF1B2F23), Color(0xFF112118))
        } else {
          listOf(Color(0xFFEDF8F2), Color(0xFFF5FBF8))
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("metric_card_month_income"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, incomeBorderColor)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(incomeGradientColors))
              .padding(14.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "চলতি মাসের ইনকাম" else "Monthly Income",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) ProfitGreen.copy(alpha = 0.15f) else Color(0xFF059669).copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = if (language == AppLanguage.BANGLA) "ভাড়া − ট্রিপ খরচ" else "Rent − Expenses",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) ProfitGreen.copy(alpha = 0.2f) else Color(0xFF059669).copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = if (isDark) ProfitGreen else Color(0xFF059669),
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "৳" + String.format(Locale.US, "%,.0f", currentMonthIncome),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. মাঝের ২ টি বর্গাকার কার্ড: সার্ভিস হিস্টোরি ও চলতি মাসের প্রফিট
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // কার্ড ২.১: সার্ভিস হিস্টোরি (Service History)
          val serviceBorderColor = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color(0xFFD97706).copy(alpha = 0.4f)
          val serviceGradientColors = if (isDark) {
            listOf(Color(0xFF2C2417), Color(0xFF1E1910))
          } else {
            listOf(Color(0xFFFEF7E8), Color(0xFFFFFDF7))
          }

          Card(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(18.dp))
              .clickable { showServiceHistoryDialog = true }
              .testTag("metric_card_service_history"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, serviceBorderColor)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(serviceGradientColors))
                .padding(13.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "মেইনটেনেন্স খরচ" else "Maintenance Cost",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Box(
                    modifier = Modifier
                      .size(26.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isDark) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFFD97706).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Build,
                      contentDescription = null,
                      tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "৳" + String.format(Locale.US, "%,.0f", totalServiceCost),
                  fontSize = 19.sp,
                  fontWeight = FontWeight.Black,
                  color = if (isDark) Color.White else Color(0xFF0F172A),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.12f)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = if (language == AppLanguage.BANGLA)
                        "${serviceTrips.size}টি কাজ • দেখুন"
                      else
                        "${serviceTrips.size} services • View",
                      fontSize = 10.sp,
                      color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                      contentDescription = null,
                      tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                      modifier = Modifier.size(9.dp)
                    )
                  }
                }
              }
            }
          }

          // কার্ড ২.২: চলতি মাসের প্রফিট (Current Month Profit)
          val isProfitPositive = displayMonthProfit >= 0
          val profitBorderColor = if (isDark) {
            if (isProfitPositive) MintGreenAccent.copy(alpha = 0.35f) else LossRed.copy(alpha = 0.35f)
          } else {
            if (isProfitPositive) Color(0xFF10B981).copy(alpha = 0.45f) else LossRed.copy(alpha = 0.45f)
          }
          val profitGradientColors = if (isDark) {
            if (isProfitPositive) listOf(Color(0xFF142E23), Color(0xFF0F211A))
            else listOf(Color(0xFF331616), Color(0xFF220E0E))
          } else {
            if (isProfitPositive) listOf(Color(0xFFE8F7F0), Color(0xFFF3FBF7))
            else listOf(Color(0xFFFDE8E8), Color(0xFFFEF2F2))
          }

          Card(
            modifier = Modifier
              .weight(1f)
              .testTag("metric_card_month_profit"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, profitBorderColor)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(profitGradientColors))
                .padding(13.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "চলতি মাসের প্রফিট" else "Month Profit",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Box(
                    modifier = Modifier
                      .size(26.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        if (isProfitPositive) {
                          if (isDark) ProfitGreen.copy(alpha = 0.2f) else Color(0xFF059669).copy(alpha = 0.15f)
                        } else {
                          LossRed.copy(alpha = if (isDark) 0.2f else 0.15f)
                        }
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (isProfitPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                      contentDescription = null,
                      tint = if (isProfitPositive) (if (isDark) ProfitGreen else Color(0xFF059669)) else LossRed,
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "৳" + String.format(Locale.US, "%,.0f", displayMonthProfit),
                  fontSize = 19.sp,
                  fontWeight = FontWeight.Black,
                  color = if (isProfitPositive) (if (isDark) Color(0xFF4ADE80) else Color(0xFF047857)) else LossRed,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.testTag("metric_profit_value")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isProfitPositive) {
                    if (isDark) ProfitGreen.copy(alpha = 0.15f) else Color(0xFF059669).copy(alpha = 0.12f)
                  } else {
                    LossRed.copy(alpha = if (isDark) 0.15f else 0.12f)
                  }
                ) {
                  Text(
                    text = if (isProfitPositive) {
                      if (language == AppLanguage.BANGLA) "✓ নিট লাভ" else "✓ In Profit"
                    } else {
                      if (language == AppLanguage.BANGLA) "⚠ লোকসান" else "⚠ Loss"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfitPositive) (if (isDark) ProfitGreen else Color(0xFF047857)) else LossRed,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. নিচের কার্ড: চলতি মাসের ট্রিপ (Horizontal Full-Width Rectangular Card)
        val tripsBorderColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFF0284C7).copy(alpha = 0.4f)
        val tripsGradientColors = if (isDark) {
          listOf(Color(0xFF122833), Color(0xFF0E1E26))
        } else {
          listOf(Color(0xFFEBF7FD), Color(0xFFF3FAFE))
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onMonthTripsClick(activeYear, activeMonth) }
            .testTag("metric_card_month_trips"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, tripsBorderColor)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(tripsGradientColors))
              .padding(14.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "চলতি মাসের ট্রিপ" else "Month Trips",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.12f)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "তালিকা দেখুন" else "View Trips",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                      )
                      Spacer(modifier = Modifier.width(2.dp))
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.size(9.dp)
                      )
                    }
                  }
                }
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "$displayMonthTrips টি",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                maxLines = 1,
                modifier = Modifier.testTag("metric_trips_value")
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 5: ক্যালকুলেটর (Centered Square Feature Card)
        val calcBorderColor = if (isDark) Color(0xFF818CF8).copy(alpha = 0.35f) else Color(0xFF6366F1).copy(alpha = 0.4f)
        val calcGradientColors = if (isDark) {
          listOf(Color(0xFF191932), Color(0xFF111122))
        } else {
          listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          Card(
            modifier = Modifier
              .fillMaxWidth(0.52f)
              .clip(RoundedCornerShape(18.dp))
              .clickable { showCalculatorDialog = true }
              .testTag("metric_card_calculator"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, calcBorderColor)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(calcGradientColors))
                .padding(13.dp)
            ) {
              Column(
                horizontalAlignment = Alignment.Start
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "ক্যালকুলেটর" else "Calculator",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Box(
                    modifier = Modifier
                      .size(26.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isDark) Color(0xFF818CF8).copy(alpha = 0.2f) else Color(0xFF6366F1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Calculate,
                      contentDescription = null,
                      tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                      modifier = Modifier.size(15.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = if (language == AppLanguage.BANGLA) "ভাড়া ও খরচের হিসাব" else "Rent & Expense Math",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isDark) Color(0xFF818CF8).copy(alpha = 0.15f) else Color(0xFF6366F1).copy(alpha = 0.12f)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = if (language == AppLanguage.BANGLA) "খুলুন" else "Open",
                      fontSize = 10.sp,
                      color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                      contentDescription = null,
                      tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                      modifier = Modifier.size(9.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // BOTTOM SECTION: Prominent Primary Action Button ("+ নতুন ট্রিপ যোগ করুন")
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 4.dp)
      ) {
        Button(
          onClick = onAddTripClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("dashboard_add_trip_primary_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
          )
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (language == AppLanguage.BANGLA) "নতুন ট্রিপ যোগ করুন" else "Add New Trip",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary,
            letterSpacing = 0.5.sp
          )
        }
      }
    }

    // Quick Edit Profile Dialog
    if (showEditProfileDialog) {
      AlertDialog(
        onDismissRequest = { showEditProfileDialog = false },
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Edit Profile",
            tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
            modifier = Modifier.size(36.dp)
          )
        },
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "প্রোফাইল তথ্য পরিবর্তন" else "Edit Profile",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column {
            OutlinedTextField(
              value = editName,
              onValueChange = { editName = it },
              label = { Text(if (language == AppLanguage.BANGLA) "ড্রাইভারের নাম" else "Driver Name") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_profile_dialog_name")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = editCarModel,
              onValueChange = { editCarModel = it },
              label = { Text(if (language == AppLanguage.BANGLA) "গাড়ির মডেল" else "Car Model") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_profile_dialog_car_model")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = editCarPlate,
              onValueChange = { editCarPlate = it },
              label = { Text(if (language == AppLanguage.BANGLA) "গাড়ির নম্বর প্লেট" else "License Plate") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_profile_dialog_car_plate")
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              showEditProfileDialog = false
              onUpdateProfileQuick(editName.trim(), editCarModel.trim(), editCarPlate.trim())
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("edit_profile_dialog_save_button")
          ) {
            Text(
              text = if (language == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Save",
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { showEditProfileDialog = false }) {
            Text(
              if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel",
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      )
    }

    // Service History Dialog (সার্ভিস হিস্টোরি পপআপ / ডায়ালগ)
    if (showServiceHistoryDialog) {
      Dialog(
        onDismissRequest = { showServiceHistoryDialog = false }
      ) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 580.dp),
          shape = RoundedCornerShape(22.dp),
          color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          shadowElevation = 10.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            // Dialog Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) MintGreenAccent.copy(alpha = 0.15f) else Color(0xFF0F766E).copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "মেইনটেনেন্স ও সার্ভিস হিস্টোরি" else "Maintenance & Service History",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = if (language == AppLanguage.BANGLA) "কাজের বিবরণ ও খরচ মূল্য তালিকা" else "Maintenance records & costs",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              IconButton(
                onClick = { showServiceHistoryDialog = false },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Banner
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "মোট কাজের সংখ্যা" else "Total Services",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "${serviceTrips.size} টি",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "সর্বমোট কাজের খরচ" else "Total Service Cost",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "৳" + String.format(Locale.US, "%,.0f", totalServiceCost),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LossRed
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Service Records
            if (serviceTrips.isEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f, fill = false)
                  .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                    modifier = Modifier.size(44.dp)
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA) "কোনো সার্ভিস রেকর্ড নেই" else "No Service Records",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA)
                      "ট্রিপ যোগ করার সময় মেইনটেন্যান্স খরচ ও কাজের বিবরণ দিলে তা এখানে সংগৃহীত হবে।"
                    else
                      "When adding trips, enter maintenance cost and description to see them here.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                  )
                }
              }
            } else {
              LazyColumn(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(serviceTrips) { sTrip ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                  ) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                    ) {
                      // Line 1: কাজের বিবরণ ও কাজের খরচ মূল্য
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                      ) {
                        Row(
                          modifier = Modifier.weight(1f),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = null,
                            tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                            modifier = Modifier.size(15.dp)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = sTrip.description.ifBlank {
                              if (language == AppLanguage.BANGLA) "সার্ভিসিং ও মেরামত কাজ" else "Maintenance Service"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                          )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                          // কাজের খরচ মূল্য
                          Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LossRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LossRed.copy(alpha = 0.3f))
                          ) {
                            Text(
                              text = "৳" + String.format(Locale.US, "%,.0f", sTrip.maintenanceCost),
                              fontSize = 13.sp,
                              fontWeight = FontWeight.ExtraBold,
                              color = LossRed,
                              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                          }

                          Spacer(modifier = Modifier.width(4.dp))

                          IconButton(
                            onClick = { maintenanceTripToEdit = sTrip },
                            modifier = Modifier
                              .size(28.dp)
                              .testTag("btn_edit_maintenance_${sTrip.id}")
                          ) {
                            Icon(
                              imageVector = Icons.Default.Edit,
                              contentDescription = "Edit Maintenance",
                              tint = MintGreenAccent,
                              modifier = Modifier.size(16.dp)
                            )
                          }
                        }
                      }

                      Spacer(modifier = Modifier.height(8.dp))

                      // Line 2: তারিখ ও জায়গা
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        // তারিখ
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = sTrip.dateString,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }

                        // জায়গা
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          modifier = Modifier.padding(start = 6.dp)
                        ) {
                          Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                            modifier = Modifier.size(12.dp)
                          )
                          Spacer(modifier = Modifier.width(3.dp))
                          Text(
                            text = sTrip.place,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                      }

                      if (sTrip.kmDriven > 0.0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                          text = "মিটার: ${String.format(Locale.US, "%,.1f", sTrip.kmDriven)} কিমি",
                          fontSize = 10.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action
            Button(
              onClick = { showServiceHistoryDialog = false },
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Text(
                text = if (language == AppLanguage.BANGLA) "ঠিক আছে" else "Close",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              )
            }
          }
        }
      }
    }

    // Edit Maintenance Record Dialog
    maintenanceTripToEdit?.let { sTrip ->
      EditTripDialog(
        trip = sTrip,
        language = language,
        isMaintenanceMode = true,
        onDismiss = { maintenanceTripToEdit = null },
        onSaveTrip = { updatedTrip ->
          onUpdateTrip(updatedTrip)
          maintenanceTripToEdit = null
        }
      )
    }

    // Workable Dashboard Calendar & Month Selector Dialog
    if (showCalendarDialog) {
      DashboardCalendarDialog(
        language = language,
        isDark = isDark,
        initialYear = activeYear,
        initialMonth = activeMonth,
        trips = recentTrips,
        onDismiss = { showCalendarDialog = false },
        onApplyMonthToDashboard = { y, m ->
          onSelectMonthYear(y, m)
          showCalendarDialog = false
        },
        onOpenMonthFolder = { y, m ->
          onMonthTripsClick(y, m)
          showCalendarDialog = false
        },
        onResetToCurrentMonth = {
          onResetToCurrentMonth()
          showCalendarDialog = false
        }
      )
    }

    // Quick Calculator Modal Dialog
    if (showCalculatorDialog) {
      DashboardCalculatorDialog(
        language = language,
        isDark = isDark,
        onDismiss = { showCalculatorDialog = false }
      )
    }
  }
}

@Composable
fun DashboardCalendarDialog(
  language: AppLanguage,
  isDark: Boolean,
  initialYear: Int,
  initialMonth: Int,
  trips: List<TripEntity>,
  onDismiss: () -> Unit,
  onApplyMonthToDashboard: (year: Int, month: Int) -> Unit,
  onOpenMonthFolder: (year: Int, month: Int) -> Unit,
  onResetToCurrentMonth: () -> Unit
) {
  var viewYear by remember { mutableStateOf(initialYear) }
  var viewMonth by remember { mutableStateOf(initialMonth) }
  var selectedDay by remember { mutableStateOf<Int?>(null) }

  val curCal = remember { Calendar.getInstance() }
  val curYear = curCal.get(Calendar.YEAR)
  val curMonth = curCal.get(Calendar.MONTH)
  val curDay = curCal.get(Calendar.DAY_OF_MONTH)

  val banglaMonths = listOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
  )
  val banglaMonthsShort = listOf(
    "জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
  )
  val englishMonths = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  )
  val englishMonthsShort = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  )

  val currentMonthName = if (language == AppLanguage.BANGLA) banglaMonths[viewMonth] else englishMonths[viewMonth]

  // Calculate first day of week & total days
  val (firstDayOfWeek, totalDaysInMonth) = remember(viewYear, viewMonth) {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, viewYear)
      set(Calendar.MONTH, viewMonth)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    // Sunday = 1 -> 0, Monday = 2 -> 1, ..., Saturday = 7 -> 6
    val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1
    val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    Pair(firstDow, totalDays)
  }

  // Filter trips for viewYear & viewMonth
  val monthTrips = remember(trips, viewYear, viewMonth) {
    trips.filter { trip ->
      val tripCal = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      tripCal.get(Calendar.YEAR) == viewYear && tripCal.get(Calendar.MONTH) == viewMonth
    }
  }

  // Group trips by day of month
  val dayTripsMap = remember(monthTrips) {
    monthTrips.groupBy { trip ->
      val tripCal = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      tripCal.get(Calendar.DAY_OF_MONTH)
    }
  }

  // Month stats
  val totalGrossRent = remember(monthTrips) { monthTrips.sumOf { it.rent } }
  val totalIncome = remember(monthTrips) {
    val rent = monthTrips.sumOf { it.rent }
    val exp = monthTrips.sumOf { it.gratuity }
    rent - exp
  }
  val totalProfit = remember(monthTrips) { monthTrips.sumOf { it.profit } }
  val totalMaintenance = remember(monthTrips) { monthTrips.sumOf { it.maintenanceCost } }

  // Trips for selected day if any
  val selectedDayTrips = remember(dayTripsMap, selectedDay) {
    selectedDay?.let { dayTripsMap[it] } ?: emptyList()
  }

  val weekDays = if (language == AppLanguage.BANGLA) {
    listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
  } else {
    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 660.dp)
        .testTag("dashboard_calendar_dialog"),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isDark) Color(0xFF0F241A) else Color(0xFFF7FCF9)
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.2.dp,
        if (isDark) MintGreenAccent.copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.5f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Dialog Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) MintGreenAccent.copy(alpha = 0.2f) else Color(0xFF0F766E).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "ক্যালেন্ডার ও মাস নির্বাচন" else "Calendar & Month Selector",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "$currentMonthName $viewYear",
                fontSize = 11.sp,
                color = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Year Selector & Reset to Today Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                viewYear--
                selectedDay = null
              },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Year",
                tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = "$viewYear",
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
              onClick = {
                viewYear++
                selectedDay = null
              },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Year",
                tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                modifier = Modifier.size(16.dp)
              )
            }
          }

          // Jump to Today/Current Month button
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                viewYear = curYear
                viewMonth = curMonth
                selectedDay = curDay
              },
            shape = RoundedCornerShape(8.dp),
            color = if (viewYear == curYear && viewMonth == curMonth) {
              if (isDark) MintGreenAccent.copy(alpha = 0.2f) else Color(0xFF0F766E).copy(alpha = 0.15f)
            } else {
              Color.Transparent
            },
            border = androidx.compose.foundation.BorderStroke(
              0.8.dp,
              if (isDark) MintGreenAccent.copy(alpha = 0.6f) else Color(0xFF0F766E).copy(alpha = 0.5f)
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Today,
                contentDescription = null,
                tint = if (isDark) MintGreenAccent else Color(0xFF0F766E),
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "চলতি মাস" else "Current Month",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) MintGreenAccent else Color(0xFF0F766E)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 12-Month Quick Chips Row
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
        ) {
          item {
            // Month Chips (2 rows of 6)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              for (row in 0..1) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  for (col in 0..5) {
                    val mIndex = row * 6 + col
                    val isSelectedMonth = (mIndex == viewMonth)
                    val mLabel = if (language == AppLanguage.BANGLA) banglaMonthsShort[mIndex] else englishMonthsShort[mIndex]
                    
                    // Check if there are trips in this month of viewYear
                    val countForMonth = trips.count { trip ->
                      val tCal = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
                      tCal.get(Calendar.YEAR) == viewYear && tCal.get(Calendar.MONTH) == mIndex
                    }

                    Surface(
                      modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                          viewMonth = mIndex
                          selectedDay = null
                        },
                      shape = RoundedCornerShape(8.dp),
                      color = if (isSelectedMonth) {
                        if (isDark) MintGreenAccent.copy(alpha = 0.3f) else Color(0xFF0F766E).copy(alpha = 0.22f)
                      } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                      },
                      border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelectedMonth) (if (isDark) MintGreenAccent else Color(0xFF0F766E))
                        else Color.Transparent
                      )
                    ) {
                      Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                      ) {
                        Text(
                          text = mLabel,
                          fontSize = 10.5.sp,
                          fontWeight = if (isSelectedMonth) FontWeight.Bold else FontWeight.Medium,
                          color = if (isSelectedMonth) (if (isDark) Color.White else Color(0xFF0F766E))
                          else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (countForMonth > 0) {
                          Box(
                            modifier = Modifier
                              .size(4.dp)
                              .clip(CircleShape)
                              .background(if (isDark) ProfitGreen else Color(0xFF059669))
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Days Matrix
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                // Days of week header
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  weekDays.forEach { wDay ->
                    Text(
                      text = wDay,
                      modifier = Modifier.weight(1f),
                      textAlign = TextAlign.Center,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Days Grid (up to 6 weeks)
                val totalCells = firstDayOfWeek + totalDaysInMonth
                val totalRows = (totalCells + 6) / 7

                for (r in 0 until totalRows) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    for (c in 0 until 7) {
                      val cellIndex = r * 7 + c
                      val dayNumber = cellIndex - firstDayOfWeek + 1

                      if (cellIndex < firstDayOfWeek || dayNumber > totalDaysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                      } else {
                        val isToday = (viewYear == curYear && viewMonth == curMonth && dayNumber == curDay)
                        val isDaySelected = (selectedDay == dayNumber)
                        val tripsOnDay = dayTripsMap[dayNumber] ?: emptyList()
                        val hasTrips = tripsOnDay.isNotEmpty()

                        Surface(
                          modifier = Modifier
                            .weight(1f)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                              selectedDay = if (selectedDay == dayNumber) null else dayNumber
                            },
                          shape = RoundedCornerShape(6.dp),
                          color = when {
                            isDaySelected -> if (isDark) MintGreenAccent.copy(alpha = 0.35f) else Color(0xFF0F766E).copy(alpha = 0.3f)
                            hasTrips -> if (isDark) ProfitGreen.copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.12f)
                            else -> Color.Transparent
                          },
                          border = if (isToday) {
                            androidx.compose.foundation.BorderStroke(1.dp, if (isDark) MintGreenAccent else Color(0xFF0F766E))
                          } else if (isDaySelected) {
                            androidx.compose.foundation.BorderStroke(1.dp, if (isDark) MintGreenAccent else Color(0xFF0F766E))
                          } else null
                        ) {
                          Column(
                            modifier = Modifier.padding(vertical = 3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                          ) {
                            Text(
                              text = "$dayNumber",
                              fontSize = 11.sp,
                              fontWeight = if (isToday || isDaySelected || hasTrips) FontWeight.Bold else FontWeight.Normal,
                              color = when {
                                isDaySelected -> if (isDark) MintGreenAccent else Color(0xFF0F766E)
                                isToday -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                hasTrips -> if (isDark) Color.White else Color(0xFF0F172A)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                              },
                              textAlign = TextAlign.Center
                            )
                            if (hasTrips) {
                              Box(
                                modifier = Modifier
                                  .size(3.5.dp)
                                  .clip(CircleShape)
                                  .background(if (isDark) ProfitGreen else Color(0xFF059669))
                              )
                            } else {
                              Spacer(modifier = Modifier.height(3.5.dp))
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Snapshot Card: Selected Day details OR Month Summary
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                if (selectedDay != null) {
                  // Single Day View
                  val dTrips = selectedDayTrips
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "$selectedDay $currentMonthName $viewYear",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = if (dTrips.isNotEmpty()) ProfitGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.15f)
                    ) {
                      Text(
                        text = if (dTrips.isNotEmpty()) "${dTrips.size}টি ট্রিপ" else if (language == AppLanguage.BANGLA) "ট্রিপ নেই" else "No Trips",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dTrips.isNotEmpty()) (if (isDark) ProfitGreen else Color(0xFF059669)) else LossRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }

                  if (dTrips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val dRent = dTrips.sumOf { it.rent }
                    val dProfit = dTrips.sumOf { it.profit }
                    val dMaint = dTrips.sumOf { it.maintenanceCost }

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = "মোট ভাড়া: ৳${String.format(Locale.US, "%,.0f", dRent)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = "নিট লাভ: ৳${String.format(Locale.US, "%,.0f", dProfit)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dProfit >= 0) (if (isDark) ProfitGreen else Color(0xFF059669)) else LossRed
                      )
                    }

                    if (dMaint > 0) {
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = "সার্ভিসিং খরচ: ৳${String.format(Locale.US, "%,.0f", dMaint)}",
                        fontSize = 10.5.sp,
                        color = LossRed
                      )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    dTrips.take(3).forEach { t ->
                      Text(
                        text = "• ${t.place} (৳${String.format(Locale.US, "%,.0f", t.rent)})",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                } else {
                  // Entire Month Summary
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "$currentMonthName $viewYear এর সারসংক্ষেপ",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${monthTrips.size}টি ট্রিপ",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDark) MintGreenAccent else Color(0xFF0F766E)
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "নিট প্রফিট" else "Net Profit",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Text(
                        text = "৳" + String.format(Locale.US, "%,.0f", totalProfit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (totalProfit >= 0) (if (isDark) ProfitGreen else Color(0xFF059669)) else LossRed
                      )
                    }

                    Column {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "মোট ইনকাম" else "Total Income",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Text(
                        text = "৳" + String.format(Locale.US, "%,.0f", totalIncome),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "সার্ভিসিং" else "Maintenance",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Text(
                        text = "৳" + String.format(Locale.US, "%,.0f", totalMaintenance),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalMaintenance > 0) LossRed else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Button 1: Open Month Folder in All Trips
          Button(
            onClick = { onOpenMonthFolder(viewYear, viewMonth) },
            modifier = Modifier
              .weight(1f)
              .height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "ট্রিপ ফোল্ডার" else "Trip Folder",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // Button 2: Apply to Dashboard
          Button(
            onClick = { onApplyMonthToDashboard(viewYear, viewMonth) },
            modifier = Modifier
              .weight(1f)
              .height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            )
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "ড্যাশবোর্ডে দেখুন" else "Set Dashboard",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun DashboardCalculatorDialog(
  language: AppLanguage,
  isDark: Boolean,
  onDismiss: () -> Unit
) {
  var expression by remember { mutableStateOf("") }
  var resultText by remember { mutableStateOf("0") }
  var lastResultCalculated by remember { mutableStateOf(false) }

  fun onInput(char: String) {
    if (lastResultCalculated && char in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "00", ".")) {
      expression = char
      lastResultCalculated = false
    } else {
      lastResultCalculated = false
      val operators = listOf("+", "−", "×", "÷", "%")
      if (char in operators && expression.isNotEmpty()) {
        val lastChar = expression.takeLast(1)
        if (lastChar in operators) {
          expression = expression.dropLast(1) + char
          return
        }
      }
      expression += char
    }

    val preview = evaluateCalcExpression(expression)
    if (preview != null && expression.any { it in listOf('+', '−', '×', '÷', '%') }) {
      resultText = formatCalcNumber(preview)
    } else if (expression.isEmpty()) {
      resultText = "0"
    }
  }

  fun onClear() {
    expression = ""
    resultText = "0"
    lastResultCalculated = false
  }

  fun onBackspace() {
    if (expression.isNotEmpty()) {
      expression = expression.dropLast(1)
      val preview = evaluateCalcExpression(expression)
      resultText = if (preview != null && expression.any { it in listOf('+', '−', '×', '÷', '%') }) {
        formatCalcNumber(preview)
      } else if (expression.isNotEmpty()) {
        expression
      } else {
        "0"
      }
      lastResultCalculated = false
    }
  }

  fun onEquals() {
    if (expression.isNotEmpty()) {
      val evaluated = evaluateCalcExpression(expression)
      if (evaluated != null) {
        val formatted = formatCalcNumber(evaluated)
        resultText = formatted
        expression = formatted
        lastResultCalculated = true
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 580.dp)
        .testTag("dashboard_calculator_dialog"),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isDark) Color(0xFF0F1722) else Color(0xFFF8FAFC)
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.2.dp,
        if (isDark) Color(0xFF818CF8).copy(alpha = 0.5f) else Color(0xFF6366F1).copy(alpha = 0.5f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Calculator Dialog Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) Color(0xFF818CF8).copy(alpha = 0.22f) else Color(0xFF6366F1).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "ক্যালকুলেটর" else "Calculator",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (language == AppLanguage.BANGLA) "ভাড়া ও খরচের দ্রুত হিসাব" else "Quick math & trip costs",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Digital Display / LCD Surface
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
          shape = RoundedCornerShape(12.dp),
          color = if (isDark) Color(0xFF070B12) else Color(0xFFEDF2F7),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0xFF818CF8).copy(alpha = 0.35f) else Color(0xFF6366F1).copy(alpha = 0.35f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = expression.ifEmpty { "0" },
              fontSize = 13.sp,
              color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.End
            ) {
              Text(
                text = "৳ ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
              )
              Text(
                text = resultText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Keypad Grid (5 rows x 4 columns)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val row1 = listOf("C", "%", "÷", "⌫")
          val row2 = listOf("7", "8", "9", "×")
          val row3 = listOf("4", "5", "6", "−")
          val row4 = listOf("1", "2", "3", "+")
          val row5 = listOf("00", "0", ".", "=")

          val allRows = listOf(row1, row2, row3, row4, row5)

          for (row in allRows) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              for (key in row) {
                val isOperator = key in listOf("÷", "×", "−", "+", "%")
                val isEquals = key == "="
                val isClear = key == "C"

                val keyColor = when {
                  isEquals -> MaterialTheme.colorScheme.primary
                  isClear -> if (isDark) Color(0xFF3B1818) else Color(0xFFFFE4E6)
                  isOperator -> if (isDark) Color(0xFF1E1E3A) else Color(0xFFEEF2FF)
                  else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val textColor = when {
                  isEquals -> MaterialTheme.colorScheme.onPrimary
                  isClear -> LossRed
                  isOperator -> if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
                  else -> MaterialTheme.colorScheme.onSurface
                }

                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                      when (key) {
                        "C" -> onClear()
                        "⌫" -> onBackspace()
                        "=" -> onEquals()
                        else -> onInput(key)
                      }
                    }
                    .testTag("calc_dialog_btn_$key"),
                  shape = RoundedCornerShape(10.dp),
                  color = keyColor,
                  border = androidx.compose.foundation.BorderStroke(
                    0.8.dp,
                    if (isEquals) Color.Transparent
                    else if (isOperator) (if (isDark) Color(0xFF818CF8).copy(alpha = 0.35f) else Color(0xFF6366F1).copy(alpha = 0.35f))
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                  )
                ) {
                  Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                  ) {
                    Text(
                      text = key,
                      fontSize = if (key.length > 1 && !isEquals) 13.sp else 16.sp,
                      fontWeight = if (isEquals || isOperator || isClear) FontWeight.ExtraBold else FontWeight.Bold,
                      color = textColor
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun evaluateCalcExpression(expr: String): Double? {
  return try {
    val clean = expr.replace("×", "*").replace("÷", "/").replace("−", "-")
    if (clean.isBlank()) return null
    val tokens = mutableListOf<String>()
    var currentNumber = StringBuilder()
    var i = 0
    while (i < clean.length) {
      val c = clean[i]
      if (c.isDigit() || c == '.') {
        currentNumber.append(c)
      } else if (c in listOf('+', '-', '*', '/', '%')) {
        if (currentNumber.isNotEmpty()) {
          tokens.add(currentNumber.toString())
          currentNumber = StringBuilder()
        }
        tokens.add(c.toString())
      }
      i++
    }
    if (currentNumber.isNotEmpty()) {
      tokens.add(currentNumber.toString())
    }
    if (tokens.isEmpty()) return null

    // Pass 1: * / %
    val pass1 = mutableListOf<String>()
    var idx = 0
    while (idx < tokens.size) {
      val token = tokens[idx]
      if ((token == "*" || token == "/" || token == "%") && pass1.isNotEmpty() && idx + 1 < tokens.size) {
        val left = pass1.removeAt(pass1.size - 1).toDoubleOrNull() ?: return null
        val right = tokens[idx + 1].toDoubleOrNull() ?: return null
        val res = when (token) {
          "*" -> left * right
          "/" -> if (right == 0.0) return null else left / right
          "%" -> left * (right / 100.0)
          else -> 0.0
        }
        pass1.add(res.toString())
        idx += 2
      } else {
        pass1.add(token)
        idx++
      }
    }

    // Pass 2: + -
    if (pass1.isEmpty()) return null
    var total = pass1[0].toDoubleOrNull() ?: return null
    var opIdx = 1
    while (opIdx < pass1.size) {
      val op = pass1[opIdx]
      if (opIdx + 1 < pass1.size) {
        val right = pass1[opIdx + 1].toDoubleOrNull() ?: return null
        when (op) {
          "+" -> total += right
          "-" -> total -= right
        }
        opIdx += 2
      } else {
        break
      }
    }
    total
  } catch (_: Exception) {
    null
  }
}

private fun formatCalcNumber(num: Double): String {
  return if (num % 1.0 == 0.0 && num >= -1e12 && num <= 1e12) {
    String.format(Locale.US, "%,.0f", num)
  } else {
    String.format(Locale.US, "%,.2f", num).trimEnd('0').trimEnd('.')
  }
}


