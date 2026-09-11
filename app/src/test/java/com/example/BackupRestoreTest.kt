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
@Config(sdk = [34])
class BackupRestoreTest {

  private val testProfile = UserProfile(
    carName = "Toyota Axio",
    carModel = "2020 Hybrid",
    carNumber = "Dhaka Metro GA-11-2233",
    driverName = "Md. Mahfujur Rahman",
    driverNameBangla = "মোঃ মাহফুজুর রহমান",
    driverPhone = "+8801700000000",
    driverEmail = "driver@example.com",
    userUniqueKey = "CH-99999"
  )

  private val testDocuments = VehicleDocuments(
    taxTokenExpiryMillis = 1750000000000L,
    taxTokenNumber = "TT-123456",
    fitnessExpiryMillis = 1750000000000L,
    fitnessNumber = "FIT-654321",
    routePermitExpiryMillis = 1750000000000L,
    routePermitNumber = "RP-987654",
    insuranceExpiryMillis = 1750000000000L,
    insuranceNumber = "INS-456789",
    drivingLicenseExpiryMillis = 1750000000000L,
    drivingLicenseNumber = "DL-112233"
  )

  private val testMobilService = MobilServiceInfo(
    currentOdometerKm = 45200.0,
    lastMobilChangeKm = 42000.0,
    mobilChangeIntervalKm = 3000.0,
    lastMobilChangeDateMillis = 1720000000000L,
    mobilBrandGrade = "Mobil 1 5W-30",
    generalNotes = "Next filter replacement at 48000 km"
  )

  private val testTrips = listOf(
    TripEntity(
      id = 101L,
      dateMillis = 1725350400000L,
      dateString = "03 Sep 2026",
      place = "Dhaka to Gazipur",
      rent = 2500.0,
      gratuity = 200.0,
      maintenanceCost = 300.0,
      kmDriven = 35.0,
      description = "Highway toll & CNG",
      income = 2300.0,
      profit = 2000.0
    ),
    TripEntity(
      id = 102L,
      dateMillis = 1725264000000L,
      dateString = "02 Sep 2026",
      place = "Airport to Uttara",
      rent = 1200.0,
      gratuity = 100.0,
      maintenanceCost = 150.0,
      kmDriven = 12.0,
      description = "Airport passenger",
      income = 1100.0,
      profit = 950.0
    )
  )

  private val testBookings = listOf(
    BookingEntity(
      id = 501L,
      passengerName = "Karim Ahmed",
      passengerPhone = "01800000000",
      pickupLocation = "Dhanmondi",
      dropLocation = "Cox's Bazar",
      tripDateMillis = 1726000000000L,
      tripDateString = "10 Sep 2026",
      tripTimeString = "06:00 AM",
      totalFare = 15000.0,
      advancePaid = 3000.0,
      dueFare = 12000.0,
      status = "CONFIRMED",
      notes = "3 day tour reservation"
    )
  )

  @Test
  fun testSerializeAndParseBackupJson() {
    val jsonString = GoogleDriveBackupManager.serializeBackupJson(
      trips = testTrips,
      profile = testProfile,
      documents = testDocuments,
      mobilService = testMobilService,
      bookings = testBookings
    )

    assertNotNull(jsonString)
    assertTrue("JSON string should contain app name", jsonString.contains("CarHisab"))
    assertTrue("JSON string should contain driver name", jsonString.contains("Md. Mahfujur Rahman"))
    assertTrue("JSON string should contain Dhaka to Gazipur", jsonString.contains("Dhaka to Gazipur"))

    val parsedPayload = GoogleDriveBackupManager.parseBackupPayload(jsonString)

    assertNotNull(parsedPayload)
    assertEquals(2, parsedPayload.trips.size)
    assertEquals(1, parsedPayload.bookings.size)

    val restoredTrip = parsedPayload.trips.first { it.id == 101L }
    assertEquals("Dhaka to Gazipur", restoredTrip.place)
    assertEquals(2500.0, restoredTrip.rent, 0.01)
    assertEquals(2000.0, restoredTrip.profit, 0.01)

    val restoredBooking = parsedPayload.bookings.first { it.id == 501L }
    assertEquals("Karim Ahmed", restoredBooking.passengerName)
    assertEquals(15000.0, restoredBooking.totalFare, 0.01)

    assertNotNull(parsedPayload.profile)
    assertEquals("Toyota Axio", parsedPayload.profile?.carName)
    assertEquals("Dhaka Metro GA-11-2233", parsedPayload.profile?.carNumber)

    assertNotNull(parsedPayload.documents)
    assertEquals("TT-123456", parsedPayload.documents?.taxTokenNumber)

    assertNotNull(parsedPayload.mobilService)
    assertEquals(45200.0, parsedPayload.mobilService?.currentOdometerKm ?: 0.0, 0.01)
    assertEquals("Mobil 1 5W-30", parsedPayload.mobilService?.mobilBrandGrade)
  }

  @Test
  fun testCorruptedJsonDoesNotCrash() {
    val invalidJson = "{ corrupt_json_content: [unclosed "

    val parsedPayload = GoogleDriveBackupManager.parseBackupPayload(invalidJson)

    assertNotNull(parsedPayload)
    assertTrue("Trips list should be empty for corrupted JSON", parsedPayload.trips.isEmpty())
    assertTrue("Bookings list should be empty for corrupted JSON", parsedPayload.bookings.isEmpty())
  }

  @Test
  fun testEmptyJsonReturnsEmptyPayloadWithoutCrashing() {
    val emptyJson = "{}"

    val parsedPayload = GoogleDriveBackupManager.parseBackupPayload(emptyJson)

    assertNotNull(parsedPayload)
    assertTrue(parsedPayload.trips.isEmpty())
    assertTrue(parsedPayload.bookings.isEmpty())
  }
}
