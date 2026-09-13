package com.example.expense_tracker.data.ai.receipt

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.ai.ParsedTransaction
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.StringReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class ReceiptResponseParser {
    fun parse(json: String, categories: List<Category>): ReceiptTransaction {
        try {
            if (json.length > MAX_RESPONSE_CHARS) invalid()
            val dto = read(cleanJson(json))
            dto.error?.let { error -> throw ReceiptParseException(errorReason(error)) }
            val amount = dto.amount ?: invalid()
            val categoryName = dto.category ?: invalid()
            val category = categories.singleOrNull {
                it.name == categoryName && (it.type == TransactionType.EXPENSE.name || it.type == "BOTH")
            } ?: invalid()
            val merchant = dto.merchant ?: invalid()
            val dateText = dto.date ?: invalid()
            if (!ISO_DATE.matches(dateText)) invalid()
            val date = LocalDate.parse(dateText, DateTimeFormatter.ISO_LOCAL_DATE)
            val note = dto.note ?: invalid()
            val items = dto.items ?: invalid()
            if (items.size > MAX_ITEMS || items.any { it.isBlank() || it.length > MAX_ITEM_LENGTH }) invalid()
            return ReceiptTransaction(
                transaction = ParsedTransaction(amount, category.id, merchant.trim(), date, note.trim(), dto.isRecurring ?: invalid()),
                items = items.map { it.trim() }
            )
        } catch (e: ReceiptParseException) {
            throw e
        } catch (_: Exception) {
            throw ReceiptParseException(ReceiptParseError.INVALID_RESPONSE)
        }
    }

    private fun read(json: String): ReceiptResponse = JsonReader(StringReader(json)).use { reader ->
        reader.strictness = Strictness.STRICT
        val names = mutableSetOf<String>()
        var amount: Long? = null; var category: String? = null; var merchant: String? = null
        var date: String? = null; var note: String? = null; var items: List<String>? = null
        var recurring: Boolean? = null; var error: ReceiptParseError? = null
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (!names.add(name)) invalid()
            when (name) {
                "amount" -> { if (reader.peek() != JsonToken.NUMBER) invalid(); amount = reader.nextString().toLongOrNull()?.takeIf { it > 0 } ?: invalid() }
                "category" -> category = reader.string(MAX_TEXT)
                "merchant" -> merchant = reader.string(MAX_TEXT)
                "date" -> date = reader.string(10)
                "note" -> note = reader.string(MAX_NOTE)
                "is_recurring" -> { if (reader.peek() != JsonToken.BOOLEAN) invalid(); recurring = reader.nextBoolean() }
                "items" -> {
                    if (reader.peek() != JsonToken.BEGIN_ARRAY) invalid()
                    reader.beginArray(); val values = mutableListOf<String>()
                    while (reader.hasNext()) { if (reader.peek() != JsonToken.STRING) invalid(); values += reader.nextString() }
                    reader.endArray(); items = values
                }
                "error" -> error = errorReason(reader.string(MAX_TEXT))
                else -> invalid()
            }
        }
        reader.endObject(); if (reader.peek() != JsonToken.END_DOCUMENT) invalid()
        if (error != null) { if (names != setOf("error")) invalid(); return@use ReceiptResponse(error = error.name.lowercase()) }
        ReceiptResponse(amount, category, merchant, date, note, items, recurring)
    }

    private fun JsonReader.string(max: Int): String {
        if (peek() != JsonToken.STRING) invalid()
        return nextString().also { if (it.length > max) invalid() }
    }

    private fun cleanJson(raw: String): String = raw.trim().let {
        if (!it.startsWith("```")) it else it.removePrefix("```json").removePrefix("```JSON").removePrefix("```").removeSuffix("```").trim()
    }

    private fun errorReason(value: String): ReceiptParseError = when (value) {
        "unclear_receipt" -> ReceiptParseError.UNCLEAR_RECEIPT
        "not_a_receipt" -> ReceiptParseError.NOT_A_RECEIPT
        "missing_total" -> ReceiptParseError.MISSING_TOTAL
        else -> ReceiptParseError.INVALID_RESPONSE
    }

    private companion object {
        const val MAX_RESPONSE_CHARS = 16_384; const val MAX_ITEMS = 100
        const val MAX_ITEM_LENGTH = 200; const val MAX_TEXT = 200; const val MAX_NOTE = 1_000
        val ISO_DATE = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")
        fun invalid(): Nothing = throw ReceiptParseException(ReceiptParseError.INVALID_RESPONSE)
    }
}
