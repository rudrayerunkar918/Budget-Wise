package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseLogEntity
import com.example.data.model.LoanEntity
import com.example.data.model.MutualFundSipEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.StockEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionShortcutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
  @Query("SELECT * FROM accounts ORDER BY id ASC")
  fun getAllAccounts(): Flow<List<AccountEntity>>

  @Query("SELECT * FROM accounts ORDER BY id ASC")
  suspend fun getAllAccountsList(): List<AccountEntity>

  @Query("SELECT * FROM accounts WHERE id = :id")
  suspend fun getAccountById(id: Long): AccountEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccount(account: AccountEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

  @Update
  suspend fun updateAccount(account: AccountEntity)

  @Delete
  suspend fun deleteAccount(account: AccountEntity)

  @Query("DELETE FROM accounts")
  suspend fun clearAll()

  @Query("UPDATE accounts SET balance = balance + :amount WHERE name = :accountName")
  suspend fun adjustBalance(accountName: String, amount: Double)
}

@Dao
interface SubscriptionDao {
  @Query("SELECT * FROM subscriptions ORDER BY nextDueDate ASC")
  fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

  @Query("SELECT * FROM subscriptions ORDER BY nextDueDate ASC")
  suspend fun getAllSubscriptionsList(): List<SubscriptionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubscription(subscription: SubscriptionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubscriptions(subscriptions: List<SubscriptionEntity>): List<Long>

  @Update
  suspend fun updateSubscription(subscription: SubscriptionEntity)

  @Delete
  suspend fun deleteSubscription(subscription: SubscriptionEntity)

  @Query("DELETE FROM subscriptions")
  suspend fun clearAll()
}

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
  suspend fun getAllExpensesList(): List<ExpenseEntity>

  @Query("SELECT * FROM expenses WHERE id = :id")
  suspend fun getExpenseById(id: Long): ExpenseEntity?

  @Query("SELECT * FROM expenses WHERE account = :accountName ORDER BY timestamp DESC")
  fun getExpensesByAccount(accountName: String): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
  fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long>

  @Update
  suspend fun updateExpense(expense: ExpenseEntity)

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntity)

  @Query("DELETE FROM expenses")
  suspend fun clearAll()

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime")
  suspend fun getTotalSpendingBetween(startTime: Long, endTime: Long): Double?

  @Query("SELECT SUM(amount) FROM expenses WHERE category = :category AND timestamp >= :startTime AND timestamp <= :endTime")
  suspend fun getCategorySpendingBetween(category: String, startTime: Long, endTime: Long): Double?
}

@Dao
interface BudgetDao {
  @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
  fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>>

  @Query("SELECT * FROM budgets")
  fun getAllBudgets(): Flow<List<BudgetEntity>>

  @Query("SELECT * FROM budgets")
  suspend fun getAllBudgetsList(): List<BudgetEntity>

  @Query("SELECT * FROM budgets WHERE category = :category AND monthYear = :monthYear LIMIT 1")
  suspend fun getBudget(category: String, monthYear: String): BudgetEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudget(budget: BudgetEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudgets(budgets: List<BudgetEntity>): List<Long>

  @Update
  suspend fun updateBudget(budget: BudgetEntity)

  @Delete
  suspend fun deleteBudget(budget: BudgetEntity)

  @Query("DELETE FROM budgets")
  suspend fun clearAll()
}

@Dao
interface SavingsGoalDao {
  @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, deadlineTimestamp ASC")
  fun getAllGoals(): Flow<List<SavingsGoalEntity>>

  @Query("SELECT * FROM savings_goals")
  suspend fun getAllGoalsList(): List<SavingsGoalEntity>

  @Query("SELECT * FROM savings_goals WHERE id = :id")
  suspend fun getGoalById(id: Long): SavingsGoalEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoal(goal: SavingsGoalEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoals(goals: List<SavingsGoalEntity>): List<Long>

  @Update
  suspend fun updateGoal(goal: SavingsGoalEntity)

  @Delete
  suspend fun deleteGoal(goal: SavingsGoalEntity)

  @Query("DELETE FROM savings_goals")
  suspend fun clearAll()
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
  fun getAllLogs(): Flow<List<NotificationLogEntity>>

  @Query("SELECT COUNT(*) FROM notification_logs WHERE isRead = 0")
  fun getUnreadCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: NotificationLogEntity): Long

  @Query("UPDATE notification_logs SET isRead = 1")
  suspend fun markAllAsRead()

  @Query("DELETE FROM notification_logs")
  suspend fun clearAll()
}

@Dao
interface LoanDao {
  @Query("SELECT * FROM loans ORDER BY isSettled ASC, creationDate DESC")
  fun getAllLoans(): Flow<List<LoanEntity>>

  @Query("SELECT * FROM loans")
  suspend fun getAllLoansList(): List<LoanEntity>

