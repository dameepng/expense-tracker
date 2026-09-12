package com.example.expense_tracker.data.ai.chat

import com.example.expense_tracker.data.ai.AiConfiguration
import com.example.expense_tracker.data.ai.AiError
import com.example.expense_tracker.data.ai.AiErrorMapper
import com.example.expense_tracker.data.ai.ClaudeApi
import com.example.expense_tracker.data.ai.ClaudeContentBlock
import com.example.expense_tracker.data.ai.ClaudeMessage
import com.example.expense_tracker.data.ai.ClaudeMessageRequest
import com.example.expense_tracker.data.ai.ClaudeMessageResponse
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.CancellationException

internal class ClaudeChatRepository(
    configuration: AiConfiguration,
    private val api: ClaudeApi,
    private val contextSource: ChatContextSource,
    private val contextBuilder: ChatContextBuilder = ChatContextBuilder(),
    private val historyPolicy: ChatHistoryPolicy = ChatHistoryPolicy(),
    private val clock: Clock = Clock.systemUTC(),
    private val idProvider: () -> String = { UUID.randomUUID().toString() }
) : ChatRepository {
    private val apiKey = configuration.apiKey
    private val model = configuration.model

    override suspend fun send(request: ChatRequest): ChatResult {
        val question = request.question.trim()
        if (question.isEmpty() || question.length > MAX_QUESTION_CHARACTERS) {
            throw ChatException(ChatError.INVALID_INPUT)
        }
        if (apiKey.isBlank()) {
            throw ChatException(ChatError.MISSING_API_KEY)
        }

        try {
            val context = contextBuilder.build(contextSource.loadContext())
            val messages = historyPolicy.select(request.history)
                .map { it.toClaudeMessage() }
                .plus(ClaudeMessage(role = USER_ROLE, content = question))
            val response = api.createMessage(
                apiKey = apiKey.trim(),
                request = ClaudeMessageRequest(
                    model = model,
                    maxTokens = MAX_OUTPUT_TOKENS,
                    system = ChatPromptBuilder.build(context),
                    messages = messages
                )
            )
            val answer = response.requireCompleteText()
            return ChatResult(
                assistantMessage = ChatMessage(
                    id = idProvider(),
                    role = ChatRole.ASSISTANT,
                    content = answer,
                    timestampEpochMillis = clock.millis()
                )
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: ChatException) {
            throw failure
        } catch (failure: ChatContextException) {
            throw ChatException(failure.reason.toChatError())
        } catch (failure: Exception) {
            throw ChatException(AiErrorMapper.toException(failure).reason.toChatError())
        }
    }

    private fun ChatMessage.toClaudeMessage() = ClaudeMessage(
        role = when (role) {
            ChatRole.USER -> USER_ROLE
            ChatRole.ASSISTANT -> ASSISTANT_ROLE
        },
        content = content
    )

    private fun ClaudeMessageResponse.requireCompleteText(): String {
        if (stopReason == MAX_TOKENS_STOP_REASON) {
            throw ChatException(ChatError.TRUNCATED_RESPONSE)
        }
        if (stopReason != END_TURN_STOP_REASON) {
            throw ChatException(ChatError.INVALID_RESPONSE)
        }
        val blocks = content ?: throw ChatException(ChatError.INVALID_RESPONSE)
        if (blocks.isEmpty()) {
            throw ChatException(ChatError.EMPTY_RESPONSE)
        }
        if (blocks.any { it.type != TEXT_BLOCK_TYPE }) {
            throw ChatException(ChatError.UNSUPPORTED_RESPONSE)
        }
        val text = blocks.joinToString(separator = "") { block -> block.requireText() }.trim()
        if (text.isEmpty()) {
            throw ChatException(ChatError.EMPTY_RESPONSE)
        }
        return text
    }

    private fun ClaudeContentBlock.requireText(): String =
        text ?: throw ChatException(ChatError.INVALID_RESPONSE)

    private fun ChatContextError.toChatError(): ChatError = when (this) {
        ChatContextError.DATA_UNAVAILABLE -> ChatError.DATA_UNAVAILABLE
        ChatContextError.INVALID_PREFERENCES -> ChatError.INVALID_PREFERENCES
        ChatContextError.INVALID_CONTEXT -> ChatError.INVALID_CONTEXT
        ChatContextError.CONTEXT_TOO_LARGE -> ChatError.CONTEXT_TOO_LARGE
    }

    private fun AiError.toChatError(): ChatError = when (this) {
        AiError.MISSING_API_KEY -> ChatError.MISSING_API_KEY
        AiError.INVALID_INPUT -> ChatError.INVALID_INPUT
        AiError.NETWORK -> ChatError.NETWORK
        AiError.TIMEOUT -> ChatError.TIMEOUT
        AiError.RATE_LIMIT -> ChatError.RATE_LIMIT
        AiError.AUTHENTICATION -> ChatError.AUTHENTICATION
        AiError.SERVER -> ChatError.SERVER
        AiError.INVALID_RESPONSE -> ChatError.INVALID_RESPONSE
    }

    private companion object {
        const val MAX_QUESTION_CHARACTERS = 1_000
        const val MAX_OUTPUT_TOKENS = 1_024
        const val USER_ROLE = "user"
        const val ASSISTANT_ROLE = "assistant"
        const val TEXT_BLOCK_TYPE = "text"
        const val END_TURN_STOP_REASON = "end_turn"
        const val MAX_TOKENS_STOP_REASON = "max_tokens"
    }
}
