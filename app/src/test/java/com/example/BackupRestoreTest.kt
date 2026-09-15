package com.example

import android.content.Context
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.TripEntity
import com.example.data.backup.BackupResult
import com.example.data.backup.GoogleDriveBackupManager
import com.example.data.backup.RestoreResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRestoreTest {

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
  fun testAutoBackupRulesXmlIncludesDatabaseAndSharedPref() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val parser = context.resources.getXml(R.xml.backup_rules)

    var hasDatabaseInclude = false
    var hasSharedPrefInclude = false
    var hasCacheExclude = false

    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
      if (eventType == XmlPullParser.START_TAG) {
        val tagName = parser.name
        val domain = parser.getAttributeValue(null, "domain")
        val path = parser.getAttributeValue(null, "path")

        if (tagName == "include" && domain == "database" && path == "car_hisab_database") {
          hasDatabaseInclude = true
        }
        if (tagName == "include" && domain == "sharedpref") {
          hasSharedPrefInclude = true
        }
        if (tagName == "exclude" && domain == "cache") {
          hasCacheExclude = true
        }
      }
      eventType = parser.next()
    }

    assertTrue("backup_rules.xml must include car_hisab_database", hasDatabaseInclude)
    assertTrue("backup_rules.xml must include sharedpref", hasSharedPrefInclude)
    assertTrue("backup_rules.xml must exclude cache", hasCacheExclude)
  }

  @Test
  fun testDatabaseTransactionalOperations() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // Clear old data
    db.withTransaction {
      db.tripDao().deleteAllTrips()
      db.bookingDao().deleteAllBookings()
      db.tripDao().insertTrips(testTrips)
      db.bookingDao().insertBookings(testBookings)
    }

    val restoredTrips = db.tripDao().getAllTripsSnapshot()
    val restoredBookings = db.bookingDao().getAllBookingsSnapshot()

    assertEquals(2, restoredTrips.size)
    assertEquals(1, restoredBookings.size)
    assertTrue("New trips should contain Dhaka to Gazipur", restoredTrips.any { it.place == "Dhaka to Gazipur" })
  }

  @Test
  fun testEmptyDatabaseVerificationAndPayloadCheck() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    db.withTransaction {
      db.tripDao().deleteAllTrips()
      db.bookingDao().deleteAllBookings()
    }

    val isNotEmpty = GoogleDriveBackupManager.verifyDatabaseNotEmpty(context)
    assertFalse("Empty database should fail verification", isNotEmpty)

    val backupResult = GoogleDriveBackupManager.performBackup(context, isBangla = true)
    assertTrue("Backup should return Empty for 0 user records", backupResult is BackupResult.Empty)
  }

  @Test
  fun testNonEmptyDatabaseBackupAndRestore() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    db.withTransaction {
      db.tripDao().deleteAllTrips()
      db.bookingDao().deleteAllBookings()
      db.tripDao().insertTrips(testTrips)
      db.bookingDao().insertBookings(testBookings)
    }

    val isNotEmpty = GoogleDriveBackupManager.verifyDatabaseNotEmpty(context)
    assertTrue("Non-empty database should pass verification", isNotEmpty)

    val backupResult = GoogleDriveBackupManager.performBackup(context, isBangla = true)
    assertTrue("Backup should succeed for non-empty payload", backupResult is BackupResult.Success)

    val metadata = (backupResult as BackupResult.Success).metadata
    assertTrue("Metadata exists should be true", metadata.exists)
    assertTrue("Date string should not be blank", metadata.dateString.isNotBlank())
    assertTrue("Size string should contain unit", metadata.sizeString.contains("কেবি") || metadata.sizeString.contains("এমবি"))

    val restoreResult = GoogleDriveBackupManager.restoreBackup(context, isBangla = true)
    assertTrue("Restore should succeed", restoreResult is RestoreResult.Success)

    val restoredSuccess = restoreResult as RestoreResult.Success
    assertEquals(2, restoredSuccess.tripsCount)
    assertEquals(1, restoredSuccess.bookingsCount)
  }
}
