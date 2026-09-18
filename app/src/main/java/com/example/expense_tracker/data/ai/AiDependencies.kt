package com.example.expense_tracker.data.ai

import com.example.expense_tracker.BuildConfig
import com.example.expense_tracker.data.ai.chat.ChatContextSource
import com.example.expense_tracker.data.ai.chat.ChatRepository
import com.example.expense_tracker.data.ai.chat.ClaudeChatRepository
import com.example.expense_tracker.data.ai.receipt.ClaudeReceiptRepository
import com.example.expense_tracker.data.ai.receipt.ReceiptImageProcessor
import com.example.expense_tracker.data.ai.receipt.ReceiptRepository

internal class AiConfiguration(
    val apiKey: String,
    val model: String
)

/**
 * Manual dependency container shared by every AI feature in this application process.
 * Feature repositories remain separate while using the same transport and configuration.
 */
internal class AiDependencies(
    val configuration: AiConfiguration,
    val claudeApi: ClaudeApi
) {
    fun createNaturalLanguageRepository(): NaturalLanguageRepository =
        ClaudeNaturalLanguageRepository(
            apiKey = configuration.apiKey,
            model = configuration.model,
            api = claudeApi
        )

    fun createChatRepository(contextSource: ChatContextSource): ChatRepository =
        ClaudeChatRepository(
            configuration = configuration,
            api = claudeApi,
            contextSource = contextSource
        )

    fun createReceiptRepository(imageProcessor: ReceiptImageProcessor): ReceiptRepository =
        ClaudeReceiptRepository(
            apiKey = configuration.apiKey,
            model = configuration.model,
            api = claudeApi,
            imageProcessor = imageProcessor
        )

    companion object {
        val shared: AiDependencies by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            AiDependencies(
                configuration = AiConfiguration(
                    apiKey = BuildConfig.ANTHROPIC_API_KEY,
                    model = BuildConfig.CLAUDE_MODEL
                ),
                claudeApi = OkHttpClaudeApi()
            )
        }
    }
}
