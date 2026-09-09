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
      // Re-schedule alarm after reboot if enabled
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

    // Schedule next week's alarm
    scheduleWeeklyBackupReminder(context)
  }

  companion object {
    const val CHANNEL_ID = "car_hisab_backup_channel"
    const val NOTIFICATION_ID = 1001
    const val REQUEST_CODE = 2002

    fun createNotificationChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "গুগল ড্রাইভ ব্যাকআপ রিমাইন্ডার"
        val descriptionText = "সাপ্তাহিক বৃহস্পতিবার সন্ধ্যা ৬টায় ক্লাউড ব্যাকআপ নোটিফিকেশন"
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
        putExtra("NAVIGATE_TO_DRIVE_BACKUP", true)
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val isBangla = userPrefs.languageFlow.value.name == "BANGLA"

      val title = if (isBangla) {
        "🔔 গাড়ির হিসাব ব্যাকআপ নিন (বৃহস্পতিবার)"
      } else {
        "🔔 Weekly Backup Reminder (Thursday)"
      }

      val content = if (isBangla) {
        "আপনার ১ সপ্তাহের সমস্ত ট্রিপ ও আয়ের হিসাব সুরক্ষিত রাখতে গুগল ড্রাইভে ব্যাকআপ নিন।"
      } else {
        "Tap here to backup this week's trip records securely to Google Drive."
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

    /**
     * Schedules alarm for every Thursday at 6:00 PM (18:00)
     */
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

        // If today is Thursday after 6 PM or any subsequent day, schedule for NEXT Thursday
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
