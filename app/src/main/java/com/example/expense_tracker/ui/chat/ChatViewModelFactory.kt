package com.example.expense_tracker.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expense_tracker.data.ai.AiDependencies
import com.example.expense_tracker.data.ai.chat.ChatContextSource
import com.example.expense_tracker.data.ai.chat.ChatRepository

object ChatViewModelFactory {
    fun create(repository: ChatRepository): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    return ChatViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    internal fun create(
        aiDependencies: AiDependencies,
        contextSource: ChatContextSource
    ): ViewModelProvider.Factory = create(
        aiDependencies.createChatRepository(contextSource)
    )
}
