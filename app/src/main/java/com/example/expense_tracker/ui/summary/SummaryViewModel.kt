package com.example.expense_tracker.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
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
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState(isLoading = true))
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private data class SummaryFilterParams(
        val filter: FilterPeriod,
        val type: TransactionType,
        val customStartDate: Long?,
        val customEndDate: Long?
    )

    private val filterParamsFlow = MutableStateFlow(
        SummaryFilterParams(
            filter = FilterPeriod.MONTH,
            type = TransactionType.EXPENSE,
            customStartDate = null,
            customEndDate = null
        )
    )

    init {
        viewModelScope.launch {
            filterParamsFlow
                .flatMapLatest { params ->
                    val (start, end) = if (params.filter == FilterPeriod.CUSTOM && params.customStartDate != null && params.customEndDate != null) {
                        Pair(params.customStartDate, params.customEndDate + 86400000L)
                    } else {
                        TimeRangeCalculator.calculateRange(params.filter)
                    }
                    
                    repository.getBreakdownByCategory(start, end, params.type)
                }
                .collect { breakdown ->
                    val total = breakdown.sumOf { it.totalAmount }
                    val items = breakdown.map { item ->
                        BreakdownItem(
                            categoryId = item.categoryId,
                            categoryName = item.categoryName,
                            amount = item.totalAmount,
                            percentage = if (total > 0) item.totalAmount.toFloat() / total.toFloat() else 0f
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        items = items,
                        totalAmount = total,
                        isLoading = false,
                        filter = filterParamsFlow.value.filter,
                        customStartDate = filterParamsFlow.value.customStartDate,
                        customEndDate = filterParamsFlow.value.customEndDate,
                        transactionType = filterParamsFlow.value.type
                    )
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
}
