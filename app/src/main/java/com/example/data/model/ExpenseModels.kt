package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.CatEducation
import com.example.ui.theme.CatEntertainment
import com.example.ui.theme.CatFood
import com.example.ui.theme.CatGroceries
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatHousing
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatShopping
import com.example.ui.theme.CatTransport

enum class ExpenseCategory(
  val displayName: String,
  val icon: ImageVector,
  val color: Color
) {
  FOOD("Food & Dining", Icons.Default.Restaurant, CatFood),
  GROCERIES("Groceries", Icons.Default.LocalGroceryStore, CatGroceries),
  SHOPPING("Shopping", Icons.Default.ShoppingBag, CatShopping),
  HOUSING("Housing & Bills", Icons.Default.Home, CatHousing),
  TRANSPORT("Transportation", Icons.Default.DirectionsCar, CatTransport),
  ENTERTAINMENT("Entertainment", Icons.Default.Movie, CatEntertainment),
  HEALTH("Health & Fitness", Icons.Default.FitnessCenter, CatHealth),
  EDUCATION("Education & Work", Icons.Default.School, CatEducation),
  SALARY("Salary & Income", Icons.Default.Payments, Color(0xFF00B894)),
  FREELANCE("Freelance & Projects", Icons.Default.Laptop, Color(0xFF0984E3)),
  INVESTMENT("Investments", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFF39C12)),
  GIFTS("Gifts & Grants", Icons.Default.CardGiftcard, Color(0xFFE84393)),
  OTHER("Other", Icons.Default.MoreHoriz, CatOther);

  val isIncome: Boolean
    get() = this == SALARY || this == FREELANCE || this == INVESTMENT || this == GIFTS

  val emoji: String
    get() = when (this) {
      FOOD -> "🍔"
      GROCERIES -> "🛒"
      SHOPPING -> "🛍️"
      HOUSING -> "🏠"
      TRANSPORT -> "🚗"
      ENTERTAINMENT -> "🎬"
      HEALTH -> "💊"
      EDUCATION -> "📚"
      SALARY -> "💰"
      FREELANCE -> "💻"
      INVESTMENT -> "📈"
      GIFTS -> "🎁"
      OTHER -> "🏷️"
    }

  companion object {
    fun fromName(name: String): ExpenseCategory {
      return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
    }

    fun expenseCategories(): List<ExpenseCategory> = entries.filter { !it.isIncome }
    fun incomeCategories(): List<ExpenseCategory> = entries.filter { it.isIncome }
  }
}

@Entity(tableName = "accounts")
data class AccountEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val type: String = "CHECKING", // "CHECKING", "CASH", "SAVINGS", "CREDIT"
  val balance: Double = 0.0,
  val colorHex: Long = 0xFF1B664BL,
  val iconName: String = "ACCOUNT"
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val billingCycle: String = "Monthly", // "Weekly", "Monthly", "Yearly"
  val category: String = "ENTERTAINMENT",
  val account: String = "Main Checking",
  val nextDueDate: Long,
  val note: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String, // ExpenseCategory.name
  val timestamp: Long,
  val note: String = "",
  val type: String = "EXPENSE", // "EXPENSE", "INCOME", "TRANSFER"
  val account: String = "Main Checking",
  val toAccount: String = "",
  val stockSymbol: String? = null,
  val stockPrice: Double? = null,
  val stockShares: Double? = null,
  val assetType: String? = null // "STOCK", "MUTUAL_FUND", "SIP"
) {
  val isInvestmentTransaction: Boolean
    get() = !stockSymbol.isNullOrBlank() || category.equals("INVESTMENT", ignoreCase = true) || assetType != null || note.contains("Bought ", ignoreCase = true) || note.contains("SIP", ignoreCase = true)

  val isScheduled: Boolean
    get() = timestamp > System.currentTimeMillis() ||
            note.contains("Scheduled", ignoreCase = true) ||
            note.contains("Type: Upcoming", ignoreCase = true)
}

@Entity(tableName = "budgets")
data class BudgetEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val category: String, // ExpenseCategory.name or "TOTAL"
  val monthlyLimit: Double,
  val monthYear: String // e.g. "2026-09"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val targetAmount: Double,
  val currentAmount: Double,
  val deadlineTimestamp: Long,
  val category: String = "SAVINGS",
  val isCompleted: Boolean = false,
  val notifiedFiftyPercent: Boolean = false,
  val notifiedHundredPercent: Boolean = false
) {
  val progressFraction: Float
    get() = if (targetAmount > 0) ((currentAmount / targetAmount).toFloat()).coerceIn(0f, 1f) else 0f

  val progressPercent: Int
    get() = (progressFraction * 100).toInt()
}

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val message: String,
  val type: String, // "BUDGET_WARNING", "BUDGET_EXCEEDED", "GOAL_MILESTONE", "GOAL_REACHED", "REMINDER"
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

