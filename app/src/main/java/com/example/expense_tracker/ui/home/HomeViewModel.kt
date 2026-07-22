package com.example.expense_tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: ExpenseRepository,
    private val walletRepository: com.example.expense_tracker.data.WalletRepository,
    private val billReminderRepository: com.example.expense_tracker.data.BillReminderRepository,
    private val userPreferencesRepository: com.example.expense_tracker.data.UserPreferencesRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                userPreferencesRepository.selectedWalletIdFlow.collect { savedWalletId ->
                    _uiState.value = _uiState.value.copy(selectedWalletId = savedWalletId)
                    refresh()
                }
            }
            launch {
                userPreferencesRepository.userNameFlow.collect { name ->
                    _uiState.value = _uiState.value.copy(userName = name)
                }
            }
            launch {
                userPreferencesRepository.userPhotoUriFlow.collect { uri ->
                    _uiState.value = _uiState.value.copy(userPhotoUri = uri)
                }
            }
        }
    }

    fun selectWallet(walletId: Long?) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedWalletId(walletId)
        }
    }
    
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val (start, end) = TimeRangeCalculator.calculateRange(FilterPeriod.MONTH)
            val walletId = _uiState.value.selectedWalletId
            
            val totalExpense = withContext(ioDispatcher) { 
                if (walletId != null) repository.getTotalExpenseByWallet(walletId, start, end)
                else repository.getTotalExpense(start, end)
            }
            val totalIncome = withContext(ioDispatcher) { 
                if (walletId != null) repository.getTotalIncomeByWallet(walletId, start, end)
                else repository.getTotalIncome(start, end)
            }
            val transactions = withContext(ioDispatcher) { 
                if (walletId != null) repository.getTransactionsByWallet(walletId, start, end)
                else repository.getAllTransactionsBetween(start, end)
            }
            val categories = withContext(ioDispatcher) { repository.getCategories() }
            val wallets = withContext(ioDispatcher) { walletRepository.getAllWallets() }
            val activeRemindersCount = withContext(ioDispatcher) {
                val currentMonth = java.time.YearMonth.now().toString()
                billReminderRepository.getActiveReminders().count { it.lastPaidMonth != currentMonth }
            }

            val withCategory = transactions.map { expense ->
                val category = categories.find { it.id == expense.categoryId }
                ExpenseWithCategory(
                    id = expense.id,
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    categoryName = category?.name ?: "Lainnya",
                    description = expense.description,
                    timestamp = expense.timestamp,
                    type = expense.type,
                    walletId = expense.walletId
                )
            }

            val selectedWallet = wallets.find { it.id == walletId }
            val finalWalletId = if (walletId != null && selectedWallet == null) null else walletId
            val finalWalletName = selectedWallet?.name ?: "All Wallets"

            _uiState.value = _uiState.value.copy(
                selectedWalletId = finalWalletId,
                selectedWalletName = finalWalletName,
                totalAmount = totalIncome - totalExpense,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                transactions = withCategory,
                wallets = wallets,
                activeRemindersCount = activeRemindersCount,
                isLoading = false
            )
        }
    }

    fun deleteExpense(expense: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val dbExpense = com.example.expense_tracker.data.Expense(
                    id = expense.id,
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    description = expense.description,
                    timestamp = expense.timestamp,
                    type = expense.type,
                    walletId = expense.walletId
                )
                repository.deleteExpense(dbExpense)
            }
            refresh()
        }
    }

    fun undoDeleteExpense(expense: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val dbExpense = com.example.expense_tracker.data.Expense(
                    id = expense.id,
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    description = expense.description,
                    timestamp = expense.timestamp,
                    type = expense.type,
                    walletId = expense.walletId
                )
                repository.insertExpense(dbExpense)
            }
            refresh()
        }
    }
}
