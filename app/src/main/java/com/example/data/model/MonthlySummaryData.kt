package com.example.data.model

data class DayTrend(
  val dayOfMonth: Int,
  val dateLabel: String,
  val income: Double,
  val expense: Double,
  val profit: Double,
  val tripsCount: Int
)

data class WeekTrend(
  val weekNumber: Int,
  val weekLabel: String,
  val dateRangeLabel: String,
  val income: Double,
  val expense: Double,
  val profit: Double,
  val tripsCount: Int
)

data class MonthlySummaryData(
  val year: Int = 0,
  val month: Int = 0,
  val monthName: String = "",
  val totalGrossRent: Double = 0.0,
  val totalIncome: Double = 0.0,
  val totalMaintenance: Double = 0.0,
  val totalGratuity: Double = 0.0,
  val totalExpense: Double = 0.0,
  val totalProfit: Double = 0.0,
  val profitMargin: Double = 0.0,
  val totalTrips: Int = 0,
  val totalKm: Double = 0.0,
  val avgProfitPerTrip: Double = 0.0,
  val peakEarningDate: String = "",
  val peakEarningAmount: Double = 0.0,
  val dailyTrends: List<DayTrend> = emptyList(),
  val weeklyTrends: List<WeekTrend> = emptyList(),
  val cumulativeProfits: List<Pair<String, Double>> = emptyList(),
  val hasTrips: Boolean = false
)
