package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.api.StockDirectoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockIpoDailyUpdateWorker(
  appContext: Context,
  params: WorkerParameters
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    try {
      Log.d("StockIpoDailyWorker", "Starting daily 24h IPO stock & NFO directory update...")
      val ipoSummary = StockDirectoryManager.syncDailyIpos(applicationContext)
      val nfoSummary = com.example.data.api.MutualFundDirectoryManager.syncDailyNfos(applicationContext)
      Log.d("StockIpoDailyWorker", "Daily IPO & NFO update completed: ${ipoSummary.statusMessage} | ${nfoSummary.statusMessage}")
      Result.success()
    } catch (e: Exception) {
      Log.e("StockIpoDailyWorker", "Failed to update daily IPO stock directory", e)
      Result.retry()
    }
  }
}
