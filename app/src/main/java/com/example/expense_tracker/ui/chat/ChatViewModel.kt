package com.example.expense_tracker.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.ai.chat.ChatError
import com.example.expense_tracker.data.ai.chat.ChatException
import com.example.expense_tracker.data.ai.chat.ChatHistoryPolicy
import com.example.expense_tracker.data.ai.chat.ChatMessage
import com.example.expense_tracker.data.ai.chat.ChatRepository
import com.example.expense_tracker.data.ai.chat.ChatRequest
import com.example.expense_tracker.data.ai.chat.ChatRole
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel internal constructor(
    private val repository: ChatRepository,
    private val historyPolicy: ChatHistoryPolicy = ChatHistoryPolicy(),
    private val clock: Clock = Clock.systemUTC(),
    private val idProvider: () -> String = { UUID.randomUUID().toString() }
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null
    private var requestGeneration = 0L

    fun onInputChange(text: String) {
        _uiState.update { state ->
            state.copy(
                inputText = text,
                error = state.error.takeUnless {
                    state.failedMessage == null &&
                        (it == ChatUiError.INVALID_INPUT || it == ChatUiError.INPUT_LIMIT)
                }
            )
        }
    }

    fun send() {
        val state = _uiState.value
        if (state.isLoading || state.failedMessage != null) return

        val question = state.inputText.trim()
        when {
            question.isEmpty() -> {
                _uiState.update { it.copy(error = ChatUiError.INVALID_INPUT) }
                return
            }
            question.length > MAX_INPUT_CHARACTERS -> {
                _uiState.update { it.copy(error = ChatUiError.INPUT_LIMIT) }
                return
            }
        }

        val userMessage = ChatMessage(
            id = idProvider(),
            role = ChatRole.USER,
            content = question,
            timestampEpochMillis = clock.millis()
        )
        val history = state.messages
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true,
                error = null,
                pendingMessage = userMessage,
                failedMessage = null
            )
        }
        launchRequest(userMessage, history)
    }

    fun retryFailedMessage() {
        val state = _uiState.value
        val failedMessage = state.failedMessage ?: return
        if (state.isLoading) return

        val history = state.messages.filterNot { it.id == failedMessage.id }
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                pendingMessage = failedMessage,
                failedMessage = null
            )
        }
        launchRequest(failedMessage, history)
    }

    fun cancelRequest() {
        val pendingMessage = _uiState.value.pendingMessage ?: return
        invalidateActiveRequest()
        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                pendingMessage = null,
                failedMessage = pendingMessage
            )
        }
    }

    fun discardFailedMessage() {
        val state = _uiState.value
        val failedMessage = state.failedMessage ?: return
        if (state.isLoading) return

        _uiState.update {
            it.copy(
                messages = it.messages.filterNot { message -> message.id == failedMessage.id },
                error = null,
                failedMessage = null
            )
        }
    }

    fun resetSession() {
        invalidateActiveRequest()
        _uiState.value = ChatUiState()
    }

    private fun launchRequest(userMessage: ChatMessage, history: List<ChatMessage>) {
        val generation = ++requestGeneration
        requestJob = viewModelScope.launch {
            try {
                val result = repository.send(
                    ChatRequest(question = userMessage.content, history = history)
                )
                if (generation != requestGeneration) return@launch

                _uiState.update { state ->
                    val completedMessages = state.messages + result.assistantMessage
                    val retainedMessages = historyPolicy.select(completedMessages)
                    state.copy(
                        messages = retainedMessages,
                        isLoading = false,
                        error = null,
                        pendingMessage = null,
                        failedMessage = null,
                        isHistoryTruncated = state.isHistoryTruncated ||
                            retainedMessages.size < completedMessages.size
                    )
                }
            } catch (cancellation: CancellationException) {
                if (generation == requestGeneration) {
                    markRequestFailed(userMessage, error = null)
                }
                throw cancellation
            } catch (failure: ChatException) {
                if (generation == requestGeneration) {
                    markRequestFailed(userMessage, failure.reason.toUiError())
                }
            } catch (_: Exception) {
                if (generation == requestGeneration) {
                    markRequestFailed(userMessage, ChatUiError.SERVICE)
                }
            } finally {
                if (generation == requestGeneration) requestJob = null
            }
        }
    }

    private fun markRequestFailed(userMessage: ChatMessage, error: ChatUiError?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = error,
                pendingMessage = null,
                failedMessage = userMessage
            )
        }
    }

    private fun invalidateActiveRequest() {
        requestGeneration += 1
        requestJob?.cancel()
        requestJob = null
    }

    private fun ChatError.toUiError(): ChatUiError = when (this) {
        ChatError.MISSING_API_KEY -> ChatUiError.CONFIGURATION
        ChatError.INVALID_INPUT -> ChatUiError.INVALID_INPUT
        ChatError.DATA_UNAVAILABLE,
        ChatError.INVALID_PREFERENCES,
        ChatError.INVALID_CONTEXT -> ChatUiError.DATA_LOADING
        ChatError.CONTEXT_TOO_LARGE -> ChatUiError.CONTEXT_LIMIT
        ChatError.NETWORK -> ChatUiError.NETWORK
        ChatError.TIMEOUT -> ChatUiError.TIMEOUT
        ChatError.RATE_LIMIT -> ChatUiError.RATE_LIMIT
        ChatError.AUTHENTICATION -> ChatUiError.AUTHENTICATION
        ChatError.SERVER -> ChatUiError.SERVICE
        ChatError.EMPTY_RESPONSE,
        ChatError.INVALID_RESPONSE,
        ChatError.UNSUPPORTED_RESPONSE,
        ChatError.TRUNCATED_RESPONSE -> ChatUiError.INVALID_RESPONSE
    }

    private companion object {
        const val MAX_INPUT_CHARACTERS = 1_000
    }
}
