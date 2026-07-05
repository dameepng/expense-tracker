package com.example.expense_tracker.ui.summary

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomSummaryRepository

object SummaryViewModelFactory {
    fun create(application: Application): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
                    val db = AppDatabase.getInstance(application)
                    val repository = RoomSummaryRepository(db.expenseDao())
                    return SummaryViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
