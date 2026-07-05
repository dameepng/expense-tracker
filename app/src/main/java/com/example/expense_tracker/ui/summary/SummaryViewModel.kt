package com.example.expense_tracker.ui.summary

import androidx.lifecycle.ViewModel
import com.example.expense_tracker.data.FilterPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class SummaryViewModel(
    private val repository: SummaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private val oneDay = 86_400_000L

    init {
        loadData()
    }

    fun onFilterSelected(filter: FilterPeriod) {
        _uiState.value = _uiState.value.copy(filter = filter)
        loadData()
    }

    private fun loadData() {
        val (start, end) = calculateRange(_uiState.value.filter)
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

    private fun calculateRange(filter: FilterPeriod): Pair<Long, Long> {
        val todayEnd = todayStart() + oneDay
        return when (filter) {
            FilterPeriod.TODAY -> Pair(todayStart(), todayEnd)
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

    private fun todayStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
