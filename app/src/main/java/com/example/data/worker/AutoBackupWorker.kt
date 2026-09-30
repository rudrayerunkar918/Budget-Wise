package com.example.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.backup.DataBackupManager
import com.example.data.preferences.UserPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
  appContext: Context,
  params: WorkerParameters
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    try {
      val prefs = UserPreferencesManager(applicationContext)
      // Check if auto backup is enabled
      if (!prefs.autoBackupEnabled.value) {
        return@withContext Result.success()
      }

      // Create snapshot (retaining up to 7 snapshots)
      DataBackupManager.createLocalSnapshot(
        context = applicationContext,
        isAutomated = true,
        passphrase = null,
        maxRetained = 7
      )

      // Update timestamp in preferences
      prefs.setLastAutoBackupTimestamp(System.currentTimeMillis())

      Result.success()
    } catch (e: Exception) {
      e.printStackTrace()
      Result.retry()
    }
  }

  companion object {
    private const val WORK_NAME_PERIODIC = "budgetwise_auto_backup_periodic"
    private const val WORK_NAME_ONEOFF = "budgetwise_auto_backup_oneoff"

    fun schedule(context: Context, intervalHours: Long = 24) {
      val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()

      val safeInterval = intervalHours.coerceAtLeast(1)
      val periodicRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(safeInterval, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_NAME_PERIODIC,
        ExistingPeriodicWorkPolicy.UPDATE,
        periodicRequest
      )
    }

    fun cancel(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
    }

    fun runOnce(context: Context) {
      val oneOffRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
        .build()

      WorkManager.getInstance(context).enqueueUniqueWork(
        WORK_NAME_ONEOFF,
        ExistingWorkPolicy.REPLACE,
        oneOffRequest
      )
    }
  }
}
