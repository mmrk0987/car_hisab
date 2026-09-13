package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.MobilServiceInfo
import com.example.data.model.VehicleDocuments
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
  val carName: String = "",
  val carModel: String = "",
  val carNumber: String = "",
  val driverName: String = "",
  val driverNameBangla: String = "",
  val driverNameEnglish: String = "",
  val birthDate: String = "",
  val driverPhone: String = "",
  val driverEmail: String = "",
  val profileImageUri: String? = null,
  val rememberEmail: Boolean = false,
  val savedEmail: String = "",
  val nidFrontAttached: Boolean = false,
  val nidBackAttached: Boolean = false,
  val selfieAttached: Boolean = false,
  val isProfileCompleted: Boolean = false,
  val isLoggedIn: Boolean = false,
  val isFreeTrialActive: Boolean = true,
  val trialMonthsRemaining: Int = 6,
  val registrationDateMillis: Long = System.currentTimeMillis(),
  val subscriptionExpiryMillis: Long = Long.MAX_VALUE,
  val userUniqueKey: String = "CH-84920",
  val currentPlanName: String = "লাইফটাইম আনলিমিটেড"
)

class UserPreferencesRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("car_hisab_prefs", Context.MODE_PRIVATE)

  private val _languageFlow = MutableStateFlow(loadLanguage())
  val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

  private val _themeModeFlow = MutableStateFlow(loadThemeMode())
  val themeModeFlow: StateFlow<AppThemeMode> = _themeModeFlow.asStateFlow()

  private val _profileFlow = MutableStateFlow(loadProfile())
  val profileFlow: StateFlow<UserProfile> = _profileFlow.asStateFlow()

  private val _documentsFlow = MutableStateFlow(loadDocuments())
  val documentsFlow: StateFlow<VehicleDocuments> = _documentsFlow.asStateFlow()

  private val _mobilServiceFlow = MutableStateFlow(loadMobilService())
  val mobilServiceFlow: StateFlow<MobilServiceInfo> = _mobilServiceFlow.asStateFlow()

  private fun loadLanguage(): AppLanguage {
    val langStr = prefs.getString("key_language", AppLanguage.BANGLA.name)
    return try {
      AppLanguage.valueOf(langStr ?: AppLanguage.BANGLA.name)
    } catch (_: Exception) {
      AppLanguage.BANGLA
    }
  }

  fun setLanguage(language: AppLanguage) {
    prefs.edit().putString("key_language", language.name).apply()
    _languageFlow.value = language
  }

  private fun loadThemeMode(): AppThemeMode {
    val themeStr = prefs.getString("key_theme", AppThemeMode.DARK.name)
    val mode = try {
      AppThemeMode.valueOf(themeStr ?: AppThemeMode.DARK.name)
    } catch (_: Exception) {
      AppThemeMode.DARK
    }
    return if (mode == AppThemeMode.LIGHT) AppThemeMode.DARK else mode
  }

  fun setThemeMode(mode: AppThemeMode) {
    prefs.edit().putString("key_theme", mode.name).apply()
    _themeModeFlow.value = mode
  }

  private fun loadProfile(): UserProfile {
    val now = System.currentTimeMillis()
    var dName = prefs.getString("key_driver_name", "") ?: ""
    var dNameBn = prefs.getString("key_driver_name_bn", dName) ?: dName
    var dNameEn = prefs.getString("key_driver_name_en", "") ?: ""
    var carName = prefs.getString("key_car_name", "") ?: ""
    var carModel = prefs.getString("key_car_model", "") ?: ""
    var carNumber = prefs.getString("key_car_number", "") ?: ""
    var birthDate = prefs.getString("key_birthdate", "") ?: ""
    var driverPhone = prefs.getString("key_driver_phone", "") ?: ""

    // Strip out demo profile data if stored previously
    var shouldCleanPrefs = false
    if (dName.contains("রফিকুল") || dName.contains("Rafiqul") || dName == "Demo Driver") {
      dName = ""
      shouldCleanPrefs = true
    }
    if (dNameBn.contains("রফিকুল") || dNameBn.contains("Rafiqul") || dNameBn == "Demo Driver") {
      dNameBn = ""
      shouldCleanPrefs = true
    }
    if (dNameEn.contains("Rafiqul") || dNameEn.contains("রফিকুল") || dNameEn == "Demo Driver") {
      dNameEn = ""
      shouldCleanPrefs = true
    }
    if (carNumber == "ঢাকা মেট্রো চ-১১-২২৩৩") {
      carNumber = ""
      shouldCleanPrefs = true
    }
    if (carName == "Toyota Noah") {
      carName = ""
      shouldCleanPrefs = true
    }
    if (carModel == "2020") {
      carModel = ""
      shouldCleanPrefs = true
    }
    if (birthDate == "15/08/1990") {
      birthDate = ""
      shouldCleanPrefs = true
    }
    if (driverPhone == "01712345678") {
      driverPhone = ""
      shouldCleanPrefs = true
    }

    if (shouldCleanPrefs) {
      prefs.edit()
        .putString("key_driver_name", dName)
        .putString("key_driver_name_bn", dNameBn)
        .putString("key_driver_name_en", dNameEn)
        .putString("key_car_number", carNumber)
        .putString("key_car_name", carName)
        .putString("key_car_model", carModel)
        .putString("key_birthdate", birthDate)
        .putString("key_driver_phone", driverPhone)
        .apply()
    }

    return UserProfile(
      carName = carName,
      carModel = carModel,
      carNumber = carNumber,
      driverName = dName,
      driverNameBangla = dNameBn,
      driverNameEnglish = dNameEn,
      birthDate = birthDate,
      driverPhone = driverPhone,
      driverEmail = prefs.getString("key_driver_email", "") ?: "",
      profileImageUri = prefs.getString("key_profile_image_uri", null),
      rememberEmail = prefs.getBoolean("key_remember_email", false),
      savedEmail = prefs.getString("key_saved_email", "") ?: "",
      nidFrontAttached = prefs.getBoolean("key_nid_front", false),
      nidBackAttached = prefs.getBoolean("key_nid_back", false),
      selfieAttached = prefs.getBoolean("key_selfie", false),
      isProfileCompleted = prefs.getBoolean("key_profile_completed", false),
      isLoggedIn = prefs.getBoolean("key_logged_in", false),
      isFreeTrialActive = true,
      trialMonthsRemaining = 6,
      registrationDateMillis = prefs.getLong("key_reg_date", now),
      subscriptionExpiryMillis = Long.MAX_VALUE,
      userUniqueKey = prefs.getString("key_user_unique_key", "CH-84920") ?: "CH-84920",
      currentPlanName = "লাইফটাইম আনলিমিটেড"
    )
  }

  fun updateProfile(profile: UserProfile) {
    prefs.edit()
      .putString("key_car_name", profile.carName)
      .putString("key_car_model", profile.carModel)
      .putString("key_car_number", profile.carNumber)
      .putString("key_driver_name", profile.driverName)
      .putString("key_driver_name_bn", profile.driverNameBangla)
      .putString("key_driver_name_en", profile.driverNameEnglish)
      .putString("key_birthdate", profile.birthDate)
      .putString("key_driver_phone", profile.driverPhone)
      .putString("key_driver_email", profile.driverEmail)
      .putString("key_profile_image_uri", profile.profileImageUri)
      .putBoolean("key_remember_email", profile.rememberEmail)
      .putString("key_saved_email", profile.savedEmail)
      .putBoolean("key_nid_front", profile.nidFrontAttached)
      .putBoolean("key_nid_back", profile.nidBackAttached)
      .putBoolean("key_selfie", profile.selfieAttached)
      .putBoolean("key_profile_completed", profile.isProfileCompleted)
      .putBoolean("key_logged_in", profile.isLoggedIn)
      .putBoolean("key_free_trial", true)
      .putInt("key_trial_months", 6)
      .putLong("key_reg_date", profile.registrationDateMillis)
      .putLong("key_subscription_expiry", Long.MAX_VALUE)
      .putString("key_user_unique_key", profile.userUniqueKey)
      .putString("key_current_plan_name", "লাইফটাইম আনলিমিটেড")
      .apply()
    _profileFlow.value = profile
  }

  private val _autoWeeklyBackupReminderFlow = MutableStateFlow(isAutoWeeklyBackupReminderEnabled())
  val autoWeeklyBackupReminderFlow: StateFlow<Boolean> = _autoWeeklyBackupReminderFlow.asStateFlow()

  fun isAutoWeeklyBackupReminderEnabled(): Boolean {
    return prefs.getBoolean("key_auto_weekly_backup_reminder", true)
  }

  fun setAutoWeeklyBackupReminderEnabled(enabled: Boolean) {
    prefs.edit().putBoolean("key_auto_weekly_backup_reminder", enabled).apply()
    _autoWeeklyBackupReminderFlow.value = enabled
  }

  fun getLastDriveBackupTime(): Long {
    return prefs.getLong("key_last_drive_backup_time", 0L)
  }

  fun setLastDriveBackupTime(timeMillis: Long) {
    prefs.edit().putLong("key_last_drive_backup_time", timeMillis).apply()
  }

  fun getLastBackupTripCount(): Int {
    return prefs.getInt("key_last_drive_backup_count", 0)
  }

  fun setLastBackupTripCount(count: Int) {
    prefs.edit().putInt("key_last_drive_backup_count", count).apply()
  }

  private fun loadDocuments(): VehicleDocuments {
    val now = System.currentTimeMillis()
    return VehicleDocuments(
      taxTokenExpiryMillis = prefs.getLong("key_doc_tax_token_expiry", now + (365L * 24 * 60 * 60 * 1000)),
      taxTokenNumber = prefs.getString("key_doc_tax_token_num", "") ?: "",
      fitnessExpiryMillis = prefs.getLong("key_doc_fitness_expiry", now + (365L * 24 * 60 * 60 * 1000)),
      fitnessNumber = prefs.getString("key_doc_fitness_num", "") ?: "",
      routePermitExpiryMillis = prefs.getLong("key_doc_route_permit_expiry", now + (365L * 24 * 60 * 60 * 1000)),
      routePermitNumber = prefs.getString("key_doc_route_permit_num", "") ?: "",
      insuranceExpiryMillis = prefs.getLong("key_doc_insurance_expiry", now + (365L * 24 * 60 * 60 * 1000)),
      insuranceNumber = prefs.getString("key_doc_insurance_num", "") ?: "",
      drivingLicenseExpiryMillis = prefs.getLong("key_doc_license_expiry", now + (365L * 24 * 60 * 60 * 1000)),
      drivingLicenseNumber = prefs.getString("key_doc_license_num", "") ?: ""
    )
  }

  fun updateDocuments(docs: VehicleDocuments) {
    prefs.edit()
      .putLong("key_doc_tax_token_expiry", docs.taxTokenExpiryMillis)
      .putString("key_doc_tax_token_num", docs.taxTokenNumber)
      .putLong("key_doc_fitness_expiry", docs.fitnessExpiryMillis)
      .putString("key_doc_fitness_num", docs.fitnessNumber)
      .putLong("key_doc_route_permit_expiry", docs.routePermitExpiryMillis)
      .putString("key_doc_route_permit_num", docs.routePermitNumber)
      .putLong("key_doc_insurance_expiry", docs.insuranceExpiryMillis)
      .putString("key_doc_insurance_num", docs.insuranceNumber)
      .putLong("key_doc_license_expiry", docs.drivingLicenseExpiryMillis)
      .putString("key_doc_license_num", docs.drivingLicenseNumber)
      .apply()
    _documentsFlow.value = docs
  }

  private fun loadMobilService(): MobilServiceInfo {
    val now = System.currentTimeMillis()
    return MobilServiceInfo(
      currentOdometerKm = prefs.getFloat("key_service_odometer", 0f).toDouble(),
      lastMobilChangeKm = prefs.getFloat("key_service_last_mobil_km", 0f).toDouble(),
      mobilChangeIntervalKm = prefs.getFloat("key_service_mobil_interval", 3000f).toDouble(),
      lastMobilChangeDateMillis = prefs.getLong("key_service_last_mobil_date", now),
      mobilBrandGrade = prefs.getString("key_service_mobil_brand", "") ?: "",
      lastBrakeCheckKm = prefs.getFloat("key_service_brake_km", 0f).toDouble(),
      lastAirFilterKm = prefs.getFloat("key_service_air_filter_km", 0f).toDouble(),
      lastGearOilKm = prefs.getFloat("key_service_gear_oil_km", 0f).toDouble(),
      generalNotes = prefs.getString("key_service_notes", "") ?: ""
    )
  }

  fun updateMobilService(service: MobilServiceInfo) {
    prefs.edit()
      .putFloat("key_service_odometer", service.currentOdometerKm.toFloat())
      .putFloat("key_service_last_mobil_change_km", service.lastMobilChangeKm.toFloat())
      .putFloat("key_service_mobil_interval", service.mobilChangeIntervalKm.toFloat())
      .putLong("key_service_last_mobil_date", service.lastMobilChangeDateMillis)
      .putString("key_service_mobil_brand", service.mobilBrandGrade)
      .putFloat("key_service_brake_km", service.lastBrakeCheckKm.toFloat())
      .putFloat("key_service_air_filter_km", service.lastAirFilterKm.toFloat())
      .putFloat("key_service_gear_oil_km", service.lastGearOilKm.toFloat())
      .putString("key_service_notes", service.generalNotes)
      .apply()
    _mobilServiceFlow.value = service
  }
}
