package com.example.expense_tracker.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.ai.TransactionDraftRepository
import com.example.expense_tracker.data.ai.receipt.ReceiptRepository

object ReceiptScanViewModelFactory {
    fun create(repository: ReceiptRepository, draftRepository: TransactionDraftRepository): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReceiptScanViewModel::class.java))
                    return ReceiptScanViewModel(repository, draftRepository) as T
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
}
