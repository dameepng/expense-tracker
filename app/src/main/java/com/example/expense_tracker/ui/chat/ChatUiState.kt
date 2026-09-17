package com.example.expense_tracker.ui.chat

import androidx.compose.runtime.Immutable
import com.example.expense_tracker.data.ai.chat.ChatMessage

enum class ChatUiError {
    CONFIGURATION,
    NETWORK,
    TIMEOUT,
    RATE_LIMIT,
    AUTHENTICATION,
    SERVICE,
    INVALID_RESPONSE,
    DATA_LOADING,
    CONTEXT_LIMIT,
    INVALID_INPUT,
    INPUT_LIMIT
}

@Immutable
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: ChatUiError? = null,
    val pendingMessage: ChatMessage? = null,
    val failedMessage: ChatMessage? = null,
    val isHistoryTruncated: Boolean = false
) {
    val canSend: Boolean
        get() = !isLoading && failedMessage == null &&
            inputText.trim().let { it.isNotEmpty() && it.length <= MAX_INPUT_CHARACTERS }

    val canRetry: Boolean
        get() = !isLoading && failedMessage != null

    private companion object {
        const val MAX_INPUT_CHARACTERS = 1_000
    }
}
