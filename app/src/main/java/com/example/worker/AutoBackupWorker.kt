package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.data.drive.GoogleDriveBackupManager
import com.example.data.repository.BookingRepository
import com.example.data.repository.TripRepository
import com.example.data.repository.UserPreferencesRepository
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    return try {
      Log.d(TAG, "Starting WorkManager automated Google Drive AppData backup...")
      val db = AppDatabase.getDatabase(applicationContext)
      val tripRepo = TripRepository(db.tripDao())
      val bookingRepo = BookingRepository(db.bookingDao())
      val userPrefsRepo = UserPreferencesRepository(applicationContext)

      val trips = tripRepo.getAllTripsSnapshot()
      val bookings = bookingRepo.getAllBookingsSnapshot()
      val profile = userPrefsRepo.profileFlow.value
      val documents = userPrefsRepo.documentsFlow.value
      val mobilService = userPrefsRepo.mobilServiceFlow.value

      val jsonString = GoogleDriveBackupManager.serializeBackupJson(
        trips = trips,
        profile = profile,
        documents = documents,
        mobilService = mobilService,
        bookings = bookings
      )

      val success = GoogleDriveBackupManager.backupToAppDataFolder(
        context = applicationContext,
        jsonContent = jsonString
      )

      if (success) {
        val now = System.currentTimeMillis()
        userPrefsRepo.setLastDriveBackupTime(now)
        userPrefsRepo.setLastBackupTripCount(trips.size)
        Log.d(TAG, "WorkManager automated backup completed successfully: ${trips.size} trips")
        Result.success()
      } else {
        Log.e(TAG, "WorkManager automated backup failed during writing payload")
        Result.retry()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error executing WorkManager automated backup worker", e)
      Result.retry()
    }
  }

  companion object {
    private const val TAG = "AutoBackupWorker"
    private const val PERIODIC_WORK_NAME = "CarHisabPeriodicAppDataBackup"
    private const val ONE_TIME_WORK_NAME = "CarHisabOneTimeAppDataBackup"

    /**
     * Schedules periodic WorkManager background backup job.
     */
    fun schedulePeriodicBackup(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val periodicWorkRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
        7, TimeUnit.DAYS
      )
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        PERIODIC_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest
      )
      Log.d(TAG, "Scheduled weekly periodic WorkManager backup job")
    }

    /**
     * Triggers immediate background backup job after data changes or on app start.
     */
    fun triggerOneTimeBackup(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val oneTimeWorkRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context).enqueueUniqueWork(
        ONE_TIME_WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        oneTimeWorkRequest
      )
      Log.d(TAG, "Triggered one-time WorkManager backup job")
    }
  }
}
