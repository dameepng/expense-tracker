package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.TransactionType
import java.time.LocalDate
import java.time.ZoneId

data class NaturalLanguageRequest(
    val text: String,
    val categories: List<Category>,
    val referenceDate: LocalDate,
    val zoneId: ZoneId
)

data class ParsedTransaction(
    val amount: Long,
    val categoryId: Long,
    val merchant: String,
    val date: LocalDate,
    val note: String,
    val isRecurring: Boolean,
    val type: String = TransactionType.EXPENSE.name
)

interface NaturalLanguageRepository {
    suspend fun parse(request: NaturalLanguageRequest): List<ParsedTransaction>
    suspend fun parseSingle(request: NaturalLanguageRequest): ParsedTransaction = parse(request).first()
}

enum class AiError {
    MISSING_API_KEY,
    NETWORK,
    TIMEOUT,
    RATE_LIMIT,
    AUTHENTICATION,
    SERVER,
    INVALID_RESPONSE,
    INVALID_INPUT
}

// Only a stable reason reaches the UI; server messages can contain sensitive input.
class AiInputException(val reason: AiError) : Exception(reason.name)
