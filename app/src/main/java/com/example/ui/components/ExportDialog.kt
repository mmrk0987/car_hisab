package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.ExportFormat
import com.example.data.export.ExportScope
import com.example.data.model.TripEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import java.util.Locale

@Composable
fun ExportDialog(
  language: AppLanguage,
  allTrips: List<TripEntity>,
  filteredTrips: List<TripEntity> = emptyList(),
  initialScope: ExportScope = ExportScope.CURRENT_MONTH,
  onDismiss: () -> Unit,
  onPerformExport: (format: ExportFormat, trips: List<TripEntity>) -> Unit
) {
  var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }

  val currentCal = remember { java.util.Calendar.getInstance() }
  val curYear = currentCal.get(java.util.Calendar.YEAR)
  val curMonth = currentCal.get(java.util.Calendar.MONTH)

  val currentMonthTrips = remember(allTrips) {
    allTrips.filter { trip ->
      val tCal = java.util.Calendar.getInstance().apply { timeInMillis = trip.dateMillis }
      tCal.get(java.util.Calendar.YEAR) == curYear && tCal.get(java.util.Calendar.MONTH) == curMonth
    }
  }

  val bengaliMonths = listOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
  )
  val englishMonths = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  )
  val currentMonthName = if (language == AppLanguage.BANGLA)
    "${bengaliMonths[curMonth]} $curYear"
  else
    "${englishMonths[curMonth]} $curYear"

  val isSpecialFilteredSubset = filteredTrips.isNotEmpty() &&
    filteredTrips != allTrips &&
    filteredTrips != currentMonthTrips

  var selectedScope by remember(initialScope) {
    mutableStateOf(
      if (initialScope == ExportScope.FILTERED && !isSpecialFilteredSubset) {
        ExportScope.CURRENT_MONTH
      } else {
        initialScope
      }
    )
  }

  val targetTrips = when (selectedScope) {
    ExportScope.CURRENT_MONTH -> currentMonthTrips
    ExportScope.ALL -> allTrips
    ExportScope.FILTERED -> filteredTrips
  }

  val totalRent = targetTrips.sumOf { it.rent }
  val totalProfit = targetTrips.sumOf { it.profit }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("export_dialog_root"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }
        Column {
          Text(
            text = AppStrings.exportTitle(language),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (language == AppLanguage.BANGLA) "অ্যাকাউন্টিং ও অডিট রিপোর্ট" else "Accounting & Audit Records",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = AppStrings.exportSubtitle(language),
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
          lineHeight = 16.sp
        )

        // Section: File Format Selection
        Text(
          text = AppStrings.exportFormatLabel(language),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Option 1: PDF Document
        FormatSelectionCard(
          title = AppStrings.exportPdf(language),
          subtitle = AppStrings.exportPdfSub(language),
          badgeText = "PDF",
          badgeColor = Color(0xFFDC2626), // red
          icon = Icons.Default.PictureAsPdf,
          isSelected = selectedFormat == ExportFormat.PDF,
          testTag = "export_option_pdf",
          onClick = { selectedFormat = ExportFormat.PDF }
        )

        // Option 2: Excel / CSV Spreadsheet
        FormatSelectionCard(
          title = AppStrings.exportCsv(language),
          subtitle = AppStrings.exportCsvSub(language),
          badgeText = "CSV",
          badgeColor = Color(0xFF16A34A), // green
          icon = Icons.Default.TableChart,
          isSelected = selectedFormat == ExportFormat.CSV,
          testTag = "export_option_csv",
          onClick = { selectedFormat = ExportFormat.CSV }
        )

        // Section: Scope Selection
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = AppStrings.exportScopeLabel(language),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = selectedScope == ExportScope.CURRENT_MONTH,
              onClick = { selectedScope = ExportScope.CURRENT_MONTH },
              label = {
                Text(
                  text = if (language == AppLanguage.BANGLA)
                    "চলতি মাস (${currentMonthTrips.size}টি)"
                  else
                    "This Month (${currentMonthTrips.size})",
                  fontSize = 11.sp,
                  fontWeight = if (selectedScope == ExportScope.CURRENT_MONTH) FontWeight.Bold else FontWeight.Normal
                )
              },
              leadingIcon = if (selectedScope == ExportScope.CURRENT_MONTH) {
                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
              } else null,
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryEmerald.copy(alpha = 0.2f),
                selectedLabelColor = PrimaryEmerald
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("export_scope_current_month_chip")
            )

            FilterChip(
              selected = selectedScope == ExportScope.ALL,
              onClick = { selectedScope = ExportScope.ALL },
              label = {
                Text(
                  text = if (language == AppLanguage.BANGLA)
                    "সকল ট্রিপ (${allTrips.size}টি)"
                  else
                    "All Trips (${allTrips.size})",
                  fontSize = 11.sp,
                  fontWeight = if (selectedScope == ExportScope.ALL) FontWeight.Bold else FontWeight.Normal
                )
              },
              leadingIcon = if (selectedScope == ExportScope.ALL) {
                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
              } else null,
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryEmerald.copy(alpha = 0.2f),
                selectedLabelColor = PrimaryEmerald
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("export_scope_all_chip")
            )
          }

          if (isSpecialFilteredSubset) {
            FilterChip(
              selected = selectedScope == ExportScope.FILTERED,
              onClick = { selectedScope = ExportScope.FILTERED },
              label = {
                Text(
                  text = AppStrings.exportScopeFiltered(language, filteredTrips.size),
                  fontSize = 11.sp,
                  fontWeight = if (selectedScope == ExportScope.FILTERED) FontWeight.Bold else FontWeight.Normal
                )
              },
              leadingIcon = if (selectedScope == ExportScope.FILTERED) {
                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
              } else null,
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryEmerald.copy(alpha = 0.2f),
                selectedLabelColor = PrimaryEmerald
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("export_scope_filtered_chip")
            )
          }
        }

        // Summary Preview Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "নির্বাচিত ট্রিপ" else "Selected Trips",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${targetTrips.size} ${if (language == AppLanguage.BANGLA) "টি" else "trips"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "মোট ভাড়া" else "Total Rent",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "৳${String.format(Locale.US, "%,.0f", totalRent)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Column {
              Text(
                text = if (language == AppLanguage.BANGLA) "মোট লাভ" else "Net Profit",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              val isProfit = totalProfit >= 0
              Text(
                text = "৳${String.format(Locale.US, "%,.0f", totalProfit)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isProfit) ProfitGreen else LossRed
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onPerformExport(selectedFormat, targetTrips)
          onDismiss()
        },
        enabled = targetTrips.isNotEmpty(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
        modifier = Modifier.testTag("export_dialog_confirm_button")
      ) {
        Icon(
          imageVector = Icons.Default.FileDownload,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(AppStrings.exportBtn(language), fontSize = 13.sp)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("export_dialog_cancel_button")
      ) {
        Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
      }
    }
  )
}

@Composable
private fun FormatSelectionCard(
  title: String,
  subtitle: String,
  badgeText: String,
  badgeColor: Color,
  icon: ImageVector,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  val borderColor = if (isSelected) PrimaryEmerald else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  val backgroundColor = if (isSelected) PrimaryEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(14.dp)
      )
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(badgeColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = badgeColor,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeColor.copy(alpha = 0.15f)
          ) {
            Text(
              text = badgeText,
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = badgeColor,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      RadioButton(
        selected = isSelected,
        onClick = onClick
      )
    }
  }
}
