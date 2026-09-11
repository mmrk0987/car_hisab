package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.drive.GoogleDriveBackupManager
import com.example.data.export.ExportFormat
import com.example.data.export.ExportManager
import com.example.data.model.BookingEntity
import com.example.data.model.DayTrend
import com.example.data.model.MobilServiceInfo
import com.example.data.model.MonthlySummaryData
import com.example.data.model.TripEntity
import com.example.data.model.VehicleDocuments
import com.example.data.model.WeekTrend
import com.example.data.repository.BookingRepository
import com.example.data.repository.TripRepository
import com.example.data.repository.UserProfile
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
  SPLASH,
  LANGUAGE_SELECT,
  LOGIN,
  NID_VERIFY,
  PROFILE_SETUP,
  DASHBOARD,
  ADD_TRIP,
  ALL_TRIPS,
  SUBSCRIPTION,
  SETTINGS,
  LANGUAGE_SETTINGS,
  DRIVE_BACKUP_SETTINGS,
  THEME_SETTINGS,
  DOCUMENTS_SERVICES,
  BOOKINGS
}

enum class TripFilter {
  ALL, TODAY, THIS_MONTH, PROFIT, LOSS
}

data class DashboardStats(
  val todayIncome: Double = 0.0,
  val monthIncome: Double = 0.0,
  val monthProfit: Double = 0.0,
  val totalTrips: Int = 0,
  val totalKm: Double = 0.0
)

class CarHisabViewModel(application: Application) : AndroidViewModel(application) {
  private val tripRepo: TripRepository
  private val bookingRepo: BookingRepository
  private val userPrefsRepo: UserPreferencesRepository

  init {
    val db = AppDatabase.getDatabase(application)
    tripRepo = TripRepository(db.tripDao())
    bookingRepo = BookingRepository(db.bookingDao())
    userPrefsRepo = UserPreferencesRepository(application)

    viewModelScope.launch {
      tripRepo.clearPassengerNamesFromTrips()
      bookingRepo.deleteDemoBookings()
    }
  }

  // Navigation
  private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _allTripsTargetFolder = MutableStateFlow<Pair<Int, Int>?>(null)
  val allTripsTargetFolder: StateFlow<Pair<Int, Int>?> = _allTripsTargetFolder.asStateFlow()

  fun navigateTo(screen: AppScreen) {
    if (screen != AppScreen.ALL_TRIPS) {
      _allTripsTargetFolder.value = null
    }
    _currentScreen.value = screen
  }

  fun navigateToAllTripsMonth(year: Int, month: Int) {
    _allTripsTargetFolder.value = Pair(year, month)
    _currentScreen.value = AppScreen.ALL_TRIPS
  }

  fun clearAllTripsTargetFolder() {
    _allTripsTargetFolder.value = null
  }

  // User Profile & Preferences
  val language: StateFlow<AppLanguage> = userPrefsRepo.languageFlow
  val themeMode: StateFlow<AppThemeMode> = userPrefsRepo.themeModeFlow
  val userProfile: StateFlow<UserProfile> = userPrefsRepo.profileFlow
  val vehicleDocuments: StateFlow<VehicleDocuments> = userPrefsRepo.documentsFlow
  val mobilServiceInfo: StateFlow<MobilServiceInfo> = userPrefsRepo.mobilServiceFlow
  val emailOtpProvider: StateFlow<String> = userPrefsRepo.emailOtpProviderFlow
  val emailOtpApiKey: StateFlow<String> = userPrefsRepo.emailOtpApiKeyFlow
  val emailOtpWebhookUrl: StateFlow<String> = userPrefsRepo.emailOtpWebhookUrlFlow
  val emailOtpSenderEmail: StateFlow<String> = userPrefsRepo.emailOtpSenderEmailFlow

  fun setEmailOtpProvider(provider: String) {
    userPrefsRepo.setEmailOtpProvider(provider)
  }

  fun setEmailOtpApiKey(apiKey: String) {
    userPrefsRepo.setEmailOtpApiKey(apiKey)
  }

  fun setEmailOtpWebhookUrl(url: String) {
    userPrefsRepo.setEmailOtpWebhookUrl(url)
  }

  fun setEmailOtpSenderEmail(sender: String) {
    userPrefsRepo.setEmailOtpSenderEmail(sender)
  }

  fun setLanguage(lang: AppLanguage) {
    userPrefsRepo.setLanguage(lang)
  }

  fun setThemeMode(mode: AppThemeMode) {
    userPrefsRepo.setThemeMode(mode)
  }

