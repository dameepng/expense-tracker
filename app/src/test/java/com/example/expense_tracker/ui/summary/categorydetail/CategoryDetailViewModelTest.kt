package com.example.expense_tracker.ui.summary.categorydetail

import androidx.lifecycle.SavedStateHandle
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.ExpenseWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeExpenseRepository

    private class FakeExpenseRepository : ExpenseRepository {
        val categories = mutableListOf<Category>()
        val expenses = mutableListOf<Expense>()
        val deletedExpenses = mutableListOf<Expense>()
        val insertedExpenses = mutableListOf<Expense>()

        override fun getCategories(): Flow<List<Category>> = flowOf(categories.toList())

        override fun getCategoriesByType(type: String): Flow<List<Category>> =
            flowOf(categories.filter { it.type == type })

        override fun insertExpense(expense: Expense) {
            insertedExpenses.add(expense)
            expenses.add(expense)
        }

        override fun deleteExpense(expense: Expense) {
            deletedExpenses.add(expense)
            expenses.removeAll { it.id == expense.id }
        }

        override fun getExpenseById(id: Long): Expense? = expenses.find { it.id == id }

        override fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>> =
            flowOf(expenses.filter { it.timestamp in startTime..endTime })

        override fun getAllTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Expense>> =
            flowOf(expenses.filter { it.timestamp in startTime..endTime })

        override fun getAllTransactions(): List<Expense> = expenses.toList()

        override fun getTotalExpense(startTime: Long, endTime: Long): Flow<Long> =
            flowOf(expenses.filter { it.type == "EXPENSE" && it.timestamp in startTime..endTime }.sumOf { it.amount })

        override fun getTotalIncome(startTime: Long, endTime: Long): Flow<Long> =
            flowOf(expenses.filter { it.type == "INCOME" && it.timestamp in startTime..endTime }.sumOf { it.amount })

        override fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long> =
            flowOf(expenses.filter { it.walletId == walletId && it.type == "EXPENSE" && it.timestamp in startTime..endTime }.sumOf { it.amount })

        override fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long> =
            flowOf(expenses.filter { it.walletId == walletId && it.type == "INCOME" && it.timestamp in startTime..endTime }.sumOf { it.amount })

        override fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<List<Expense>> =
            flowOf(expenses.filter { it.walletId == walletId && it.timestamp in startTime..endTime })

        override fun deleteExpensesByWalletId(walletId: Long) {
            expenses.removeAll { it.walletId == walletId }
        }

        override fun getTransactionsByCategory(categoryId: Long, startTime: Long, endTime: Long): Flow<List<Expense>> =
            flowOf(expenses.filter { it.categoryId == categoryId && it.timestamp in startTime..endTime })

        override fun getTransactionsByCategoryAndWallet(
            categoryId: Long,
            walletId: Long,
            startTime: Long,
            endTime: Long
        ): Flow<List<Expense>> =
            flowOf(expenses.filter { it.categoryId == categoryId && it.walletId == walletId && it.timestamp in startTime..endTime })
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeExpenseRepository().apply {
            categories.add(Category(id = 1L, name = "Food", type = "EXPENSE"))
            expenses.add(
                Expense(
                    id = 101L,
                    amount = 50_000L,
                    description = "Lunch",
                    timestamp = 2000L,
                    type = "EXPENSE",
                    categoryId = 1L,
                    walletId = 5L
                )
            )
        }
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadData_loadsCategoryAndTransactions() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "categoryId" to "1",
                "startTime" to "1000",
                "endTime" to "3000"
            )
        )

        val viewModel = CategoryDetailViewModel(fakeRepository, savedStateHandle, testDispatcher)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.category)
        assertEquals("Food", state.category?.name)
        assertEquals(1, state.transactions.size)
        assertEquals("Lunch", state.transactions[0].description)
        assertEquals("Food", state.transactions[0].categoryName)
    }

    @Test
    fun deleteExpense_and_undoDeleteExpense_delegateToRepository() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "categoryId" to "1",
                "startTime" to "1000",
                "endTime" to "3000"
            )
        )

        val viewModel = CategoryDetailViewModel(fakeRepository, savedStateHandle, testDispatcher)
        testScheduler.advanceUntilIdle()

        val itemToDelete = viewModel.uiState.value.transactions[0]
        viewModel.deleteExpense(itemToDelete)
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.deletedExpenses.size)
        assertEquals(101L, fakeRepository.deletedExpenses[0].id)

        viewModel.undoDeleteExpense(itemToDelete)
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.insertedExpenses.size)
        assertEquals(101L, fakeRepository.insertedExpenses[0].id)
    }

    @Test
    fun loadData_withWalletFilter_filtersByWallet() = runTest(testDispatcher) {
        fakeRepository.expenses.add(
            Expense(
                id = 102L,
                amount = 25_000L,
                description = "Snack Other Wallet",
                timestamp = 2500L,
                type = "EXPENSE",
                categoryId = 1L,
                walletId = 99L
            )
        )

        val savedStateHandle = SavedStateHandle(
            mapOf(
                "categoryId" to "1",
                "walletId" to "5",
                "startTime" to "1000",
                "endTime" to "3000"
            )
        )

        val viewModel = CategoryDetailViewModel(fakeRepository, savedStateHandle, testDispatcher)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.transactions.size)
        assertEquals(101L, state.transactions[0].id)
    }
}
