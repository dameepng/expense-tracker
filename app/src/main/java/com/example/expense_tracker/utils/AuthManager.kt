package com.example.expense_tracker.utils

import kotlinx.coroutines.flow.MutableStateFlow

object AuthManager {
    val isAuthenticated = MutableStateFlow(false)
    var lastBackgroundTime = 0L
    
    fun lock() {
        isAuthenticated.value = false
    }
    
    fun unlock() {
        isAuthenticated.value = true
    }
}