@Entity(tableName = "loans")
data class LoanEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val personName: String,
  val type: String, // "LENT" (Owed to you) or "LOAN" (Borrowed - you owe them)
  val totalAmount: Double,
  val paidAmount: Double = 0.0,
  val account: String = "Main Checking",
  val dueDate: Long? = null,
  val creationDate: Long = System.currentTimeMillis(),
  val interestRate: Double = 0.0,
  val isSettled: Boolean = false,
  val note: String = ""
) {
  val remainingAmount: Double
    get() = (totalAmount - paidAmount).coerceAtLeast(0.0)

  val progressFraction: Float
    get() = if (totalAmount > 0) ((paidAmount / totalAmount).toFloat()).coerceIn(0f, 1f) else 0f

  val progressPercent: Int
    get() = (progressFraction * 100).toInt()
}

@Entity(tableName = "stocks")
data class StockEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val symbol: String,
  val companyName: String,
  val shares: Double,
  val avgBuyPrice: Double,
  val currentPrice: Double,
  val purchaseDate: Long = System.currentTimeMillis(),
  val currencySymbol: String = "₹",
  val notes: String = "",
  val lastPriceUpdated: Long = System.currentTimeMillis(),
  val dailyChangePercent: Double = 0.0,
  val exchange: String = "NSE",
  val assetType: String = "STOCK", // "STOCK" or "MUTUAL_FUND"
  val debitAccount: String = "Main Checking"
) {
  val investedAmount: Double
    get() = shares * avgBuyPrice

  val currentValue: Double
    get() = shares * currentPrice

  val totalPnl: Double
    get() = currentValue - investedAmount

  val pnlPercentage: Double
    get() = if (investedAmount > 0) (totalPnl / investedAmount) * 100.0 else 0.0

  val isProfit: Boolean
    get() = totalPnl >= 0.0

  // Day's P&L calculation:
  val dayPnl: Double
    get() {
      if (dailyChangePercent == 0.0) return 0.0
      val prevPrice = currentPrice / (1.0 + (dailyChangePercent / 100.0))
      return (currentPrice - prevPrice) * shares
    }

  val isDayProfit: Boolean
    get() = dailyChangePercent >= 0.0
}

@Entity(tableName = "transaction_shortcuts")
data class TransactionShortcutEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String = "FOOD", // ExpenseCategory.name or custom category
  val type: String = "EXPENSE", // "EXPENSE", "INCOME"
  val account: String = "Main Checking",
  val iconEmoji: String = "⚡",
  val orderIndex: Int = 0
)

object RecurringFrequencyHelper {
  data class FrequencyItem(
    val id: String,
    val displayName: String,
    val shortLabel: String,
    val description: String,
    val monthlyMultiplier: Double
  )

  val FREQUENCIES: List<FrequencyItem> = listOf(
    FrequencyItem("Daily", "Daily", "Day", "Every day (~30x/mo)", 30.42),
    FrequencyItem("Weekdays (Mon-Fri)", "Weekdays (Mon-Fri)", "Workday", "Monday to Friday (~22x/mo)", 21.67),
    FrequencyItem("Weekly", "Weekly", "Week", "Once every week (~4.3x/mo)", 4.333),
    FrequencyItem("Bi-weekly (2 weeks)", "Bi-weekly (Every 2 weeks)", "2 Wks", "Every 14 days (~2.2x/mo)", 2.167),
    FrequencyItem("Every 3 Weeks", "Every 3 Weeks", "3 Wks", "Every 21 days (~1.4x/mo)", 1.444),
    FrequencyItem("Every 4 Weeks", "Every 4 Weeks (28 days)", "4 Wks", "Every 28 days (~1.1x/mo)", 1.083),
    FrequencyItem("Monthly", "Monthly", "Month", "Once a month (1x/mo)", 1.0),
    FrequencyItem("Bi-monthly (2 months)", "Bi-monthly (Every 2 months)", "2 Mos", "Every 2 months (0.5x/mo)", 0.5),
    FrequencyItem("Quarterly (3 months)", "Quarterly (Every 3 months)", "Quarter", "Every 3 months (4x/yr)", 0.3333),
    FrequencyItem("Semi-annually (6 months)", "Semi-annually (Every 6 months)", "6 Mos", "Every 6 months (2x/yr)", 0.1667),
    FrequencyItem("Yearly (Annual)", "Yearly (Annual)", "Year", "Once a year (1x/yr)", 1.0 / 12.0),
    FrequencyItem("Every 30 Days", "Every 30 Days", "30 Days", "Fixed 30-day interval", 1.014)
  )

  fun getMonthlyCost(amount: Double, cycle: String): Double {
    val item = FREQUENCIES.firstOrNull { it.id.equals(cycle, ignoreCase = true) || it.displayName.equals(cycle, ignoreCase = true) }
    return if (item != null) {
      amount * item.monthlyMultiplier
    } else when (cycle.lowercase()) {
      "weekly" -> amount * 4.333
      "yearly", "annual" -> amount / 12.0
      "daily" -> amount * 30.42
      else -> amount
    }
  }
}

@Entity(tableName = "mutual_fund_sips")
data class MutualFundSipEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val schemeCode: String,
  val schemeName: String,
  val installmentAmount: Double,
  val frequency: String = "Monthly", // "Daily", "Weekly", "Bi-weekly", "Monthly", "Quarterly"
  val debitAccount: String = "Main Checking",
  val sipDayOfMonth: Int = 5,
  val nextExecutionDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
  val isActive: Boolean = true,
  val totalInvested: Double = 0.0,
  val installmentsCompleted: Int = 0,
  val notes: String = ""
)


