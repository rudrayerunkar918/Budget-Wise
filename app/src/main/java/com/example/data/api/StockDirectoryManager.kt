package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.worker.StockIpoDailyUpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class IpoSyncSummary(
  val newIposAdded: Int,
  val totalIposCount: Int,
  val lastSyncDate: String,
  val statusMessage: String
)

object StockDirectoryManager {
  private const val PREFS_NAME = "cashew_stock_directory_prefs"
  private const val KEY_CUSTOM_IPOS_JSON = "custom_ipos_json"
  private const val KEY_LAST_SYNC_TIME = "last_ipo_sync_timestamp"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun initialize(context: Context) {
    try {
      IpoManager.initialize(context)
      StockDatabaseCatalog.initPersistent(context)
    } catch (e: Exception) {
      Log.e("StockDirectoryManager", "Error initializing IPO and stock directory", e)
    }

    // Schedule daily background sync
    scheduleDailyIpoSync(context)
  }

  fun getLastSyncDate(context: Context): String {
    return IpoManager.getLastSyncDate(context)
  }

  fun getTotalDirectoryCount(): Int {
    return StockDatabaseCatalog.getAllStocks().size
  }

  fun getIpoCount(): Int {
    return IpoManager.getAllIpos().size
  }

  suspend fun syncDailyIpos(context: Context): IpoSyncSummary {
    return IpoManager.syncDailyIpos(context)
  }

  fun markIpoAsListed(context: Context, symbol: String, listingPrice: Double = 0.0): DetailedIpoItem? {
    return IpoManager.markIpoAsListed(context, symbol, listingPrice)
  }

  private fun saveDynamicIpos(context: Context, ipos: List<StockCatalogItem>) {
    try {
      val array = JSONArray()
      for (item in ipos) {
        val obj = JSONObject()
        obj.put("symbol", item.symbol)
        obj.put("displaySymbol", item.displaySymbol)
        obj.put("name", item.name)
        obj.put("exchange", item.exchange)
        obj.put("sector", item.sector)
        obj.put("approximatePrice", item.approximatePrice)
        obj.put("currencySymbol", item.currencySymbol)
        obj.put("isIpo", item.isIpo)
        obj.put("listingDate", item.listingDate)
        obj.put("issuePrice", item.issuePrice)
        array.put(obj)
      }
      getPrefs(context).edit().putString(KEY_CUSTOM_IPOS_JSON, array.toString()).apply()
    } catch (e: Exception) {
      Log.e("StockDirectoryManager", "Error saving dynamic IPOs", e)
    }
  }

  fun scheduleDailyIpoSync(context: Context) {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    // 24-Hour Periodic Cycle for Daily Stock Directory IPO Updates
    val periodicWork = PeriodicWorkRequestBuilder<StockIpoDailyUpdateWorker>(
      24, TimeUnit.HOURS,
      2, TimeUnit.HOURS
    )
      .setConstraints(constraints)
      .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      "StockIpoDailyUpdateWorker",
      ExistingPeriodicWorkPolicy.KEEP,
      periodicWork
    )
  }
}
