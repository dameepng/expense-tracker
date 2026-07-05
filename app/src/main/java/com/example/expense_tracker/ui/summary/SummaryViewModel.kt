package com.example.expense_tracker.ui.summary

import androidx.lifecycle.ViewModel
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SummaryViewModel(
    private val repository: SummaryRepository
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
        val (start, end) = TimeRangeCalculator.calculateRange(_uiState.value.filter)
        val breakdown = repository.getBreakdownByCategory(start, end)

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
