package com.example.expense_tracker.ui.reminder

import com.example.expense_tracker.data.BillReminder
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.FakeBillReminderRepository
import com.example.expense_tracker.data.FakeWalletRepository
import com.example.expense_tracker.data.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var reminderRepository: FakeBillReminderRepository
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var walletRepository: FakeWalletRepository
    private lateinit var viewModel: ReminderListViewModel

    private class FakeExpenseRepository(val presetCategories: List<Category>) : ExpenseRepository {
        val insertedExpenses = mutableListOf<Expense>()
        
        override fun getCategories(): List<Category> = presetCategories
        override fun getCategoriesByType(type: String): List<Category> = presetCategories.filter { it.type == type }
        override fun insertExpense(expense: Expense) {
            insertedExpenses.add(expense)
        }
        override fun deleteExpense(expense: Expense) {}
        override fun getExpenseById(id: Long): Expense? = null
        override fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense> = emptyList()
        override fun getAllTransactionsBetween(startTime: Long, endTime: Long): List<Expense> = emptyList()
        override fun getAllTransactions(): List<Expense> = emptyList()
        override fun getTotalExpense(startTime: Long, endTime: Long): Long = 0
        override fun getTotalIncome(startTime: Long, endTime: Long): Long = 0
        override fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Long = 0
        override fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Long = 0
        override fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): List<Expense> = emptyList()
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        reminderRepository = FakeBillReminderRepository()
        walletRepository = FakeWalletRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadReminders updates uiState with active reminders mapped to category and wallet names`() {
        reminderRepository.insertReminder(
            BillReminder(name = "Test Reminder", amount = 1000, dueDay = 5, categoryId = 1, walletId = 1, isActive = true)
        )
        expenseRepository = FakeExpenseRepository(listOf(Category(1, "Listrik", "EXPENSE")))
        walletRepository.insertWallet(Wallet(1, "BCA", 0))

        viewModel = ReminderListViewModel(reminderRepository, expenseRepository, walletRepository, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.activeReminders.size)
        
        val item = state.activeReminders[0]
        assertEquals("Test Reminder", item.reminder.name)
        assertEquals("Listrik", item.categoryName)
        assertEquals("BCA", item.walletName)
    }

    @Test
    fun `markAsPaid inserts expense and updates lastPaidMonth`() {
        val reminderId = reminderRepository.insertReminder(
            BillReminder(name = "Test Reminder", amount = 1000, dueDay = 5, categoryId = 1, walletId = 1, isActive = true)
        )
        expenseRepository = FakeExpenseRepository(listOf(Category(1, "Listrik", "EXPENSE")))
        walletRepository.insertWallet(Wallet(1, "BCA", 5000))

        viewModel = ReminderListViewModel(reminderRepository, expenseRepository, walletRepository, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        val reminder = reminderRepository.getReminderById(reminderId)!!
        viewModel.markAsPaid(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify expense inserted
        assertEquals(1, expenseRepository.insertedExpenses.size)
        val expense = expenseRepository.insertedExpenses.first()
        assertEquals(1000, expense.amount)
        assertEquals("Test Reminder", expense.description)
        
        // Verify wallet updated
        val wallet = walletRepository.getWalletById(1)!!
        assertEquals(4000, wallet.balance)
        
        // Verify reminder updated
        val updatedReminder = reminderRepository.getReminderById(reminderId)!!
        val currentMonth = java.time.YearMonth.now().toString()
        assertEquals(currentMonth, updatedReminder.lastPaidMonth)
    }
}
