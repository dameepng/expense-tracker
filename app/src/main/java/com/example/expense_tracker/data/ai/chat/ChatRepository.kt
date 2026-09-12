package com.example.expense_tracker.data.ai.chat

interface ChatRepository {
    suspend fun send(request: ChatRequest): ChatResult
}

enum class ChatError {
    MISSING_API_KEY,
    INVALID_INPUT,
    DATA_UNAVAILABLE,
    INVALID_PREFERENCES,
    INVALID_CONTEXT,
    CONTEXT_TOO_LARGE,
    NETWORK,
    TIMEOUT,
    RATE_LIMIT,
    AUTHENTICATION,
    SERVER,
    EMPTY_RESPONSE,
    INVALID_RESPONSE,
    UNSUPPORTED_RESPONSE,
    TRUNCATED_RESPONSE
}

/** Exposes only a stable reason; transport and response details stay below the UI boundary. */
class ChatException(val reason: ChatError) : Exception(reason.name)
