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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var reminderRepository: FakeBillReminderRepository
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var walletRepository: FakeWalletRepository

    private class FakeExpenseRepository(val presetCategories: List<Category> = emptyList()) : ExpenseRepository {
        override fun getCategories(): List<Category> = presetCategories
        override fun getCategoriesByType(type: String): List<Category> = presetCategories.filter { it.type == type }
        override fun insertExpense(expense: Expense) {}
        override fun deleteExpense(expense: Expense) {}
        override fun getExpenseById(id: Long): Expense? = null
        override fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense> = emptyList()
        override fun getAllTransactionsBetween(startTime: Long, endTime: Long): List<Expense> = emptyList()
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
        expenseRepository = FakeExpenseRepository()
        walletRepository = FakeWalletRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(reminderId: Long? = null): ReminderFormViewModel {
        val vm = ReminderFormViewModel(reminderRepository, expenseRepository, walletRepository, reminderId, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `initial state is empty when reminderId is null`() {
        val viewModel = createViewModel(null)
        val state = viewModel.uiState.value

        assertEquals("", state.name)
        assertEquals("", state.amountText)
        assertEquals("", state.dueDay)
        assertFalse(state.isSaveEnabled)
    }

    @Test
    fun `form validation enables save button when all fields are valid`() {
        val viewModel = createViewModel(null)
        
        viewModel.onNameChange("Tagihan Internet")
        assertFalse(viewModel.uiState.value.isSaveEnabled)

        viewModel.onAmountChange("300000")
        assertFalse(viewModel.uiState.value.isSaveEnabled)

        viewModel.onDueDayChange("25")
        assertFalse(viewModel.uiState.value.isSaveEnabled)

        viewModel.onCategorySelected(1L)
        assertFalse(viewModel.uiState.value.isSaveEnabled)

        viewModel.onWalletSelected(1L)
        assertTrue(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun `saveReminder inserts new reminder when reminderId is null`() {
        val viewModel = createViewModel(null)
        
        viewModel.onNameChange("Test")
        viewModel.onAmountChange("100")
        viewModel.onDueDayChange("1")
        viewModel.onCategorySelected(1L)
        viewModel.onWalletSelected(1L)
        
        viewModel.saveReminder()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, reminderRepository.getAllReminders().size)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saveReminder updates reminder when reminderId is not null`() {
        val id = reminderRepository.insertReminder(BillReminder(name = "Old", amount = 10, dueDay = 1, categoryId = 1, walletId = 1))

        val viewModel = createViewModel(id)
        
        viewModel.onNameChange("New Name")
        viewModel.saveReminder()
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = reminderRepository.getReminderById(id)
        assertEquals("New Name", updated?.name)
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
