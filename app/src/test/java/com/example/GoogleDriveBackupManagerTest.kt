package com.example

import com.example.data.drive.GoogleDriveBackupManager
import com.example.data.model.BookingEntity
import com.example.data.model.MobilServiceInfo
import com.example.data.model.TripEntity
import com.example.data.model.VehicleDocuments
import com.example.data.repository.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GoogleDriveBackupManagerTest {

  private val testProfile = UserProfile(
    carName = "Toyota Noah",
    carModel = "2021 Hybrid",
    carNumber = "Dhaka Metro CHA-55-1234",
    driverName = "Md. Rafiqul Islam",
    driverNameBangla = "মোঃ রফিকুল ইসলাম",
    driverPhone = "01711223344",
    driverEmail = "rafiqul.driver@gmail.com"
  )

  private val testTrips = listOf(
    TripEntity(
      id = 101,
      dateMillis = 1700000000000L,
      dateString = "15 Nov 2023",
      place = "Dhaka to Uttara",
      rent = 1200.0,
      gratuity = 100.0,
      maintenanceCost = 200.0,
      kmDriven = 18.5,
      description = "Evening airport drop",
      passengerName = "Rahim",
      passengerPhone = "01800000000",
      income = 1100.0,
      profit = 900.0
    )
  )

  private val testBookings = listOf(
    BookingEntity(
      id = 501,
      passengerName = "Karim Ahmed",
      passengerPhone = "01911223344",
      pickupLocation = "Dhanmondi",
      dropLocation = "Cox's Bazar",
      tripDateMillis = 1710000000000L,
      tripDateString = "10 Mar 2024",
      tripTimeString = "06:00 AM",
      totalFare = 15000.0,
      advancePaid = 3000.0,
      dueFare = 12000.0,
      status = "CONFIRMED",
      notes = "Family tour booking"
    )
  )

  private val testDocs = VehicleDocuments(
    taxTokenNumber = "TT-998877",
    taxTokenExpiryMillis = 1750000000000L,
    fitnessNumber = "FIT-112233",
    fitnessExpiryMillis = 1755000000000L,
    routePermitNumber = "RP-DH-4455",
    routePermitExpiryMillis = 1760000000000L,
    insuranceNumber = "INS-889900",
    insuranceExpiryMillis = 1765000000000L,
    drivingLicenseNumber = "DL-667788",
    drivingLicenseExpiryMillis = 1770000000000L
  )

  private val testMobil = MobilServiceInfo(
    currentOdometerKm = 52000.0,
    lastMobilChangeKm = 50000.0,
    mobilChangeIntervalKm = 3000.0,
    lastMobilChangeDateMillis = 1705000000000L,
    mobilBrandGrade = "Mobil 1 Synthetic 5W-30",
    lastBrakeCheckKm = 48000.0,
    lastAirFilterKm = 49000.0,
    lastGearOilKm = 45000.0,
    generalNotes = "Service OK"
  )

  @Test
  fun testSerializeAndParseFullBackupJson() {
    val jsonString = GoogleDriveBackupManager.serializeFullBackupJson(
      trips = testTrips,
      bookings = testBookings,
      profile = testProfile,
      documents = testDocs,
      mobilService = testMobil
    )

    assertNotNull(jsonString)
    assertTrue("JSON should contain version", jsonString.contains("\"version\": 2"))
    assertTrue("JSON should contain car number", jsonString.contains("Dhaka Metro CHA-55-1234"))
    assertTrue("JSON should contain trip place", jsonString.contains("Dhaka to Uttara"))
    assertTrue("JSON should contain booking passenger", jsonString.contains("Karim Ahmed"))
    assertTrue("JSON should contain tax token", jsonString.contains("TT-998877"))
    assertTrue("JSON should contain mobil brand", jsonString.contains("Mobil 1 Synthetic 5W-30"))

    val restoredPayload = GoogleDriveBackupManager.parseFullBackupJson(jsonString)

    assertNotNull(restoredPayload)
    assertEquals(1, restoredPayload.trips.size)
    assertEquals("Dhaka to Uttara", restoredPayload.trips[0].place)
    assertEquals(1100.0, restoredPayload.trips[0].income, 0.01)

    assertEquals(1, restoredPayload.bookings.size)
    assertEquals("Karim Ahmed", restoredPayload.bookings[0].passengerName)
    assertEquals(15000.0, restoredPayload.bookings[0].totalFare, 0.01)

    assertNotNull(restoredPayload.profile)
    assertEquals("Md. Rafiqul Islam", restoredPayload.profile?.driverName)
    assertEquals("01711223344", restoredPayload.profile?.driverPhone)

    assertNotNull(restoredPayload.vehicleDocuments)
    assertEquals("TT-998877", restoredPayload.vehicleDocuments?.taxTokenNumber)

    assertNotNull(restoredPayload.mobilServiceInfo)
    assertEquals("Mobil 1 Synthetic 5W-30", restoredPayload.mobilServiceInfo?.mobilBrandGrade)
  }

  @Test
  fun testEncryptionAndDecryptionRoundtrip() {
    val plainText = "{\"app\":\"CarHisab\",\"testKey\":\"testValue123\"}"
    val encrypted = GoogleDriveBackupManager.encryptData(plainText)

    assertTrue("Encrypted data should start with security header", encrypted.startsWith("CARHISAB_SECURE_V1:"))

    val decrypted = GoogleDriveBackupManager.decryptData(encrypted)
    assertEquals(plainText, decrypted)
  }

  @Test
  fun testParseFullBackupJson_emptyOrInvalidString_returnsEmptyPayloadWithoutCrashing() {
    val emptyPayload = GoogleDriveBackupManager.parseFullBackupJson("")
    assertNotNull(emptyPayload)
    assertTrue(emptyPayload.trips.isEmpty())
    assertTrue(emptyPayload.bookings.isEmpty())

    val invalidPayload = GoogleDriveBackupManager.parseFullBackupJson("Not a valid JSON string")
    assertNotNull(invalidPayload)
    assertTrue(invalidPayload.trips.isEmpty())
    assertTrue(invalidPayload.bookings.isEmpty())
  }

  @Test
  fun testParseLegacyV1TripsJson() {
    val legacyJson = GoogleDriveBackupManager.serializeBackupJson(testTrips, testProfile)
    val restoredPayload = GoogleDriveBackupManager.parseFullBackupJson(legacyJson)

    assertNotNull(restoredPayload)
    assertEquals(1, restoredPayload.trips.size)
    assertEquals("Dhaka to Uttara", restoredPayload.trips[0].place)
    assertNotNull(restoredPayload.profile)
  }
}
