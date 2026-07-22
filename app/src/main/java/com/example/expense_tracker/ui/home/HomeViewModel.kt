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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: ExpenseRepository,
    private val walletRepository: com.example.expense_tracker.data.WalletRepository,
    private val billReminderRepository: com.example.expense_tracker.data.BillReminderRepository,
    private val userPreferencesRepository: com.example.expense_tracker.data.UserPreferencesRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val timeRangeFlow = MutableStateFlow(TimeRangeCalculator.calculateRange(FilterPeriod.MONTH))

    init {
        viewModelScope.launch {
            val walletIdFlow = userPreferencesRepository.selectedWalletIdFlow
            val nameFlow = userPreferencesRepository.userNameFlow
            val photoFlow = userPreferencesRepository.userPhotoUriFlow

            val transactionsFlow = combine(walletIdFlow, timeRangeFlow) { walletId, timeRange ->
                Pair(walletId, timeRange)
            }.flatMapLatest { (walletId, timeRange) ->
                val (start, end) = timeRange
                if (walletId != null) repository.getTransactionsByWallet(walletId, start, end)
                else repository.getAllTransactionsBetween(start, end)
            }

            val totalExpenseFlow = combine(walletIdFlow, timeRangeFlow) { walletId, timeRange ->
                Pair(walletId, timeRange)
            }.flatMapLatest { (walletId, timeRange) ->
                val (start, end) = timeRange
                if (walletId != null) repository.getTotalExpenseByWallet(walletId, start, end)
                else repository.getTotalExpense(start, end)
            }

            val totalIncomeFlow = combine(walletIdFlow, timeRangeFlow) { walletId, timeRange ->
                Pair(walletId, timeRange)
            }.flatMapLatest { (walletId, timeRange) ->
                val (start, end) = timeRange
                if (walletId != null) repository.getTotalIncomeByWallet(walletId, start, end)
                else repository.getTotalIncome(start, end)
            }

            val categoriesFlow = repository.getCategories()
            val walletsFlow = walletRepository.getAllWallets()
            
            val activeRemindersCountFlow = billReminderRepository.getActiveReminders().map { reminders ->
                val currentMonth = java.time.YearMonth.now().toString()
                reminders.count { it.lastPaidMonth != currentMonth }
            }

            combine(
                walletIdFlow,
                nameFlow,
                photoFlow,
                transactionsFlow,
                totalExpenseFlow,
                totalIncomeFlow,
                categoriesFlow,
                walletsFlow,
                activeRemindersCountFlow
            ) { args ->
                val walletId = args[0] as Long?
                val userName = args[1] as String
                val userPhotoUri = args[2] as String?
                val transactions = args[3] as List<com.example.expense_tracker.data.Expense>
                val totalExpense = args[4] as Long
                val totalIncome = args[5] as Long
                val categories = args[6] as List<com.example.expense_tracker.data.Category>
                val wallets = args[7] as List<com.example.expense_tracker.data.Wallet>
                val activeRemindersCount = args[8] as Int

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

                HomeUiState(
                    selectedWalletId = finalWalletId,
                    selectedWalletName = finalWalletName,
                    totalAmount = totalIncome - totalExpense,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    transactions = withCategory,
                    wallets = wallets,
                    activeRemindersCount = activeRemindersCount,
                    userName = userName,
                    userPhotoUri = userPhotoUri,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectWallet(walletId: Long?) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedWalletId(walletId)
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
        }
    }
}
