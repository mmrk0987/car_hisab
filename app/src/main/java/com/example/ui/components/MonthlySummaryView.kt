package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayTrend
import com.example.data.model.MonthlySummaryData
import com.example.data.model.WeekTrend
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private enum class ChartTab {
  INCOME_EXPENSE,
  EXPENSE_RATIO,
  PROFIT_CURVE
}

@Composable
fun MonthlySummaryView(
  summary: MonthlySummaryData,
  language: AppLanguage,
  selectedMonthOffset: Int,
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onResetToCurrentMonth: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(ChartTab.INCOME_EXPENSE) }
  var trendTimeMode by remember { mutableStateOf("daily") } // "daily" or "weekly"

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("monthly_summary_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // 1. Header with Month Navigator & Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PrimaryEmerald.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = PrimaryEmerald,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = AppStrings.monthlySummaryTitle(language),
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = AppStrings.monthlySummarySubtitle(language),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Month Selector Badge with Navigation Arrows
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          border = null
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            IconButton(
              onClick = onPrevMonth,
              modifier = Modifier
                .size(32.dp)
                .testTag("btn_prev_month")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Month",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clickable(enabled = selectedMonthOffset != 0) { onResetToCurrentMonth() }
                .padding(horizontal = 6.dp)
            ) {
              Text(
                text = summary.monthName.ifBlank { "Current Month" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              if (selectedMonthOffset == 0) {
                Text(
                  text = AppStrings.currentMonthBadge(language),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = PrimaryEmerald
                )
              } else {
                Text(
                  text = if (language == AppLanguage.BANGLA) "আজকে ফিরুন" else "Reset to Now",
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            IconButton(
              onClick = onNextMonth,
              modifier = Modifier
                .size(32.dp)
                .testTag("btn_next_month")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Month",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Aggregated Financial KPI Cards (3 Cards: Income, Expense, Profit)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Income Metric
        FinancialKpiCard(
          title = AppStrings.totalGrossIncome(language),
          amount = summary.totalIncome,
          subtext = "${if (language == AppLanguage.BANGLA) "ভাড়া" else "Rent"}: ৳${String.format(Locale.US, "%.0f", summary.totalGrossRent)}",
          icon = Icons.AutoMirrored.Filled.TrendingUp,
          accentColor = PrimaryEmerald,
          containerColor = PrimaryEmerald.copy(alpha = 0.1f),
          modifier = Modifier.weight(1f),
          testTag = "kpi_monthly_income"
        )

        // Expense Metric
        FinancialKpiCard(
          title = AppStrings.totalExpensesAggregate(language),
          amount = summary.totalExpense,
          subtext = "${if (language == AppLanguage.BANGLA) "মেরামত" else "Maint"}: ৳${String.format(Locale.US, "%.0f", summary.totalMaintenance)}",
          icon = Icons.Default.Build,
          accentColor = WarningAmber,
          containerColor = WarningAmber.copy(alpha = 0.1f),
          modifier = Modifier.weight(1f),
          testTag = "kpi_monthly_expense"
        )

        // Profit Metric
        val isNetProfit = summary.totalProfit >= 0
        FinancialKpiCard(
          title = AppStrings.netProfitShare(language),
          amount = summary.totalProfit,
          subtext = "${AppStrings.netProfitMargin(language)}: ${String.format(Locale.US, "%.1f", summary.profitMargin)}%",
          icon = if (isNetProfit) Icons.Default.NorthEast else Icons.Default.SouthEast,
          accentColor = if (isNetProfit) ProfitGreen else LossRed,
          containerColor = if (isNetProfit) ProfitGreen.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f),
          modifier = Modifier.weight(1f),
          testTag = "kpi_monthly_profit"
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (!summary.hasTrips) {
        // Empty State when month has no logged trips
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.ShowChart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = AppStrings.noTripsThisMonth(language),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = AppStrings.noTripsThisMonthDesc(language),
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        // 3. Chart Navigation Mode Tabs
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(3.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          ChartTabButton(
            title = AppStrings.monthlyTrendsTab(language),
            icon = Icons.Default.ShowChart,
            selected = selectedTab == ChartTab.INCOME_EXPENSE,
            onClick = { selectedTab = ChartTab.INCOME_EXPENSE },
            modifier = Modifier.weight(1f)
          )
          ChartTabButton(
            title = AppStrings.monthlyBreakdownTab(language),
            icon = Icons.Default.PieChart,
            selected = selectedTab == ChartTab.EXPENSE_RATIO,
            onClick = { selectedTab = ChartTab.EXPENSE_RATIO },
            modifier = Modifier.weight(1f)
          )
          ChartTabButton(
            title = AppStrings.monthlyProfitCurveTab(language),
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            selected = selectedTab == ChartTab.PROFIT_CURVE,
            onClick = { selectedTab = ChartTab.PROFIT_CURVE },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Interactive Visual Chart Canvas View
        Crossfade(
          targetState = selectedTab,
          animationSpec = tween(250),
          label = "chart_tab_crossfade"
        ) { tab ->
          when (tab) {
            ChartTab.INCOME_EXPENSE -> {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Secondary granularity toggle: Daily vs Weekly
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    // Legend
                    ChartLegendDot(color = PrimaryEmerald, label = AppStrings.totalGrossIncome(language))
                    Spacer(modifier = Modifier.width(10.dp))
                    ChartLegendDot(color = WarningAmber, label = AppStrings.totalExpensesAggregate(language))
                  }

                  // Daily vs Weekly Pill
                  Row(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                      .padding(2.dp)
                  ) {
                    val dailySelected = trendTimeMode == "daily"
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (dailySelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { trendTimeMode = "daily" }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "দৈনিক" else "Daily",
                        fontSize = 11.sp,
                        fontWeight = if (dailySelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (dailySelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!dailySelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { trendTimeMode = "weekly" }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text(
                        text = if (language == AppLanguage.BANGLA) "সাপ্তাহিক" else "Weekly",
                        fontSize = 11.sp,
                        fontWeight = if (!dailySelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (!dailySelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (trendTimeMode == "daily") {
                  IncomeExpenseDailyBarChart(
                    dailyTrends = summary.dailyTrends,
                    language = language
                  )
                } else {
                  IncomeExpenseWeeklyBarChart(
                    weeklyTrends = summary.weeklyTrends,
                    language = language
                  )
                }
              }
            }

            ChartTab.EXPENSE_RATIO -> {
              ExpenseRatioDonutChart(
                summary = summary,
                language = language
              )
            }

            ChartTab.PROFIT_CURVE -> {
              ProfitTrajectoryCurveChart(
                dailyTrends = summary.dailyTrends,
                language = language
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Monthly Highlights Chips (Peak Earning Day, Avg Profit per Trip, Total Distance)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (summary.peakEarningDate.isNotBlank()) {
            InsightChip(
              icon = Icons.Default.Star,
              iconColor = Color(0xFFEAB308),
              label = AppStrings.peakDayLabel(language),
              value = "${summary.peakEarningDate} • ৳${String.format(Locale.US, "%.0f", summary.peakEarningAmount)}"
            )
          }

          InsightChip(
            icon = Icons.Default.Route,
            iconColor = PrimaryEmerald,
            label = AppStrings.avgProfitPerTripLabel(language),
            value = "৳${String.format(Locale.US, "%.0f", summary.avgProfitPerTrip)} / ${if (language == AppLanguage.BANGLA) "ট্রিপ" else "trip"}"
          )

          InsightChip(
            icon = Icons.Default.Speed,
            iconColor = WarningAmber,
            label = if (language == AppLanguage.BANGLA) "চলতি মাসে ভ্রমণ" else "Month Distance",
            value = "${String.format(Locale.US, "%.1f", summary.totalKm)} KM"
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Component: Financial KPI Stat Card
// -------------------------------------------------------------
@Composable
private fun FinancialKpiCard(
  title: String,
  amount: Double,
  subtext: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  containerColor: Color,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  Card(
    modifier = modifier.testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(15.dp)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "৳${String.format(Locale.US, "%,.0f", amount)}",
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = accentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = subtext,
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

// -------------------------------------------------------------
// Component: Chart Tab Toggle Button
// -------------------------------------------------------------
@Composable
private fun ChartTabButton(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val backgroundColor by animateColorAsState(
    targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
    label = "tab_bg"
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    label = "tab_content"
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(9.dp))
      .background(backgroundColor)
      .clickable { onClick() }
      .padding(vertical = 7.dp, horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.Medium
    )
  }
}

// -------------------------------------------------------------
// Chart 1A: Daily Income vs Expense Bar Chart with Touch Tooltip
// -------------------------------------------------------------
@Composable
private fun IncomeExpenseDailyBarChart(
  dailyTrends: List<DayTrend>,
  language: AppLanguage
) {
  var selectedIndex by remember { mutableIntStateOf(-1) }
  val activeIndex = if (selectedIndex in dailyTrends.indices) selectedIndex else -1

  val maxVal = max(
    100.0,
    dailyTrends.maxOfOrNull { max(it.income, it.expense) } ?: 100.0
  )

  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

  Column(modifier = Modifier.fillMaxWidth()) {
    // Tooltip Banner when a bar is pressed
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(28.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (activeIndex != -1 && activeIndex < dailyTrends.size) {
        val dt = dailyTrends[activeIndex]
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${dt.dateLabel}: ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "${if (language == AppLanguage.BANGLA) "আয়" else "Inc"}: ৳${String.format(Locale.US, "%.0f", dt.income)}  ",
            fontSize = 11.sp,
            color = PrimaryEmerald,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${if (language == AppLanguage.BANGLA) "ব্যয়" else "Exp"}: ৳${String.format(Locale.US, "%.0f", dt.expense)}  ",
            fontSize = 11.sp,
            color = WarningAmber,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${if (language == AppLanguage.BANGLA) "লাভ" else "Prof"}: ৳${String.format(Locale.US, "%.0f", dt.profit)}",
            fontSize = 11.sp,
            color = if (dt.profit >= 0) ProfitGreen else LossRed,
            fontWeight = FontWeight.Bold
          )
        }
      } else {
        Text(
          text = AppStrings.touchForDetails(language),
          fontSize = 10.sp,
          color = onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Canvas Bar Chart
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
    ) {
      val width = constraints.maxWidth.toFloat()
      val height = constraints.maxHeight.toFloat()
      val chartHeight = height - 26f // leave space for bottom date labels

      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(dailyTrends) {
            detectTapGestures(
              onPress = { offset ->
                if (dailyTrends.isNotEmpty()) {
                  val slotWidth = width / dailyTrends.size
                  val clicked = (offset.x / slotWidth).toInt().coerceIn(0, dailyTrends.size - 1)
                  selectedIndex = clicked
                }
              },
              onTap = { offset ->
                if (dailyTrends.isNotEmpty()) {
                  val slotWidth = width / dailyTrends.size
                  val clicked = (offset.x / slotWidth).toInt().coerceIn(0, dailyTrends.size - 1)
                  selectedIndex = if (selectedIndex == clicked) -1 else clicked
                }
              }
            )
          }
      ) {
        // Grid reference lines (0%, 50%, 100%)
        val lineSteps = 3
        for (i in 0..lineSteps) {
          val y = chartHeight * (i.toFloat() / lineSteps)
          drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
          )
        }

        val count = dailyTrends.size
        if (count > 0) {
          val slotWidth = width / count
          val barWidth = min(slotWidth * 0.35f, 16.dp.toPx())
          val barSpacing = 2.dp.toPx()

          dailyTrends.forEachIndexed { index, item ->
            val centerX = slotWidth * index + (slotWidth / 2f)
            val isSelected = index == activeIndex

            if (isSelected) {
              // Highlight column band
              drawRoundRect(
                color = onSurfaceVariant.copy(alpha = 0.08f),
                topLeft = Offset(slotWidth * index + 2f, 0f),
                size = Size(slotWidth - 4f, chartHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
              )
            }

            // Income bar (Left, Emerald)
            val incomeH = ((item.income / maxVal) * chartHeight).toFloat().coerceAtLeast(2f)
            val incomeTop = chartHeight - incomeH
            val incomeX = centerX - barWidth - (barSpacing / 2f)

            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(PrimaryEmerald, PrimaryEmerald.copy(alpha = 0.7f)),
                startY = incomeTop,
                endY = chartHeight
              ),
              topLeft = Offset(incomeX, incomeTop),
              size = Size(barWidth, incomeH),
              cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Expense bar (Right, Amber/Red)
            val expenseH = ((item.expense / maxVal) * chartHeight).toFloat().coerceAtLeast(2f)
            val expenseTop = chartHeight - expenseH
            val expenseX = centerX + (barSpacing / 2f)

            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(WarningAmber, WarningAmber.copy(alpha = 0.7f)),
                startY = expenseTop,
                endY = chartHeight
              ),
              topLeft = Offset(expenseX, expenseTop),
              size = Size(barWidth, expenseH),
              cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
          }
        }
      }

      // Day of month labels along bottom
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .height(22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        dailyTrends.forEachIndexed { idx, item ->
          val isSelected = idx == activeIndex
          Text(
            text = item.dateLabel.take(2), // e.g. "01", "02"
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) PrimaryEmerald else onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Chart 1B: Weekly Income vs Expense Bar Chart
// -------------------------------------------------------------
@Composable
private fun IncomeExpenseWeeklyBarChart(
  weeklyTrends: List<WeekTrend>,
  language: AppLanguage
) {
  var selectedIndex by remember { mutableIntStateOf(-1) }
  val activeIndex = if (selectedIndex in weeklyTrends.indices) selectedIndex else -1

  val maxVal = max(
    100.0,
    weeklyTrends.maxOfOrNull { max(it.income, it.expense) } ?: 100.0
  )

  val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

  Column(modifier = Modifier.fillMaxWidth()) {
    // Tooltip Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(28.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (activeIndex != -1 && activeIndex < weeklyTrends.size) {
        val wt = weeklyTrends[activeIndex]
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${wt.weekLabel} (${wt.dateRangeLabel}): ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "Inc: ৳${String.format(Locale.US, "%.0f", wt.income)}  ",
            fontSize = 11.sp,
            color = PrimaryEmerald,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Exp: ৳${String.format(Locale.US, "%.0f", wt.expense)}  ",
            fontSize = 11.sp,
            color = WarningAmber,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Profit: ৳${String.format(Locale.US, "%.0f", wt.profit)}",
            fontSize = 11.sp,
            color = if (wt.profit >= 0) ProfitGreen else LossRed,
            fontWeight = FontWeight.Bold
          )
        }
      } else {
        Text(
          text = AppStrings.touchForDetails(language),
          fontSize = 10.sp,
          color = onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
    ) {
      val width = constraints.maxWidth.toFloat()
      val height = constraints.maxHeight.toFloat()
      val chartHeight = height - 26f

      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(weeklyTrends) {
            detectTapGestures(
              onTap = { offset ->
                if (weeklyTrends.isNotEmpty()) {
                  val slotWidth = width / weeklyTrends.size
                  val clicked = (offset.x / slotWidth).toInt().coerceIn(0, weeklyTrends.size - 1)
                  selectedIndex = if (selectedIndex == clicked) -1 else clicked
                }
              }
            )
          }
      ) {
        val lineSteps = 3
        for (i in 0..lineSteps) {
          val y = chartHeight * (i.toFloat() / lineSteps)
          drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
          )
        }

        val count = weeklyTrends.size
        if (count > 0) {
          val slotWidth = width / count
          val barWidth = min(slotWidth * 0.32f, 24.dp.toPx())
          val barSpacing = 4.dp.toPx()

          weeklyTrends.forEachIndexed { index, item ->
            val centerX = slotWidth * index + (slotWidth / 2f)
            val isSelected = index == activeIndex

            if (isSelected) {
              drawRoundRect(
                color = onSurfaceVariant.copy(alpha = 0.08f),
                topLeft = Offset(slotWidth * index + 4f, 0f),
                size = Size(slotWidth - 8f, chartHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
              )
            }

            // Income bar
            val incomeH = ((item.income / maxVal) * chartHeight).toFloat().coerceAtLeast(3f)
            val incomeTop = chartHeight - incomeH
            val incomeX = centerX - barWidth - (barSpacing / 2f)

            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(PrimaryEmerald, PrimaryEmerald.copy(alpha = 0.7f)),
                startY = incomeTop,
                endY = chartHeight
              ),
              topLeft = Offset(incomeX, incomeTop),
              size = Size(barWidth, incomeH),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Expense bar
            val expenseH = ((item.expense / maxVal) * chartHeight).toFloat().coerceAtLeast(3f)
            val expenseTop = chartHeight - expenseH
            val expenseX = centerX + (barSpacing / 2f)

            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(WarningAmber, WarningAmber.copy(alpha = 0.7f)),
                startY = expenseTop,
                endY = chartHeight
              ),
              topLeft = Offset(expenseX, expenseTop),
              size = Size(barWidth, expenseH),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
          }
        }
      }

      // Week labels along bottom
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .height(22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        weeklyTrends.forEachIndexed { idx, item ->
          val isSelected = idx == activeIndex
          Text(
            text = "${item.weekLabel} (${item.dateRangeLabel})",
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) PrimaryEmerald else onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Chart 2: Expense Ratio Donut / Radial Chart
// -------------------------------------------------------------
@Composable
private fun ExpenseRatioDonutChart(
  summary: MonthlySummaryData,
  language: AppLanguage
) {
  val gross = summary.totalGrossRent.coerceAtLeast(1.0)
  val profitFraction = (summary.totalProfit.coerceAtLeast(0.0) / gross).toFloat().coerceIn(0f, 1f)
  val maintenanceFraction = (summary.totalMaintenance / gross).toFloat().coerceIn(0f, 1f)
  val gratuityFraction = (summary.totalGratuity / gross).toFloat().coerceIn(0f, 1f)

  val profitSweep = profitFraction * 360f
  val maintenanceSweep = maintenanceFraction * 360f
  val gratuitySweep = gratuityFraction * 360f

  val profitColor = ProfitGreen
  val maintenanceColor = WarningAmber
  val gratuityColor = Color(0xFF3B82F6)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left: Custom Canvas Donut Ring with Center Percentage
    Box(
      modifier = Modifier
        .size(140.dp)
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 18.dp.toPx()
        val diameter = min(size.width, size.height) - strokeWidth
        val topLeft = Offset(
          (size.width - diameter) / 2f,
          (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)

        // Background track
        drawArc(
          color = Color.LightGray.copy(alpha = 0.25f),
          startAngle = 0f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        var startAngle = -90f

        // Profit Arc
        if (profitSweep > 0) {
          drawArc(
            color = profitColor,
            startAngle = startAngle,
            sweepAngle = profitSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )
          startAngle += profitSweep
        }

        // Maintenance Arc
        if (maintenanceSweep > 0) {
          drawArc(
            color = maintenanceColor,
            startAngle = startAngle,
            sweepAngle = maintenanceSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )
          startAngle += maintenanceSweep
        }

        // Gratuity Arc
        if (gratuitySweep > 0) {
          drawArc(
            color = gratuityColor,
            startAngle = startAngle,
            sweepAngle = gratuitySweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )
        }
      }

      // Center Percentage Callout
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val isPositive = summary.profitMargin >= 0
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isPositive) Icons.Default.NorthEast else Icons.Default.SouthEast,
            contentDescription = null,
            tint = if (isPositive) ProfitGreen else LossRed,
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = "${String.format(Locale.US, "%.1f", summary.profitMargin)}%",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isPositive) ProfitGreen else LossRed
          )
        }
        Text(
          text = AppStrings.netProfitMargin(language),
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Right: Detailed Breakdown Legend with Amounts & Percentages
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      RatioLegendRow(
        color = profitColor,
        title = AppStrings.netProfitShare(language),
        amount = summary.totalProfit,
        percentage = (profitFraction * 100).toDouble()
      )

      RatioLegendRow(
        color = maintenanceColor,
        title = AppStrings.maintenanceExpense(language),
        amount = summary.totalMaintenance,
        percentage = (maintenanceFraction * 100).toDouble()
      )

      RatioLegendRow(
        color = gratuityColor,
        title = AppStrings.gratuityExpense(language),
        amount = summary.totalGratuity,
        percentage = (gratuityFraction * 100).toDouble()
      )
    }
  }
}

@Composable
private fun RatioLegendRow(
  color: Color,
  title: String,
  amount: Double,
  percentage: Double
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "৳${String.format(Locale.US, "%,.0f", amount)}",
          fontSize = 10.sp,
          color = color,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(6.dp),
      color = color.copy(alpha = 0.12f)
    ) {
      Text(
        text = "${String.format(Locale.US, "%.1f", percentage)}%",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}

// -------------------------------------------------------------
// Chart 3: Net Profit Trajectory Curve (Cumulative Line Chart)
// -------------------------------------------------------------
@Composable
private fun ProfitTrajectoryCurveChart(
  dailyTrends: List<DayTrend>,
  language: AppLanguage
) {
  var selectedIndex by remember { mutableIntStateOf(-1) }
  val activeIndex = if (selectedIndex in dailyTrends.indices) selectedIndex else -1

  // Compute cumulative series
  var runningTotal = 0.0
  val cumulativePoints = dailyTrends.map { day ->
    runningTotal += day.profit
    runningTotal
  }

  val maxVal = max(100.0, cumulativePoints.maxOrNull() ?: 100.0)
  val minVal = min(0.0, cumulativePoints.minOrNull() ?: 0.0)
  val range = max(100.0, maxVal - minVal)

  val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

  Column(modifier = Modifier.fillMaxWidth()) {
    // Tooltip Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(28.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (activeIndex != -1 && activeIndex < dailyTrends.size) {
        val dt = dailyTrends[activeIndex]
        val cumProf = cumulativePoints[activeIndex]
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${dt.dateLabel}: ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "${if (language == AppLanguage.BANGLA) "দিনেক লাভ" else "Day"}: ৳${String.format(Locale.US, "%.0f", dt.profit)}  ",
            fontSize = 11.sp,
            color = if (dt.profit >= 0) ProfitGreen else LossRed,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${if (language == AppLanguage.BANGLA) "ক্রমবর্ধমান লাভ" else "Cumulative"}: ৳${String.format(Locale.US, "%.0f", cumProf)}",
            fontSize = 11.sp,
            color = PrimaryEmerald,
            fontWeight = FontWeight.Bold
          )
        }
      } else {
        Text(
          text = if (language == AppLanguage.BANGLA) "মাসের মোট মুনাফার প্রবৃদ্ধির গতিপথ (টাচ করে বিশদ দেখুন)"
          else "Cumulative profit trajectory over time (touch to inspect)",
          fontSize = 10.sp,
          color = onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
    ) {
      val width = constraints.maxWidth.toFloat()
      val height = constraints.maxHeight.toFloat()
      val chartHeight = height - 26f

      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(dailyTrends) {
            detectTapGestures(
              onTap = { offset ->
                if (dailyTrends.isNotEmpty()) {
                  val slotWidth = width / dailyTrends.size
                  val clicked = (offset.x / slotWidth).toInt().coerceIn(0, dailyTrends.size - 1)
                  selectedIndex = if (selectedIndex == clicked) -1 else clicked
                }
              }
            )
          }
      ) {
        // Zero baseline position
        val zeroY = chartHeight - (((0.0 - minVal) / range) * chartHeight).toFloat()
        drawLine(
          color = LossRed.copy(alpha = 0.5f),
          start = Offset(0f, zeroY),
          end = Offset(width, zeroY),
          strokeWidth = 1.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

        val count = cumulativePoints.size
        if (count > 0) {
          val slotWidth = width / max(1, count - 1)

          val points = cumulativePoints.mapIndexed { index, value ->
            val x = if (count == 1) width / 2f else index * slotWidth
            val y = chartHeight - (((value - minVal) / range) * chartHeight).toFloat()
            Offset(x, y)
          }

          // Build line path
          val linePath = Path().apply {
            if (points.isNotEmpty()) {
              moveTo(points.first().x, points.first().y)
              for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val controlX = (p0.x + p1.x) / 2f
                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
              }
            }
          }

          // Build fill path
          val fillPath = Path().apply {
            if (points.isNotEmpty()) {
              moveTo(points.first().x, points.first().y)
              for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val controlX = (p0.x + p1.x) / 2f
                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
              }
              lineTo(points.last().x, chartHeight)
              lineTo(points.first().x, chartHeight)
              close()
            }
          }

          // Draw gradient fill under curve
          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(PrimaryEmerald.copy(alpha = 0.35f), Color.Transparent),
              startY = 0f,
              endY = chartHeight
            )
          )

          // Draw curve stroke
          drawPath(
            path = linePath,
            color = PrimaryEmerald,
            style = Stroke(
              width = 3.dp.toPx(),
              cap = StrokeCap.Round,
              join = StrokeJoin.Round
            )
          )

          // Draw points & active highlight
          points.forEachIndexed { idx, pt ->
            val isSelected = idx == activeIndex
            drawCircle(
              color = if (isSelected) GoldAccent else PrimaryEmerald,
              radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
              center = pt
            )
            drawCircle(
              color = Color.White,
              radius = if (isSelected) 3.5.dp.toPx() else 1.5.dp.toPx(),
              center = pt
            )
          }
        }
      }

      // Date labels along bottom
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .height(22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        dailyTrends.forEachIndexed { idx, item ->
          val isSelected = idx == activeIndex
          Text(
            text = item.dateLabel.take(2),
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) PrimaryEmerald else onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Component: Insight Highlighting Chip
// -------------------------------------------------------------
@Composable
private fun InsightChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconColor: Color,
  label: String,
  value: String
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = null
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = label,
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}
