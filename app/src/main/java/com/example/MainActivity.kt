package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.AddTripScreen
import com.example.ui.screens.AllTripsScreen
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LanguageSelectScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NidVerifyScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.ThemeSettingsScreen
import com.example.ui.screens.VehicleDocumentsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CarHisabViewModel

import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
  private val viewModel: CarHisabViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    com.example.receiver.WeeklyBackupReminderReceiver.createNotificationChannel(this)
    com.example.receiver.WeeklyBackupReminderReceiver.scheduleWeeklyBackupReminder(this)

    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val language by viewModel.language.collectAsStateWithLifecycle()
      val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
      val profile by viewModel.userProfile.collectAsStateWithLifecycle()
      val pendingRestoreMetadata by viewModel.pendingRestoreMetadata.collectAsStateWithLifecycle()
      val context = LocalContext.current
      var restoreResultDialogMessage by remember { mutableStateOf<String?>(null) }

      LaunchedEffect(profile.isLoggedIn) {
        if (profile.isLoggedIn) {
          viewModel.checkDriveBackupOnLogin()
        }
      }

      MyApplicationTheme(themeMode = themeMode) {
        BackHandler(enabled = currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.DASHBOARD) {
          when (currentScreen) {
            AppScreen.LANGUAGE_SELECT -> viewModel.navigateTo(AppScreen.SPLASH)
            AppScreen.LOGIN -> viewModel.navigateTo(AppScreen.SPLASH)
            AppScreen.PROFILE_SETUP -> viewModel.navigateTo(AppScreen.LOGIN)
            AppScreen.NID_VERIFY -> viewModel.navigateTo(AppScreen.PROFILE_SETUP)
            AppScreen.LANGUAGE_SETTINGS,
            AppScreen.THEME_SETTINGS -> viewModel.navigateTo(AppScreen.SETTINGS)
            AppScreen.ADD_TRIP, AppScreen.ALL_TRIPS ->
              viewModel.navigateTo(AppScreen.DASHBOARD)
            else -> viewModel.navigateTo(AppScreen.DASHBOARD)
          }
        }

        val isMainScreen = currentScreen == AppScreen.DASHBOARD ||
            currentScreen == AppScreen.ADD_TRIP ||
            currentScreen == AppScreen.ALL_TRIPS ||
            currentScreen == AppScreen.SETTINGS

        val showTopBar = currentScreen != AppScreen.SPLASH &&
            currentScreen != AppScreen.DASHBOARD &&
            currentScreen != AppScreen.LOGIN &&
            currentScreen != AppScreen.PROFILE_SETUP &&
            currentScreen != AppScreen.NID_VERIFY &&
            currentScreen != AppScreen.LANGUAGE_SETTINGS &&
            currentScreen != AppScreen.THEME_SETTINGS

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            if (showTopBar) {
              TopAppBar(
                title = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = when (currentScreen) {
                        AppScreen.ALL_TRIPS -> when (language) {
                          AppLanguage.BANGLA -> "সব ট্রিপ ফোল্ডার"
                          AppLanguage.HINDI -> "सभी ट्रिप फ़ोल्डर"
                          AppLanguage.TAMIL -> "அனைத்து பயணங்கள்"
                          AppLanguage.URDU -> "تمام ٹرپس"
                          else -> "All Trips Folder"
                        }
                        AppScreen.ADD_TRIP -> when (language) {
                          AppLanguage.BANGLA -> "নতুন ট্রিপ যোগ"
                          AppLanguage.HINDI -> "नया ट्रिप जोड़ें"
                          AppLanguage.TAMIL -> "புதிய பயணம் சேர்க்க"
                          AppLanguage.URDU -> "نیاٹرپ شامل کریں"
                          else -> "Add New Trip"
                        }
                        AppScreen.SETTINGS -> when (language) {
                          AppLanguage.BANGLA -> "সেটিংস"
                          AppLanguage.HINDI -> "সেটিংড"
                          AppLanguage.TAMIL -> "அமைப்புகள்"
                          AppLanguage.URDU -> "سیٹنگز"
                          else -> "Settings"
                        }
                        AppScreen.DOCUMENTS_SERVICES -> when (language) {
                          AppLanguage.BANGLA -> "কাগজপত্র ও সার্ভিস"
                          AppLanguage.HINDI -> "দস্তাবেজ এবং সেবা"
                          AppLanguage.TAMIL -> "ஆவணங்கள் & சேவை"
                          AppLanguage.URDU -> "دستاویزات اور سروس"
                          else -> "Documents & Service"
                        }
                        AppScreen.BOOKINGS -> when (language) {
                          AppLanguage.BANGLA -> "অগ্রিম বুকিং"
                          AppLanguage.HINDI -> "অগ্রিম বুকিং"
                          AppLanguage.TAMIL -> "முன்பதிவுகள்"
                          AppLanguage.URDU -> "ایডوانস بکنگ"
                          else -> "Bookings"
                        }
                        else -> "Car Hisab"
                      },
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                },
                navigationIcon = {
                  IconButton(
                    onClick = {
                      if (currentScreen == AppScreen.LANGUAGE_SETTINGS ||
                          currentScreen == AppScreen.THEME_SETTINGS) {
                        viewModel.navigateTo(AppScreen.SETTINGS)
                      } else {
                        viewModel.navigateTo(AppScreen.DASHBOARD)
                      }
                    }
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.surface
                )
              )
            }
          },
          bottomBar = {
            if (isMainScreen) {
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                  .windowInsetsPadding(WindowInsets.navigationBars)
                  .testTag("main_bottom_navigation")
              ) {
                // 1. ড্যাশবোর্ড (Dashboard)
                NavigationBarItem(
                  selected = currentScreen == AppScreen.DASHBOARD,
                  onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                  icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", modifier = Modifier.size(20.dp)) },
                  label = {
                    Text(
                      text = when (language) {
                        AppLanguage.BANGLA -> "ড্যাশবোর্ড"
                        AppLanguage.HINDI -> "ড্যাশবোর্ড"
                        AppLanguage.TAMIL -> "டாஷ்போர்டு"
                        AppLanguage.URDU -> "ڈیش بورڈ"
                        else -> "Dashboard"
                      },
                      fontSize = 10.sp,
                      fontWeight = if (currentScreen == AppScreen.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  modifier = Modifier.testTag("nav_item_dashboard")
                )

                // 2. ট্রিপ যোগ (Add Trip)
                NavigationBarItem(
                  selected = currentScreen == AppScreen.ADD_TRIP,
                  onClick = { viewModel.navigateTo(AppScreen.ADD_TRIP) },
                  icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add Trip", modifier = Modifier.size(20.dp)) },
                  label = {
                    Text(
                      text = when (language) {
                        AppLanguage.BANGLA -> "ট্রিপ যোগ"
                        AppLanguage.HINDI -> "ট্রিপ जोड़ें"
                        AppLanguage.TAMIL -> "பயணம் சேர்க்க"
                        AppLanguage.URDU -> "ٹرپ شامل کریں"
                        else -> "Add Trip"
                      },
                      fontSize = 10.sp,
                      fontWeight = if (currentScreen == AppScreen.ADD_TRIP) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  modifier = Modifier.testTag("nav_item_add_trip")
                )

                // 3. All Trip ফোল্ডার
                NavigationBarItem(
                  selected = currentScreen == AppScreen.ALL_TRIPS,
                  onClick = { viewModel.navigateTo(AppScreen.ALL_TRIPS) },
                  icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "All Trip ফোল্ডার", modifier = Modifier.size(20.dp)) },
                  label = {
                    Text(
                      text = when (language) {
                        AppLanguage.BANGLA -> "All Trip ফোল্ডার"
                        AppLanguage.HINDI -> "सभी ट्रिप फ़ोल्डर"
                        AppLanguage.TAMIL -> "பயணங்கள்"
                        AppLanguage.URDU -> "تمام ٹرپس"
                        else -> "All Trips"
                      },
                      fontSize = 10.sp,
                      fontWeight = if (currentScreen == AppScreen.ALL_TRIPS) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  modifier = Modifier.testTag("nav_item_all_trips")
                )

                // 4. সেটিংস (Settings)
                NavigationBarItem(
                  selected = currentScreen == AppScreen.SETTINGS,
                  onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                  icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(19.dp)) },
                  label = {
                    Text(
                      text = when (language) {
                        AppLanguage.BANGLA -> "সেটিংস"
                        AppLanguage.HINDI -> "সেটিংড"
                        AppLanguage.TAMIL -> "அமைப்புகள்"
                        AppLanguage.URDU -> "سیٹنگز"
                        else -> "Settings"
                      },
                      fontSize = 10.sp,
                      fontWeight = if (currentScreen == AppScreen.SETTINGS) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  modifier = Modifier.testTag("nav_item_settings")
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (currentScreen) {
              AppScreen.SPLASH -> {
                SplashScreen(
                  language = language,
                  onProceed = {
                    if (profile.isLoggedIn) {
                      viewModel.checkDriveBackupOnLogin()
                      viewModel.navigateTo(AppScreen.DASHBOARD)
                    } else {
                      viewModel.navigateTo(AppScreen.LOGIN)
                    }
                  }
                )
              }

              AppScreen.LANGUAGE_SELECT -> {
                LanguageSelectScreen(
                  currentLanguage = language,
                  onLanguageSelected = { viewModel.setLanguage(it) },
                  onContinue = { viewModel.navigateTo(AppScreen.LOGIN) }
                )
              }

              AppScreen.LOGIN -> {
                LoginScreen(
                  language = language,
                  savedEmail = profile.savedEmail,
                  savedRememberEmail = profile.rememberEmail,
                  onLoginSuccess = { email, remember ->
                    viewModel.updateRememberEmail(email, remember)
                    viewModel.checkDriveBackupOnLogin()
                    viewModel.navigateTo(AppScreen.DASHBOARD)
                  },
                  onBiometricLogin = { onSuccess, onError ->
                    viewModel.loginWithBiometric(
                      onSuccess = { email ->
                        viewModel.checkDriveBackupOnLogin()
                        viewModel.navigateTo(AppScreen.DASHBOARD)
                        onSuccess(email)
                      },
                      onError = { msg ->
                        onError(msg)
                      }
                    )
                  },
                  onGoogleAuthSuccess = { email, name ->
                    viewModel.onGoogleAuthSuccess(email, name)
                  },
                  onSignUpSuccess = { email, phone ->
                    viewModel.updateFullProfile(
                      nameBangla = profile.driverNameBangla,
                      nameEnglish = profile.driverNameEnglish,
                      birthdate = profile.birthDate,
                      phone = phone,
                      email = email,
                      carName = profile.carName,
                      carModel = profile.carModel,
                      carNumber = profile.carNumber
                    )
                    viewModel.navigateTo(AppScreen.PROFILE_SETUP)
                  }
                )
              }

              AppScreen.PROFILE_SETUP -> {
                ProfileSetupScreen(
                  language = language,
                  initialProfile = profile,
                  onSaveProfile = { nameBangla, nameEnglish, birthdate, phone, email, carName, carModel, carNumber ->
                    viewModel.updateFullProfile(
                      nameBangla = nameBangla,
                      nameEnglish = nameEnglish,
                      birthdate = birthdate,
                      phone = phone,
                      email = email,
                      carName = carName,
                      carModel = carModel,
                      carNumber = carNumber
                    )
                    viewModel.navigateTo(AppScreen.NID_VERIFY)
                  }
                )
              }

              AppScreen.NID_VERIFY -> {
                NidVerifyScreen(
                  language = language,
                  initialFront = profile.nidFrontAttached,
                  initialBack = profile.nidBackAttached,
                  initialSelfie = profile.selfieAttached,
                  onVerificationSubmitted = { front, back, selfie ->
                    viewModel.updateNidStatus(front, back, selfie)
                    viewModel.navigateTo(AppScreen.DASHBOARD)
                  },
                  onSkip = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
              }

              AppScreen.DASHBOARD -> {
                val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
                val trips by viewModel.allTrips.collectAsStateWithLifecycle()
                val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
                val monthOffset by viewModel.selectedMonthOffset.collectAsStateWithLifecycle()
                val documents by viewModel.vehicleDocuments.collectAsStateWithLifecycle()
                val mobilService by viewModel.mobilServiceInfo.collectAsStateWithLifecycle()
                val upcomingBookings by viewModel.upcomingBookings.collectAsStateWithLifecycle()

                DashboardScreen(
                  language = language,
                  profile = profile,
                  stats = stats,
                  themeMode = themeMode,
                  recentTrips = trips,
                  documents = documents,
                  mobilService = mobilService,
                  upcomingBookings = upcomingBookings,
                  monthlySummary = monthlySummary,
                  selectedMonthOffset = monthOffset,
                  onPrevMonth = { viewModel.changeMonthOffset(-1) },
                  onNextMonth = { viewModel.changeMonthOffset(1) },
                  onResetToCurrentMonth = { viewModel.resetToCurrentMonth() },
                  onSelectMonthYear = { y, m -> viewModel.selectMonthYear(y, m) },
                  onAddTripClick = { viewModel.navigateTo(AppScreen.ADD_TRIP) },
                  onViewAllTripsClick = { viewModel.navigateTo(AppScreen.ALL_TRIPS) },
                  onMonthTripsClick = { year, month ->
                    viewModel.navigateToAllTripsMonth(year, month)
                  },
                  onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                  onDocsClick = { viewModel.navigateTo(AppScreen.DOCUMENTS_SERVICES) },
                  onBookingsClick = { viewModel.navigateTo(AppScreen.BOOKINGS) },
                  onSubscriptionClick = {},
                  onExportTrips = { format, selectedTrips ->
                    viewModel.exportTrips(
                      context = context,
                      format = format,
                      trips = selectedTrips,
                      onSuccess = {
                        Toast.makeText(context, AppStrings.exportSuccess(language), Toast.LENGTH_SHORT).show()
                      },
                      onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onUpdateTrip = { updatedTrip ->
                    viewModel.updateTrip(
                      trip = updatedTrip,
                      onSuccess = {
                        Toast.makeText(
                          context,
                          if (language == AppLanguage.BANGLA) "হিসাব আপডেট করা হয়েছে!" else "Record updated!",
                          Toast.LENGTH_SHORT
                        ).show()
                      },
                      onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onUpdateProfileQuick = { nameBn, carModel, carPlate ->
                    viewModel.updateFullProfile(
                      nameBangla = nameBn,
                      nameEnglish = profile.driverNameEnglish,
                      birthdate = profile.birthDate,
                      phone = profile.driverPhone,
                      email = profile.driverEmail,
                      carName = profile.carName,
                      carModel = carModel,
                      carNumber = carPlate
                    )
                  }
                )
              }

              AppScreen.BOOKINGS -> {
                val bookings by viewModel.allBookings.collectAsStateWithLifecycle()

                BookingsScreen(
                  language = language,
                  profile = profile,
                  bookings = bookings,
                  onAddBooking = { name, phone, pickup, drop, dateMillis, timeStr, totalFare, advance, notes ->
                    viewModel.addBooking(name, phone, pickup, drop, dateMillis, timeStr, totalFare, advance, notes)
                    Toast.makeText(context, "অগ্রিম বুকিং সংরক্ষণ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                  },
                  onUpdateStatus = { id, status ->
                    viewModel.updateBookingStatus(id, status)
                  },
                  onDeleteBooking = { id ->
                    viewModel.deleteBooking(id)
                    Toast.makeText(context, "বুকিং মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                  },
                  onConvertToTrip = { booking, gratuity, maintenance, km ->
                    viewModel.convertBookingToTrip(
                      booking = booking,
                      gratuity = gratuity,
                      maintenanceCost = maintenance,
                      kmDriven = km,
                      onCompleted = {
                        Toast.makeText(context, "বুকিং সফলভাবে ট্রিপে রূপান্তরিত হয়েছে!", Toast.LENGTH_SHORT).show()
                      },
                      onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                      }
                    )
                  }
                )
              }

              AppScreen.DOCUMENTS_SERVICES -> {
                val documents by viewModel.vehicleDocuments.collectAsStateWithLifecycle()
                val mobilService by viewModel.mobilServiceInfo.collectAsStateWithLifecycle()

                VehicleDocumentsScreen(
                  language = language,
                  profile = profile,
                  documents = documents,
                  mobilService = mobilService,
                  onUpdateDocuments = { updatedDocs ->
                    viewModel.updateVehicleDocuments(updatedDocs)
                    Toast.makeText(context, "কাগজপত্রের তথ্য আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                  },
                  onUpdateMobilService = { updatedService ->
                    viewModel.updateMobilService(updatedService)
                    Toast.makeText(context, "সার্ভিস তথ্য সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                  },
                  onLogMobilChange = { km, brand, cost ->
                    viewModel.logMobilChanged(
                      newKm = km,
                      brand = brand,
                      cost = cost,
                      onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                      }
                    )
                    Toast.makeText(context, "মবিল পরিবর্তন এন্ট্রি সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                  },
                  onUpdateOdometer = { newKm ->
                    viewModel.updateOdometer(newKm)
                    Toast.makeText(context, "ওডোমিটার আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                  }
                )
              }

              AppScreen.ADD_TRIP -> {
                val date by viewModel.tripDate.collectAsStateWithLifecycle()
                val place by viewModel.tripPlace.collectAsStateWithLifecycle()
                val passengerName by viewModel.passengerNameInput.collectAsStateWithLifecycle()
                val passengerPhone by viewModel.passengerPhoneInput.collectAsStateWithLifecycle()
                val rent by viewModel.rentInput.collectAsStateWithLifecycle()
                val gratuity by viewModel.gratuityInput.collectAsStateWithLifecycle()
                val maintenance by viewModel.maintenanceInput.collectAsStateWithLifecycle()
                val km by viewModel.kmInput.collectAsStateWithLifecycle()
                val desc by viewModel.descInput.collectAsStateWithLifecycle()
                val income by viewModel.calculatedIncome.collectAsStateWithLifecycle()
                val profit by viewModel.calculatedProfit.collectAsStateWithLifecycle()

                AddTripScreen(
                  language = language,
                  date = date,
                  place = place,
                  passengerName = passengerName,
                  passengerPhone = passengerPhone,
                  rent = rent,
                  gratuity = gratuity,
                  maintenance = maintenance,
                  km = km,
                  description = desc,
                  calculatedIncome = income,
                  calculatedProfit = profit,
                  onDateChange = { viewModel.setTripDate(it) },
                  onPlaceChange = { viewModel.setTripPlace(it) },
                  onPassengerNameChange = { viewModel.setPassengerNameInput(it) },
                  onPassengerPhoneChange = { viewModel.setPassengerPhoneInput(it) },
                  onRentChange = { viewModel.setRentInput(it) },
                  onGratuityChange = { viewModel.setGratuityInput(it) },
                  onMaintenanceChange = { viewModel.setMaintenanceInput(it) },
                  onKmChange = { viewModel.setKmInput(it) },
                  onDescriptionChange = { viewModel.setDescInput(it) },
                  onSaveTrip = {
                    viewModel.saveTrip(
                      onSuccess = {
                        Toast.makeText(context, AppStrings.tripSavedSuccess(language), Toast.LENGTH_SHORT).show()
                        viewModel.navigateTo(AppScreen.ALL_TRIPS)
                      },
                      onError = { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                      }
                    )
                  }
                )
              }

              AppScreen.ALL_TRIPS -> {
                val allTripsList by viewModel.allTrips.collectAsStateWithLifecycle()
                val filteredTrips by viewModel.filteredTrips.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
                val targetFolder by viewModel.allTripsTargetFolder.collectAsStateWithLifecycle()

                AllTripsScreen(
                  language = language,
                  trips = filteredTrips,
                  allTrips = allTripsList,
                  searchQuery = searchQuery,
                  activeFilter = activeFilter,
                  initialYear = targetFolder?.first,
                  initialMonth = targetFolder?.second,
                  onSearchChange = { viewModel.setSearchQuery(it) },
                  onFilterChange = { viewModel.setActiveFilter(it) },
                  onDeleteTrip = {
                    viewModel.deleteTrip(it)
                    Toast.makeText(context, AppStrings.tripDeleted(language), Toast.LENGTH_SHORT).show()
                  },
                  onUpdateTrip = { updatedTrip ->
                    viewModel.updateTrip(
                      trip = updatedTrip,
                      onSuccess = {
                        Toast.makeText(
                          context,
                          if (language == AppLanguage.BANGLA) "ট্রিপ আপডেট করা হয়েছে!" else "Trip updated!",
                          Toast.LENGTH_SHORT
                        ).show()
                      },
                      onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onAddNewTrip = { viewModel.navigateTo(AppScreen.ADD_TRIP) },
                  onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                  onExportTrips = { format, selectedTrips ->
                    viewModel.exportTrips(
                      context = context,
                      format = format,
                      trips = selectedTrips,
                      onSuccess = {
                        Toast.makeText(context, AppStrings.exportSuccess(language), Toast.LENGTH_SHORT).show()
                      },
                      onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                      }
                    )
                  }
                )
              }

              AppScreen.SUBSCRIPTION -> {
                val trxId by viewModel.trxId.collectAsStateWithLifecycle()
                val screenshotAttached by viewModel.screenshotAttached.collectAsStateWithLifecycle()

                SubscriptionScreen(
                  language = language,
                  isFreeTrialActive = profile.isFreeTrialActive,
                  monthsLeft = profile.trialMonthsRemaining,
                  userUniqueId = profile.userUniqueKey,
                  currentPlanName = profile.currentPlanName,
                  expiryDateMillis = profile.subscriptionExpiryMillis,
                  trxId = trxId,
                  screenshotAttached = screenshotAttached,
                  onTrxIdChange = { viewModel.setTrxId(it) },
                  onScreenshotToggle = { viewModel.setScreenshotAttached(!screenshotAttached) },
                  onSubmitVerification = {
                    viewModel.submitPaymentVerification { msg ->
                      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                  },
                  onActivateWithKey = { key, callback ->
                    viewModel.activateWithAdminKey(key) { success, msg ->
                      callback(success, msg)
                      if (success) {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                      }
                    }
                  },
                  onToggleStatusDemo = { viewModel.toggleSubscriptionDemo() }
                )
              }

              AppScreen.SETTINGS -> {
                val allTripsForExport by viewModel.allTrips.collectAsStateWithLifecycle()
                val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
                val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
                val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
                val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()

                SettingsScreen(
                  language = language,
                  themeMode = themeMode,
                  profile = profile,
                  trips = allTripsForExport,
                  lastBackupTime = lastBackupTime,
                  isBackingUp = isBackingUp,
                  isRestoring = isRestoring,
                  backupStatusMessage = backupStatusMessage,
                  onLanguageChange = { viewModel.setLanguage(it) },
                  onOpenLanguageSettings = { viewModel.navigateTo(AppScreen.LANGUAGE_SETTINGS) },
                  onOpenDriveBackup = { /* No-op or navigate */ },
                  onOpenThemeSettings = { viewModel.navigateTo(AppScreen.THEME_SETTINGS) },
                  onThemeModeChange = { viewModel.setThemeMode(it) },
                  onUpdateProfile = { cName, cModel, cNumber, dName, dPhone, dEmail ->
                    viewModel.updateProfile(cName, cModel, cNumber, dName, dPhone, dEmail)
                    Toast.makeText(context, if (language == AppLanguage.BANGLA) "প্রোফাইল আপডেট হয়েছে" else "Profile updated", Toast.LENGTH_SHORT).show()
                  },
                  onSaveProfilePhoto = { uri ->
                    val saved = viewModel.saveProfilePhotoFromUri(context, uri)
                    if (saved != null) {
                      Toast.makeText(context, if (language == AppLanguage.BANGLA) "প্রোফাইল ছবি যুক্ত হয়েছে!" else "Profile photo saved!", Toast.LENGTH_SHORT).show()
                    } else {
                      Toast.makeText(context, if (language == AppLanguage.BANGLA) "ছবি সংরক্ষণে সমস্যা হয়েছে" else "Failed to save photo", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onRemoveProfilePhoto = {
                    viewModel.updateProfilePhoto(null)
                    Toast.makeText(context, if (language == AppLanguage.BANGLA) "প্রোফাইল ছবি মুছে ফেলা হয়েছে" else "Profile photo removed", Toast.LENGTH_SHORT).show()
                  },
                  onOpenSubscription = { viewModel.navigateTo(AppScreen.SUBSCRIPTION) },
                  onOpenDocuments = { viewModel.navigateTo(AppScreen.DOCUMENTS_SERVICES) },
                  onExportTrips = { format, selectedTrips ->
                    viewModel.exportTrips(
                      context = context,
                      format = format,
                      trips = selectedTrips,
                      onSuccess = {
                        Toast.makeText(context, AppStrings.exportSuccess(language), Toast.LENGTH_SHORT).show()
                      },
                      onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onGoogleDriveBackup = {
                    viewModel.triggerAutoBackup(
                      onSuccess = { msg ->
                        restoreResultDialogMessage = msg
                      },
                      onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onGoogleDriveRestore = { uri ->
                    if (uri != null) {
                      viewModel.restoreFromUri(
                        uri = uri,
                        onSuccess = { msg ->
                          restoreResultDialogMessage = msg
                        },
                        onError = { err ->
                          Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                      )
                    } else {
                      viewModel.performDriveRestore(
                        onSuccess = { msg ->
                          restoreResultDialogMessage = msg
                        },
                        onError = { err ->
                          Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                      )
                    }
                  },
                  onSaveBackupUri = { uri ->
                    viewModel.saveBackupToUri(
                      uri = uri,
                      onSuccess = { msg ->
                        restoreResultDialogMessage = msg
                      },
                      onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onRestoreBackupUri = { uri ->
                    viewModel.restoreFromUri(
                      uri = uri,
                      onSuccess = { msg ->
                        restoreResultDialogMessage = msg
                      },
                      onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onShareBackup = {
                    viewModel.getShareBackupIntent(
                      context = context,
                      onReady = { intent ->
                        context.startActivity(intent)
                      },
                      onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                      }
                    )
                  },
                  onLogout = {
                    viewModel.logout()
                    Toast.makeText(context, if (language == AppLanguage.BANGLA) "লগআউট সফল হয়েছে" else "Logged out", Toast.LENGTH_SHORT).show()
                  }
                )
              }

              AppScreen.LANGUAGE_SETTINGS -> {
                LanguageSelectScreen(
                  currentLanguage = language,
                  onLanguageSelected = { newLang ->
                    viewModel.setLanguage(newLang)
                  },
                  onContinue = {
                    Toast.makeText(
                      context,
                      when (language) {
                        AppLanguage.BANGLA -> "ভাষা সফলভাবে পরিবর্তন করা হয়েছে"
                        AppLanguage.HINDI -> "भाषा सफलतापूर्वक बदल दी गई"
                        AppLanguage.TAMIL -> "மொழி வெற்றிகரமாக மாற்றப்பட்டது"
                        AppLanguage.URDU -> "زبان کامیابی سے تبدیل ہو گئی"
                        else -> "Language changed successfully"
                      },
                      Toast.LENGTH_SHORT
                    ).show()
                    viewModel.navigateTo(AppScreen.SETTINGS)
                  },
                  isFromSettings = true,
                  onBack = {
                    viewModel.navigateTo(AppScreen.SETTINGS)
                  }
                )
              }

              AppScreen.THEME_SETTINGS -> {
                ThemeSettingsScreen(
                  language = language,
                  themeMode = themeMode,
                  onThemeModeChange = { newMode ->
                    viewModel.setThemeMode(newMode)
                    Toast.makeText(
                      context,
                      when (newMode) {
                        com.example.ui.theme.AppThemeMode.DARK, com.example.ui.theme.AppThemeMode.LIGHT -> if (language == AppLanguage.BANGLA) "ডার্ক মোড সক্রিয় করা হয়েছে" else "Dark mode activated"
                        com.example.ui.theme.AppThemeMode.SYSTEM -> if (language == AppLanguage.BANGLA) "সিস্টেম থিম সক্রিয় করা হয়েছে" else "System theme activated"
                      },
                      Toast.LENGTH_SHORT
                    ).show()
                  },
                  onBack = {
                    viewModel.navigateTo(AppScreen.SETTINGS)
                  }
                )
              }
            }

            // WhatsApp-Style Restore Prompt Dialog on Login
            pendingRestoreMetadata?.let { metadata ->
              val dialogText = if (language == AppLanguage.BANGLA) {
                "গুগল ড্রাইভে ব্যাকআপ পাওয়া গেছে (তারিখ: ${metadata.dateString}, সাইজ: ${metadata.sizeString})। রিস্টোর করতে চান?"
              } else {
                "Google Drive backup found (Date: ${metadata.dateString}, Size: ${metadata.sizeString}). Do you wish to restore?"
              }

              AlertDialog(
                onDismissRequest = { viewModel.dismissRestorePrompt() },
                title = {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "গুগল ড্রাইভ ব্যাকআপ" else "Google Drive Backup",
                    fontWeight = FontWeight.Bold
                  )
                },
                text = {
                  Text(text = dialogText)
                },
                confirmButton = {
                  Button(
                    onClick = {
                      viewModel.performDriveRestore(
                        onSuccess = { msg ->
                          restoreResultDialogMessage = msg
                        },
                        onError = { err ->
                          Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                      )
                    }
                  ) {
                    Text(text = if (language == AppLanguage.BANGLA) "রিস্টোর করুন" else "Restore")
                  }
                },
                dismissButton = {
                  OutlinedButton(
                    onClick = { viewModel.dismissRestorePrompt() }
                  ) {
                    Text(text = if (language == AppLanguage.BANGLA) "এড়িয়ে যান" else "Skip")
                  }
                }
              )
            }

            // Detailed Restore Result Summary Dialog
            restoreResultDialogMessage?.let { dialogMsg ->
              AlertDialog(
                onDismissRequest = { restoreResultDialogMessage = null },
                title = {
                  Text(
                    text = if (language == AppLanguage.BANGLA) "ডাটা রিস্টোর বিবরণ" else "Data Restore Details",
                    fontWeight = FontWeight.Bold
                  )
                },
                text = {
                  Text(
                    text = dialogMsg,
                    style = MaterialTheme.typography.bodyMedium
                  )
                },
                confirmButton = {
                  Button(onClick = { restoreResultDialogMessage = null }) {
                    Text(text = if (language == AppLanguage.BANGLA) "ঠিক আছে" else "OK")
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}

// Retained for GreetingScreenshotTest compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}
