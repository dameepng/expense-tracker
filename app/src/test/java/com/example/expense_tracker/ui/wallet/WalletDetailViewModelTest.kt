package com.example.expense_tracker.ui.wallet

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalletDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeWalletRepository(
        private var wallets: List<Wallet> = emptyList()
    ) : WalletRepository {
        override fun getAllWallets(): List<Wallet> = wallets
        override fun getWalletById(id: Long): Wallet? = wallets.find { it.id == id }
        override fun insertWallet(wallet: Wallet) {
            wallets = wallets + wallet
        }
        override fun deleteWallet(wallet: Wallet) {
            wallets = wallets.filterNot { it.id == wallet.id }
        }
    }

    private class FakeExpenseRepository(
        private var expenses: List<Expense> = emptyList()
    ) : ExpenseRepository {
        override fun getTotalExpense(startTime: Long, endTime: Long): Long = 0
        override fun getTotalIncome(startTime: Long, endTime: Long): Long = 0
        override fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense> = emptyList()
        override fun getAllTransactionsBetween(startTime: Long, endTime: Long): List<Expense> = emptyList()
        override fun getCategories(): List<Category> = emptyList()
        override fun insertExpense(expense: Expense) {}
        override fun deleteExpense(expense: Expense) {}
        override fun getExpenseById(id: Long): Expense? = null

        override fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): List<Expense> {
            return expenses.filter { it.walletId == walletId && it.timestamp in startTime until endTime }
        }

        override fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Long {
            return expenses.filter { it.walletId == walletId && it.type == TransactionType.EXPENSE.name && it.timestamp in startTime until endTime }
                .sumOf { it.amount }
        }

        override fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Long {
            return expenses.filter { it.walletId == walletId && it.type == TransactionType.INCOME.name && it.timestamp in startTime until endTime }
                .sumOf { it.amount }
        }

        fun setData(newExpenses: List<Expense>) {
            expenses = newExpenses
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads wallet and calculates balance correctly`() {
        val walletRepo = FakeWalletRepository(listOf(Wallet(id = 1L, name = "Cash", balance = 100_000L)))
        val expenseRepo = FakeExpenseRepository().apply {
            setData(listOf(
                Expense(1L, 50_000L, 1L, "", System.currentTimeMillis(), TransactionType.INCOME.name, walletId = 1L),
                Expense(2L, 20_000L, 2L, "", System.currentTimeMillis(), TransactionType.EXPENSE.name, walletId = 1L),
                // Another wallet's transaction
                Expense(3L, 500_000L, 1L, "", System.currentTimeMillis(), TransactionType.INCOME.name, walletId = 2L),
            ))
        }

        val viewModel = WalletDetailViewModel(1L, walletRepo, expenseRepo, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.wallet)
        assertEquals("Cash", state.wallet?.name)
        assertEquals(50_000L, state.totalIncome)
        assertEquals(20_000L, state.totalExpense)
        // initial (100k) + income (50k) - expense (20k) = 130k
        assertEquals(130_000L, state.balance)
        assertEquals(2, state.transactions.size)
    }
}
