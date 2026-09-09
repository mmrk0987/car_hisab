package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.BookingEntity
import com.example.data.repository.UserProfile
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BookingFilterTab {
  ALL, UPCOMING, COMPLETED, CANCELLED
}

@Composable
fun BookingsScreen(
  language: AppLanguage,
  profile: UserProfile,
  bookings: List<BookingEntity>,
  onAddBooking: (
    passengerName: String,
    passengerPhone: String,
    pickupLocation: String,
    dropLocation: String,
    tripDateMillis: Long,
    tripTimeString: String,
    totalFare: Double,
    advancePaid: Double,
    notes: String
  ) -> Unit,
  onUpdateStatus: (bookingId: Long, newStatus: String) -> Unit,
  onDeleteBooking: (bookingId: Long) -> Unit,
  onConvertToTrip: (
    booking: BookingEntity,
    gratuity: Double,
    maintenanceCost: Double,
    kmDriven: Double
  ) -> Unit
) {
  var selectedTab by remember { mutableStateOf(BookingFilterTab.ALL) }
  var showAddDialog by remember { mutableStateOf(false) }
  var convertTargetBooking by remember { mutableStateOf<BookingEntity?>(null) }
  val context = LocalContext.current

  val filteredBookings = remember(bookings, selectedTab) {
    when (selectedTab) {
      BookingFilterTab.ALL -> bookings
      BookingFilterTab.UPCOMING -> bookings.filter { it.status != "COMPLETED" && it.status != "CANCELLED" }
      BookingFilterTab.COMPLETED -> bookings.filter { it.status == "COMPLETED" }
      BookingFilterTab.CANCELLED -> bookings.filter { it.status == "CANCELLED" }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("bookings_screen_root")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Filter Tabs Row
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val tabs = listOf(
          Pair(BookingFilterTab.ALL, if (language == AppLanguage.BANGLA) "সকল (${bookings.size})" else "All (${bookings.size})"),
          Pair(BookingFilterTab.UPCOMING, if (language == AppLanguage.BANGLA) "আসন্ন (${bookings.count { it.status != "COMPLETED" && it.status != "CANCELLED" }})" else "Upcoming"),
          Pair(BookingFilterTab.COMPLETED, if (language == AppLanguage.BANGLA) "সম্পন্ন (${bookings.count { it.status == "COMPLETED" }})" else "Completed"),
          Pair(BookingFilterTab.CANCELLED, if (language == AppLanguage.BANGLA) "বাতিল (${bookings.count { it.status == "CANCELLED" }})" else "Cancelled")
        )

        items(tabs) { (tab, label) ->
          val isSelected = selectedTab == tab
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { selectedTab = tab }
          ) {
            Text(
              text = label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
          }
        }
      }

      // Bookings List
      if (filteredBookings.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "কোনো অগ্রিম বুকিং পাওয়া যায়নি" else "No bookings found",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "নতুন ট্রিপ বুকিং শিডিউল করতে নিচের '+' বাটনে চাপ দিন।" else "Tap '+' below to add an advance trip booking.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(filteredBookings, key = { it.id }) { booking ->
            BookingCard(
              booking = booking,
              language = language,
              driverPhone = profile.driverPhone,
              onCall = {
                if (booking.passengerPhone.isNotBlank()) {
                  val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${booking.passengerPhone}")
                  }
                  try {
                    context.startActivity(intent)
                  } catch (e: Exception) {
                    Toast.makeText(context, "ডায়ালার ওপেন করা যায়নি", Toast.LENGTH_SHORT).show()
                  }
                }
              },
              onShareWhatsApp = {
                val shareText = buildString {
                  append("🚗 *Car Hisab - ট্রিপ কনফার্মেশন*\n")
                  append("যাত্রীর নাম: ${booking.passengerName}\n")
                  append("তারিখ: ${booking.tripDateString} (${booking.tripTimeString})\n")
                  append("পিকআপ: ${booking.pickupLocation}\n")
                  append("গন্তব্য: ${booking.dropLocation}\n")
                  append("মোট নির্ধারিত ভাড়া: ৳${booking.totalFare.toInt()}\n")
                  if (booking.advancePaid > 0) {
                    append("অগ্রিম প্রাপ্তি: ৳${booking.advancePaid.toInt()}\n")
                    append("বাকি ভাড়া: ৳${booking.dueFare.toInt()}\n")
                  }
                  if (booking.notes.isNotBlank()) {
                    append("বিশেষ নোট: ${booking.notes}\n")
                  }
                  append("ড্রাইভারের নাম: ${profile.driverName} (${profile.driverPhone})")
                }
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, shareText)
                  type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "যাত্রীকে বুকিং মেসেজ পাঠান")
                context.startActivity(shareIntent)
              },
              onUpdateStatus = { newStatus -> onUpdateStatus(booking.id, newStatus) },
              onDelete = { onDeleteBooking(booking.id) },
              onConvertToTrip = { convertTargetBooking = booking }
            )
          }
        }
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = { showAddDialog = true },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("fab_add_booking"),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = Color.White
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Booking")
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (language == AppLanguage.BANGLA) "নতুন বুকিং" else "New Booking",
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  // Add Booking Dialog
  if (showAddDialog) {
    AddBookingDialog(
      language = language,
      onDismiss = { showAddDialog = false },
      onSave = { name, phone, pickup, drop, dateMillis, timeStr, totalFare, adv, notes ->
        onAddBooking(name, phone, pickup, drop, dateMillis, timeStr, totalFare, adv, notes)
        showAddDialog = false
      }
    )
  }

  // Convert Booking to Trip Dialog
  convertTargetBooking?.let { b ->
    ConvertBookingDialog(
      booking = b,
      language = language,
      onDismiss = { convertTargetBooking = null },
      onConfirm = { gratuity, maintenance, km ->
        onConvertToTrip(b, gratuity, maintenance, km)
        convertTargetBooking = null
        Toast.makeText(
          context,
          if (language == AppLanguage.BANGLA) "ট্রিপটি সফলভাবে মূল হিসাবে যোগ করা হয়েছে!" else "Trip saved to records!",
          Toast.LENGTH_SHORT
        ).show()
      }
    )
  }
}

