package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.launch
import com.example.data.backup.SupabaseSyncManager
import android.widget.Toast

@Composable
fun SupabaseSyncSection(
    activeEmail: String,
    language: AppLanguage,
    accentColor: Color,
    isDark: Boolean
) {
    var isSyncing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "ইউজার ডেটা ক্লাউড সিঙ্ক" else "User Data Cloud Sync",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট: ${activeEmail.ifBlank { "লগইন করা হয়নি" }}" else "Account: ${activeEmail.ifBlank { "Not Logged In" }}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    val lastBackupTime = remember(isSyncing) {
                        com.example.data.repository.UserPreferencesRepository(context).getLastDriveBackupTime()
                    }
                    if (lastBackupTime > 0L) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val dateFormatted = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(lastBackupTime))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "সর্বশেষ ক্লাউড ব্যাকআপ: $dateFormatted" else "Last cloud backup: $dateFormatted",
                            fontSize = 10.5.sp,
                            color = accentColor.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isEmailValid = activeEmail.isNotBlank() && !activeEmail.contains("অজানা") && !activeEmail.contains("Not Logged In") && activeEmail.contains("@")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (!isEmailValid) {
                            val alertMsg = if (language == AppLanguage.BANGLA) "দয়া করে প্রথমে সেটিংসে বা প্রোফাইলে সঠিক ইমেইল যুক্ত করুন" else "Please set a valid email in Settings first"
                            Toast.makeText(context, alertMsg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSyncing = true
                        coroutineScope.launch {
                            val success = SupabaseSyncManager.pushToSupabase(context, activeEmail)
                            isSyncing = false
                            val msg = if (success) {
                                if (language == AppLanguage.BANGLA) "নাম, প্রোফাইল তথ্য ও ট্রিপ সফলভাবে ব্যাকআপ হয়েছে!" else "Profile info & trip data backed up successfully!"
                            } else {
                                if (language == AppLanguage.BANGLA) "ব্যাকআপ ব্যর্থ হয়েছে, ইন্টারনেট সংযোগ চেক করুন" else "Failed to push backup. Check internet connection"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = if (isDark) Color(0xFF022B1E) else Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (language == AppLanguage.BANGLA) "ব্যাকআপ পুশ" else "Push Backup",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF022B1E) else Color.White
                    )
                }

                Button(
                    onClick = {
                        if (!isEmailValid) {
                            val alertMsg = if (language == AppLanguage.BANGLA) "দয়া করে প্রথমে সেটিংসে বা প্রোফাইলে সঠিক ইমেইল যুক্ত করুন" else "Please set a valid email in Settings first"
                            Toast.makeText(context, alertMsg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSyncing = true
                        coroutineScope.launch {
                            val success = SupabaseSyncManager.pullFromSupabase(context, activeEmail)
                            isSyncing = false
                            val msg = if (success) {
                                if (language == AppLanguage.BANGLA) "প্রোফাইল ও ট্রিপ ডেটা সফলভাবে রিস্টোর হয়েছে!" else "Profile & trip data restored successfully!"
                            } else {
                                if (language == AppLanguage.BANGLA) "কোনো ব্যাকআপ পাওয়া যায়নি অথবা রিস্টোর করা যায়নি" else "No backup found or restore failed"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "রিস্টোর করুন" else "Restore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
