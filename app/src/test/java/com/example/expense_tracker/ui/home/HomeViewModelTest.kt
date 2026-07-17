package com.example.expense_tracker.ui.home

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.CategoryBreakdown
import com.example.expense_tracker.data.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeExpenseRepository
    private val testDispatcher = StandardTestDispatcher()

    // ── Fake Repository ────────────────────────────────────────────

    private class FakeExpenseRepository(
        private var expenses: List<Expense> = emptyList(),
        private var categories: List<Category> = listOf()
    ) : ExpenseRepository {

        override fun getTotalExpense(startTime: Long, endTime: Long): Long {
            return expenses
                .filter { it.timestamp in startTime until endTime && it.type == TransactionType.EXPENSE.name }
                .sumOf { it.amount }
        }
        
        override fun getTotalIncome(startTime: Long, endTime: Long): Long {
            return expenses
                .filter { it.timestamp in startTime until endTime && it.type == TransactionType.INCOME.name }
                .sumOf { it.amount }
        }

        override fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense> {
            return expenses
                .filter { it.timestamp in startTime until endTime && it.type == TransactionType.EXPENSE.name }
                .sortedByDescending { it.timestamp }
        }
        
        override fun getAllTransactionsBetween(startTime: Long, endTime: Long): List<Expense> {
            return expenses
                .filter { it.timestamp in startTime until endTime }
                .sortedByDescending { it.timestamp }
        }

        override fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): List<Expense> {
            return expenses
                .filter { it.walletId == walletId && it.timestamp in startTime until endTime }
                .sortedByDescending { it.timestamp }
        }

        override fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Long {
            return expenses
                .filter { it.walletId == walletId && it.timestamp in startTime until endTime && it.type == TransactionType.EXPENSE.name }
                .sumOf { it.amount }
        }

        override fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Long {
            return expenses
                .filter { it.walletId == walletId && it.timestamp in startTime until endTime && it.type == TransactionType.INCOME.name }
                .sumOf { it.amount }
        }

        override fun getCategories(): List<Category> = categories.toList()

        fun setData(expenses: List<Expense>, categories: List<Category>) {
            this.expenses = expenses.toList()
            this.categories = categories.toList()
        }

        override fun deleteExpense(expense: Expense) {
            expenses = expenses.filterNot { it.id == expense.id }
        }

        override fun insertExpense(expense: Expense) {
            expenses = expenses + expense
        }

        override fun getExpenseById(id: Long): Expense? {
            return expenses.find { it.id == id }
        }
        
    }

    // ── Helpers ────────────────────────────────────────────────────

    private fun todayMidnight(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private val oneDay = 86_400_000L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeExpenseRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun initAndAdvance() {
        viewModel = HomeViewModel(fakeRepository, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    // ── Total & List Tests ─────────────────────────────────────────

    @Test
    fun `total reflects sum of incomes minus expenses`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"), Category(2, "Gaji"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 10_000L, categoryId = 1, timestamp = today + 1000, type = TransactionType.EXPENSE.name),
                Expense(amount = 50_000L, categoryId = 2, timestamp = today + 2000, type = TransactionType.INCOME.name),
                Expense(amount = 20_000L, categoryId = 1, timestamp = today + 3000, type = TransactionType.EXPENSE.name)
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(30_000L, state.totalExpense)
        assertEquals(50_000L, state.totalIncome)
        assertEquals(20_000L, state.totalAmount) // 50k - 30k
        assertEquals(3, state.transactions.size)
    }

    @Test
    fun `list items include category name via join`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"), Category(2, "Gaji"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 15_000L, categoryId = 1, timestamp = today + 1000, type = TransactionType.EXPENSE.name),
                Expense(amount = 25_000L, categoryId = 2, timestamp = today + 2000, type = TransactionType.INCOME.name),
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(2, state.transactions.size)
        // DESC order: newest first
        assertEquals("Gaji", state.transactions[0].categoryName)
        assertEquals("Makanan", state.transactions[1].categoryName)
    }

    // ── Empty State Tests ──────────────────────────────────────────

    @Test
    fun `empty state when no expenses`() {
        initAndAdvance()
        val state = viewModel.uiState.value
        assertEquals(0L, state.totalAmount)
        assertEquals(0L, state.totalIncome)
        assertEquals(0L, state.totalExpense)
        assertTrue(state.transactions.isEmpty())
    }
}
