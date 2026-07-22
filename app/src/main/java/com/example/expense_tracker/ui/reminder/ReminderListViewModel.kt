package com.example.expense_tracker.ui.reminder

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.BillReminder
import com.example.expense_tracker.data.BillReminderRepository
import com.example.expense_tracker.data.RoomBillReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.RoomExpenseRepository
import com.example.expense_tracker.data.RoomWalletRepository
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.WalletRepository

data class ReminderItemUiState(
    val reminder: BillReminder,
    val categoryName: String,
    val walletName: String
)

data class ReminderListUiState(
    val activeReminders: List<ReminderItemUiState> = emptyList(),
    val isLoading: Boolean = true
)

class ReminderListViewModel(
    private val repository: BillReminderRepository,
    private val expenseRepository: ExpenseRepository,
    private val walletRepository: WalletRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderListUiState())
    val uiState: StateFlow<ReminderListUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val reminders = withContext(ioDispatcher) {
                repository.getActiveReminders()
            }
            val categories = withContext(ioDispatcher) {
                expenseRepository.getCategories().first().associateBy { it.id }
            }
            val wallets = withContext(ioDispatcher) {
                walletRepository.getAllWallets().first().associateBy { it.id }
            }
            
            val items = reminders.first().map { reminder ->
                ReminderItemUiState(
                    reminder = reminder,
                    categoryName = categories[reminder.categoryId]?.name ?: "Unknown",
                    walletName = wallets[reminder.walletId]?.name ?: "Unknown"
                )
            }
            
            _uiState.value = _uiState.value.copy(
                activeReminders = items,
                isLoading = false
            )
        }
    }

    fun deleteReminder(reminder: BillReminder) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.deleteReminder(reminder)
            }
            loadReminders()
        }
    }

    fun markAsPaid(reminder: BillReminder) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                // 1. Create an Expense
                val expense = com.example.expense_tracker.data.Expense(
                    amount = reminder.amount,
                    categoryId = reminder.categoryId,
                    walletId = reminder.walletId,
                    description = reminder.name,
                    timestamp = System.currentTimeMillis(),
                    type = "EXPENSE"
                )
                expenseRepository.insertExpense(expense)
                
                // 2. Update wallet balance
                val wallet = walletRepository.getWalletById(reminder.walletId)
                if (wallet != null) {
                    walletRepository.insertWallet(wallet.copy(balance = wallet.balance - reminder.amount))
                }

                // 3. Mark reminder as paid for this month
                val currentMonth = java.time.YearMonth.now().toString() // e.g., "2026-07"
                repository.updateReminder(reminder.copy(lastPaidMonth = currentMonth))
            }
            loadReminders()
        }
    }
}

class ReminderListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderListViewModel::class.java)) {
            val database = AppDatabase.getInstance(application)
            val repository = RoomBillReminderRepository(database.billReminderDao())
            val expenseRepository = RoomExpenseRepository(database.expenseDao())
            val walletRepository = RoomWalletRepository(database.walletDao())
            @Suppress("UNCHECKED_CAST")
            return ReminderListViewModel(repository, expenseRepository, walletRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
