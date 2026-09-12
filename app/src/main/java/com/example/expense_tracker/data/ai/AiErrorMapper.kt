package com.example.expense_tracker.data.ai

import com.google.gson.JsonParseException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException

/** Converts transport failures to stable reasons without exposing response or user data. */
internal object AiErrorMapper {
    fun toException(failure: Exception): AiInputException = when (failure) {
        is CancellationException -> throw failure
        is AiInputException -> failure
        is ClaudeHttpException -> AiInputException(
            when (failure.statusCode) {
                401, 403 -> AiError.AUTHENTICATION
                408, 504 -> AiError.TIMEOUT
                429 -> AiError.RATE_LIMIT
                else -> AiError.SERVER
            }
        )
        is SocketTimeoutException,
        is InterruptedIOException -> AiInputException(AiError.TIMEOUT)
        is IOException -> AiInputException(AiError.NETWORK)
        is JsonParseException,
        is IllegalStateException -> AiInputException(AiError.INVALID_RESPONSE)
        else -> AiInputException(AiError.SERVER)
    }
}
