package com.example.expense_tracker.data.ai.receipt

import com.example.expense_tracker.data.ai.AiDependencies
import com.example.expense_tracker.data.ai.AiError
import com.example.expense_tracker.data.ai.AiErrorMapper
import com.example.expense_tracker.data.ai.AiInputException
import com.example.expense_tracker.data.ai.ClaudeApi
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
        if (apiKey.isBlank() || request.categories.isEmpty()) throw AiInputException(if (apiKey.isBlank()) AiError.MISSING_API_KEY else AiError.INVALID_INPUT)
        try {
            val image = imageProcessor.process(request.imageUri)
            val response = api.createMessage(apiKey.trim(), ClaudeMessageRequest(model, 1_024, ReceiptPromptBuilder.build(request.referenceDate, request.categories), listOf(ClaudeMessage("user", "Return the receipt JSON.", com.example.expense_tracker.data.ai.ClaudeImage(image.mediaType, image.base64)))))
            if (response.stopReason != "end_turn") throw AiInputException(AiError.INVALID_RESPONSE)
            val content = response.content?.singleOrNull()?.takeIf { it.type == "text" }?.text ?: throw AiInputException(AiError.INVALID_RESPONSE)
            return parser.parse(content, request.categories)
        } catch (failure: Exception) {
            if (failure is ReceiptParseException) throw failure
            if (failure is ReceiptImageException) throw failure
            throw AiErrorMapper.toException(failure)
        }
    }
}
