package com.example.expense_tracker.ui.home

data class StreakCounterUiState(
    val streak: Int = 0,
    val hasStreak: Boolean = false,
    val encouragementText: String = "Mulai streak kamu hari ini!"
)
