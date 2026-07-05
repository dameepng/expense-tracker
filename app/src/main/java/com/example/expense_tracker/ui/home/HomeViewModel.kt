package com.example.expense_tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onFilterSelected(filter: FilterPeriod) {
        _uiState.value = _uiState.value.copy(
            filter = filter,
            periodLabel = filter.label
        )
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val (start, end) = TimeRangeCalculator.calculateRange(_uiState.value.filter)
            val total = repository.getTotalExpense(start, end)
            val expenses = repository.getExpensesBetween(start, end)
            val categories = repository.getCategories()

            val withCategory = expenses.map { expense ->
                val category = categories.find { it.id == expense.categoryId }
                ExpenseWithCategory(
                    id = expense.id,
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    categoryName = category?.name ?: "Lainnya",
                    timestamp = expense.timestamp
                )
            }

            _uiState.value = _uiState.value.copy(
                totalAmount = total,
                expenses = withCategory,
                isLoading = false
            )
        }
    }
}
