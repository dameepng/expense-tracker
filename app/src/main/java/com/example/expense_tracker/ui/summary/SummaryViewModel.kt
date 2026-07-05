package com.example.expense_tracker.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryViewModel(
    private val repository: SummaryRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onFilterSelected(filter: FilterPeriod) {
        _uiState.value = _uiState.value.copy(filter = filter)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val currentFilter = _uiState.value.filter
            val (start, end) = TimeRangeCalculator.calculateRange(currentFilter)
            val breakdown = withContext(ioDispatcher) {
                repository.getBreakdownByCategory(start, end)
            }

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
                totalAmount = total
            )
        }
    }
}
