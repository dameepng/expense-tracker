package com.example.expense_tracker.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
import com.example.expense_tracker.data.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.expense_tracker.data.TransactionType

class SummaryViewModel(
    private val repository: SummaryRepository,
    private val walletRepository: WalletRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState(isLoading = true))
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private data class SummaryFilterParams(
        val filter: FilterPeriod,
        val type: TransactionType,
        val customStartDate: Long?,
        val customEndDate: Long?,
        val walletId: Long? // null = all wallets
    )

    private val filterParamsFlow = MutableStateFlow(
        SummaryFilterParams(
            filter = FilterPeriod.MONTH,
            type = TransactionType.EXPENSE,
            customStartDate = null,
            customEndDate = null,
            walletId = null
        )
    )

    init {
        // Load wallets
        viewModelScope.launch {
            walletRepository.getAllWallets().collect { wallets ->
                _uiState.value = _uiState.value.copy(wallets = wallets)
            }
        }

        // React to filter changes
        viewModelScope.launch {
            filterParamsFlow
                .flatMapLatest { params ->
                    val (start, end) = if (params.filter == FilterPeriod.CUSTOM && params.customStartDate != null && params.customEndDate != null) {
                        Pair(params.customStartDate, params.customEndDate + 86400000L)
                    } else {
                        TimeRangeCalculator.calculateRange(params.filter)
                    }
                    
                    kotlinx.coroutines.flow.combine(
                        repository.getTotalBalance(params.walletId),
                        repository.getBreakdownByCategory(start, end, params.type, params.walletId),
                        repository.getTotalIncome(start, end, params.walletId),
                        repository.getTotalExpense(start, end, params.walletId),
                        repository.getTransactionsBetween(start, end, params.walletId)
                    ) { balance, breakdown, income, expense, transactions ->
                        // Calculate breakdown percentages
                        val totalAmountType = breakdown.sumOf { it.totalAmount }
                        val items = breakdown.map { item ->
                            BreakdownItem(
                                categoryId = item.categoryId,
                                categoryName = item.categoryName,
                                amount = item.totalAmount,
                                percentage = if (totalAmountType > 0) item.totalAmount.toFloat() / totalAmountType.toFloat() else 0f
                            )
                        }

                        // Calculate Net Cash Flow
                        val netCashFlow = income - expense

                        // Calculate Percentage Change (approximate vs before this period)
                        val prevBalance = balance - netCashFlow
                        val percentageChange = if (prevBalance > 0) {
                            (netCashFlow.toFloat() / prevBalance.toFloat()) * 100f
                        } else {
                            0f
                        }

                        // Aggregate Daily Cash Flow
                        val calendar = java.util.Calendar.getInstance()
                        val dailyMap = mutableMapOf<Long, Pair<Long, Long>>() // Map of day start millis -> (Income, Expense)
                        
                        transactions.forEach { tx ->
                            calendar.timeInMillis = tx.timestamp
                            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            calendar.set(java.util.Calendar.MINUTE, 0)
                            calendar.set(java.util.Calendar.SECOND, 0)
                            calendar.set(java.util.Calendar.MILLISECOND, 0)
                            val dayMillis = calendar.timeInMillis
                            
                            val current = dailyMap[dayMillis] ?: Pair(0L, 0L)
                            if (tx.type == "INCOME") {
                                dailyMap[dayMillis] = current.copy(first = current.first + tx.amount)
                            } else {
                                dailyMap[dayMillis] = current.copy(second = current.second + tx.amount)
                            }
                        }
                        
                        val dailyCashFlow = dailyMap.map { (date, amounts) ->
                            DailyCashFlow(dateMillis = date, income = amounts.first, expense = amounts.second)
                        }.sortedBy { it.dateMillis }

                        SummaryUiState(
                            items = items,
                            totalAmount = totalAmountType, // Still used for Donut chart total of current type
                            totalBalance = balance,
                            totalIncome = income,
                            totalExpense = expense,
                            netCashFlow = netCashFlow,
                            balancePercentageChange = percentageChange,
                            dailyCashFlow = dailyCashFlow,
                            isLoading = false,
                            filter = params.filter,
                            customStartDate = params.customStartDate,
                            customEndDate = params.customEndDate,
                            transactionType = params.type,
                            selectedWalletId = params.walletId,
                            wallets = _uiState.value.wallets
                        )
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onFilterSelected(filter: FilterPeriod, customStartDate: Long? = null, customEndDate: Long? = null) {
        filterParamsFlow.value = filterParamsFlow.value.copy(
            filter = filter,
            customStartDate = customStartDate ?: filterParamsFlow.value.customStartDate,
            customEndDate = customEndDate ?: filterParamsFlow.value.customEndDate
        )
        _uiState.value = _uiState.value.copy(isLoading = true)
    }

    fun onTransactionTypeSelected(type: TransactionType) {
        filterParamsFlow.value = filterParamsFlow.value.copy(type = type)
        _uiState.value = _uiState.value.copy(isLoading = true)
    }

    fun onWalletSelected(walletId: Long?) {
        filterParamsFlow.value = filterParamsFlow.value.copy(walletId = walletId)
        _uiState.value = _uiState.value.copy(isLoading = true)
    }
}
