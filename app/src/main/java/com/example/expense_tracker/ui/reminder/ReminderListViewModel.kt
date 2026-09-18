package com.example.expense_tracker.ui.reminder

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.BillReminder
import com.example.expense_tracker.data.BillReminderRepository
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.RoomBillReminderRepository
import com.example.expense_tracker.data.RoomExpenseRepository
import com.example.expense_tracker.data.RoomWalletRepository
import com.example.expense_tracker.data.WalletRepository
import java.time.YearMonth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class ReminderItemUiState(
    val reminder: BillReminder,
    val categoryName: String,
    val walletName: String,
    val isPaidThisMonth: Boolean = false
) {
    val isPaid: Boolean
        get() = (reminder.isRepeat && isPaidThisMonth) || (!reminder.isRepeat && !reminder.isActive)
}

@Immutable
data class ReminderListUiState(
    val activeReminders: List<ReminderItemUiState> = emptyList(),
    val totalAmount: Long = 0L,
    val unpaidCount: Int = 0,
    val paidCount: Int = 0,
    val isLoading: Boolean = true
)

class ReminderListViewModel(
    private val repository: BillReminderRepository,
    private val expenseRepository: ExpenseRepository,
    private val walletRepository: WalletRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderListUiState())
    val uiState: StateFlow<ReminderListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val remindersFlow = repository.getActiveReminders()
            val categoriesFlow = expenseRepository.getCategories()
            val walletsFlow = walletRepository.getAllWallets()

            combine(
                remindersFlow,
                categoriesFlow,
                walletsFlow
            ) { reminders, categoriesList, walletsList ->
                val categories = categoriesList.associateBy { it.id }
                val wallets = walletsList.associateBy { it.id }
                val currentMonth = YearMonth.now().toString()
                
                val items = reminders.map { reminder ->
                    ReminderItemUiState(
                        reminder = reminder,
                        categoryName = categories[reminder.categoryId]?.name ?: "Unknown",
                        walletName = wallets[reminder.walletId]?.name ?: "Unknown",
                        isPaidThisMonth = reminder.lastPaidMonth == currentMonth
                    )
                }.sortedWith(compareBy<ReminderItemUiState> { it.isPaid }.thenBy { it.reminder.dueDay })

                val totalAmount = items.filter { it.reminder.isRepeat || !it.isPaid }.sumOf { it.reminder.amount }
                val unpaidCount = items.count { !it.isPaid }
                val paidCount = items.count { it.isPaid }

                ReminderListUiState(
                    activeReminders = items,
                    totalAmount = totalAmount,
                    unpaidCount = unpaidCount,
                    paidCount = paidCount,
                    isLoading = false
                )
            }
            .flowOn(ioDispatcher)
            .collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun deleteReminder(reminder: BillReminder) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.deleteReminder(reminder)
            }
        }
    }

    fun insertReminder(reminder: BillReminder) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.insertReminder(reminder)
            }
        }
    }

    fun markAsPaid(reminder: BillReminder) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                // 1. Create an Expense
                val expense = Expense(
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
                    walletRepository.updateWallet(wallet.copy(balance = wallet.balance - reminder.amount))
                }

                // 3. Mark reminder as paid for this month, or deactivate if it's one-time
                val currentMonth = YearMonth.now().toString() // e.g., "2026-07"
                if (reminder.isRepeat) {
                    repository.updateReminder(reminder.copy(lastPaidMonth = currentMonth))
                } else {
                    repository.updateReminder(reminder.copy(isActive = false, lastPaidMonth = currentMonth))
                }
            }
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