  fun updateProfile(
    carName: String,
    carModel: String,
    carNumber: String,
    driverName: String,
    phone: String,
    email: String,
    profilePhotoUri: String? = userProfile.value.profileImageUri
  ) {
    val current = userProfile.value
    userPrefsRepo.updateProfile(
      current.copy(
        carName = carName,
        carModel = carModel,
        carNumber = carNumber,
        driverName = driverName,
        driverNameBangla = driverName,
        driverPhone = phone,
        driverEmail = email,
        profileImageUri = profilePhotoUri,
        isProfileCompleted = true
      )
    )
  }

  fun updateProfilePhoto(photoUri: String?) {
    val current = userProfile.value
    userPrefsRepo.updateProfile(
      current.copy(profileImageUri = photoUri)
    )
  }

  fun saveProfilePhotoFromUri(context: Context, sourceUri: Uri): String? {
    return try {
      val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
      val photosDir = File(context.filesDir, "profile_photos").apply { mkdirs() }
      val destFile = File(photosDir, "user_avatar_${System.currentTimeMillis()}.jpg")
      destFile.outputStream().use { out ->
        inputStream.copyTo(out)
      }
      val uriString = destFile.absolutePath
      updateProfilePhoto(uriString)
      uriString
    } catch (e: Exception) {
      null
    }
  }

  fun updateFullProfile(
    nameBangla: String,
    nameEnglish: String,
    birthdate: String,
    phone: String,
    email: String,
    carName: String = userProfile.value.carName,
    carModel: String = userProfile.value.carModel,
    carNumber: String = userProfile.value.carNumber
  ) {
    val current = userProfile.value
    userPrefsRepo.updateProfile(
      current.copy(
        driverName = if (nameBangla.isNotBlank()) nameBangla else nameEnglish,
        driverNameBangla = nameBangla,
        driverNameEnglish = nameEnglish,
        birthDate = birthdate,
        driverPhone = phone,
        driverEmail = email,
        carName = carName,
        carModel = carModel,
        carNumber = carNumber,
        isProfileCompleted = true,
        isLoggedIn = true
      )
    )
  }

  fun updateRememberEmail(email: String, remember: Boolean) {
    val current = userProfile.value
    userPrefsRepo.updateProfile(
      current.copy(
        rememberEmail = remember,
        savedEmail = if (remember) email else "",
        driverEmail = if (current.driverEmail.isBlank()) email else current.driverEmail,
        isLoggedIn = true
      )
    )
  }

  fun logout() {
    val current = userProfile.value
    userPrefsRepo.updateProfile(
      current.copy(isLoggedIn = false)
    )
    _currentScreen.value = AppScreen.LOGIN
  }

  fun updateNidStatus(front: Boolean, back: Boolean, selfie: Boolean) {
    val current = userProfile.value
    userPrefsRepo.updateProfile(
      current.copy(
        nidFrontAttached = front,
        nidBackAttached = back,
        selfieAttached = selfie
      )
    )
  }

  fun toggleSubscriptionDemo() {
    val current = userProfile.value
    val newActive = !current.isFreeTrialActive
    val now = System.currentTimeMillis()
    val newExpiry = if (newActive) now + (180L * 24 * 60 * 60 * 1000) else now - 1000L
    userPrefsRepo.updateProfile(
      current.copy(
        isFreeTrialActive = newActive,
        subscriptionExpiryMillis = newExpiry,
        trialMonthsRemaining = if (newActive) 6 else 0
      )
    )
  }

