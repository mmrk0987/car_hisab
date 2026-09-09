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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TripEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.LossRed
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EditTripDialog(
  trip: TripEntity,
  language: AppLanguage,
  isMaintenanceMode: Boolean = false,
  onDismiss: () -> Unit,
  onSaveTrip: (TripEntity) -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var editDate by remember { mutableStateOf(trip.dateString) }
  var editDateMillis by remember { mutableStateOf(trip.dateMillis) }
  var editPlace by remember { mutableStateOf(trip.place) }
  var editRent by remember {
    mutableStateOf(if (trip.rent > 0) String.format(Locale.US, "%.0f", trip.rent) else "")
  }
  var editGratuity by remember {
    mutableStateOf(if (trip.gratuity > 0) String.format(Locale.US, "%.0f", trip.gratuity) else "")
  }
  var editMaintenance by remember {
    mutableStateOf(if (trip.maintenanceCost > 0) String.format(Locale.US, "%.0f", trip.maintenanceCost) else "")
  }
  var editKm by remember {
    mutableStateOf(if (trip.kmDriven > 0) String.format(Locale.US, "%.1f", trip.kmDriven) else "")
  }
  var editDesc by remember { mutableStateOf(trip.description) }

  val datePickerDialog = remember(context, editDate) {
    val cal = Calendar.getInstance()
    if (editDateMillis > 0) {
      cal.timeInMillis = editDateMillis
    } else {
      try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val parsed = sdf.parse(editDate.trim())
        if (parsed != null) cal.time = parsed
      } catch (_: Exception) {}
    }

    android.app.DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        val selected = Calendar.getInstance().apply {
          set(Calendar.YEAR, year)
          set(Calendar.MONTH, month)
          set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        editDateMillis = selected.timeInMillis
        editDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(selected.time)
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH)
    )
  }

  val rentVal by remember { derivedStateOf { editRent.toDoubleOrNull() ?: 0.0 } }
  val gratuityVal by remember { derivedStateOf { editGratuity.toDoubleOrNull() ?: 0.0 } }
  val maintenanceVal by remember { derivedStateOf { editMaintenance.toDoubleOrNull() ?: 0.0 } }
  val calculatedIncome by remember { derivedStateOf { rentVal - gratuityVal } }
  val calculatedProfit by remember { derivedStateOf { calculatedIncome - maintenanceVal } }
  val isProfitable by remember { derivedStateOf { calculatedProfit >= 0 } }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(vertical = 20.dp)
        .testTag("dialog_edit_trip"),
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(scrollState)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isMaintenanceMode) Color(0xFFF59E0B).copy(alpha = 0.18f) else PrimaryEmerald.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isMaintenanceMode) Icons.Default.Build else Icons.Default.Edit,
                contentDescription = null,
                tint = if (isMaintenanceMode) Color(0xFFD97706) else PrimaryEmerald,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (isMaintenanceMode) {
                  if (language == AppLanguage.BANGLA) "মেইনটেনেন্স খরচ এডিট" else "Edit Maintenance Record"
                } else {
                  if (language == AppLanguage.BANGLA) "ট্রিপ এডিট করুন" else "Edit Saved Trip"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (language == AppLanguage.BANGLA) "তথ্য পরিবর্তন করে আপডেট করুন" else "Update trip details",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live calculation card if rent or cost present
        if (!isMaintenanceMode || rentVal > 0) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (isProfitable) ProfitGreen.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isProfitable) ProfitGreen.copy(alpha = 0.3f) else LossRed.copy(alpha = 0.3f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = if (language == AppLanguage.BANGLA) "হিসাব অনুযায়ী লাভ/লোকসান" else "Calculated Profit/Loss",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = (if (isProfitable) "+৳" else "-৳") +
                    String.format(Locale.US, "%,.0f", kotlin.math.abs(calculatedProfit)),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isProfitable) ProfitGreen else LossRed
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
              ) {
                Text(
                  text = "আয়: ৳${String.format(Locale.US, "%,.0f", calculatedIncome)}",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Field 1: Date
        OutlinedTextField(
          value = editDate,
          onValueChange = { editDate = it },
          label = { Text(if (language == AppLanguage.BANGLA) "তারিখ" else "Date") },
          readOnly = true,
          trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Select Date",
                tint = PrimaryEmerald
              )
            }
          },
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Field 2: Place / Route
        OutlinedTextField(
          value = editPlace,
          onValueChange = { editPlace = it },
          label = { Text(AppStrings.placeLabel(language)) },
          placeholder = { Text(if (language == AppLanguage.BANGLA) "যেমন: ঢাকা টু গাজীপুর" else "e.g. Dhaka to Gazipur") },
          leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald) },
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Field 3 & 4: Rent and Gratuity / Expense
        if (!isMaintenanceMode) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = editRent,
              onValueChange = { editRent = it },
              label = { Text(if (language == AppLanguage.BANGLA) "মোট ভাড়া (৳)" else "Total Rent (৳)") },
              placeholder = { Text("0") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              shape = RoundedCornerShape(14.dp),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = editGratuity,
              onValueChange = { editGratuity = it },
              label = { Text(if (language == AppLanguage.BANGLA) "ট্রিপ খরচ (৳)" else "Trip Cost (৳)") },
              placeholder = { Text("0") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              shape = RoundedCornerShape(14.dp),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        // Field 5: Maintenance Cost
        OutlinedTextField(
          value = editMaintenance,
          onValueChange = { editMaintenance = it },
          label = {
            Text(
              if (language == AppLanguage.BANGLA) "মেইনটেনেন্স / মেরামতের খরচ (৳)"
              else "Maintenance Cost (৳)"
            )
          },
          placeholder = { Text("0") },
          leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFD97706)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Field 6: Distance (KM)
        OutlinedTextField(
          value = editKm,
          onValueChange = { editKm = it },
          label = { Text(if (language == AppLanguage.BANGLA) "দূরত্ব / ওডোমিটার (কিমি)" else "Distance (Km)") },
          placeholder = { Text("0") },
          leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Field 7: Description / Notes
        OutlinedTextField(
          value = editDesc,
          onValueChange = { editDesc = it },
          label = {
            Text(
              if (isMaintenanceMode) {
                if (language == AppLanguage.BANGLA) "কাজের বিবরণ / পার্টস" else "Service Details / Parts"
              } else {
                if (language == AppLanguage.BANGLA) "নোট বা বিবরণ" else "Notes / Description"
              }
            )
          },
          placeholder = {
            Text(
              if (isMaintenanceMode) {
                if (language == AppLanguage.BANGLA) "যেমন: ইঞ্জিন অয়েল পরিবর্তন, ব্রেক প্যাড"
                else "e.g. Engine oil change, brake pad"
              } else {
                if (language == AppLanguage.BANGLA) "ট্রিপ বা খরচের বিবরণ" else "Trip details"
              }
            )
          },
          leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel",
              fontWeight = FontWeight.SemiBold
            )
          }

          Button(
            onClick = {
              val updated = trip.copy(
                dateMillis = editDateMillis,
                dateString = editDate.trim().ifEmpty { trip.dateString },
                place = editPlace.trim().ifEmpty { trip.place },
                rent = rentVal,
                gratuity = gratuityVal,
                maintenanceCost = maintenanceVal,
                kmDriven = editKm.toDoubleOrNull() ?: 0.0,
                description = editDesc.trim(),
                passengerName = "",
                passengerPhone = "",
                income = calculatedIncome,
                profit = calculatedProfit
              )
              onSaveTrip(updated)
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("btn_save_edited_trip"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
          ) {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = null,
              modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "আপডেট করুন" else "Update",
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
