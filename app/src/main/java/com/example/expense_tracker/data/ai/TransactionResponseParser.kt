package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.TransactionType
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.StringReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Raw model text stays at this boundary and never enters UI state. */
internal class TransactionResponseParser {
    fun parse(json: String, request: NaturalLanguageRequest): ParsedTransaction {
        try {
            if (json.length > MAX_RESPONSE_CHARS) invalidResponse()
            val cleaned = cleanJson(json)
            val dto = readTransaction(cleaned)
            if (dto.error != null) {
                if (dto.error in SUPPORTED_ERRORS) throw AiInputException(AiError.INVALID_INPUT)
                invalidResponse()
            }
            val category = request.categories.singleOrNull {
                it.name == dto.category &&
                    (it.type == dto.type || it.type == "BOTH")
            } ?: invalidResponse()
            if (!ISO_DATE.matches(dto.date)) invalidResponse()
            val parsedDate = LocalDate.parse(dto.date, DateTimeFormatter.ISO_LOCAL_DATE)
            if (parsedDate.year !in 1..9999) invalidResponse()
            val date = RelativeDateResolver.resolve(request.text, request.referenceDate) ?: parsedDate
            return ParsedTransaction(
                amount = dto.amount,
                categoryId = category.id,
                merchant = dto.merchant.trim(),
                date = date,
                note = dto.note.trim(),
                isRecurring = dto.isRecurring,
                type = dto.type
            )
        } catch (exception: AiInputException) {
            throw exception
        } catch (_: Exception) {
            throw AiInputException(AiError.INVALID_RESPONSE)
        }
    }

    private fun cleanJson(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("```")) {
            return trimmed
                .removePrefix("```json")
                .removePrefix("```JSON")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        }
        return trimmed
    }

    private fun readTransaction(json: String): TransactionDto = JsonReader(StringReader(json)).use { reader ->
        reader.strictness = Strictness.STRICT
        val names = mutableSetOf<String>()
        var amount: Long? = null
        var type: String? = null
        var category: String? = null
        var merchant: String? = null
        var date: String? = null
        var note: String? = null
        var isRecurring: Boolean? = null
        var error: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (!names.add(name)) invalidResponse()
            when (name) {
                "amount" -> {
                    if (reader.peek() != JsonToken.NUMBER) invalidResponse()
                    val number = reader.nextString()
                    if (!POSITIVE_INTEGER.matches(number)) invalidResponse()
                    amount = number.toLongOrNull()?.takeIf { it > 0 } ?: invalidResponse()
                }
                "type" -> {
                    val parsedType = reader.readString(20).uppercase()
                    if (parsedType != TransactionType.EXPENSE.name && parsedType != TransactionType.INCOME.name) {
                        invalidResponse()
                    }
                    type = parsedType
                }
                "category" -> category = reader.readString(200)
                "merchant" -> merchant = reader.readString(200)
                "date" -> date = reader.readString(10)
                "note" -> note = reader.readString(1_000)
                "is_recurring" -> {
                    if (reader.peek() != JsonToken.BOOLEAN) invalidResponse()
                    isRecurring = reader.nextBoolean()
                }
                "error" -> error = reader.readString(100)
                else -> invalidResponse()
            }
        }
        reader.endObject()
        if (reader.peek() != JsonToken.END_DOCUMENT) invalidResponse()
        if (error != null) {
            if (names != setOf("error")) invalidResponse()
            return@use TransactionDto(error = error)
        }
        TransactionDto(
            amount = amount ?: invalidResponse(),
            type = type ?: TransactionType.EXPENSE.name,
            category = category ?: invalidResponse(),
            merchant = merchant ?: invalidResponse(),
            date = date ?: invalidResponse(),
            note = note ?: invalidResponse(),
            isRecurring = isRecurring ?: invalidResponse()
        )
    }

    private fun JsonReader.readString(maxLength: Int): String {
        if (peek() != JsonToken.STRING) invalidResponse()
        return nextString().also { if (it.length > maxLength) invalidResponse() }
    }

    private data class TransactionDto(
        val amount: Long = 0,
        val type: String = TransactionType.EXPENSE.name,
        val category: String = "",
        val merchant: String = "",
        val date: String = "",
        val note: String = "",
        val isRecurring: Boolean = false,
        val error: String? = null
    )

    private companion object {
        const val MAX_RESPONSE_CHARS = 16_384
        val POSITIVE_INTEGER = Regex("[1-9][0-9]*")
        val ISO_DATE = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")
        val SUPPORTED_ERRORS = setOf("unsupported_transaction", "ambiguous_input")
        fun invalidResponse(): Nothing = throw AiInputException(AiError.INVALID_RESPONSE)
    }
}
