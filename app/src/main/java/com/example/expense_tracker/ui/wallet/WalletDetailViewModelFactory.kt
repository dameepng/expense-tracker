package com.example.expense_tracker.ui.wallet

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomExpenseRepository
import com.example.expense_tracker.data.RoomWalletRepository

class WalletDetailViewModelFactory(
    private val application: Application,
    private val walletId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletDetailViewModel::class.java)) {
            val db = AppDatabase.getInstance(application)
            val walletRepository = RoomWalletRepository(db.walletDao())
            val expenseRepository = RoomExpenseRepository(db.expenseDao())
            return WalletDetailViewModel(walletId, walletRepository, expenseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
