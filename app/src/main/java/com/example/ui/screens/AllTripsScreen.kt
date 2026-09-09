package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.ExportFormat
import com.example.data.export.ExportScope
import com.example.data.model.TripEntity
import com.example.ui.components.EditTripDialog
import com.example.ui.components.ExportDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.LossRed
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.TripFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class YearDirectory(
  val year: Int,
  val totalTrips: Int,
  val totalProfit: Double,
  val monthsCount: Int
)

data class MonthDirectory(
  val year: Int,
  val monthIndex: Int, // 1..12
  val monthName: String,
  val trips: List<TripEntity>,
  val totalProfit: Double
)

@Composable
fun AllTripsScreen(
  language: AppLanguage,
  trips: List<TripEntity>,
  allTrips: List<TripEntity> = trips,
  searchQuery: String = "",
  activeFilter: TripFilter = TripFilter.ALL,
  initialYear: Int? = null,
  initialMonth: Int? = null,
  onSearchChange: (String) -> Unit = {},
  onFilterChange: (TripFilter) -> Unit = {},
  onDeleteTrip: (TripEntity) -> Unit,
  onUpdateTrip: (TripEntity) -> Unit = {},
  onAddNewTrip: () -> Unit = {},
  onSettingsClick: () -> Unit = {},
  onExportTrips: (format: ExportFormat, tripsToExport: List<TripEntity>) -> Unit = { _, _ -> }
) {
  val bengaliMonths = listOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
  )
  val englishMonths = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  )

  // 1-based month index: 1 = January .. 12 = December
  val normalizedInitialMonth = initialMonth?.let { if (it in 0..11) it + 1 else it }

  // Navigation states within Directory hierarchy:
  // selectedYear == null -> Root (Year folders)
  // selectedYear != null && selectedMonth == null -> Sub-directory (Month files)
  // selectedYear != null && selectedMonth != null -> Inside File (Monthly trip list)
  var selectedYear by remember(initialYear) { mutableStateOf<Int?>(initialYear) }
  var selectedMonth by remember(normalizedInitialMonth) { mutableStateOf<Int?>(normalizedInitialMonth) }
  var selectedMonthName by remember(normalizedInitialMonth, language) {
    mutableStateOf<String?>(
      normalizedInitialMonth?.let { m ->
        val idx = (m - 1).coerceIn(0, 11)
        if (language == AppLanguage.BANGLA) bengaliMonths[idx] else englishMonths[idx]
      }
    )
  }

  var tripToDelete by remember { mutableStateOf<TripEntity?>(null) }
  var tripToEdit by remember { mutableStateOf<TripEntity?>(null) }
  var showExportDialog by remember { mutableStateOf(false) }
  var searchWithinMonth by remember { mutableStateOf("") }

  // Helper to parse Year & Month from TripEntity
  fun getTripCalendar(trip: TripEntity): Calendar {
    val cal = Calendar.getInstance()
    if (trip.dateMillis > 0) {
      cal.timeInMillis = trip.dateMillis
    } else {
      val formats = listOf(
        SimpleDateFormat("dd MMM yyyy", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("dd-MM-yyyy", Locale.US),
        SimpleDateFormat("d MMM yyyy", Locale.US),
        SimpleDateFormat("d MMMM yyyy", Locale.US),
        SimpleDateFormat("dd MMMM yyyy", Locale.US),
        SimpleDateFormat("yyyy/MM/dd", Locale.US)
      )
      for (sdf in formats) {
        try {
          sdf.isLenient = true
          val parsed = sdf.parse(trip.dateString.trim())
          if (parsed != null) {
            cal.time = parsed
            break
          }
        } catch (_: Exception) {}
      }
    }
    return cal
  }

  fun getMonthDisplayName(monthIndex: Int): String {
    val idx = (monthIndex - 1).coerceIn(0, 11)
    return if (language == AppLanguage.BANGLA) bengaliMonths[idx] else englishMonths[idx]
  }

  // 1. Group all trips by Year
  val yearDirectories by remember(allTrips) {
    derivedStateOf {
      val map = mutableMapOf<Int, MutableList<TripEntity>>()
      allTrips.forEach { trip ->
        val yr = getTripCalendar(trip).get(Calendar.YEAR)
        map.getOrPut(yr) { mutableListOf() }.add(trip)
      }
      if (map.isEmpty()) {
        val currentYr = Calendar.getInstance().get(Calendar.YEAR)
        map[currentYr] = mutableListOf()
      }
      map.entries.map { (yr, list) ->
        val monthsSet = list.map { getTripCalendar(it).get(Calendar.MONTH) + 1 }.toSet()
        val totalProfit = list.sumOf { it.profit }
        YearDirectory(
          year = yr,
          totalTrips = list.size,
          totalProfit = totalProfit,
          monthsCount = monthsSet.size.coerceAtLeast(if (list.isEmpty()) 0 else 1)
        )
      }.sortedByDescending { it.year }
    }
  }

  // 2. Group trips of selected Year by Month
  val monthDirectories by remember(allTrips, selectedYear) {
    derivedStateOf {
      val yr = selectedYear ?: return@derivedStateOf emptyList<MonthDirectory>()
      val tripsInYear = allTrips.filter { getTripCalendar(it).get(Calendar.YEAR) == yr }
      val map = mutableMapOf<Int, MutableList<TripEntity>>()
      tripsInYear.forEach { trip ->
        val m = getTripCalendar(trip).get(Calendar.MONTH) + 1
        map.getOrPut(m) { mutableListOf() }.add(trip)
      }
      if (map.isEmpty()) {
        val currentM = Calendar.getInstance().get(Calendar.MONTH) + 1
        map[currentM] = mutableListOf()
      }
      map.entries.map { (mIdx, list) ->
        MonthDirectory(
          year = yr,
          monthIndex = mIdx,
          monthName = getMonthDisplayName(mIdx),
          trips = list.sortedByDescending { it.id },
          totalProfit = list.sumOf { it.profit }
        )
      }.sortedByDescending { it.monthIndex }
    }
  }

  fun matchesSearch(trip: TripEntity, rawQuery: String): Boolean {
    val q = rawQuery.trim()
    if (q.isBlank()) return true

    if (trip.place.contains(q, ignoreCase = true) ||
        trip.description.contains(q, ignoreCase = true) ||
        trip.dateString.contains(q, ignoreCase = true)) {
      return true
    }

    val normalizedQuery = q.map { ch ->
      when (ch) {
        '০' -> '0'; '১' -> '1'; '২' -> '2'; '৩' -> '3'; '৪' -> '4'
        '৫' -> '5'; '৬' -> '6'; '৭' -> '7'; '৮' -> '8'; '৯' -> '9'
        else -> ch
      }
    }.joinToString("")

    if (normalizedQuery != q) {
      if (trip.place.contains(normalizedQuery, ignoreCase = true) ||
          trip.description.contains(normalizedQuery, ignoreCase = true) ||
          trip.dateString.contains(normalizedQuery, ignoreCase = true)) {
        return true
      }
    }

    val cal = getTripCalendar(trip)
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH) + 1
    val year = cal.get(Calendar.YEAR)

    val bMonth = when (month) {
      1 -> "জানুয়ারি"; 2 -> "ফেব্রুয়ারি"; 3 -> "মার্চ"; 4 -> "এপ্রিল"
      5 -> "মে"; 6 -> "জুন"; 7 -> "জুলাই"; 8 -> "আগস্ট"
      9 -> "সেপ্টেম্বর"; 10 -> "অক্টোবর"; 11 -> "নভেম্বর"; 12 -> "ডিসেম্বর"
      else -> ""
    }
    val bMonthAlt = if (month in 1..12) bengaliMonths[month - 1] else ""
    val eMonth = if (month in 1..12) englishMonths[month - 1] else ""

    if (bMonth.contains(q, ignoreCase = true) || bMonthAlt.contains(q, ignoreCase = true) || eMonth.contains(q, ignoreCase = true)) {
      return true
    }

    val d1 = String.format(Locale.US, "%02d/%02d/%04d", day, month, year)
    val d2 = String.format(Locale.US, "%02d-%02d-%04d", day, month, year)
    val d3 = String.format(Locale.US, "%d/%d", day, month)
    val d4 = String.format(Locale.US, "%02d/%02d", day, month)
    val d5 = String.format(Locale.US, "%d %s", day, eMonth.take(3))
    val d6 = "$day $bMonthAlt"

    val qLower = normalizedQuery.lowercase()
    if (d1.contains(qLower) || d2.contains(qLower) || d3.contains(qLower) ||
        d4.contains(qLower) || d5.lowercase().contains(qLower) || d6.lowercase().contains(q.lowercase()) ||
        year.toString().contains(qLower)) {
      return true
    }

    return false
  }

  // Global search across all trips
  val globalSearchResults by remember(allTrips, searchQuery) {
    derivedStateOf {
      if (searchQuery.isBlank()) emptyList()
      else allTrips.filter { matchesSearch(it, searchQuery) }.sortedByDescending { it.id }
    }
  }

  // 3. Trips in Selected Month File
  val tripsInCurrentMonth by remember(allTrips, selectedYear, selectedMonth, searchWithinMonth) {
    derivedStateOf {
      val yr = selectedYear ?: return@derivedStateOf emptyList<TripEntity>()
      val m = selectedMonth ?: return@derivedStateOf emptyList<TripEntity>()
      allTrips.filter { trip ->
        val cal = getTripCalendar(trip)
        cal.get(Calendar.YEAR) == yr && (cal.get(Calendar.MONTH) + 1) == m
      }.filter { trip ->
        if (searchWithinMonth.isBlank()) true
        else matchesSearch(trip, searchWithinMonth)
      }.sortedByDescending { it.id }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("all_trips_screen_root")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Directory Breadcrumb & Navigation Bar
      Surface(
        color = DarkGreenSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (searchQuery.isNotBlank()) {
                IconButton(
                  onClick = { onSearchChange("") },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Clear Search",
                    tint = MintGreenAccent
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
              } else if (selectedYear != null) {
                IconButton(
                  onClick = {
                    if (selectedMonth != null) {
                      selectedMonth = null
                      selectedMonthName = null
                      searchWithinMonth = ""
                    } else {
                      selectedYear = null
                    }
                  },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MintGreenAccent
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
              } else {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MintGreenAccent.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = null,
                    tint = MintGreenAccent,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
              }

              Column {
                Text(
                  text = when {
                    searchQuery.isNotBlank() -> if (language == AppLanguage.BANGLA) "সার্চ ফলাফল" else "Search Results"
                    selectedMonth != null -> "$selectedMonthName $selectedYear"
                    selectedYear != null -> "${selectedYear} সালের ট্রিপ ফাইল"
                    else -> if (language == AppLanguage.BANGLA) "ট্রিপ আর্কাইভ ডিরেক্টরি" else "Trip Archive Directory"
                  },
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )

                // Breadcrumb trail
                Text(
                  text = if (searchQuery.isNotBlank()) {
                    "🔍 '$searchQuery' (${globalSearchResults.size}টি ট্রিপ)"
                  } else {
                    buildString {
                      append("📁 রুট")
                      if (selectedYear != null) append(" / 📁 $selectedYear")
                      if (selectedMonthName != null) append(" / 📄 $selectedMonthName")
                    }
                  },
                  fontSize = 11.sp,
                  color = MintGreenAccent,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              // Export Action if searching or inside a Month
              val canExport = (searchQuery.isNotBlank() && globalSearchResults.isNotEmpty()) ||
                  (searchQuery.isBlank() && selectedMonth != null && tripsInCurrentMonth.isNotEmpty())
              if (canExport) {
                IconButton(
                  onClick = { showExportDialog = true },
                  modifier = Modifier
                    .size(36.dp)
                    .testTag("export_month_trips_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export Trips",
                    tint = MintGreenAccent
                  )
                }
              }
            }
          }
        }
      }

      // Persistent Search Bar for All Trips (Date or Place Name)
      Surface(
        color = DarkGreenSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchChange,
          placeholder = {
            Text(
              text = if (language == AppLanguage.BANGLA)
                "তারিখ (যেমন: 05 Sep, মে) বা জায়গার নাম..."
              else
                "Search date (e.g. 05 Sep, May) or place...",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8)
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MintGreenAccent,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChange("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear Search",
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkGreenCard,
            unfocusedContainerColor = DarkGreenCard,
            focusedBorderColor = MintGreenAccent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("all_trips_global_search_bar")
        )
      }

      // MAIN CONTENT AREA: Search Results OR Directory Hierarchy
      if (searchQuery.isNotBlank()) {
        val totalSearchRent = globalSearchResults.sumOf { it.rent }
        val totalSearchProfit = globalSearchResults.sumOf { it.profit }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("all_trips_search_results_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Search Results Summary Banner
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("search_results_summary_card"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.4f))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MintGreenAccent.copy(alpha = 0.15f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "সার্চ ফলাফল" else "Search Results",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                      Text(
                        text = if (language == AppLanguage.BANGLA)
                          "মোট ${globalSearchResults.size}টি ট্রিপ পাওয়া গেছে"
                        else
                          "Found ${globalSearchResults.size} trips",
                        fontSize = 11.sp,
                        color = MintGreenAccent
                      )
                    }
                  }

                  TextButton(
                    onClick = { onSearchChange("") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = if (language == AppLanguage.BANGLA) "ক্লিয়ার" else "Clear",
                      color = Color(0xFF94A3B8),
                      fontSize = 12.sp
                    )
                  }
                }

                if (globalSearchResults.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(10.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = DarkGreenCard,
                      modifier = Modifier.weight(1f)
                    ) {
                      Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                          text = if (language == AppLanguage.BANGLA) "মোট ভাড়া" else "Total Rent",
                          fontSize = 10.sp,
                          color = Color(0xFF94A3B8)
                        )
                        Text(
                          text = "৳" + String.format(Locale.US, "%,.0f", totalSearchRent),
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color.White
                        )
                      }
                    }

                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = DarkGreenCard,
                      modifier = Modifier.weight(1f)
                    ) {
                      Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                          text = if (language == AppLanguage.BANGLA) "নীট প্রফিট" else "Net Profit",
                          fontSize = 10.sp,
                          color = Color(0xFF94A3B8)
                        )
                        Text(
                          text = "৳" + String.format(Locale.US, "%,.0f", totalSearchProfit),
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (totalSearchProfit >= 0) ProfitGreen else LossRed
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          if (globalSearchResults.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(48.dp)
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA)
                      "'$searchQuery' দিয়ে কোনো ট্রিপ পাওয়া যায়নি"
                    else
                      "No trips found matching '$searchQuery'",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA)
                      "তারিখ (যেমন: 05 Sep, মে, ২০২৬) অথবা জায়গার নাম দিয়ে আবার চেষ্টা করুন।"
                    else
                      "Try searching by date (e.g. 05 Sep, May) or place name.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                  )
                }
              }
            }
          } else {
            items(globalSearchResults) { trip ->
              val tripProfit = trip.profit
              val isTripProfit = tripProfit >= 0
              val tripCal = getTripCalendar(trip)
              val tripYear = tripCal.get(Calendar.YEAR)
              val tripMonth = tripCal.get(Calendar.MONTH) + 1
              val tripMonthName = getMonthDisplayName(tripMonth)

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("search_trip_card_${trip.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  // Top Row: Place & Date
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f)
                    ) {
                      Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = trip.place,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }

                    // Date Badge
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = DarkGreenCard
                    ) {
                      Text(
                        text = trip.dateString,
                        fontSize = 11.sp,
                        color = MintGreenAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  // Folder indicator pill (clickable to jump directly to month folder)
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MintGreenAccent.copy(alpha = 0.10f),
                    modifier = Modifier.clickable {
                      onSearchChange("")
                      selectedYear = tripYear
                      selectedMonth = tripMonth
                      selectedMonthName = tripMonthName
                    }
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "ফোল্ডার: $tripMonthName $tripYear (খুলতে ট্যাপ করুন)",
                        fontSize = 10.sp,
                        color = MintGreenAccent,
                        fontWeight = FontWeight.Medium
                      )
                    }
                  }

                  if (trip.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = trip.description,
                      fontSize = 12.sp,
                      color = Color(0xFFCBD5E1),
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  // Financial Details Row
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column {
                      Text(
                        text = "ভাড়া: ৳${String.format(Locale.US, "%,.0f", trip.rent)}",
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold
                      )
                      if (trip.kmDriven > 0) {
                        Text(
                          text = "${String.format(Locale.US, "%.1f", trip.kmDriven)} কিমি",
                          fontSize = 11.sp,
                          color = Color(0xFF94A3B8)
                        )
                      }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTripProfit) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
                      ) {
                        Text(
                          text = (if (isTripProfit) "লাভ: +৳" else "লোকসান: -৳") +
                            String.format(Locale.US, "%,.0f", kotlin.math.abs(tripProfit)),
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isTripProfit) ProfitGreen else LossRed,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }

                      Spacer(modifier = Modifier.width(4.dp))

                      IconButton(
                        onClick = { tripToEdit = trip },
                        modifier = Modifier
                          .size(32.dp)
                          .testTag("btn_edit_search_trip_${trip.id}")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Edit,
                          contentDescription = "Edit",
                          tint = MintGreenAccent,
                          modifier = Modifier.size(17.dp)
                        )
                      }

                      IconButton(
                        onClick = { tripToDelete = trip },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "Delete",
                          tint = Color(0xFF94A3B8),
                          modifier = Modifier.size(18.dp)
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

      // LEVEL 0: ROOT DIRECTORY (Year Folders)
      else if (selectedYear == null) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("root_year_folders_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Text(
              text = if (language == AppLanguage.BANGLA) "বছর অনুযায়ী ফোল্ডারসমূহ:" else "Folders by Year:",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF94A3B8),
              modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
          }

          items(yearDirectories) { yDir ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { selectedYear = yDir.year }
                .testTag("year_folder_${yDir.year}"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MintGreenAccent.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = "Year Folder",
                    tint = MintGreenAccent,
                    modifier = Modifier.size(30.dp)
                  )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${yDir.year} সাল",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = "${yDir.totalTrips} টি ট্রিপ • ${yDir.monthsCount} টি মাসের ফাইল",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "৳" + String.format(Locale.US, "%,.0f", yDir.totalProfit),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (yDir.totalProfit >= 0) ProfitGreen else LossRed
                  )
                  Text(
                    text = "প্রফিট",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1)
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  tint = MintGreenAccent,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      // LEVEL 1: SUB-DIRECTORY (Monthly Files inside Selected Year)
      else if (selectedMonth == null) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("sub_directory_months_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Text(
              text = if (language == AppLanguage.BANGLA)
                "${selectedYear} সালের মাসিক ফাইলসমূহ (ট্যাপ করে ট্রিপ দেখুন):"
              else
                "Monthly files for $selectedYear (Tap to open trips):",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF94A3B8),
              modifier = Modifier.padding(bottom = 4.dp)
            )
          }

          items(monthDirectories) { mDir ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                  selectedMonth = mDir.monthIndex
                  selectedMonthName = mDir.monthName
                }
                .testTag("month_file_${mDir.monthIndex}"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGreenCard),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Month File",
                    tint = MintGreenAccent,
                    modifier = Modifier.size(26.dp)
                  )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${mDir.monthName} $selectedYear",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "${mDir.trips.size} টি এন্ট্রি ফাইল",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "৳" + String.format(Locale.US, "%,.0f", mDir.totalProfit),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (mDir.totalProfit >= 0) ProfitGreen else LossRed
                  )
                  Text(
                    text = "মাসিক প্রফিট",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1)
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  tint = MintGreenAccent,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }

      // LEVEL 2: INSIDE FILE (List of Specific Trip Entries for this exact month only)
      else {
        val totalMonthRent = tripsInCurrentMonth.sumOf { it.rent }
        val totalMonthProfit = tripsInCurrentMonth.sumOf { it.profit }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("month_trips_entries_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Month Folder Header Card with Download Option
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("month_folder_summary_card"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, MintGreenAccent.copy(alpha = 0.4f))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MintGreenAccent.copy(alpha = 0.15f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(24.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(
                        text = "$selectedMonthName $selectedYear",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                      Text(
                        text = if (language == AppLanguage.BANGLA)
                          "মোট ${tripsInCurrentMonth.size}টি ট্রিপ রেকর্ড"
                        else
                          "Total ${tripsInCurrentMonth.size} trip records",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                      )
                    }
                  }

                  // Quick Download Icon in Card
                  if (tripsInCurrentMonth.isNotEmpty()) {
                    IconButton(
                      onClick = { showExportDialog = true },
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryEmerald.copy(alpha = 0.2f))
                        .testTag("month_card_download_icon_button")
                    ) {
                      Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Download Month Report",
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkGreenCard,
                    modifier = Modifier.weight(1f)
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "মোট ভাড়া" else "Total Rent",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                      )
                      Text(
                        text = "৳" + String.format(Locale.US, "%,.0f", totalMonthRent),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkGreenCard,
                    modifier = Modifier.weight(1f)
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "নীট প্রফিট" else "Net Profit",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                      )
                      Text(
                        text = "৳" + String.format(Locale.US, "%,.0f", totalMonthProfit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalMonthProfit >= 0) ProfitGreen else LossRed
                      )
                    }
                  }
                }

                if (tripsInCurrentMonth.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(12.dp))

                  Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = PrimaryEmerald,
                      contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(44.dp)
                      .testTag("download_month_statement_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.FileDownload,
                      contentDescription = null,
                      tint = Color.Black,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (language == AppLanguage.BANGLA)
                        "এই মাসের হিসাব ডাউনলোড (PDF ও CSV)"
                      else
                        "Download $selectedMonthName Statement (PDF / CSV)",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }

          // Month Search & Summary
          item {
            OutlinedTextField(
              value = searchWithinMonth,
              onValueChange = { searchWithinMonth = it },
              placeholder = { Text("গন্তব্য বা স্থান খুঁজুন...", fontSize = 13.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MintGreenAccent) },
              trailingIcon = {
                if (searchWithinMonth.isNotEmpty()) {
                  IconButton(onClick = { searchWithinMonth = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkGreenSurface,
                unfocusedContainerColor = DarkGreenSurface,
                focusedBorderColor = MintGreenAccent,
                unfocusedBorderColor = DarkGreenBorder,
                focusedTextColor = Color.White
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("month_trips_search_bar")
            )
          }

          if (tripsInCurrentMonth.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(48.dp)
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = if (language == AppLanguage.BANGLA)
                      "এই মাসে কোনো ট্রিপ এন্ট্রি নেই"
                    else
                      "No trip entries found in this month",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                  )
                }
              }
            }
          } else {
            items(tripsInCurrentMonth) { trip ->
              val tripProfit = trip.profit
              val isTripProfit = tripProfit >= 0

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("trip_entry_card_${trip.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenBorder)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  // Top Row: Route & Date
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f)
                    ) {
                      Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = trip.place,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }

                    Text(
                      text = trip.dateString,
                      fontSize = 11.sp,
                      color = Color(0xFF94A3B8)
                    )
                  }

                  if (trip.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = trip.description,
                      fontSize = 12.sp,
                      color = Color(0xFFCBD5E1),
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  // Financial Details Row (Rent and Net Profit; NO total expense shown)
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column {
                      Text(
                        text = "ভাড়া: ৳${String.format(Locale.US, "%,.0f", trip.rent)}",
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold
                      )
                      if (trip.kmDriven > 0) {
                        Text(
                          text = "${String.format(Locale.US, "%.1f", trip.kmDriven)} কিমি",
                          fontSize = 11.sp,
                          color = Color(0xFF94A3B8)
                        )
                      }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTripProfit) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
                      ) {
                        Text(
                          text = (if (isTripProfit) "লাভ: +৳" else "লোকসান: -৳") +
                            String.format(Locale.US, "%,.0f", kotlin.math.abs(tripProfit)),
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isTripProfit) ProfitGreen else LossRed,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }

                      Spacer(modifier = Modifier.width(4.dp))

                      IconButton(
                        onClick = { tripToEdit = trip },
                        modifier = Modifier
                          .size(32.dp)
                          .testTag("btn_edit_trip_${trip.id}")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Edit,
                          contentDescription = "Edit Trip",
                          tint = MintGreenAccent,
                          modifier = Modifier.size(17.dp)
                        )
                      }

                      IconButton(
                        onClick = { tripToDelete = trip },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "Delete",
                          tint = Color(0xFF94A3B8),
                          modifier = Modifier.size(18.dp)
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

    // Delete Confirmation Dialog
    tripToDelete?.let { trip ->
      AlertDialog(
        onDismissRequest = { tripToDelete = null },
        containerColor = DarkGreenSurface,
        title = {
          Text(
            text = if (language == AppLanguage.BANGLA) "ট্রিপ মুছে ফেলবেন?" else "Delete Trip?",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Text(
            text = if (language == AppLanguage.BANGLA)
              "আপনি কি নিশ্চিত যে '${trip.place}' ট্রিপটি মুছে ফেলতে চান?"
            else
              "Are you sure you want to delete '${trip.place}'?",
            color = Color(0xFFCBD5E1)
          )
        },
        confirmButton = {
          Button(
            onClick = {
              onDeleteTrip(trip)
              tripToDelete = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = LossRed),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(if (language == AppLanguage.BANGLA) "মুছুন" else "Delete", color = Color.White)
          }
        },
        dismissButton = {
          TextButton(onClick = { tripToDelete = null }) {
            Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = Color(0xFF94A3B8))
          }
        }
      )
    }

    // Edit Trip Dialog
    tripToEdit?.let { trip ->
      EditTripDialog(
        trip = trip,
        language = language,
        isMaintenanceMode = (trip.rent == 0.0 && trip.maintenanceCost > 0),
        onDismiss = { tripToEdit = null },
        onSaveTrip = { updatedTrip ->
          onUpdateTrip(updatedTrip)
          tripToEdit = null
        }
      )
    }

    // Export Dialog
    if (showExportDialog) {
      val exportList = when {
        searchQuery.isNotBlank() -> globalSearchResults
        selectedMonth != null -> tripsInCurrentMonth
        else -> allTrips
      }
      ExportDialog(
        language = language,
        allTrips = allTrips,
        filteredTrips = exportList,
        initialScope = if (searchQuery.isNotBlank() || selectedMonth != null) ExportScope.FILTERED else ExportScope.CURRENT_MONTH,
        onDismiss = { showExportDialog = false },
        onPerformExport = { format, tripsToExport ->
          showExportDialog = false
          onExportTrips(format, tripsToExport)
        }
      )
    }
  }
}
