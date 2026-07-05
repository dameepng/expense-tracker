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
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onFilterSelected(filter: FilterPeriod) {
        _uiState.value = _uiState.value.copy(
            filter = filter,
            periodLabel = filter.label
        )
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val currentFilter = _uiState.value.filter
            val (start, end) = TimeRangeCalculator.calculateRange(currentFilter)
            val (total, expenses, categories) = withContext(ioDispatcher) {
                Triple(
                    repository.getTotalExpense(start, end),
                    repository.getExpensesBetween(start, end),
                    repository.getCategories()
                )
            }

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

    fun deleteExpense(expense: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val dbExpense = com.example.expense_tracker.data.Expense(
                    id = expense.id,
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    timestamp = expense.timestamp
                )
                repository.deleteExpense(dbExpense)
            }
            refresh()
        }
    }
}
