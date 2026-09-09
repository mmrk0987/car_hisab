package com.example.data.model

data class VehicleDocuments(
  val taxTokenExpiryMillis: Long = System.currentTimeMillis() + (45L * 24 * 60 * 60 * 1000), // 45 days remaining by default
  val taxTokenNumber: String = "TT-8923481",
  val fitnessExpiryMillis: Long = System.currentTimeMillis() + (120L * 24 * 60 * 60 * 1000), // 120 days remaining
  val fitnessNumber: String = "FIT-552910",
  val routePermitExpiryMillis: Long = System.currentTimeMillis() + (18L * 24 * 60 * 60 * 1000), // 18 days (Warning)
  val routePermitNumber: String = "RP-DH-9921",
  val insuranceExpiryMillis: Long = System.currentTimeMillis() + (210L * 24 * 60 * 60 * 1000), // 210 days
  val insuranceNumber: String = "INS-GR-34901",
  val drivingLicenseExpiryMillis: Long = System.currentTimeMillis() + (360L * 24 * 60 * 60 * 1000), // 1 year
  val drivingLicenseNumber: String = "DL-DK-118839"
)

data class MobilServiceInfo(
  val currentOdometerKm: Double = 48500.0,
  val lastMobilChangeKm: Double = 46000.0,
  val mobilChangeIntervalKm: Double = 3000.0,
  val lastMobilChangeDateMillis: Long = System.currentTimeMillis() - (25L * 24 * 60 * 60 * 1000),
  val mobilBrandGrade: String = "Castrol Magnatec 10W-40",
  val lastBrakeCheckKm: Double = 42000.0,
  val lastAirFilterKm: Double = 45000.0,
  val lastGearOilKm: Double = 35000.0,
  val generalNotes: String = "পরবর্তী সার্ভিসিংয়ে এসি ফিল্টার ও ব্রেক প্যাড চেক করাতে হবে।"
)
