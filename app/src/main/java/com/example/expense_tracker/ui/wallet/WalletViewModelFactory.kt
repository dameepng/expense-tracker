package com.example.expense_tracker.ui.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomWalletRepository
import com.example.expense_tracker.data.UserPreferencesRepositoryImpl
import com.example.expense_tracker.data.dataStore

class WalletViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            val database = AppDatabase.getInstance(context)
            val repository = RoomWalletRepository(database.walletDao())
            val userPrefs = UserPreferencesRepositoryImpl(context.dataStore)
            @Suppress("UNCHECKED_CAST")
            return WalletViewModel(repository, userPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun create(context: Context) = WalletViewModelFactory(context)
    }
}