  fun activateWithAdminKey(
    keyInput: String,
    onResult: (success: Boolean, message: String) -> Unit
  ) {
    val cleanKey = keyInput.trim().uppercase()
    if (cleanKey.isEmpty()) {
      onResult(false, "দয়া করে অ্যাক্টিভেশন কোডটি লিখুন।")
      return
    }

    val current = userProfile.value
    val now = System.currentTimeMillis()

    val (durationDays, planTitle) = when {
      cleanKey == "CH-30-M100" || cleanKey == "MMRK-MONTH-100" || cleanKey == "PROMO-30" || cleanKey == "MONTH100" -> {
        Pair(30, "১ মাসের রেগুলার প্ল্যান (৳১০০)")
      }
      cleanKey == "CH-180-H500" || cleanKey == "MMRK-HALF-500" || cleanKey == "PROMO-180" || cleanKey == "HALF500" -> {
        Pair(180, "৬ মাসের হাফ-ইয়ারলি প্ল্যান (৳৫০) ")
      }
      cleanKey == "CH-365-Y950" || cleanKey == "MMRK-YEAR-950" || cleanKey == "MMRK-ADMIN-VIP" || cleanKey == "PROMO-365" || cleanKey == "YEAR950" -> {
        Pair(365, "১ বছরের মেগা সেভার প্ল্যান (৳৯৫০)")
      }
      cleanKey.startsWith("CH-") && cleanKey.contains("-VIP") -> {
        val days = cleanKey.split("-").getOrNull(1)?.toIntOrNull() ?: 30
        Pair(days, "$days দিনের প্রিমিয়াম প্ল্যান")
      }
      else -> {
        Pair(0, "")
      }
    }

    if (durationDays > 0) {
      val durationMillis = durationDays.toLong() * 24 * 60 * 60 * 1000L
      val newExpiry = maxOf(current.subscriptionExpiryMillis, now) + durationMillis
      val monthsRemaining = maxOf(1, durationDays / 30)

      userPrefsRepo.updateProfile(
        current.copy(
          isFreeTrialActive = true,
          subscriptionExpiryMillis = newExpiry,
          trialMonthsRemaining = monthsRemaining,
          currentPlanName = planTitle
        )
      )
      onResult(true, "অভিনন্দন! আপনার $planTitle সফলভাবে সক্রিয় হয়েছে।")
    } else {
      onResult(false, "অবৈধ অ্যাক্টিভেশন কোড! এডমিনের থেকে সঠিক কোড সংগ্রহ করুন।")
    }
  }