@Composable
private fun BookingCard(
  booking: BookingEntity,
  language: AppLanguage,
  driverPhone: String,
  onCall: () -> Unit,
  onShareWhatsApp: () -> Unit,
  onUpdateStatus: (String) -> Unit,
  onDelete: () -> Unit,
  onConvertToTrip: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  val statusColor = when (booking.status) {
    "CONFIRMED" -> PrimaryEmerald
    "PENDING" -> WarningAmber
    "COMPLETED" -> ProfitGreen
    "CANCELLED" -> LossRed
    else -> MaterialTheme.colorScheme.primary
  }

  val statusBg = statusColor.copy(alpha = 0.12f)

  val statusLabel = when (booking.status) {
    "CONFIRMED" -> if (language == AppLanguage.BANGLA) "নিশ্চিত" else "Confirmed"
    "PENDING" -> if (language == AppLanguage.BANGLA) "অপেক্ষমান" else "Pending"
    "COMPLETED" -> if (language == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"
    "CANCELLED" -> if (language == AppLanguage.BANGLA) "বাতিল" else "Cancelled"
    else -> booking.status
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Passenger Name & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = booking.passengerName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            if (booking.passengerPhone.isNotBlank()) {
              Text(
                text = booking.passengerPhone,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusBg
          ) {
            Text(
              text = statusLabel,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = statusColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Box {
            IconButton(onClick = { showMenu = true }) {
              Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
              DropdownMenuItem(
                text = { Text("CONFIRMED (নিশ্চিত)") },
                onClick = { onUpdateStatus("CONFIRMED"); showMenu = false }
              )
              DropdownMenuItem(
                text = { Text("PENDING (অপেক্ষমান)") },
                onClick = { onUpdateStatus("PENDING"); showMenu = false }
              )
              DropdownMenuItem(
                text = { Text("CANCELLED (বাতিল)") },
                onClick = { onUpdateStatus("CANCELLED"); showMenu = false }
              )
              DropdownMenuItem(
                text = { Text("মুছে ফেলুন (Delete)", color = LossRed) },
                onClick = { onDelete(); showMenu = false }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Date & Time Banner
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${booking.tripDateString} (${booking.tripTimeString})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Route Info
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = ProfitGreen,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${booking.pickupLocation} ➔ ${booking.dropLocation}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (booking.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "নোট: ${booking.notes}",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Financials (Fare, Advance, Due)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (language == AppLanguage.BANGLA) "নির্ধারিত ভাড়া" else "Total Fare",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
          Text(
            text = "৳${booking.totalFare.toInt()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = if (language == AppLanguage.BANGLA) "অগ্রিম জমা" else "Advance",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
          Text(
            text = "৳${booking.advancePaid.toInt()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ProfitGreen
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = if (language == AppLanguage.BANGLA) "বাকি পাওনা" else "Due",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
          Text(
            text = "৳${booking.dueFare.toInt()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (booking.dueFare > 0) WarningAmber else MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons: Call, Share, Convert to Trip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Direct Call Button
        OutlinedButton(
          onClick = onCall,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryEmerald)
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = AppStrings.callPassenger(language),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryEmerald
          )
        }

        // WhatsApp Share Button
        OutlinedButton(
          onClick = onShareWhatsApp,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (language == AppLanguage.BANGLA) "মেসেজ" else "Share",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Convert to Real Trip Button
        if (booking.status != "COMPLETED") {
          Button(
            onClick = onConvertToTrip,
            modifier = Modifier.weight(1.3f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (language == AppLanguage.BANGLA) "ট্রিপে যোগ" else "To Trip",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AddBookingDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (
    name: String,
    phone: String,
    pickup: String,
    drop: String,
    dateMillis: Long,
    timeStr: String,
    totalFare: Double,
    advance: Double,
    notes: String
  ) -> Unit
) {
  val context = LocalContext.current
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var pickup by remember { mutableStateOf("") }
  var drop by remember { mutableStateOf("") }
  var dateMillis by remember { mutableStateOf(System.currentTimeMillis() + 24 * 60 * 60 * 1000L) }
  var timeStr by remember { mutableStateOf("08:00 AM") }
  var totalFareStr by remember { mutableStateOf("") }
  var advanceStr by remember { mutableStateOf("0") }
  var notes by remember { mutableStateOf("") }

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
  val dateDisplay = dateFormat.format(Date(dateMillis))

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.addBookingTitle(language),
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp
      )
    },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(AppStrings.passengerName(language)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(AppStrings.passengerPhone(language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          OutlinedTextField(
            value = pickup,
            onValueChange = { pickup = it },
            label = { Text(AppStrings.pickupPoint(language)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          OutlinedTextField(
            value = drop,
            onValueChange = { drop = it },
            label = { Text(AppStrings.destinationPoint(language)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = dateDisplay,
              onValueChange = {},
              readOnly = true,
              label = { Text(if (language == AppLanguage.BANGLA) "তারিখ" else "Date") },
              trailingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.clickable {
                  val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                  DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      val sel = Calendar.getInstance().apply { set(y, m, d) }
                      dateMillis = sel.timeInMillis
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                  ).show()
                })
              },
              modifier = Modifier.weight(1.2f)
            )

            OutlinedTextField(
              value = timeStr,
              onValueChange = {},
              readOnly = true,
              label = { Text(if (language == AppLanguage.BANGLA) "সময়" else "Time") },
              trailingIcon = {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.clickable {
                  TimePickerDialog(
                    context,
                    { _, h, min ->
                      val amPm = if (h >= 12) "PM" else "AM"
                      val h12 = if (h % 12 == 0) 12 else h % 12
                      timeStr = String.format(Locale.US, "%02d:%02d %s", h12, min, amPm)
                    },
                    8, 0, false
                  ).show()
                })
              },
              modifier = Modifier.weight(1f)
            )
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = totalFareStr,
              onValueChange = { totalFareStr = it },
              label = { Text(if (language == AppLanguage.BANGLA) "মোট ভাড়া (৳)" else "Total Fare (৳)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f),
              singleLine = true
            )

            OutlinedTextField(
              value = advanceStr,
              onValueChange = { advanceStr = it },
              label = { Text(if (language == AppLanguage.BANGLA) "অগ্রিম জমা (৳)" else "Advance (৳)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
        }

        item {
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(if (language == AppLanguage.BANGLA) "বিশেষ মন্তব্য / বিবরণ" else "Notes / Instructions") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank() || pickup.isBlank() || drop.isBlank()) {
            Toast.makeText(context, "যাত্রীর নাম ও স্থান লিখুন", Toast.LENGTH_SHORT).show()
            return@Button
          }
          val tf = totalFareStr.toDoubleOrNull() ?: 0.0
          val adv = advanceStr.toDoubleOrNull() ?: 0.0
          onSave(name, phone, pickup, drop, dateMillis, timeStr, tf, adv, notes)
        }
      ) {
        Text(if (language == AppLanguage.BANGLA) "সংরক্ষণ" else "Save Booking")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
      }
    }
  )
}

@Composable
private fun ConvertBookingDialog(
  booking: BookingEntity,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (gratuity: Double, maintenance: Double, km: Double) -> Unit
) {
  var gratuityStr by remember { mutableStateOf("0") }
  var maintenanceStr by remember { mutableStateOf("0") }
  var kmStr by remember { mutableStateOf("25") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (language == AppLanguage.BANGLA) "ট্রিপ সম্পন্ন ও হিসাবে যুক্ত" else "Complete & Save Trip",
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "${booking.passengerName} - ${booking.pickupLocation} টু ${booking.dropLocation}",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )

        Text(
          text = "মোট ভাড়া: ৳${booking.totalFare.toInt()} | প্রাপ্তি: ৳${booking.advancePaid.toInt()}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        OutlinedTextField(
          value = kmStr,
          onValueChange = { kmStr = it },
          label = { Text(if (language == AppLanguage.BANGLA) "মোট চালানো দূরত্ব (কিমি)" else "KM Driven") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = maintenanceStr,
          onValueChange = { maintenanceStr = it },
          label = { Text(if (language == AppLanguage.BANGLA) "সিএনজি / তেল / টোল খরচ (৳)" else "Fuel / Toll Cost (৳)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = gratuityStr,
          onValueChange = { gratuityStr = it },
          label = { Text(if (language == AppLanguage.BANGLA) "ট্রিপ খরচ (৳)" else "Trip Expense (৳)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val g = gratuityStr.toDoubleOrNull() ?: 0.0
          val m = maintenanceStr.toDoubleOrNull() ?: 0.0
          val km = kmStr.toDoubleOrNull() ?: 0.0
          onConfirm(g, m, km)
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
      ) {
        Text(if (language == AppLanguage.BANGLA) "হিসাবে যোগ করুন" else "Save to Records")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (language == AppLanguage.BANGLA) "বাতিল" else "Cancel")
      }
    }
  )
}
