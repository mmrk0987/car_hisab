package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserProfile
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.DarkGreenBorder
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.DarkGreenPrimary
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.MintGreenAccent

@Composable
fun ProfileSetupScreen(
  language: AppLanguage,
  initialProfile: UserProfile,
  onSaveProfile: (
    nameBangla: String,
    nameEnglish: String,
    birthdate: String,
    phone: String,
    email: String,
    carName: String,
    carModel: String,
    carNumber: String
  ) -> Unit
) {
  var nameBangla by remember { mutableStateOf(initialProfile.driverNameBangla.ifEmpty { initialProfile.driverName }) }
  var nameEnglish by remember { mutableStateOf(initialProfile.driverNameEnglish) }
  var birthdate by remember { mutableStateOf(initialProfile.birthDate) }
  var phone by remember { mutableStateOf(initialProfile.driverPhone) }
  var email by remember { mutableStateOf(initialProfile.driverEmail) }
  var carName by remember { mutableStateOf(initialProfile.carName) }
  var carModel by remember { mutableStateOf(initialProfile.carModel) }
  var carNumber by remember { mutableStateOf(initialProfile.carNumber) }

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("profile_setup_screen_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(DarkGreenCard),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "Profile Setup",
          tint = MintGreenAccent,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = if (language == AppLanguage.BANGLA) "ড্রাইভার ও প্রোফাইল সেটআপ" else "Driver & Profile Setup",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFF1F5F9),
        textAlign = TextAlign.Center
      )

      Text(
        text = if (language == AppLanguage.BANGLA)
          "আপনার সঠিক নাম, জন্ম তারিখ এবং গাড়ির তথ্য প্রদান করুন"
        else
          "Enter your legal name, birthdate, and vehicle details",
        fontSize = 13.sp,
        color = Color(0xFF94A3B8),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
      )

      // Driver Name (Bangla)
      OutlinedTextField(
        value = nameBangla,
        onValueChange = { nameBangla = it },
        label = { Text(if (language == AppLanguage.BANGLA) "পূর্ণ নাম (বাংলায়)" else "Full Name (Bangla)") },
        placeholder = { Text(if (language == AppLanguage.BANGLA) "আপনার নাম লিখুন" else "Enter your name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MintGreenAccent) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkGreenSurface,
          unfocusedContainerColor = DarkGreenSurface,
          focusedBorderColor = MintGreenAccent,
          unfocusedBorderColor = DarkGreenBorder,
          focusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profile_name_bangla_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Driver Name (English)
      OutlinedTextField(
        value = nameEnglish,
        onValueChange = { nameEnglish = it },
        label = { Text(if (language == AppLanguage.BANGLA) "পূর্ণ নাম (ইংরেজিতে)" else "Full Name (English)") },
        placeholder = { Text(if (language == AppLanguage.BANGLA) "ইংরেজিতে নাম লিখুন" else "Enter name in English") },
        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MintGreenAccent) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkGreenSurface,
          unfocusedContainerColor = DarkGreenSurface,
          focusedBorderColor = MintGreenAccent,
          unfocusedBorderColor = DarkGreenBorder,
          focusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profile_name_english_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Birthdate
      OutlinedTextField(
        value = birthdate,
        onValueChange = { birthdate = it },
        label = { Text(if (language == AppLanguage.BANGLA) "জন্ম তারিখ (DD/MM/YYYY)" else "Birthdate (DD/MM/YYYY)") },
        placeholder = { Text("DD/MM/YYYY") },
        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = MintGreenAccent) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkGreenSurface,
          unfocusedContainerColor = DarkGreenSurface,
          focusedBorderColor = MintGreenAccent,
          unfocusedBorderColor = DarkGreenBorder,
          focusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profile_birthdate_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Driver Phone
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর" else "Phone Number") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MintGreenAccent) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkGreenSurface,
          unfocusedContainerColor = DarkGreenSurface,
          focusedBorderColor = MintGreenAccent,
          unfocusedBorderColor = DarkGreenBorder,
          focusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profile_phone_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Driver Email
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(if (language == AppLanguage.BANGLA) "জিমেইল ঠিকানা" else "Gmail Address") },
        placeholder = { Text("driver@gmail.com") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MintGreenAccent) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkGreenSurface,
          unfocusedContainerColor = DarkGreenSurface,
          focusedBorderColor = MintGreenAccent,
          unfocusedBorderColor = DarkGreenBorder,
          focusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profile_email_input")
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Vehicle Info Section Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.DirectionsCar,
          contentDescription = null,
          tint = MintGreenAccent,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (language == AppLanguage.BANGLA) "গাড়ির বিবরণ" else "Vehicle Details",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MintGreenAccent
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Car Name
      OutlinedTextField(
        value = carName,
        onValueChange = { carName = it },
        label = { Text(if (language == AppLanguage.BANGLA) "গাড়ির নাম / ব্র্যান্ড" else "Car Name") },
        placeholder = { Text("Toyota Noah / Hiace") },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkGreenSurface,
          unfocusedContainerColor = DarkGreenSurface,
          focusedBorderColor = MintGreenAccent,
          unfocusedBorderColor = DarkGreenBorder,
          focusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profile_car_name_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Car Number Plate
      OutlinedTextField(
        value = carNumber,
        onValueChange = { carNumber = it },
        label = { Text(if (language == AppLanguage.BANGLA) "গাড়ির নম্বর প্লেট" else "License Plate") },
        placeholder = { Text(if (language == AppLanguage.BANGLA) "নম্বর প্লেট লিখুন" else "Enter plate number") },
        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = MintGreenAccent) },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkGreenSurface,
          unfocusedContainerColor = DarkGreenSurface,
          focusedBorderColor = MintGreenAccent,
          unfocusedBorderColor = DarkGreenBorder,
          focusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profile_car_number_input")
      )

      Spacer(modifier = Modifier.height(26.dp))

      // Next / Continue to NID Verification Button
      Button(
        onClick = {
          onSaveProfile(
            nameBangla.trim(),
            nameEnglish.trim(),
            birthdate.trim(),
            phone.trim(),
            email.trim(),
            carName.trim(),
            carModel.trim(),
            carNumber.trim()
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("profile_save_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
      ) {
        Text(
          text = if (language == AppLanguage.BANGLA) "পরবর্তী ধাপ (এনআইডি যাচাই)" else "Next Step (NID Verification)",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF022B1E)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = Color(0xFF022B1E)
        )
      }
    }
  }
}
