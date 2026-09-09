package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddTripScreen(
  language: AppLanguage,
  date: String,
  place: String,
  passengerName: String = "",
  passengerPhone: String = "",
  rent: String,
  gratuity: String,
  maintenance: String,
  km: String,
  description: String,
  calculatedIncome: Double,
  calculatedProfit: Double,
  onDateChange: (String) -> Unit,
  onPlaceChange: (String) -> Unit,
  onPassengerNameChange: (String) -> Unit = {},
  onPassengerPhoneChange: (String) -> Unit = {},
  onRentChange: (String) -> Unit,
  onGratuityChange: (String) -> Unit,
  onMaintenanceChange: (String) -> Unit,
  onKmChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onSaveTrip: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val isProfitable = calculatedProfit >= 0
  val isHighMaintenance = (maintenance.toDoubleOrNull() ?: 0.0) > (rent.toDoubleOrNull() ?: 0.0) * 0.5 && (rent.toDoubleOrNull() ?: 0.0) > 0

  val calendar = remember { Calendar.getInstance() }
  val datePickerDialog = remember(context, date) {
    // Try to pre-set calendar to the current date string
    val cal = Calendar.getInstance()
    try {
      val formats = listOf(
        SimpleDateFormat("dd MMM yyyy", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US)
      )
      for (sdf in formats) {
        val parsed = sdf.parse(date.trim())
        if (parsed != null) {
          cal.time = parsed
          break
        }
      }
    } catch (_: Exception) {}

    android.app.DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        val selected = Calendar.getInstance().apply {
          set(Calendar.YEAR, year)
          set(Calendar.MONTH, month)
          set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        val formatted = SimpleDateFormat("dd MMM yyyy", Locale.US).format(selected.time)
        onDateChange(formatted)
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH)
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("add_trip_screen_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
      // Header Banner
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = AppStrings.addTripTitle(language),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = AppStrings.liveCalculation(language),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Live Calculation Real-time Floating Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .border(
            width = 1.5.dp,
            color = if (isProfitable) ProfitGreen else LossRed,
            shape = RoundedCornerShape(18.dp)
          )
          .testTag("live_calculation_card"),
        colors = CardDefaults.cardColors(
          containerColor = if (isProfitable) ProfitGreen.copy(alpha = 0.07f) else LossRed.copy(alpha = 0.07f)
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (isProfitable) Icons.Default.NorthEast else Icons.Default.SouthEast,
                contentDescription = null,
                tint = if (isProfitable) ProfitGreen else LossRed,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isProfitable) AppStrings.calculatedProfit(language) else AppStrings.calculatedLoss(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isProfitable) ProfitGreen else LossRed
              )
            }

            Text(
              text = "৳${String.format(Locale.US, "%,.0f", calculatedProfit)}",
              fontSize = 24.sp,
              fontWeight = FontWeight.Black,
              color = if (isProfitable) ProfitGreen else LossRed
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Breakdown: Income = Rent - Gratuity, Profit = Income - Maintenance
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = AppStrings.calculatedIncome(language),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "৳${String.format(Locale.US, "%,.0f", calculatedIncome)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryEmerald
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = AppStrings.formulaIncome(language),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              )
              Text(
                text = AppStrings.formulaProfit(language),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              )
            }
          }

          // Warning badge if maintenance cost is unusually high
          AnimatedVisibility(visible = isHighMaintenance) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = WarningAmber,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (language == AppLanguage.BANGLA) "সতর্কতা: মেইনটেন্যান্স খরচ ভাড়ার ৫০% এর বেশি!"
                else "Warning: Maintenance is over 50% of the rent!",
                fontSize = 11.sp,
                color = WarningAmber,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Input 1: Date
      OutlinedTextField(
        value = date,
        onValueChange = onDateChange,
        label = { Text(AppStrings.dateLabel(language)) },
        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
        trailingIcon = {
          IconButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.testTag("pick_trip_date_button")
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Pick Date",
              tint = PrimaryEmerald
            )
          }
        },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_trip_date")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Input 2: Place / Route
      OutlinedTextField(
        value = place,
        onValueChange = onPlaceChange,
        label = { Text(AppStrings.placeLabel(language)) },
        placeholder = { Text(AppStrings.placePlaceholder(language)) },
        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_trip_place")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Input 3: Rent (ভাড়া)
      OutlinedTextField(
        value = rent,
        onValueChange = onRentChange,
        label = { Text(AppStrings.rentLabel(language)) },
        placeholder = { Text("0") },
        leadingIcon = { Icon(Icons.Default.Money, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_trip_rent")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Input 4: Trip Expense (ট্রিপ খরচ)
      OutlinedTextField(
        value = gratuity,
        onValueChange = onGratuityChange,
        label = { Text(AppStrings.gratuityLabel(language)) },
        placeholder = { Text("0") },
        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_trip_gratuity")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Input 5: Maintenance Cost (মেইনটেন্যান্স খরচ)
      OutlinedTextField(
        value = maintenance,
        onValueChange = onMaintenanceChange,
        label = { Text(AppStrings.maintenanceLabel(language)) },
        placeholder = { Text("0") },
        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_trip_maintenance")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Input 6: KM Driven
      OutlinedTextField(
        value = km,
        onValueChange = onKmChange,
        label = { Text(AppStrings.kmLabel(language)) },
        placeholder = { Text("0.0") },
        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_trip_km")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Input 7: Description
      OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text(AppStrings.descLabel(language)) },
        placeholder = { Text(AppStrings.descPlaceholder(language)) },
        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
        shape = RoundedCornerShape(14.dp),
        maxLines = 3,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_trip_description")
      )

      Spacer(modifier = Modifier.height(26.dp))

      // Save Button
      Button(
        onClick = onSaveTrip,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_trip_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = AppStrings.saveTripBtn(language),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}
