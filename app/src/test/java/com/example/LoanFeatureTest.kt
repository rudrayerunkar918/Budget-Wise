package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.LoanEntity
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoanFeatureTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: ExpenseRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = ExpenseRepository(database)

    runBlocking {
      // Seed checking account
      repository.insertAccount(AccountEntity(id = 1, name = "Main Checking", type = "CHECKING", balance = 1000.0, colorHex = 0xFF1B664BL))
    }
  }

  @After
  fun teardown() {
    database.close()
  }

  @Test
  fun testLoanEntityRemainingAndProgress() {
    val lent = LoanEntity(
      personName = "Alex",
      type = "LENT",
      totalAmount = 200.0,
      paidAmount = 50.0,
      account = "Main Checking"
    )

    assertEquals(150.0, lent.remainingAmount, 0.001)
    assertEquals(0.25f, lent.progressFraction, 0.001f)
    assertEquals(25, lent.progressPercent)
    assertFalse(lent.isSettled)
  }

  @Test
  fun testInsertLoanAndRecordPayment() = runBlocking {
    // 1. Insert Lent item: Lent $100 to Sarah (balance should deduct $100 -> $900)
    val loan = LoanEntity(
      personName = "Sarah Jenkins",
      type = "LENT",
      totalAmount = 100.0,
      paidAmount = 0.0,
      account = "Main Checking"
    )
    val loanId = repository.insertLoan(loan, adjustAccountBalance = true)
    val inserted = repository.allLoans.first().first { it.id == loanId }
    assertEquals("Sarah Jenkins", inserted.personName)
    assertEquals(100.0, inserted.remainingAmount, 0.001)

    // Verify account balance reduced to 900.0
    val accountAfterLend = repository.allAccounts.first().first { it.name == "Main Checking" }
    assertEquals(900.0, accountAfterLend.balance, 0.001)

    // 2. Record partial repayment of $40 (Sarah pays back $40 -> balance increases to $940)
    repository.recordLoanPayment(
      loan = inserted,
      paymentAmount = 40.0,
      account = "Main Checking",
      updateAccountBalance = true,
      note = "Venmo repayment"
    )

    val updatedLoan = repository.allLoans.first().first { it.id == loanId }
    assertEquals(40.0, updatedLoan.paidAmount, 0.001)
    assertEquals(60.0, updatedLoan.remainingAmount, 0.001)
    assertFalse(updatedLoan.isSettled)

    val accountAfterRepay = repository.allAccounts.first().first { it.name == "Main Checking" }
    assertEquals(940.0, accountAfterRepay.balance, 0.001)

    // 3. Settle remaining $60 in full (Sarah settles remaining balance -> balance increases to $1000)
    repository.settleLoanInFull(
      loan = updatedLoan,
      account = "Main Checking",
      updateAccountBalance = true
    )

    val settledLoan = repository.allLoans.first().first { it.id == loanId }
    assertEquals(100.0, settledLoan.paidAmount, 0.001)
    assertEquals(0.0, settledLoan.remainingAmount, 0.001)
    assertTrue(settledLoan.isSettled)

    val accountAfterFullSettle = repository.allAccounts.first().first { it.name == "Main Checking" }
    assertEquals(1000.0, accountAfterFullSettle.balance, 0.001)
  }

  @Test
  fun testBorrowedLoanRepayment() = runBlocking {
    // Borrowed $500 from Dave (we receive $500 -> balance increases from $1000 to $1500)
    val loan = LoanEntity(
      personName = "Dave Miller",
      type = "LOAN",
      totalAmount = 500.0,
      paidAmount = 0.0,
      account = "Main Checking"
    )
    val loanId = repository.insertLoan(loan, adjustAccountBalance = true)

    val accountAfterBorrow = repository.allAccounts.first().first { it.name == "Main Checking" }
    assertEquals(1500.0, accountAfterBorrow.balance, 0.001)

    // We make a repayment of $200 back to Dave (balance decreases by $200 to $1300)
    val inserted = repository.allLoans.first().first { it.id == loanId }
    repository.recordLoanPayment(
      loan = inserted,
      paymentAmount = 200.0,
      account = "Main Checking",
      updateAccountBalance = true,
      note = "First installment"
    )

    val updatedLoan = repository.allLoans.first().first { it.id == loanId }
    assertEquals(200.0, updatedLoan.paidAmount, 0.001)
    assertEquals(300.0, updatedLoan.remainingAmount, 0.001)

    val accountAfterRepay = repository.allAccounts.first().first { it.name == "Main Checking" }
    assertEquals(1300.0, accountAfterRepay.balance, 0.001)
  }
}
