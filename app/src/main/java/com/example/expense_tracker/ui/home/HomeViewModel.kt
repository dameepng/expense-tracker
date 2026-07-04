package com.example.expense_tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.FilterPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val todayStart: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    private val oneDay = 86_400_000L

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
            val (start, end) = calculateRange(_uiState.value.filter)
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

    private fun calculateRange(filter: FilterPeriod): Pair<Long, Long> {
        val todayEnd = todayStart + oneDay
        return when (filter) {
            FilterPeriod.TODAY -> Pair(todayStart, todayEnd)

            FilterPeriod.WEEK -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                Pair(cal.timeInMillis, todayEnd)
            }

            FilterPeriod.MONTH -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                Pair(cal.timeInMillis, todayEnd)
            }
        }
    }
}
