package com.example.expense_tracker.data.ai.chat

enum class ChatRole {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val timestampEpochMillis: Long
)

data class ChatRequest(
    val question: String,
    val history: List<ChatMessage> = emptyList()
)

data class ChatResult(
    val assistantMessage: ChatMessage
)