  // Trips from Room
  val allTrips: StateFlow<List<TripEntity>> = tripRepo.allTrips
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Bookings from Room
  val allBookings: StateFlow<List<BookingEntity>> = bookingRepo.allBookings
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val upcomingBookings: StateFlow<List<BookingEntity>> = bookingRepo.upcomingBookings
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Booking Actions
  fun addBooking(
    passengerName: String,
    passengerPhone: String,
    pickupLocation: String,
    dropLocation: String,
    tripDateMillis: Long,
    tripTimeString: String,
    totalFare: Double,
    advancePaid: Double,
    notes: String
  ) {
    viewModelScope.launch {
      val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(tripDateMillis))
      val booking = BookingEntity(
        passengerName = passengerName.trim(),
        passengerPhone = passengerPhone.trim(),
        pickupLocation = pickupLocation.trim(),
        dropLocation = dropLocation.trim(),
        tripDateMillis = tripDateMillis,
        tripDateString = dateStr,
        tripTimeString = tripTimeString.trim(),
        totalFare = totalFare,
        advancePaid = advancePaid,
        dueFare = totalFare - advancePaid,
        status = "CONFIRMED",
        notes = notes.trim()
      )
      bookingRepo.insertBooking(booking)
    }
  }

  fun updateBookingStatus(bookingId: Long, newStatus: String) {
    viewModelScope.launch {
      val currentList = allBookings.value
      val target = currentList.find { it.id == bookingId }
      if (target != null) {
        bookingRepo.updateBooking(target.copy(status = newStatus))
      }
    }
  }

  fun deleteBooking(bookingId: Long) {
    viewModelScope.launch {
      bookingRepo.deleteBookingById(bookingId)
    }
  }

  fun convertBookingToTrip(
    booking: BookingEntity,
    gratuity: Double = 0.0,
    maintenanceCost: Double = 0.0,
    kmDriven: Double = 0.0,
    onCompleted: () -> Unit = {}
  ) {
    viewModelScope.launch {
      bookingRepo.updateBooking(booking.copy(status = "COMPLETED"))

      val rent = booking.totalFare
      val income = rent - gratuity
      val profit = income - maintenanceCost
      val trip = TripEntity(
        dateMillis = System.currentTimeMillis(),
        dateString = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
        place = "${booking.pickupLocation} টু ${booking.dropLocation}",
        rent = rent,
        gratuity = gratuity,
        maintenanceCost = maintenanceCost,
        kmDriven = kmDriven,
        description = if (booking.notes.isNotBlank()) "বুকিং আইডি #${booking.id} - ${booking.notes}" else "বুকিং আইডি #${booking.id}",
        income = income,
        profit = profit
      )
      tripRepo.insertTrip(trip)

      if (kmDriven > 0) {
        val currentService = mobilServiceInfo.value
        userPrefsRepo.updateMobilService(
          currentService.copy(currentOdometerKm = currentService.currentOdometerKm + kmDriven)
        )
      }

      onCompleted()
    }
  }

  // Vehicle Documents & Mobil Actions
  fun updateVehicleDocuments(docs: VehicleDocuments) {
    userPrefsRepo.updateDocuments(docs)
  }

  fun updateMobilService(service: MobilServiceInfo) {
    userPrefsRepo.updateMobilService(service)
  }

  fun logMobilChanged(newKm: Double, brand: String, cost: Double = 0.0) {
    val current = mobilServiceInfo.value
    val updated = current.copy(
      currentOdometerKm = maxOf(current.currentOdometerKm, newKm),
      lastMobilChangeKm = newKm,
      lastMobilChangeDateMillis = System.currentTimeMillis(),
      mobilBrandGrade = brand.ifBlank { current.mobilBrandGrade }
    )
    userPrefsRepo.updateMobilService(updated)

    if (cost > 0) {
      viewModelScope.launch {
        val trip = TripEntity(
          dateMillis = System.currentTimeMillis(),
          dateString = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
          place = "গাড়ির ইঞ্জিন অয়েল (মবিল) পরিবর্তন",
          rent = 0.0,
          gratuity = 0.0,
          maintenanceCost = cost,
          kmDriven = 0.0,
          description = "মবিল ব্র্যান্ড: $brand (${newKm.toInt()} কিমি)",
          income = 0.0,
          profit = -cost
        )
        tripRepo.insertTrip(trip)
      }
    }
  }

  fun updateOdometer(newKm: Double) {
    val current = mobilServiceInfo.value
    userPrefsRepo.updateMobilService(current.copy(currentOdometerKm = newKm))
  }

  // Filter & Search in All Trips Screen
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _activeFilter = MutableStateFlow(TripFilter.ALL)
  val activeFilter: StateFlow<TripFilter> = _activeFilter.asStateFlow()

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setActiveFilter(filter: TripFilter) {
    _activeFilter.value = filter
  }

  private fun matchesTripSearch(trip: TripEntity, rawQuery: String): Boolean {
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

    val cal = Calendar.getInstance().apply {
      if (trip.dateMillis > 0) timeInMillis = trip.dateMillis
    }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH) + 1
    val year = cal.get(Calendar.YEAR)

    val englishMonths = listOf(
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
    )
    val bMonth = when (month) {
      1 -> "জানুয়ারি"; 2 -> "ফেব্রুয়ারি"; 3 -> "মার্চ"; 4 -> "এপ্রিল"
      5 -> "মে"; 6 -> "জুন"; 7 -> "জুলাই"; 8 -> "আগস্ট"
      9 -> "সেপ্টেম্বর"; 10 -> "অক্টোবর"; 11 -> "নভেম্বর"; 12 -> "ডিসেম্বর"
      else -> ""
    }
    val eMonth = if (month in 1..12) englishMonths[month - 1] else ""

    if (bMonth.contains(q, ignoreCase = true) || eMonth.contains(q, ignoreCase = true)) {
      return true
    }

    val d1 = String.format(Locale.US, "%02d/%02d/%04d", day, month, year)
    val d2 = String.format(Locale.US, "%02d-%02d-%04d", day, month, year)
    val d3 = String.format(Locale.US, "%d/%d", day, month)
    val d4 = String.format(Locale.US, "%02d/%02d", day, month)
    val d5 = String.format(Locale.US, "%d %s", day, eMonth.take(3))
    val d6 = "$day $bMonth"

    val qLower = normalizedQuery.lowercase()
    if (d1.contains(qLower) || d2.contains(qLower) || d3.contains(qLower) ||
        d4.contains(qLower) || d5.lowercase().contains(qLower) || d6.lowercase().contains(q.lowercase()) ||
        year.toString().contains(qLower)) {
      return true
    }

    return false
  }

  val filteredTrips: StateFlow<List<TripEntity>> = combine(
    allTrips,
    _searchQuery,
    _activeFilter
  ) { trips, query, filter ->
    val cal = Calendar.getInstance()
    val todayYear = cal.get(Calendar.YEAR)
    val todayMonth = cal.get(Calendar.MONTH)
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)

    trips.filter { trip ->
      val tripCal = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      val isToday = tripCal.get(Calendar.YEAR) == todayYear &&
          tripCal.get(Calendar.MONTH) == todayMonth &&
          tripCal.get(Calendar.DAY_OF_MONTH) == todayDay
      val isThisMonth = tripCal.get(Calendar.YEAR) == todayYear &&
          tripCal.get(Calendar.MONTH) == todayMonth

      val matchesFilter = when (filter) {
        TripFilter.ALL -> true
        TripFilter.TODAY -> isToday
        TripFilter.THIS_MONTH -> isThisMonth
        TripFilter.PROFIT -> trip.profit >= 0
        TripFilter.LOSS -> trip.profit < 0
      }

      matchesFilter && matchesTripSearch(trip, query)
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Dashboard Stats calculation
  val dashboardStats: StateFlow<DashboardStats> = allTrips.combine(allTrips) { trips, _ ->
    val cal = Calendar.getInstance()
    val curYear = cal.get(Calendar.YEAR)
    val curMonth = cal.get(Calendar.MONTH)
    val curDay = cal.get(Calendar.DAY_OF_MONTH)

    var todayInc = 0.0
    var monthInc = 0.0
    var monthProf = 0.0
    var totalKmDriven = 0.0

    for (trip in trips) {
      val tripCal = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      val isSameYear = tripCal.get(Calendar.YEAR) == curYear
      val isSameMonth = isSameYear && tripCal.get(Calendar.MONTH) == curMonth
      val isSameDay = isSameMonth && tripCal.get(Calendar.DAY_OF_MONTH) == curDay

      if (isSameDay) {
        todayInc += trip.income
      }
      if (isSameMonth) {
        monthInc += trip.income
        monthProf += trip.profit
      }
      totalKmDriven += trip.kmDriven
    }

    DashboardStats(
      todayIncome = todayInc,
      monthIncome = monthInc,
      monthProfit = monthProf,
      totalTrips = trips.size,
      totalKm = totalKmDriven
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DashboardStats()
  )

  // Monthly Summary & Chart Aggregations
  private val _selectedMonthOffset = MutableStateFlow(0)
  val selectedMonthOffset: StateFlow<Int> = _selectedMonthOffset.asStateFlow()

  fun changeMonthOffset(delta: Int) {
    _selectedMonthOffset.value += delta
  }

  fun selectMonthYear(year: Int, month: Int) {
    val calNow = Calendar.getInstance()
    val curYear = calNow.get(Calendar.YEAR)
    val curMonth = calNow.get(Calendar.MONTH)
    _selectedMonthOffset.value = (year - curYear) * 12 + (month - curMonth)
  }

  fun resetToCurrentMonth() {
    _selectedMonthOffset.value = 0
  }

  val monthlySummary: StateFlow<MonthlySummaryData> = combine(
    allTrips,
    _selectedMonthOffset
  ) { trips, offset ->
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, offset)
    val targetYear = cal.get(Calendar.YEAR)
    val targetMonth = cal.get(Calendar.MONTH)
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val monthName = monthFormat.format(cal.time)

    val monthTrips = trips.filter { trip ->
      val tripCal = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      tripCal.get(Calendar.YEAR) == targetYear && tripCal.get(Calendar.MONTH) == targetMonth
    }

    val totalGrossRent = monthTrips.sumOf { it.rent }
    val totalIncome = monthTrips.sumOf { it.income }
    val totalMaintenance = monthTrips.sumOf { it.maintenanceCost }
    val totalGratuity = monthTrips.sumOf { it.gratuity }
    val totalExpense = totalMaintenance + totalGratuity
    val totalProfit = totalIncome - totalMaintenance
    val profitMargin = if (totalGrossRent > 0) (totalProfit / totalGrossRent) * 100.0 else 0.0
    val totalKm = monthTrips.sumOf { it.kmDriven }
    val totalTripsCount = monthTrips.size
    val avgProfit = if (totalTripsCount > 0) totalProfit / totalTripsCount else 0.0

    val dayFormat = SimpleDateFormat("dd MMM", Locale.US)
    val dayGroups = monthTrips.groupBy { trip ->
      val c = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      c.get(Calendar.DAY_OF_MONTH)
    }

    val dailyTrends = dayGroups.map { (day, dTrips) ->
      val dIncome = dTrips.sumOf { it.income }
      val dExpense = dTrips.sumOf { it.maintenanceCost + it.gratuity }
      val dProfit = dTrips.sumOf { it.profit }
      val dateLabel = if (dTrips.isNotEmpty()) {
        val c = Calendar.getInstance().apply { timeInMillis = dTrips.first().dateMillis }
        dayFormat.format(c.time)
      } else "$day"
      DayTrend(
        dayOfMonth = day,
        dateLabel = dateLabel,
        income = dIncome,
        expense = dExpense,
        profit = dProfit,
        tripsCount = dTrips.size
      )
    }.sortedBy { it.dayOfMonth }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeklyTrends = (1..5).map { weekNum ->
      val startDay = (weekNum - 1) * 7 + 1
      val endDay = minOf(weekNum * 7, daysInMonth)
      val wTrips = monthTrips.filter { trip ->
        val c = Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
        val d = c.get(Calendar.DAY_OF_MONTH)
        d in startDay..endDay
      }
      val wIncome = wTrips.sumOf { it.income }
      val wExpense = wTrips.sumOf { it.maintenanceCost + it.gratuity }
      val wProfit = wTrips.sumOf { it.profit }
      WeekTrend(
        weekNumber = weekNum,
        weekLabel = "W$weekNum",
        dateRangeLabel = "$startDay-$endDay",
        income = wIncome,
        expense = wExpense,
        profit = wProfit,
        tripsCount = wTrips.size
      )
    }

    var runningCum = 0.0
    val cumulativeProfits = dailyTrends.map { d ->
      runningCum += d.profit
      Pair(d.dateLabel, runningCum)
    }

    val peakDay = dailyTrends.maxByOrNull { it.income }

    MonthlySummaryData(
      year = targetYear,
      month = targetMonth,
      monthName = monthName,
      totalGrossRent = totalGrossRent,
      totalIncome = totalIncome,
      totalMaintenance = totalMaintenance,
      totalGratuity = totalGratuity,
      totalExpense = totalExpense,
      totalProfit = totalProfit,
      profitMargin = profitMargin,
      totalTrips = totalTripsCount,
      totalKm = totalKm,
      avgProfitPerTrip = avgProfit,
      peakEarningDate = peakDay?.dateLabel ?: "",
      peakEarningAmount = peakDay?.income ?: 0.0,
      dailyTrends = dailyTrends,
      weeklyTrends = weeklyTrends,
      cumulativeProfits = cumulativeProfits,
      hasTrips = monthTrips.isNotEmpty()
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = MonthlySummaryData()
  )

  // Add Trip Real-time Form State
  private val _tripDate = MutableStateFlow(SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()))
  val tripDate: StateFlow<String> = _tripDate.asStateFlow()

  private val _tripPlace = MutableStateFlow("")
  val tripPlace: StateFlow<String> = _tripPlace.asStateFlow()

  private val _rentInput = MutableStateFlow("")
  val rentInput: StateFlow<String> = _rentInput.asStateFlow()

  private val _gratuityInput = MutableStateFlow("")
  val gratuityInput: StateFlow<String> = _gratuityInput.asStateFlow()

  private val _maintenanceInput = MutableStateFlow("")
  val maintenanceInput: StateFlow<String> = _maintenanceInput.asStateFlow()

  private val _kmInput = MutableStateFlow("")
  val kmInput: StateFlow<String> = _kmInput.asStateFlow()

  private val _descInput = MutableStateFlow("")
  val descInput: StateFlow<String> = _descInput.asStateFlow()

  private val _passengerNameInput = MutableStateFlow("")
  val passengerNameInput: StateFlow<String> = _passengerNameInput.asStateFlow()

  private val _passengerPhoneInput = MutableStateFlow("")
  val passengerPhoneInput: StateFlow<String> = _passengerPhoneInput.asStateFlow()

  val calculatedIncome: StateFlow<Double> = combine(
    _rentInput,
    _gratuityInput
  ) { rentStr, gratStr ->
    val rent = rentStr.toDoubleOrNull() ?: 0.0
    val gratuity = gratStr.toDoubleOrNull() ?: 0.0
    rent - gratuity
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 0.0
  )

  val calculatedProfit: StateFlow<Double> = combine(
    calculatedIncome,
    _maintenanceInput
  ) { income, maintStr ->
    val maintenance = maintStr.toDoubleOrNull() ?: 0.0
    income - maintenance
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 0.0
  )

  fun setTripDate(date: String) { _tripDate.value = date }
  fun setTripPlace(place: String) { _tripPlace.value = place }
  fun setRentInput(rent: String) { _rentInput.value = rent }
  fun setGratuityInput(grat: String) { _gratuityInput.value = grat }
  fun setMaintenanceInput(maint: String) { _maintenanceInput.value = maint }
  fun setKmInput(km: String) { _kmInput.value = km }
  fun setDescInput(desc: String) { _descInput.value = desc }
  fun setPassengerNameInput(name: String) { _passengerNameInput.value = name }
  fun setPassengerPhoneInput(phone: String) { _passengerPhoneInput.value = phone }

  fun parseDateStringToMillis(dateStr: String): Long {
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
    val trimmed = dateStr.trim()
    for (sdf in formats) {
      try {
        sdf.isLenient = true
        val parsed = sdf.parse(trimmed)
        if (parsed != null) return parsed.time
      } catch (_: Exception) {}
    }
    return System.currentTimeMillis()
  }

  fun saveTrip(onSuccess: () -> Unit) {
    val place = _tripPlace.value.trim().ifEmpty { "Trip / ট্রিপ" }
    val rent = _rentInput.value.toDoubleOrNull() ?: 0.0
    val gratuity = _gratuityInput.value.toDoubleOrNull() ?: 0.0
    val maintenance = _maintenanceInput.value.toDoubleOrNull() ?: 0.0
    val km = _kmInput.value.toDoubleOrNull() ?: 0.0
    val desc = _descInput.value.trim()
    val income = rent - gratuity
    val profit = income - maintenance

    val tripDateMillis = parseDateStringToMillis(_tripDate.value)

    val newTrip = TripEntity(
      dateMillis = tripDateMillis,
      dateString = _tripDate.value,
      place = place,
      rent = rent,
      gratuity = gratuity,
      maintenanceCost = maintenance,
      kmDriven = km,
      description = desc,
      passengerName = "",
      passengerPhone = "",
      income = income,
      profit = profit
    )

    viewModelScope.launch {
      tripRepo.insertTrip(newTrip)
      _tripPlace.value = ""
      _rentInput.value = ""
      _gratuityInput.value = ""
      _maintenanceInput.value = ""
      _kmInput.value = ""
      _descInput.value = ""
      _passengerNameInput.value = ""
      _passengerPhoneInput.value = ""
      _tripDate.value = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
      onSuccess()
    }
  }

  fun deleteTrip(trip: TripEntity) {
    viewModelScope.launch {
      tripRepo.deleteTrip(trip)
    }
  }

  fun updateTrip(trip: TripEntity, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      val income = trip.rent - trip.gratuity
      val profit = income - trip.maintenanceCost
      val updated = trip.copy(income = income, profit = profit)
      tripRepo.updateTrip(updated)
      onSuccess()
    }
  }

  // Subscription Submission
  private val _trxId = MutableStateFlow("")
  val trxId: StateFlow<String> = _trxId.asStateFlow()

  private val _screenshotAttached = MutableStateFlow(false)
  val screenshotAttached: StateFlow<Boolean> = _screenshotAttached.asStateFlow()

  private val _submissionMessage = MutableStateFlow<String?>(null)
  val submissionMessage: StateFlow<String?> = _submissionMessage.asStateFlow()

  fun setTrxId(id: String) { _trxId.value = id }
  fun setScreenshotAttached(attached: Boolean) { _screenshotAttached.value = attached }

  fun submitPaymentVerification(onSuccess: (String) -> Unit) {
    val msg = "TrxID: ${_trxId.value.ifEmpty { "TRX-DEMO" }} submitted successfully."
    _submissionMessage.value = msg
    onSuccess(msg)
  }

  fun clearSubmissionMessage() {
    _submissionMessage.value = null
  }

  fun exportTrips(
    context: Context,
    format: ExportFormat,
    trips: List<TripEntity>,
    onSuccess: (File) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val profile = userProfile.value
        val file = when (format) {
          ExportFormat.PDF -> ExportManager.generatePdfReport(context, trips, profile)
          ExportFormat.CSV -> ExportManager.generateCsvReport(context, trips, profile)
        }
        val mimeType = if (format == ExportFormat.PDF) "application/pdf" else "text/csv"
        val title = if (format == ExportFormat.PDF) "Car Hisab Accounting PDF" else "Car Hisab Trip Logs CSV"
        val summaryMsg = "Car Hisab trip report for ${profile.driverName} (${trips.size} trips)."
        ExportManager.shareExportedFile(context, file, mimeType, title, summaryMsg)
        onSuccess(file)
      } catch (e: Exception) {
        onError(e.localizedMessage ?: "Export failed")
      }
    }
  }

  // Storage Access Framework (SAF) Backup & Restore
  val isAutoWeeklyBackupReminderEnabled: StateFlow<Boolean> = userPrefsRepo.autoWeeklyBackupReminderFlow

  fun setAutoWeeklyBackupReminderEnabled(context: Context, enabled: Boolean) {
    userPrefsRepo.setAutoWeeklyBackupReminderEnabled(enabled)
    if (enabled) {
      com.example.receiver.WeeklyBackupReminderReceiver.scheduleWeeklyBackupReminder(context)
    } else {
      com.example.receiver.WeeklyBackupReminderReceiver.cancelWeeklyBackupReminder(context)
    }
  }

  private val _lastBackupTime = MutableStateFlow(userPrefsRepo.getLastDriveBackupTime())
  val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

  private val _lastBackupCount = MutableStateFlow(userPrefsRepo.getLastBackupTripCount())
  val lastBackupCount: StateFlow<Int> = _lastBackupCount.asStateFlow()

  private val _isBackingUp = MutableStateFlow(false)
  val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

  private val _isRestoring = MutableStateFlow(false)
  val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

  private val _backupStatusMessage = MutableStateFlow<String?>(null)
  val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

  fun clearBackupStatusMessage() {
    _backupStatusMessage.value = null
  }

  fun performBackupToUri(
    context: Context,
    uri: Uri,
    onSuccess: (count: Int) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isBackingUp.value = true
      try {
        val trips = tripRepo.getAllTripsSnapshot()
        val bookings = bookingRepo.getAllBookingsSnapshot()
        val profile = userProfile.value
        val documents = vehicleDocuments.value
        val mobilService = mobilServiceInfo.value

        val jsonString = GoogleDriveBackupManager.serializeBackupJson(
          trips = trips,
          profile = profile,
          documents = documents,
          mobilService = mobilService,
          bookings = bookings
        )

        val success = GoogleDriveBackupManager.writeBackupToUri(context, uri, jsonString)
        if (success) {
          val now = System.currentTimeMillis()
          userPrefsRepo.setLastDriveBackupTime(now)
          userPrefsRepo.setLastBackupTripCount(trips.size)
          _lastBackupTime.value = now
          _lastBackupCount.value = trips.size
          _backupStatusMessage.value = "Backup created: ${trips.size} trips"
          onSuccess(trips.size)
        } else {
          onError("Failed writing backup file to storage")
        }
      } catch (e: Exception) {
        onError(e.localizedMessage ?: "Backup failed")
      } finally {
        _isBackingUp.value = false
      }
    }
  }

  fun performRestoreFromUri(
    context: Context,
    uri: Uri,
    onSuccess: (count: Int) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isRestoring.value = true
      try {
        val rawContent = GoogleDriveBackupManager.readBackupFromUri(context, uri)
        if (rawContent.isNullOrBlank()) {
          _isRestoring.value = false
          onError("Could not read backup file content")
          return@launch
        }

        val payload = GoogleDriveBackupManager.parseBackupPayload(rawContent)
        if (payload.trips.isEmpty() && payload.bookings.isEmpty() && payload.profile == null) {
          _isRestoring.value = false
          onError("Invalid backup file or no data found")
          return@launch
        }

        if (payload.trips.isNotEmpty()) {
          tripRepo.insertTrips(payload.trips)
        }
        if (payload.bookings.isNotEmpty()) {
          bookingRepo.insertBookings(payload.bookings)
        }
        payload.profile?.let { userPrefsRepo.updateProfile(it) }
        payload.documents?.let { userPrefsRepo.updateDocuments(it) }
        payload.mobilService?.let { userPrefsRepo.updateMobilService(it) }

        val count = payload.trips.size
        val now = System.currentTimeMillis()
        userPrefsRepo.setLastDriveBackupTime(now)
        userPrefsRepo.setLastBackupTripCount(count)
        _lastBackupTime.value = now
        _lastBackupCount.value = count

        _backupStatusMessage.value = "Restored $count trips successfully"
        onSuccess(count)
      } catch (e: Exception) {
        onError(e.localizedMessage ?: "Restore failed")
      } finally {
        _isRestoring.value = false
      }
    }
  }

  fun performGoogleDriveBackup(
    context: Context,
    onSuccess: (count: Int) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _backupStatusMessage.value = "Use Storage Access Framework to select backup location"
      onError("Select a location to save backup file")
    }
  }

  fun performGoogleDriveRestore(
    context: Context,
    uri: Uri? = null,
    onSuccess: (count: Int) -> Unit,
    onError: (String) -> Unit
  ) {
    if (uri != null) {
      performRestoreFromUri(context, uri, onSuccess, onError)
    } else {
      onError("No backup file selected")
    }
  }
}
