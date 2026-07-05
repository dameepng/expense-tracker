package com.example.expense_tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StreakCounterViewModel(
    private val repository: StreakRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreakCounterUiState())
    val uiState: StateFlow<StreakCounterUiState> = _uiState.asStateFlow()

    init {
        loadStreak()
    }

    fun loadStreak() {
        viewModelScope.launch {
            val streak = withContext(ioDispatcher) {
                repository.calculateStreak()
            }
            _uiState.value = StreakCounterUiState(
                streak = streak,
                hasStreak = streak > 0,
                encouragementText = if (streak > 0) {
                    "$streak hari berturut-turut!"
                } else {
                    "Mulai streak kamu hari ini!"
                }
            )
        }
    }
}

interface StreakRepository {
    fun calculateStreak(): Int
}
