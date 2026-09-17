package com.example.expense_tracker.data.ai.chat

import androidx.compose.runtime.Immutable

enum class ChatRole {
    USER,
    ASSISTANT
}

@Immutable
data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val timestampEpochMillis: Long
)

@Immutable
data class ChatRequest(
    val question: String,
    val history: List<ChatMessage> = emptyList()
)

@Immutable
data class ChatResult(
    val assistantMessage: ChatMessage
)
