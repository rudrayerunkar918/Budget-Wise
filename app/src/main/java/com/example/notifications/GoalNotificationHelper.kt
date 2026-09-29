package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.NotificationLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class GoalNotificationHelper(private val context: Context) {

  private val notificationManager = NotificationManagerCompat.from(context)
  private val db = AppDatabase.getDatabase(context)
  private val scope = CoroutineScope(Dispatchers.IO)

  companion object {
    const val CHANNEL_GOALS = "channel_goals"
    const val CHANNEL_BUDGET = "channel_budget"
  }

  init {
    createNotificationChannels()
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val goalsChannel = NotificationChannel(
        CHANNEL_GOALS,
        "Goal Milestones & Savings",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifications for savings goal progress, 50% milestone, and goal completion"
        enableVibration(true)
      }

      val budgetChannel = NotificationChannel(
        CHANNEL_BUDGET,
        "Budget Spending Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts when approaching or exceeding monthly spending limits"
        enableVibration(true)
      }

      val sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      sysManager?.createNotificationChannel(goalsChannel)
      sysManager?.createNotificationChannel(budgetChannel)
    }
  }

  fun hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      notificationManager.areNotificationsEnabled()
    }
  }

  private fun getPendingIntent(): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }

  fun notifyGoalMilestone(
    goalId: Long,
    goalTitle: String,
    percent: Int,
    currentAmount: Double,
    targetAmount: Double
  ) {
    val title = "🎯 Goal Milestone: $goalTitle"
    val message = "You've reached $percent% of your goal! (${formatCurrency(currentAmount)} of ${formatCurrency(targetAmount)} saved)"
    
    postNotification(
      notificationId = (1000 + goalId).toInt(),
      channelId = CHANNEL_GOALS,
      title = title,
      message = message,
      type = "GOAL_MILESTONE",
      progress = percent
    )
  }

  fun notifyGoalAchieved(
    goalId: Long,
    goalTitle: String,
    targetAmount: Double
  ) {
    val title = "🎉 Goal Achieved: $goalTitle!"
    val message = "Incredible job! You've reached your full target of ${formatCurrency(targetAmount)}!"
    
    postNotification(
      notificationId = (2000 + goalId).toInt(),
      channelId = CHANNEL_GOALS,
      title = title,
      message = message,
      type = "GOAL_REACHED",
      progress = 100
    )
  }

  fun notifyBudgetWarning(
    categoryName: String,
    percent: Int,
    spent: Double,
    limit: Double
  ) {
    val title = "⚠️ Budget Alert: $categoryName"
    val message = "You've spent $percent% of your budget (${formatCurrency(spent)} of ${formatCurrency(limit)}). Keep an eye on expenses!"

    postNotification(
      notificationId = (3000 + categoryName.hashCode() % 1000),
      channelId = CHANNEL_BUDGET,
      title = title,
      message = message,
      type = "BUDGET_WARNING",
      progress = percent
    )
  }

  fun notifyBudgetExceeded(
    categoryName: String,
    spent: Double,
    limit: Double
  ) {
    val overAmount = spent - limit
    val title = "🚨 Budget Exceeded: $categoryName"
    val message = "You've exceeded your limit by ${formatCurrency(overAmount)}! (${formatCurrency(spent)} spent vs ${formatCurrency(limit)} budget)."

    postNotification(
      notificationId = (4000 + categoryName.hashCode() % 1000),
      channelId = CHANNEL_BUDGET,
      title = title,
      message = message,
      type = "BUDGET_EXCEEDED"
    )
  }

  fun notifyTest(
    title: String = "🔔 Goal Tracking Alert",
    message: String = "Your savings goal notifications and visual budget tracking are active and working!"
  ) {
    postNotification(
      notificationId = 9999,
      channelId = CHANNEL_GOALS,
      title = title,
      message = message,
      type = "REMINDER"
    )
  }

  private fun postNotification(
    notificationId: Int,
    channelId: String,
    title: String,
    message: String,
    type: String,
    progress: Int? = null
  ) {
    // Save into in-app database log
    scope.launch {
      db.notificationDao().insertLog(
        NotificationLogEntity(
          title = title,
          message = message,
          type = type,
          timestamp = System.currentTimeMillis(),
          isRead = false
        )
      )
    }

    // Post to system status bar if permission is granted
    if (!hasNotificationPermission()) return

    try {
      val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification_bell)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(getPendingIntent())
        .setAutoCancel(true)

      if (progress != null) {
        builder.setProgress(100, progress, false)
      }

      notificationManager.notify(notificationId, builder.build())
    } catch (_: SecurityException) {
      // Permission not granted or revoked
    }
  }

  private fun getCurrencySymbol(): String {
    val prefs = context.getSharedPreferences("cashew_user_prefs", Context.MODE_PRIVATE)
    return prefs.getString("key_currency", prefs.getString("currency_symbol", "₹")) ?: "₹"
  }

  private fun formatCurrency(amount: Double): String {
    return "${getCurrencySymbol()}${String.format(Locale.US, "%,.2f", amount)}"
  }
}
