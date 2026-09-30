package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.MutualFundSipEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.StockEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionShortcutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseRepository(
  private val database: AppDatabase,
  private val getCurrencySymbol: () -> String = { "₹" }
) {

  private val expenseDao = database.expenseDao()
  private val budgetDao = database.budgetDao()
  private val goalDao = database.savingsGoalDao()
  private val notificationDao = database.notificationDao()
  private val accountDao = database.accountDao()
  private val subscriptionDao = database.subscriptionDao()
  private val loanDao = database.loanDao()
  private val stockDao = database.stockDao()
  private val shortcutDao = database.transactionShortcutDao()
  private val sipDao = database.mutualFundSipDao()
  private val mandateDao = database.conditionalMandateDao()

  val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
  val allGoals: Flow<List<SavingsGoalEntity>> = goalDao.getAllGoals()
  val allNotifications: Flow<List<NotificationLogEntity>> = notificationDao.getAllLogs()
  val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()
  val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
  val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
  val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()
  val allStocks: Flow<List<StockEntity>> = stockDao.getAllStocks()
  val allShortcuts: Flow<List<TransactionShortcutEntity>> = shortcutDao.getAllShortcuts()
  val allSips: Flow<List<MutualFundSipEntity>> = sipDao.getAllSips()
  val allMandates: Flow<List<com.example.data.model.ConditionalMandateEntity>> = mandateDao.getAllMandates()

  suspend fun insertMandate(mandate: com.example.data.model.ConditionalMandateEntity): Long = withContext(Dispatchers.IO) {
    mandateDao.insertMandate(mandate)
  }

  suspend fun updateMandate(mandate: com.example.data.model.ConditionalMandateEntity) = withContext(Dispatchers.IO) {
    mandateDao.updateMandate(mandate)
  }

  suspend fun deleteMandate(mandate: com.example.data.model.ConditionalMandateEntity) = withContext(Dispatchers.IO) {
    mandateDao.deleteMandate(mandate)
  }

  suspend fun toggleMandateEnabled(mandate: com.example.data.model.ConditionalMandateEntity) = withContext(Dispatchers.IO) {
    mandateDao.updateMandate(mandate.copy(isEnabled = !mandate.isEnabled))
  }

  suspend fun evaluateAndExecuteMandates(): List<String> = withContext(Dispatchers.IO) {
    val executedMessages = mutableListOf<String>()
    val activeMandates = mandateDao.getActiveMandatesList()
    if (activeMandates.isEmpty()) return@withContext emptyList()

    val accounts = accountDao.getAllAccountsList().associateBy { it.name }
    val sym = getCurrencySymbol()

    for (mandate in activeMandates) {
      val sourceAcc = accounts[mandate.sourceAccount]
      val targetAcc = accounts[mandate.targetAccount]
      if (sourceAcc == null || targetAcc == null) continue
      if (mandate.sourceAccount.equals(mandate.targetAccount, ignoreCase = true)) continue

      when (mandate.conditionType.uppercase()) {
        "BALANCE_BELOW" -> {
          // Trigger when targetAccount balance is below threshold
          if (targetAcc.balance < mandate.thresholdAmount) {
            val transferAmt = mandate.transferAmount
            if (transferAmt > 0 && sourceAcc.balance >= transferAmt) {
              // Execute transfer
              accountDao.adjustBalance(sourceAcc.name, -transferAmt)
              accountDao.adjustBalance(targetAcc.name, transferAmt)

              // Record audit log expense
              val transferExpense = ExpenseEntity(
                title = "Auto-Mandate: ${mandate.title}",
                amount = transferAmt,
                category = "OTHER",
                timestamp = System.currentTimeMillis(),
                account = sourceAcc.name,
                toAccount = targetAcc.name,
                type = "TRANSFER",
                note = "Automated low-balance transfer to ${targetAcc.name} (Balance was $sym${"%.2f".format(targetAcc.balance)} < $sym${"%.2f".format(mandate.thresholdAmount)})"
              )
              expenseDao.insertExpense(transferExpense)

              // Log notification
              val msg = "Mandate Triggered: Transferred $sym${"%.2f".format(transferAmt)} from ${sourceAcc.name} to ${targetAcc.name} because balance ($sym${"%.2f".format(targetAcc.balance)}) was below $sym${"%.2f".format(mandate.thresholdAmount)}."
              notificationDao.insertLog(
                NotificationLogEntity(
                  title = "Automated Mandate Executed",
                  message = msg,
                  type = "REMINDER",
                  timestamp = System.currentTimeMillis()
                )
              )

              mandateDao.updateMandate(
                mandate.copy(
                  lastTriggeredAt = System.currentTimeMillis(),
                  totalTriggeredCount = mandate.totalTriggeredCount + 1
                )
              )
              executedMessages.add(msg)
            }
          }
        }
        "BALANCE_ABOVE" -> {
          // Trigger when sourceAccount balance exceeds threshold (Surplus sweep)
          if (sourceAcc.balance > mandate.thresholdAmount) {
            val surplus = sourceAcc.balance - mandate.thresholdAmount
            val transferAmt = if (mandate.transferAmount > 0 && mandate.transferAmount <= surplus) {
              mandate.transferAmount
            } else {
              surplus
            }

            if (transferAmt > 0) {
              accountDao.adjustBalance(sourceAcc.name, -transferAmt)
              accountDao.adjustBalance(targetAcc.name, transferAmt)

              val transferExpense = ExpenseEntity(
                title = "Auto-Sweep: ${mandate.title}",
                amount = transferAmt,
                category = "OTHER",
                timestamp = System.currentTimeMillis(),
                account = sourceAcc.name,
                toAccount = targetAcc.name,
                type = "TRANSFER",
                note = "Automated surplus sweep to ${targetAcc.name} (Balance was $sym${"%.2f".format(sourceAcc.balance)} > $sym${"%.2f".format(mandate.thresholdAmount)})"
              )
              expenseDao.insertExpense(transferExpense)

              val msg = "Surplus Sweep Executed: Swept $sym${"%.2f".format(transferAmt)} from ${sourceAcc.name} into ${targetAcc.name}."
              notificationDao.insertLog(
                NotificationLogEntity(
                  title = "Surplus Sweep Mandate Executed",
                  message = msg,
                  type = "REMINDER",
                  timestamp = System.currentTimeMillis()
                )
              )

              mandateDao.updateMandate(
                mandate.copy(
                  lastTriggeredAt = System.currentTimeMillis(),
                  totalTriggeredCount = mandate.totalTriggeredCount + 1
                )
              )
              executedMessages.add(msg)
            }
          }
        }
      }
    }
    executedMessages
  }

  fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> =
    budgetDao.getBudgetsForMonth(monthYear)

  fun getAllBudgets(): Flow<List<BudgetEntity>> =
    budgetDao.getAllBudgets()

  suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
    val id = expenseDao.insertExpense(expense)
    // Update account balance
    val delta = if (expense.type == "INCOME") expense.amount else -expense.amount
    accountDao.adjustBalance(expense.account, delta)
    evaluateAndExecuteMandates()
    id
  }

  suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
    expenseDao.deleteExpense(expense)
    val delta = if (expense.type == "INCOME") -expense.amount else expense.amount
    accountDao.adjustBalance(expense.account, delta)
  }

  suspend fun getExpenseById(id: Long): ExpenseEntity? = withContext(Dispatchers.IO) {
    expenseDao.getExpenseById(id)
  }

  fun getExpensesByAccount(accountName: String): Flow<List<ExpenseEntity>> =
    expenseDao.getExpensesByAccount(accountName)

  suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
    expenseDao.updateExpense(expense)
  }

  // Account operations
  suspend fun insertAccount(account: AccountEntity): Long = withContext(Dispatchers.IO) {
    accountDao.insertAccount(account)
  }

  suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
    accountDao.updateAccount(account)
  }

  suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
    accountDao.deleteAccount(account)
  }

  // Subscription operations
  suspend fun insertSubscription(sub: SubscriptionEntity): Long = withContext(Dispatchers.IO) {
    subscriptionDao.insertSubscription(sub)
  }

  suspend fun updateSubscription(sub: SubscriptionEntity) = withContext(Dispatchers.IO) {
    subscriptionDao.updateSubscription(sub)
  }

  suspend fun deleteSubscription(sub: SubscriptionEntity) = withContext(Dispatchers.IO) {
    subscriptionDao.deleteSubscription(sub)
  }

  suspend fun insertBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
    budgetDao.insertBudget(budget)
  }

  suspend fun updateBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
    budgetDao.updateBudget(budget)
  }

  suspend fun deleteBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
    budgetDao.deleteBudget(budget)
  }

  suspend fun insertGoal(goal: SavingsGoalEntity): Long = withContext(Dispatchers.IO) {
    goalDao.insertGoal(goal)
  }

  suspend fun updateGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
    goalDao.updateGoal(goal)
  }

  suspend fun deleteGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
    goalDao.deleteGoal(goal)
  }

  // Loan & Lent operations
  suspend fun insertLoan(loan: LoanEntity, adjustAccountBalance: Boolean = true): Long = withContext(Dispatchers.IO) {
    val id = loanDao.insertLoan(loan)
    if (adjustAccountBalance) {
      // If LENT: money leaves user's account (-loan.totalAmount)
      // If LOAN (Borrowed): money enters user's account (+loan.totalAmount)
      val delta = if (loan.type == "LENT") -loan.totalAmount else loan.totalAmount
      accountDao.adjustBalance(loan.account, delta)

      val txType = if (loan.type == "LENT") "EXPENSE" else "INCOME"
      val txTitle = if (loan.type == "LENT") "Lent to ${loan.personName}" else "Loan from ${loan.personName}"
      expenseDao.insertExpense(
        ExpenseEntity(
          title = txTitle,
          amount = loan.totalAmount,
          category = ExpenseCategory.OTHER.name,
          timestamp = loan.creationDate,
          note = if (loan.note.isNotBlank()) "${loan.note} (Loan/Lent #$id)" else "Loan/Lent #$id",
          type = txType,
          account = loan.account
        )
      )
    }
    id
  }

  suspend fun updateLoan(loan: LoanEntity) = withContext(Dispatchers.IO) {
    loanDao.updateLoan(loan)
  }

  suspend fun deleteLoan(loan: LoanEntity) = withContext(Dispatchers.IO) {
    loanDao.deleteLoan(loan)
  }

  suspend fun recordLoanPayment(
    loan: LoanEntity,
    paymentAmount: Double,
    account: String,
    updateAccountBalance: Boolean = true,
    note: String = ""
  ) = withContext(Dispatchers.IO) {
    val newPaidAmount = loan.paidAmount + paymentAmount
    val isNowSettled = newPaidAmount >= (loan.totalAmount - 0.001)
    val updatedLoan = loan.copy(
      paidAmount = newPaidAmount.coerceAtMost(loan.totalAmount),
      isSettled = isNowSettled
    )
    loanDao.updateLoan(updatedLoan)

    if (updateAccountBalance) {
      // If LENT: they paid back -> money enters user's account (+paymentAmount)
      // If LOAN: user paid back lender -> money leaves user's account (-paymentAmount)
      val delta = if (loan.type == "LENT") paymentAmount else -paymentAmount
      accountDao.adjustBalance(account, delta)

      val txType = if (loan.type == "LENT") "INCOME" else "EXPENSE"
      val txTitle = if (loan.type == "LENT") "Repayment from ${loan.personName}" else "Loan payment to ${loan.personName}"
      expenseDao.insertExpense(
        ExpenseEntity(
          title = txTitle,
          amount = paymentAmount,
          category = ExpenseCategory.OTHER.name,
          timestamp = System.currentTimeMillis(),
          note = if (note.isNotBlank()) note else if (isNowSettled) "Fully Settled" else "Partial payment",
          type = txType,
          account = account
        )
      )
    }

    if (isNowSettled) {
      notificationDao.insertLog(
        NotificationLogEntity(
          title = if (loan.type == "LENT") "🤝 Debt Settled: ${loan.personName}" else "🎉 Loan Fully Repaid: ${loan.personName}",
          message = if (loan.type == "LENT")
            "${loan.personName} has fully paid back the ${getCurrencySymbol()}${String.format(Locale.US, "%.2f", loan.totalAmount)} debt!"
          else
            "You have fully repaid the ${getCurrencySymbol()}${String.format(Locale.US, "%.2f", loan.totalAmount)} loan to ${loan.personName}!",
          type = "GOAL_REACHED",
          timestamp = System.currentTimeMillis()
        )
      )
    }
  }

  suspend fun settleLoanInFull(loan: LoanEntity, account: String, updateAccountBalance: Boolean = true) = withContext(Dispatchers.IO) {
    val remaining = loan.remainingAmount
    if (remaining > 0) {
      recordLoanPayment(loan, remaining, account, updateAccountBalance, "Settled in full")
    } else {
      loanDao.updateLoan(loan.copy(isSettled = true))
    }
  }

  suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
    notificationDao.markAllAsRead()
  }

  suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
    notificationDao.clearAll()
  }

  suspend fun insertNotificationLog(log: NotificationLogEntity) = withContext(Dispatchers.IO) {
    notificationDao.insertLog(log)
  }

  suspend fun getCategorySpendingForMonth(category: String, startTime: Long, endTime: Long): Double {
    return withContext(Dispatchers.IO) {
      expenseDao.getCategorySpendingBetween(category, startTime, endTime) ?: 0.0
    }
  }

  suspend fun getTotalSpendingForMonth(startTime: Long, endTime: Long): Double {
    return withContext(Dispatchers.IO) {
      expenseDao.getTotalSpendingBetween(startTime, endTime) ?: 0.0
    }
  }

  suspend fun preseedDataIfEmpty() = withContext(Dispatchers.IO) {
    // Intentionally a no-op: Automatic sample data seeding on startup is disabled per user configuration.
  }

  suspend fun preseedSampleDataManually() = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60 * 60 * 1000
    val monthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time)

    // Preseed BudgetWise Accounts
    accountDao.insertAccount(
      AccountEntity(
        name = "Main Checking",
        type = "CHECKING",
        balance = 3420.00,
        colorHex = 0xFF1B664BL,
        iconName = "ACCOUNT"
      )
    )
    accountDao.insertAccount(
      AccountEntity(
        name = "Cash Wallet",
        type = "CASH",
        balance = 180.00,
        colorHex = 0xFFE09A26L,
        iconName = "CASH"
      )
    )
    accountDao.insertAccount(
      AccountEntity(
        name = "High-Yield Savings",
        type = "SAVINGS",
        balance = 8500.00,
        colorHex = 0xFF2E86DEL,
        iconName = "SAVINGS"
      )
    )
    accountDao.insertAccount(
      AccountEntity(
        name = "Sapphire Credit Card",
        type = "CREDIT",
        balance = -342.50,
        colorHex = 0xFFE74C3CL,
        iconName = "CREDIT"
      )
    )

    // Preseed Subscriptions
    subscriptionDao.insertSubscription(
      SubscriptionEntity(
        title = "Netflix 4K Premium",
        amount = 22.99,
        billingCycle = "Monthly",
        category = ExpenseCategory.ENTERTAINMENT.name,
        account = "Sapphire Credit Card",
        nextDueDate = now + (dayMillis * 4),
        note = "Family plan streaming"
      )
    )
    subscriptionDao.insertSubscription(
      SubscriptionEntity(
        title = "Spotify Duo",
        amount = 14.99,
        billingCycle = "Monthly",
        category = ExpenseCategory.ENTERTAINMENT.name,
        account = "Sapphire Credit Card",
        nextDueDate = now + (dayMillis * 11),
        note = "Music streaming"
      )
    )
    subscriptionDao.insertSubscription(
      SubscriptionEntity(
        title = "Boulder Gym Membership",
        amount = 55.00,
        billingCycle = "Monthly",
        category = ExpenseCategory.HEALTH.name,
        account = "Main Checking",
        nextDueDate = now + (dayMillis * 7),
        note = "Climbing and gym access"
      )
    )
    subscriptionDao.insertSubscription(
      SubscriptionEntity(
        title = "Fiber Gigabit Internet",
        amount = 70.00,
        billingCycle = "Monthly",
        category = ExpenseCategory.HOUSING.name,
        account = "Main Checking",
        nextDueDate = now + (dayMillis * 18),
        note = "Home high speed broadband"
      )
    )
    subscriptionDao.insertSubscription(
      SubscriptionEntity(
        title = "Google One 2TB Cloud",
        amount = 9.99,
        billingCycle = "Monthly",
        category = ExpenseCategory.EDUCATION.name,
        account = "Sapphire Credit Card",
        nextDueDate = now + (dayMillis * 23),
        note = "Drive & Photos storage"
      )
    )

    // Preseed starter transactions (Income & Expenses)
    val sampleExpenses = listOf(
      ExpenseEntity(
        title = "Monthly Tech Salary",
        amount = 4250.00,
        category = ExpenseCategory.SALARY.name,
        timestamp = now - (dayMillis * 16),
        note = "Direct deposit from employer",
        type = "INCOME",
        account = "Main Checking"
      ),
      ExpenseEntity(
        title = "Freelance App UI Design",
        amount = 680.00,
        category = ExpenseCategory.FREELANCE.name,
        timestamp = now - (dayMillis * 8),
        note = "Client milestone payment",
        type = "INCOME",
        account = "Main Checking"
      ),
      ExpenseEntity(
        title = "Organic Produce Market",
        amount = 84.50,
        category = ExpenseCategory.GROCERIES.name,
        timestamp = now - (dayMillis * 1),
        note = "Weekly produce and pantry restock",
        type = "EXPENSE",
        account = "Sapphire Credit Card"
      ),
      ExpenseEntity(
        title = "Artisan Bakery & Cafe",
        amount = 18.75,
        category = ExpenseCategory.FOOD.name,
        timestamp = now - (dayMillis * 2),
        note = "Cold brew & pastry with team",
        type = "EXPENSE",
        account = "Cash Wallet"
      ),
      ExpenseEntity(
        title = "Metro Transit Pass",
        amount = 45.00,
        category = ExpenseCategory.TRANSPORT.name,
        timestamp = now - (dayMillis * 3),
        note = "Monthly subway card reload",
        type = "EXPENSE",
        account = "Main Checking"
      ),
      ExpenseEntity(
        title = "Streaming Subscription",
        amount = 22.99,
        category = ExpenseCategory.ENTERTAINMENT.name,
        timestamp = now - (dayMillis * 5),
        note = "Video streaming subscription",
        type = "EXPENSE",
        account = "Sapphire Credit Card"
      ),
      ExpenseEntity(
        title = "City Power & Utilities",
        amount = 92.30,
        category = ExpenseCategory.HOUSING.name,
        timestamp = now - (dayMillis * 7),
        note = "Electric & clean water bill",
        type = "EXPENSE",
        account = "Main Checking"
      ),
      ExpenseEntity(
        title = "Minimalist Sneakers",
        amount = 89.00,
        category = ExpenseCategory.SHOPPING.name,
        timestamp = now - (dayMillis * 9),
        note = "Comfortable daily footwear",
        type = "EXPENSE",
        account = "Sapphire Credit Card"
      ),
      ExpenseEntity(
        title = "Bouldering Day Pass",
        amount = 28.00,
        category = ExpenseCategory.HEALTH.name,
        timestamp = now - (dayMillis * 11),
        note = "Weekend session with friends",
        type = "EXPENSE",
        account = "Cash Wallet"
      ),
      ExpenseEntity(
        title = "Software Engineering Book",
        amount = 36.50,
        category = ExpenseCategory.EDUCATION.name,
        timestamp = now - (dayMillis * 14),
        note = "Modern Architecture handbook",
        type = "EXPENSE",
        account = "Main Checking"
      ),
      ExpenseEntity(
        title = "Bought 10 TCS",
        amount = 36500.0,
        category = ExpenseCategory.INVESTMENT.name,
        timestamp = now - (dayMillis * 3),
        note = "Investment: 10 shares of TCS @ ₹3,650.00. Debited from Main Checking.",
        type = "EXPENSE",
        account = "Main Checking",
        stockSymbol = "TCS",
        stockPrice = 3650.0,
        stockShares = 10.0,
        assetType = "STOCK"
      ),
      ExpenseEntity(
        title = "SIP: Parag Parikh Flexi Cap Fund",
        amount = 5000.0,
        category = ExpenseCategory.INVESTMENT.name,
        timestamp = now - (dayMillis * 6),
        note = "Monthly SIP installment: 73.099 units @ NAV ₹68.40. Debited from Main Checking.",
        type = "EXPENSE",
        account = "Main Checking",
        stockSymbol = "119551",
        stockPrice = 68.40,
        stockShares = 73.099,
        assetType = "MUTUAL_FUND"
      )
    )

    for (exp in sampleExpenses) {
      expenseDao.insertExpense(exp)
    }

    // Preseed budgets
    budgetDao.insertBudget(
      BudgetEntity(
        category = "TOTAL",
        monthlyLimit = 1600.0,
        monthYear = monthYear
      )
    )
    budgetDao.insertBudget(
      BudgetEntity(
        category = ExpenseCategory.GROCERIES.name,
        monthlyLimit = 350.0,
        monthYear = monthYear
      )
    )
    budgetDao.insertBudget(
      BudgetEntity(
        category = ExpenseCategory.FOOD.name,
        monthlyLimit = 250.0,
        monthYear = monthYear
      )
    )
    budgetDao.insertBudget(
      BudgetEntity(
        category = ExpenseCategory.TRANSPORT.name,
        monthlyLimit = 100.0,
        monthYear = monthYear
      )
    )
    budgetDao.insertBudget(
      BudgetEntity(
        category = ExpenseCategory.HOUSING.name,
        monthlyLimit = 450.0,
        monthYear = monthYear
      )
    )
    budgetDao.insertBudget(
      BudgetEntity(
        category = ExpenseCategory.SHOPPING.name,
        monthlyLimit = 180.0,
        monthYear = monthYear
      )
    )
    budgetDao.insertBudget(
      BudgetEntity(
        category = ExpenseCategory.ENTERTAINMENT.name,
        monthlyLimit = 100.0,
        monthYear = monthYear
      )
    )

    // Preseed savings goals
    goalDao.insertGoal(
      SavingsGoalEntity(
        title = "Emergency Safety Reserve",
        targetAmount = 3000.0,
        currentAmount = 2100.0,
        deadlineTimestamp = now + (dayMillis * 90),
        category = "SAVINGS",
        isCompleted = false,
        notifiedFiftyPercent = true,
        notifiedHundredPercent = false
      )
    )
    goalDao.insertGoal(
      SavingsGoalEntity(
        title = "New M3 MacBook Pro",
        targetAmount = 1400.0,
        currentAmount = 750.0,
        deadlineTimestamp = now + (dayMillis * 45),
        category = "GADGET",
        isCompleted = false,
        notifiedFiftyPercent = true,
        notifiedHundredPercent = false
      )
    )
    goalDao.insertGoal(
      SavingsGoalEntity(
        title = "Kyoto Autumn Trip",
        targetAmount = 1000.0,
        currentAmount = 420.0,
        deadlineTimestamp = now + (dayMillis * 120),
        category = "TRAVEL",
        isCompleted = false,
        notifiedFiftyPercent = false,
        notifiedHundredPercent = false
      )
    )

    // Preseed initial notification log
    notificationDao.insertLog(
      NotificationLogEntity(
        title = "🎯 Milestone Reached: New M3 MacBook Pro",
        message = "You've reached 53% of your savings goal! ($750.00 saved of $1,400.00). Keep going!",
        type = "GOAL_MILESTONE",
        timestamp = now - (dayMillis * 2),
        isRead = false
      )
    )
    notificationDao.insertLog(
      NotificationLogEntity(
        title = "📊 Monthly Budget Active",
        message = "Your September budget is active with $1,600.00 total limit.",
        type = "INFO",
        timestamp = now - (dayMillis * 5),
        isRead = true
      )
    )

    // Preseed Loans & Lent
    loanDao.insertLoan(
      LoanEntity(
        personName = "Alex Rivera",
        type = "LENT",
        totalAmount = 180.0,
        paidAmount = 80.0,
        account = "Main Checking",
        dueDate = now + (dayMillis * 10),
        note = "Concert tickets & weekend share"
      )
    )
    loanDao.insertLoan(
      LoanEntity(
        personName = "Tech Equipment Installment",
        type = "LOAN",
        totalAmount = 1200.0,
        paidAmount = 500.0,
        account = "Sapphire Credit Card",
        dueDate = now + (dayMillis * 75),
        interestRate = 3.5,
        note = "Studio monitor and desk hardware"
      )
    )
    loanDao.insertLoan(
      LoanEntity(
        personName = "Sarah Jenkins",
        type = "LENT",
        totalAmount = 65.0,
        paidAmount = 0.0,
        account = "Cash Wallet",
        dueDate = now + (dayMillis * 4),
        note = "Team celebration dinner share"
      )
    )
    loanDao.insertLoan(
      LoanEntity(
        personName = "David Chen",
        type = "LENT",
        totalAmount = 150.0,
        paidAmount = 150.0,
        account = "Main Checking",
        dueDate = now - (dayMillis * 5),
        isSettled = true,
        note = "Moving truck reservation"
      )
    )

    // Preseed Portfolio Stocks
    val curr = getCurrencySymbol()
    if (stockDao.getAllStocks().first().isEmpty()) {
      stockDao.insertStock(
        StockEntity(
          symbol = "RELIANCE",
          companyName = "Reliance Industries Ltd",
          shares = 15.0,
          avgBuyPrice = 2480.0,
          currentPrice = 2960.50,
          currencySymbol = curr,
          notes = "Energy, retail & telecom conglomerate"
        )
      )
      stockDao.insertStock(
        StockEntity(
          symbol = "TCS",
          companyName = "Tata Consultancy Services",
          shares = 10.0,
          avgBuyPrice = 3650.0,
          currentPrice = 4180.00,
          currencySymbol = curr,
          notes = "Global IT leader & steady dividend"
        )
      )
      stockDao.insertStock(
        StockEntity(
          symbol = "INFY",
          companyName = "Infosys Limited",
          shares = 25.0,
          avgBuyPrice = 1620.0,
          currentPrice = 1515.20,
          currencySymbol = curr,
          notes = "IT consulting & digital services"
        )
      )
      stockDao.insertStock(
        StockEntity(
          symbol = "HDFCBANK",
          companyName = "HDFC Bank Ltd",
          shares = 35.0,
          avgBuyPrice = 1490.0,
          currentPrice = 1640.75,
          currencySymbol = curr,
          notes = "India's largest private bank"
        )
      )
      stockDao.insertStock(
        StockEntity(
          symbol = "TATAMOTORS",
          companyName = "Tata Motors Limited",
          shares = 50.0,
          avgBuyPrice = 820.0,
          currentPrice = 965.40,
          currencySymbol = curr,
          notes = "EV & passenger vehicle pioneer"
        )
      )
    }

    // Preseed 5 helpful transaction shortcuts if empty
    if (shortcutDao.getAllShortcutsList().isEmpty()) {
      shortcutDao.insertShortcuts(
        listOf(
          TransactionShortcutEntity(
            title = "Coffee",
            amount = 120.0,
            category = "FOOD",
            type = "EXPENSE",
            account = "Main Checking",
            iconEmoji = "☕",
            orderIndex = 0
          ),
          TransactionShortcutEntity(
            title = "Lunch",
            amount = 250.0,
            category = "FOOD",
            type = "EXPENSE",
            account = "Main Checking",
            iconEmoji = "🍔",
            orderIndex = 1
          ),
          TransactionShortcutEntity(
            title = "Metro/Transit",
            amount = 50.0,
            category = "TRANSPORT",
            type = "EXPENSE",
            account = "Main Checking",
            iconEmoji = "🚌",
            orderIndex = 2
          ),
          TransactionShortcutEntity(
            title = "Groceries",
            amount = 500.0,
            category = "GROCERIES",
            type = "EXPENSE",
            account = "Main Checking",
            iconEmoji = "🛒",
            orderIndex = 3
          ),
          TransactionShortcutEntity(
            title = "Fuel",
            amount = 1000.0,
            category = "TRANSPORT",
            type = "EXPENSE",
            account = "Main Checking",
            iconEmoji = "⛽",
            orderIndex = 4
          )
        )
      )
    }

    // Preseed Mutual Fund SIP if empty
    if (sipDao.getAllSipsList().isEmpty()) {
      sipDao.insertSip(
        MutualFundSipEntity(
          schemeCode = "119551",
          schemeName = "Parag Parikh Flexi Cap Fund",
          installmentAmount = 5000.0,
          frequency = "Monthly",
          debitAccount = "Main Checking",
          sipDayOfMonth = 5,
          nextExecutionDate = now + (dayMillis * 12),
          totalInvested = 15000.0,
          installmentsCompleted = 3,
          notes = "Diversified flexi cap growth allocation"
        )
      )
    }
  }

  // Mutual Fund SIP operations
  suspend fun insertSip(sip: MutualFundSipEntity): Long = withContext(Dispatchers.IO) {
    sipDao.insertSip(sip)
  }

  suspend fun updateSip(sip: MutualFundSipEntity) = withContext(Dispatchers.IO) {
    sipDao.updateSip(sip)
  }

  suspend fun deleteSip(sip: MutualFundSipEntity) = withContext(Dispatchers.IO) {
    sipDao.deleteSip(sip)
  }

  suspend fun getAllSipsList(): List<MutualFundSipEntity> = withContext(Dispatchers.IO) {
    sipDao.getAllSipsList()
  }

  // Transaction Shortcut operations
  suspend fun insertShortcut(shortcut: TransactionShortcutEntity): Long = withContext(Dispatchers.IO) {
    shortcutDao.insertShortcut(shortcut)
  }

  suspend fun updateShortcut(shortcut: TransactionShortcutEntity) = withContext(Dispatchers.IO) {
    shortcutDao.updateShortcut(shortcut)
  }

  suspend fun deleteShortcut(shortcut: TransactionShortcutEntity) = withContext(Dispatchers.IO) {
    shortcutDao.deleteShortcut(shortcut)
  }

  suspend fun getAllShortcutsList(): List<TransactionShortcutEntity> = withContext(Dispatchers.IO) {
    shortcutDao.getAllShortcutsList()
  }

  // Stock operations
  suspend fun insertStock(stock: StockEntity): Long = withContext(Dispatchers.IO) {
    stockDao.insertStock(stock)
  }

  suspend fun updateStock(stock: StockEntity) = withContext(Dispatchers.IO) {
    stockDao.updateStock(stock)
  }

  suspend fun updateStockPrice(id: Long, newPrice: Double) = withContext(Dispatchers.IO) {
    val existing = stockDao.getStockById(id)
    if (existing != null) {
      stockDao.updateStock(existing.copy(currentPrice = newPrice))
    }
  }

  suspend fun deleteStock(stock: StockEntity) = withContext(Dispatchers.IO) {
    stockDao.deleteStock(stock)
  }

  // --- Bulk Data Management / Clear Operations ---
  suspend fun clearTransactionsAndBudgets(): Unit = withContext(Dispatchers.IO) {
    expenseDao.clearAll()
    budgetDao.clearAll()
  }

  suspend fun clearStocksAndSips(): Unit = withContext(Dispatchers.IO) {
    stockDao.clearAll()
    sipDao.clearAll()
  }

  suspend fun clearLoansAndSubscriptions(): Unit = withContext(Dispatchers.IO) {
    loanDao.clearAll()
    subscriptionDao.clearAll()
    goalDao.clearAll()
  }

  suspend fun resetAllFinancialData(): Unit = withContext(Dispatchers.IO) {
    expenseDao.clearAll()
    accountDao.clearAll()
    budgetDao.clearAll()
    goalDao.clearAll()
    subscriptionDao.clearAll()
    loanDao.clearAll()
    stockDao.clearAll()
    sipDao.clearAll()
    notificationDao.clearAll()
    shortcutDao.clearAll()
    mandateDao.clearAll()
    // Recreate clean default starter accounts
    accountDao.insertAccount(AccountEntity(name = "Checking Account", type = "CHECKING", balance = 0.0))
    accountDao.insertAccount(AccountEntity(name = "Cash", type = "CASH", balance = 0.0))
    accountDao.insertAccount(AccountEntity(name = "Savings", type = "SAVINGS", balance = 0.0))
  }

  suspend fun clearAllDataCompletely(): Unit = withContext(Dispatchers.IO) {
    expenseDao.clearAll()
    accountDao.clearAll()
    budgetDao.clearAll()
    goalDao.clearAll()
    subscriptionDao.clearAll()
    loanDao.clearAll()
    stockDao.clearAll()
    sipDao.clearAll()
    notificationDao.clearAll()
    shortcutDao.clearAll()
    mandateDao.clearAll()
  }
}


