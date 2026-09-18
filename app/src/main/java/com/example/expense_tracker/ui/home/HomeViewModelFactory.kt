package com.example.expense_tracker.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomBillReminderRepository
import com.example.expense_tracker.data.RoomExpenseRepository
import com.example.expense_tracker.data.RoomWalletRepository
import com.example.expense_tracker.data.UserPreferencesRepositoryImpl
import com.example.expense_tracker.data.dataStore

class HomeViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // This requires Application context at runtime — ViewModelProvider.AndroidViewModelFactory handles this
            throw IllegalStateException(
                "Use HomeViewModelFactory(application) instead. " +
                "HomeViewModel requires Application context for Room database."
            )
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    companion object {
        fun create(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        val db = AppDatabase.getInstance(application)
                        val repository = RoomExpenseRepository(db.expenseDao())
                        val walletRepository = RoomWalletRepository(db.walletDao())
                        val billReminderRepository = RoomBillReminderRepository(db.billReminderDao())
                        val userPreferencesRepository = UserPreferencesRepositoryImpl(application.dataStore)
                        return HomeViewModel(repository, walletRepository, billReminderRepository, userPreferencesRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
