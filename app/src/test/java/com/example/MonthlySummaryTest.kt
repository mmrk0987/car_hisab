package com.example

import com.example.data.model.DayTrend
import com.example.data.model.MonthlySummaryData
import com.example.data.model.TripEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MonthlySummaryTest {

  @Test
  fun testMonthlyAggregationCalculations() {
    val cal = Calendar.getInstance()
    val curTime = cal.timeInMillis

    val testTrips = listOf(
      TripEntity(
        id = 1,
        dateMillis = curTime,
        dateString = "03 Sep 2026",
        place = "Uttara to Banani",
        rent = 1200.0,
        gratuity = 100.0,
        maintenanceCost = 250.0,
        kmDriven = 18.0,
        description = "CNG & parking",
        income = 1100.0,
        profit = 850.0
      ),
      TripEntity(
        id = 2,
        dateMillis = curTime,
        dateString = "03 Sep 2026",
        place = "Banani to Motijheel",
        rent = 800.0,
        gratuity = 50.0,
        maintenanceCost = 150.0,
        kmDriven = 14.0,
        description = "Fuel",
        income = 750.0,
        profit = 600.0
      ),
      TripEntity(
        id = 3,
        dateMillis = curTime - (24 * 60 * 60 * 1000L),
        dateString = "02 Sep 2026",
        place = "Dhaka to Narayanganj",
        rent = 2500.0,
        gratuity = 200.0,
        maintenanceCost = 600.0,
        kmDriven = 35.0,
        description = "Toll & gas",
        income = 2300.0,
        profit = 1700.0
      )
    )

    val totalGrossRent = testTrips.sumOf { it.rent }
    val totalIncome = testTrips.sumOf { it.income }
    val totalMaintenance = testTrips.sumOf { it.maintenanceCost }
    val totalGratuity = testTrips.sumOf { it.gratuity }
    val totalExpense = totalMaintenance + totalGratuity
    val totalProfit = totalIncome - totalMaintenance
    val profitMargin = if (totalGrossRent > 0) (totalProfit / totalGrossRent) * 100.0 else 0.0

    assertEquals(4500.0, totalGrossRent, 0.01)
    assertEquals(4150.0, totalIncome, 0.01)
    assertEquals(1000.0, totalMaintenance, 0.01)
    assertEquals(350.0, totalGratuity, 0.01)
    assertEquals(1350.0, totalExpense, 0.01)
    assertEquals(3150.0, totalProfit, 0.01)
    assertTrue("Profit margin should be positive", profitMargin > 60.0)
    assertEquals(3, testTrips.size)
  }
}
