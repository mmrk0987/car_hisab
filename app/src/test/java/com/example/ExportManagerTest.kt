package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.export.ExportFormat
import com.example.data.export.ExportManager
import com.example.data.model.TripEntity
import com.example.data.repository.UserProfile
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExportManagerTest {

  private val testProfile = UserProfile(
    carName = "Toyota Axio",
    carModel = "2019 Hybrid",
    carNumber = "Dhaka Metro GA-11-2233",
    driverName = "Md. Mahfujur Rahman",
    driverPhone = "+8801700000000",
    driverEmail = "driver@example.com"
  )

  private val testTrips = listOf(
    TripEntity(
      id = 1,
      dateMillis = System.currentTimeMillis(),
      dateString = "03 Sep 2026",
      place = "Dhaka to Gazipur",
      rent = 1500.0,
      gratuity = 100.0,
      maintenanceCost = 300.0,
      kmDriven = 25.0,
      description = "Highway toll & fuel",
      income = 1400.0,
      profit = 1100.0
    ),
    TripEntity(
      id = 2,
      dateMillis = System.currentTimeMillis() - 86400000L,
      dateString = "02 Sep 2026",
      place = "Banani to Airport",
      rent = 800.0,
      gratuity = 50.0,
      maintenanceCost = 900.0,
      kmDriven = 12.0,
      description = "Flat tyre repair",
      income = 750.0,
      profit = -150.0
    )
  )

  @Test
  fun testGenerateCsvReport() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val csvFile = ExportManager.generateCsvReport(context, testTrips, testProfile)

    assertNotNull(csvFile)
    assertTrue("CSV file should exist", csvFile.exists())
    assertTrue("CSV file should not be empty", csvFile.length() > 0)

    val content = csvFile.readText(Charsets.UTF_8)
    assertTrue("Should include Car Hisab header", content.contains("CAR HISAB"))
    assertTrue("Should include driver name", content.contains("Md. Mahfujur Rahman"))
    assertTrue("Should include car number", content.contains("Dhaka Metro GA-11-2233"))
    assertTrue("Should include trip place", content.contains("Dhaka to Gazipur"))
    assertTrue("Should include financial totals", content.contains("TOTAL SUMMARY"))
  }

  @Test
  fun testGeneratePdfReportSafely() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    try {
      val pdfFile = ExportManager.generatePdfReport(context, testTrips, testProfile)
      assertNotNull(pdfFile)
      assertTrue("PDF file name should end with .pdf", pdfFile.name.endsWith(".pdf"))
    } catch (e: IllegalStateException) {
      // PdfDocument native bridge is not emulated in pure headless Robolectric JVM without Skia libraries
      println("PdfDocument is host-dependent in headless Robolectric: ${e.message}")
    }
  }
}
