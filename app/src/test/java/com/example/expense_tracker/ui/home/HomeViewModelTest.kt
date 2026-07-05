package com.example.expense_tracker.ui.home

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.FilterPeriod
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

/**
 * TDD tests for HomeViewModel.
 *
 * Verifies US3 AC:
 * - Total displayed prominently
 * - Filter switches update total & list
 * - Empty state when no data
 */
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
                .filter { it.timestamp in startTime until endTime }
                .sumOf { it.amount }
        }

        override fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense> {
            return expenses
                .filter { it.timestamp in startTime until endTime }
                .sortedByDescending { it.timestamp }
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

    // ── Filter Tests ───────────────────────────────────────────────

    @Test
    fun `default filter is TODAY with label Hari Ini`() {
        initAndAdvance()
        val state = viewModel.uiState.value
        assertEquals(FilterPeriod.TODAY, state.filter)
        assertEquals("Hari Ini", state.periodLabel)
    }

    @Test
    fun `switch filter to WEEK updates label to Minggu Ini`() {
        initAndAdvance()
        viewModel.onFilterSelected(FilterPeriod.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Minggu Ini", viewModel.uiState.value.periodLabel)
        assertEquals(FilterPeriod.WEEK, viewModel.uiState.value.filter)
    }

    @Test
    fun `switch filter to MONTH updates label to Bulan Ini`() {
        initAndAdvance()
        viewModel.onFilterSelected(FilterPeriod.MONTH)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Bulan Ini", viewModel.uiState.value.periodLabel)
        assertEquals(FilterPeriod.MONTH, viewModel.uiState.value.filter)
    }

    @Test
    fun `switching filter back to TODAY restores original label`() {
        initAndAdvance()
        viewModel.onFilterSelected(FilterPeriod.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onFilterSelected(FilterPeriod.TODAY)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Hari Ini", viewModel.uiState.value.periodLabel)
    }

    // ── Total & List Tests ─────────────────────────────────────────

    @Test
    fun `total reflects sum of expenses for today filter`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"), Category(2, "Transport"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 10_000L, categoryId = 1, timestamp = today + 1000),
                Expense(amount = 20_000L, categoryId = 2, timestamp = today + 2000),
                Expense(amount = 30_000L, categoryId = 1, timestamp = today - oneDay)
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(30_000L, state.totalAmount)
        assertEquals(2, state.expenses.size)
    }

    @Test
    fun `list items include category name via join`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"), Category(2, "Transport"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 15_000L, categoryId = 1, timestamp = today + 1000),
                Expense(amount = 25_000L, categoryId = 2, timestamp = today + 2000),
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(2, state.expenses.size)
        // DESC order: newest first → Transport (today+2000), Makanan (today+1000)
        assertEquals("Transport", state.expenses[0].categoryName)
        assertEquals("Makanan", state.expenses[1].categoryName)
    }

    @Test
    fun `list items are ordered newest first`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 10_000L, categoryId = 1, timestamp = today + 1000),
                Expense(amount = 30_000L, categoryId = 1, timestamp = today + 3000),
                Expense(amount = 20_000L, categoryId = 1, timestamp = today + 2000),
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(3, state.expenses.size)
        assertEquals(30_000L, state.expenses[0].amount)
        assertEquals(20_000L, state.expenses[1].amount)
        assertEquals(10_000L, state.expenses[2].amount)
    }

    // ── Empty State Tests ──────────────────────────────────────────

    @Test
    fun `empty state when no expenses in current filter period`() {
        initAndAdvance()
        val state = viewModel.uiState.value
        assertEquals(0L, state.totalAmount)
        assertTrue(state.expenses.isEmpty())
    }

    @Test
    fun `empty state when all expenses are outside current filter`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 10_000L, categoryId = 1, timestamp = today - oneDay),
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(0L, state.totalAmount)
        assertTrue(state.expenses.isEmpty())
    }

    @Test
    fun `filter change recalculates total and list`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 10_000L, categoryId = 1, timestamp = today + 1000),
                Expense(amount = 20_000L, categoryId = 1, timestamp = today + 5000),
            ),
            categories = categories
        )
        initAndAdvance()

        // Initially TODAY: 30k total, 2 items
        assertEquals(30_000L, viewModel.uiState.value.totalAmount)
        assertEquals(2, viewModel.uiState.value.expenses.size)
        assertEquals(FilterPeriod.TODAY, viewModel.uiState.value.filter)

        // Switch to WEEK: same data (both today), but filter label changes
        viewModel.onFilterSelected(FilterPeriod.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(FilterPeriod.WEEK, viewModel.uiState.value.filter)
        assertEquals("Minggu Ini", viewModel.uiState.value.periodLabel)
    }
}
