package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.UserPreferencesRepository
import java.util.Calendar

class WeeklyBackupReminderReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
      val userPrefs = UserPreferencesRepository(context)
      if (userPrefs.isAutoWeeklyBackupReminderEnabled()) {
        scheduleWeeklyBackupReminder(context)
      }
      return
    }

    val userPrefs = UserPreferencesRepository(context)
    if (!userPrefs.isAutoWeeklyBackupReminderEnabled()) {
      return
    }

    showBackupNotification(context, userPrefs)

    scheduleWeeklyBackupReminder(context)
  }

  companion object {
    const val CHANNEL_ID = "car_hisab_backup_channel"
    const val NOTIFICATION_ID = 1001
    const val REQUEST_CODE = 2002

    fun createNotificationChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "অটো ব্যাকআপ রিমাইন্ডার"
        val descriptionText = "অটো ব্যাকআপ স্ট্যাটাস নোটিফিকেশন"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
          description = descriptionText
          enableVibration(true)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
      }
    }

    fun showBackupNotification(context: Context, userPrefs: UserPreferencesRepository) {
      createNotificationChannel(context)

      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val isBangla = userPrefs.languageFlow.value.name == "BANGLA"

      val title = if (isBangla) {
        "🔔 অটো ব্যাকআপ সুরক্ষিত আছে"
      } else {
        "🔔 Auto Backup Active"
      }

      val content = if (isBangla) {
        "অটো ব্যাকআপ চালু আছে (গুগল ড্রাইভে স্বয়ংক্রিয়)।"
      } else {
        "Android Auto Backup is enabled and syncing automatically via Google."
      }

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun scheduleWeeklyBackupReminder(context: Context) {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val intent = Intent(context, WeeklyBackupReminderReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        set(Calendar.HOUR_OF_DAY, 18) // 6:00 PM
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (timeInMillis <= System.currentTimeMillis()) {
          add(Calendar.WEEK_OF_YEAR, 1)
        }
      }

      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
          )
        } else {
          alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
          )
        }
      } catch (_: SecurityException) {
        alarmManager.set(
          AlarmManager.RTC_WAKEUP,
          calendar.timeInMillis,
          pendingIntent
        )
      }
    }

    fun cancelWeeklyBackupReminder(context: Context) {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val intent = Intent(context, WeeklyBackupReminderReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      alarmManager.cancel(pendingIntent)
    }
  }
}
