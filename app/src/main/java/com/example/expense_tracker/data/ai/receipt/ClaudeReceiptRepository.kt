package com.example.expense_tracker.data.ai.receipt

import com.example.expense_tracker.data.ai.AiError
import com.example.expense_tracker.data.ai.AiErrorMapper
import com.example.expense_tracker.data.ai.AiInputException
import com.example.expense_tracker.data.ai.ClaudeApi
import com.example.expense_tracker.data.ai.ClaudeImage
import com.example.expense_tracker.data.ai.ClaudeMessage
import com.example.expense_tracker.data.ai.ClaudeMessageRequest

class ClaudeReceiptRepository internal constructor(
    private val apiKey: String,
    private val model: String,
    private val api: ClaudeApi,
    private val imageProcessor: ReceiptImageProcessor,
    private val parser: ReceiptResponseParser = ReceiptResponseParser()
) : ReceiptRepository {
    override suspend fun scan(request: ReceiptScanRequest): ReceiptTransaction {
        if (apiKey.isBlank()) throw AiInputException(AiError.MISSING_API_KEY)
        if (request.categories.isEmpty()) throw AiInputException(AiError.INVALID_INPUT)

        try {
            val image = imageProcessor.process(request.imageUri)
            val prompt = ReceiptPromptBuilder.build(request.referenceDate, request.categories)
            val userMessage = ClaudeMessage(
                role = USER_ROLE,
                content = USER_PROMPT_INSTRUCTION,
                image = ClaudeImage(image.mediaType, image.base64)
            )
            val response = api.createMessage(
                apiKey = apiKey.trim(),
                request = ClaudeMessageRequest(
                    model = model,
                    maxTokens = MAX_OUTPUT_TOKENS,
                    system = prompt,
                    messages = listOf(userMessage)
                )
            )
            if (response.stopReason != END_TURN_STOP_REASON) {
                throw AiInputException(AiError.INVALID_RESPONSE)
            }
            val content = response.content?.singleOrNull()
                ?.takeIf { it.type == TEXT_BLOCK_TYPE }
                ?.text ?: throw AiInputException(AiError.INVALID_RESPONSE)
            return parser.parse(content, request.categories)
        } catch (failure: Exception) {
            if (failure is ReceiptParseException) throw failure
            if (failure is ReceiptImageException) throw failure
            throw AiErrorMapper.toException(failure)
        }
    }

    private companion object {
        const val MAX_OUTPUT_TOKENS = 1_024
        const val USER_ROLE = "user"
        const val USER_PROMPT_INSTRUCTION = "Return the receipt JSON."
        const val TEXT_BLOCK_TYPE = "text"
        const val END_TURN_STOP_REASON = "end_turn"
    }
}
