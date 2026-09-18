package com.example.expense_tracker.ui.input

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomBillReminderRepository
import com.example.expense_tracker.data.RoomInputRepository
import com.example.expense_tracker.data.RoomWalletRepository

object InputViewModelFactory {
    fun create(
        application: Application,
        expenseId: Long? = null,
        initialDescription: String? = null,
        initialAmount: String? = null
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(InputViewModel::class.java)) {
                    val db = AppDatabase.getInstance(application)
                    val repository = RoomInputRepository(db.expenseDao())
                    val walletRepository = RoomWalletRepository(db.walletDao())
                    val billReminderRepository = RoomBillReminderRepository(db.billReminderDao())
                    return InputViewModel(
                        repository,
                        walletRepository,
                        billReminderRepository,
                        expenseId = expenseId,
                        initialDescription = initialDescription,
                        initialAmount = initialAmount
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
