package com.example.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.api.StockMarketApiService
import com.example.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class StockPriceUpdateWorker(
  appContext: Context,
  params: WorkerParameters
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    try {
      val db = AppDatabase.getDatabase(applicationContext)
      val stocks = db.stockDao().getAllStocksList()
      if (stocks.isEmpty()) {
        return@withContext Result.success()
      }

      val stockApi = StockMarketApiService()
      val mfApi = com.example.data.api.MutualFundApiService()

      val (mfStocks, equityStocks) = stocks.partition { it.assetType == "MUTUAL_FUND" }

      // 1. Batch fetch all equity stock quotes concurrently
      if (equityStocks.isNotEmpty()) {
        val quotes = stockApi.fetchBatchQuotes(equityStocks.map { it.symbol })
        for (stock in equityStocks) {
          val cleanSym = stock.symbol.uppercase().removeSuffix(".NS").removeSuffix(".BO")
          val quote = quotes[cleanSym] ?: quotes[stock.symbol] ?: quotes[stock.symbol.uppercase()]
          if (quote != null && quote.regularMarketPrice > 0) {
            db.stockDao().updateStock(
              stock.copy(
                currentPrice = quote.regularMarketPrice,
                dailyChangePercent = quote.changePercent,
                lastPriceUpdated = System.currentTimeMillis()
              )
            )
          }
        }
      }

      // 2. Fetch mutual fund NAVs
      for (stock in mfStocks) {
        val result = mfApi.fetchNav(stock.symbol)
        if (result.isSuccess) {
          val quote = result.getOrNull()
          if (quote != null && quote.nav > 0) {
            db.stockDao().updateStock(
              stock.copy(
                currentPrice = quote.nav,
                dailyChangePercent = quote.changePercent,
                lastPriceUpdated = System.currentTimeMillis()
              )
            )
          }
        }
      }
      Result.success()
    } catch (e: Exception) {
      Result.retry()
    }
  }

  companion object {
    const val UNIQUE_WORK_NAME = "StockPrice6HrPeriodicUpdate"

    fun schedule6HourCycle(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      // 6-hour periodic cycle
      val periodicWorkRequest = PeriodicWorkRequestBuilder<StockPriceUpdateWorker>(
        6, TimeUnit.HOURS,
        30, TimeUnit.MINUTES
      )
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest
      )
    }
  }
}
