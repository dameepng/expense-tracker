package com.example.expense_tracker.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomStreakRepository

object StreakViewModelFactory {
    fun create(application: Application): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StreakCounterViewModel::class.java)) {
                    val db = AppDatabase.getInstance(application)
                    val repository = RoomStreakRepository(db.expenseDao())
                    return StreakCounterViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
