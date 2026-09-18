package com.example.expense_tracker.ui.home

import androidx.compose.runtime.Immutable

const val DEFAULT_STREAK_ENCOURAGEMENT = "Mulai streak kamu hari ini!"

@Immutable
data class StreakCounterUiState(
    val streak: Int = 0,
    val hasStreak: Boolean = false,
    val encouragementText: String = DEFAULT_STREAK_ENCOURAGEMENT
)
