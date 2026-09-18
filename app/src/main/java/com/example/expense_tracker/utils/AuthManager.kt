package com.example.expense_tracker.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    const val DEFAULT_TIMEOUT_MILLIS = 5 * 60 * 1000L

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    var lastBackgroundTime = 0L
    
    fun lock() {
        _isAuthenticated.value = false
    }
    
    fun unlock() {
        _isAuthenticated.value = true
    }

    fun recordBackgroundTime(timestamp: Long = System.currentTimeMillis()) {
        lastBackgroundTime = timestamp
    }

    fun isSessionTimedOut(timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS): Boolean {
        if (lastBackgroundTime <= 0L) return false
        val timeElapsed = System.currentTimeMillis() - lastBackgroundTime
        return timeElapsed > timeoutMillis
    }
}
