package com.example.expense_tracker.ui.reminder

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.BillReminder
import com.example.expense_tracker.data.BillReminderRepository
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.RoomBillReminderRepository
import com.example.expense_tracker.data.RoomExpenseRepository
import com.example.expense_tracker.data.RoomWalletRepository
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReminderFormUiState(
    val name: String = "",
    val amountText: String = "",
    val dueDay: String = "",
    val selectedCategoryId: Long? = null,
    val selectedWalletId: Long? = null,
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val isSaveEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true
)

class ReminderFormViewModel(
    private val repository: BillReminderRepository,
    private val expenseRepository: ExpenseRepository,
    private val walletRepository: WalletRepository,
    private val reminderId: Long?,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderFormUiState())
    val uiState: StateFlow<ReminderFormUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val categories = withContext(ioDispatcher) {
                // Bill reminders are usually EXPENSE
                expenseRepository.getCategoriesByType("EXPENSE")
            }
            val wallets = withContext(ioDispatcher) {
                walletRepository.getAllWallets()
            }
            
            var loadedName = ""
            var loadedAmount = ""
            var loadedDueDay = ""
            var loadedCategoryId: Long? = null
            var loadedWalletId: Long? = if (wallets.size == 1) wallets.first().id else null
            
            if (reminderId != null) {
                val reminder = withContext(ioDispatcher) {
                    repository.getReminderById(reminderId)
                }
                if (reminder != null) {
                    loadedName = reminder.name
                    loadedAmount = reminder.amount.toString()
                    loadedDueDay = reminder.dueDay.toString()
                    loadedCategoryId = reminder.categoryId
                    loadedWalletId = reminder.walletId
                }
            }
            
            _uiState.value = _uiState.value.copy(
                name = loadedName,
                amountText = loadedAmount,
                dueDay = loadedDueDay,
                selectedCategoryId = loadedCategoryId,
                selectedWalletId = loadedWalletId,
                categories = categories,
                wallets = wallets,
                isLoading = false
            )
            validateForm()
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        validateForm()
    }

    fun onAmountChange(amount: String) {
        if (amount.isEmpty() || amount.toLongOrNull() != null) {
            _uiState.value = _uiState.value.copy(amountText = amount)
            validateForm()
        }
    }

    fun onDueDayChange(day: String) {
        if (day.isEmpty() || day.toIntOrNull() != null) {
            _uiState.value = _uiState.value.copy(dueDay = day)
            validateForm()
        }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        validateForm()
    }

    fun onWalletSelected(walletId: Long) {
        _uiState.value = _uiState.value.copy(selectedWalletId = walletId)
        validateForm()
    }

    private fun validateForm() {
        val state = _uiState.value
        val amountValid = state.amountText.toLongOrNull()?.let { it > 0 } ?: false
        val dueDayValid = state.dueDay.toIntOrNull()?.let { it in 1..31 } ?: false
        val isValid = state.name.isNotBlank() && 
                      amountValid && 
                      dueDayValid && 
                      state.selectedCategoryId != null && 
                      state.selectedWalletId != null
                      
        _uiState.value = state.copy(isSaveEnabled = isValid)
    }

    fun saveReminder() {
        if (!_uiState.value.isSaveEnabled) return
        
        viewModelScope.launch {
            val state = _uiState.value
            val reminder = BillReminder(
                id = reminderId ?: 0L,
                name = state.name.trim(),
                amount = state.amountText.toLong(),
                dueDay = state.dueDay.toInt(),
                categoryId = state.selectedCategoryId!!,
                walletId = state.selectedWalletId!!,
                isActive = true
            )
            
            withContext(ioDispatcher) {
                if (reminderId == null) {
                    repository.insertReminder(reminder)
                } else {
                    repository.updateReminder(reminder)
                }
            }
            
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}

class ReminderFormViewModelFactory(
    private val application: Application,
    private val reminderId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderFormViewModel::class.java)) {
            val database = AppDatabase.getInstance(application)
            val repository = RoomBillReminderRepository(database.billReminderDao())
            val expenseRepository = RoomExpenseRepository(database.expenseDao())
            val walletRepository = RoomWalletRepository(database.walletDao())
            @Suppress("UNCHECKED_CAST")
            return ReminderFormViewModel(repository, expenseRepository, walletRepository, reminderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
