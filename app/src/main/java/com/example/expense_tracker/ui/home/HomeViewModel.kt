package com.example.expense_tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.BillReminderRepository
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.WalletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

private const val RECENT_TRANSACTIONS_LIMIT = 5
private const val DEFAULT_CATEGORY_FALLBACK = "Lainnya"

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: ExpenseRepository,
    private val walletRepository: WalletRepository,
    private val billReminderRepository: BillReminderRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val timeRangeFlow = MutableStateFlow(TimeRangeCalculator.calculateRange(FilterPeriod.MONTH))

    init {
        viewModelScope.launch {
            val walletIdFlow = userPreferencesRepository.selectedWalletIdFlow
            val nameFlow = userPreferencesRepository.userNameFlow
            val photoFlow = userPreferencesRepository.userPhotoUriFlow

            val queryFilterFlow = combine(walletIdFlow, timeRangeFlow) { walletId, timeRange ->
                Pair(walletId, timeRange)
            }

            val transactionsFlow = queryFilterFlow.flatMapLatest { (walletId, timeRange) ->
                val (start, end) = timeRange
                if (walletId != null) repository.getRecentTransactionsByWallet(walletId, start, end, RECENT_TRANSACTIONS_LIMIT)
                else repository.getRecentTransactionsBetween(start, end, RECENT_TRANSACTIONS_LIMIT)
            }

            val totalExpenseFlow = queryFilterFlow.flatMapLatest { (walletId, timeRange) ->
                val (start, end) = timeRange
                if (walletId != null) repository.getTotalExpenseByWallet(walletId, start, end)
                else repository.getTotalExpense(start, end)
            }

            val totalIncomeFlow = queryFilterFlow.flatMapLatest { (walletId, timeRange) ->
                val (start, end) = timeRange
                if (walletId != null) repository.getTotalIncomeByWallet(walletId, start, end)
                else repository.getTotalIncome(start, end)
            }

            val categoriesFlow = repository.getCategories()
            val walletsFlow = walletRepository.getAllWallets()
            
            val activeRemindersCountFlow = billReminderRepository.getActiveReminders().map { reminders ->
                val currentMonth = YearMonth.now().toString()
                reminders.count { it.isActive && it.lastPaidMonth != currentMonth }
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
                @Suppress("UNCHECKED_CAST")
                val transactions = args[3] as List<Expense>
                val totalExpense = args[4] as Long
                val totalIncome = args[5] as Long
                @Suppress("UNCHECKED_CAST")
                val categories = args[6] as List<Category>
                @Suppress("UNCHECKED_CAST")
                val wallets = args[7] as List<Wallet>
                val activeRemindersCount = args[8] as Int

                val withCategory = transactions.map { expense ->
                    val category = categories.find { it.id == expense.categoryId }
                    ExpenseWithCategory(
                        id = expense.id,
                        amount = expense.amount,
                        categoryId = expense.categoryId,
                        categoryName = category?.name ?: DEFAULT_CATEGORY_FALLBACK,
                        description = expense.description,
                        timestamp = expense.timestamp,
                        type = expense.type,
                        walletId = expense.walletId,
                        merchant = expense.merchant,
                        isRecurring = expense.isRecurring
                    )
                }

                val selectedWallet = wallets.find { it.id == walletId }
                val finalWalletId = if (walletId != null && selectedWallet == null) null else walletId
                val finalWalletName = selectedWallet?.name ?: DEFAULT_WALLET_NAME

                HomeUiState(
                    selectedWalletId = finalWalletId,
                    selectedWalletName = finalWalletName,
                    totalAmount = totalIncome - totalExpense,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    transactions = withCategory.take(RECENT_TRANSACTIONS_LIMIT),
                    wallets = wallets,
                    activeRemindersCount = activeRemindersCount,
                    userName = userName,
                    userPhotoUri = userPhotoUri,
                    isLoading = false
                )
            }
            .flowOn(ioDispatcher)
            .collect { newState ->
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
                repository.deleteExpense(expense.toEntity())
            }
        }
    }

    fun undoDeleteExpense(expense: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.insertExpense(expense.toEntity())
            }
        }
    }

    private fun ExpenseWithCategory.toEntity(): Expense = Expense(
        id = id,
        amount = amount,
        categoryId = categoryId,
        description = description,
        timestamp = timestamp,
        type = type,
        walletId = walletId,
        merchant = merchant,
        isRecurring = isRecurring
    )
}