  @Query("SELECT * FROM loans WHERE id = :id")
  suspend fun getLoanById(id: Long): LoanEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLoan(loan: LoanEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLoans(loans: List<LoanEntity>): List<Long>

  @Update
  suspend fun updateLoan(loan: LoanEntity)

  @Delete
  suspend fun deleteLoan(loan: LoanEntity)

  @Query("DELETE FROM loans")
  suspend fun clearAll()
}

@Dao
interface StockDao {
  @Query("SELECT * FROM stocks ORDER BY id DESC")
  fun getAllStocks(): Flow<List<StockEntity>>

  @Query("SELECT * FROM stocks")
  suspend fun getAllStocksList(): List<StockEntity>

  @Query("SELECT * FROM stocks WHERE id = :id")
  suspend fun getStockById(id: Long): StockEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStock(stock: StockEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStocks(stocks: List<StockEntity>): List<Long>

  @Update
  suspend fun updateStock(stock: StockEntity)

  @Delete
  suspend fun deleteStock(stock: StockEntity)

  @Query("DELETE FROM stocks")
  suspend fun clearAll()
}

@Dao
interface TransactionShortcutDao {
  @Query("SELECT * FROM transaction_shortcuts ORDER BY orderIndex ASC, id ASC LIMIT 5")
  fun getAllShortcuts(): Flow<List<TransactionShortcutEntity>>

  @Query("SELECT * FROM transaction_shortcuts ORDER BY orderIndex ASC, id ASC LIMIT 5")
  suspend fun getAllShortcutsList(): List<TransactionShortcutEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertShortcut(shortcut: TransactionShortcutEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertShortcuts(shortcuts: List<TransactionShortcutEntity>): List<Long>

  @Update
  suspend fun updateShortcut(shortcut: TransactionShortcutEntity)

  @Delete
  suspend fun deleteShortcut(shortcut: TransactionShortcutEntity)

  @Query("DELETE FROM transaction_shortcuts")
  suspend fun clearAll()
}

@Dao
interface MutualFundSipDao {
  @Query("SELECT * FROM mutual_fund_sips ORDER BY nextExecutionDate ASC")
  fun getAllSips(): Flow<List<MutualFundSipEntity>>

  @Query("SELECT * FROM mutual_fund_sips ORDER BY nextExecutionDate ASC")
  suspend fun getAllSipsList(): List<MutualFundSipEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSip(sip: MutualFundSipEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSips(sips: List<MutualFundSipEntity>): List<Long>

  @Update
  suspend fun updateSip(sip: MutualFundSipEntity)

  @Delete
  suspend fun deleteSip(sip: MutualFundSipEntity)

  @Query("DELETE FROM mutual_fund_sips")
  suspend fun clearAll()
}

@Dao
interface ConditionalMandateDao {
  @Query("SELECT * FROM conditional_mandates ORDER BY id DESC")
  fun getAllMandates(): Flow<List<com.example.data.model.ConditionalMandateEntity>>

  @Query("SELECT * FROM conditional_mandates ORDER BY id DESC")
  suspend fun getAllMandatesList(): List<com.example.data.model.ConditionalMandateEntity>

  @Query("SELECT * FROM conditional_mandates WHERE isEnabled = 1")
  suspend fun getActiveMandatesList(): List<com.example.data.model.ConditionalMandateEntity>

  @Query("SELECT * FROM conditional_mandates WHERE id = :id")
  suspend fun getMandateById(id: Long): com.example.data.model.ConditionalMandateEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMandate(mandate: com.example.data.model.ConditionalMandateEntity): Long

  @Update
  suspend fun updateMandate(mandate: com.example.data.model.ConditionalMandateEntity)

  @Delete
  suspend fun deleteMandate(mandate: com.example.data.model.ConditionalMandateEntity)

  @Query("DELETE FROM conditional_mandates")
  suspend fun clearAll()
}

@Database(
  entities = [
    AccountEntity::class,
    SubscriptionEntity::class,
    ExpenseEntity::class,
    ExpenseLogEntity::class,
    BudgetEntity::class,
    SavingsGoalEntity::class,
    NotificationLogEntity::class,
    LoanEntity::class,
    StockEntity::class,
    TransactionShortcutEntity::class,
    MutualFundSipEntity::class,
    com.example.data.model.ConditionalMandateEntity::class
  ],
  version = 10,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun accountDao(): AccountDao
  abstract fun subscriptionDao(): SubscriptionDao
  abstract fun expenseDao(): ExpenseDao
  abstract fun expenseLogDao(): ExpenseLogDao
  abstract fun budgetDao(): BudgetDao
  abstract fun savingsGoalDao(): SavingsGoalDao
  abstract fun notificationDao(): NotificationDao
  abstract fun loanDao(): LoanDao
  abstract fun stockDao(): StockDao
  abstract fun transactionShortcutDao(): TransactionShortcutDao
  abstract fun mutualFundSipDao(): MutualFundSipDao
  abstract fun conditionalMandateDao(): ConditionalMandateDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "expense_tracker.db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
