package com.example.expense_tracker.ui.ai

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.ai.AiDependencies
import com.example.expense_tracker.data.ai.RoomTransactionDraftRepository

object NaturalLanguageViewModelFactory {
    fun create(application: Application): ViewModelProvider.Factory =
        create(application, AiDependencies.shared)

    internal fun create(
        application: Application,
        aiDependencies: AiDependencies
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NaturalLanguageViewModel::class.java)) {
                return NaturalLanguageViewModel(
                    aiRepository = aiDependencies.createNaturalLanguageRepository(),
                    draftRepository = RoomTransactionDraftRepository(AppDatabase.getInstance(application))
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
