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
    fun parse(json: String, request: NaturalLanguageRequest): List<ParsedTransaction> {
        try {
            if (json.length > MAX_RESPONSE_CHARS) invalidResponse()
            val cleaned = cleanJson(json)
            val dtos = readTransactions(cleaned)
            if (dtos.isEmpty()) invalidResponse()
            return dtos.map { dto ->
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
                ParsedTransaction(
                    amount = dto.amount,
                    categoryId = category.id,
                    merchant = dto.merchant.trim(),
                    date = date,
                    note = dto.note.trim(),
                    isRecurring = dto.isRecurring,
                    type = dto.type
                )
            }
        } catch (exception: AiInputException) {
            throw exception
        } catch (_: Exception) {
            throw AiInputException(AiError.INVALID_RESPONSE)
        }
    }

    fun parseSingle(json: String, request: NaturalLanguageRequest): ParsedTransaction =
        parse(json, request).first()

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

    private fun readTransactions(json: String): List<TransactionDto> = JsonReader(StringReader(json)).use { reader ->
        reader.strictness = Strictness.STRICT
        when (reader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                val list = mutableListOf<TransactionDto>()
                reader.beginArray()
                while (reader.hasNext()) {
                    list += readSingleTransactionObject(reader)
                }
                reader.endArray()
                if (reader.peek() != JsonToken.END_DOCUMENT) invalidResponse()
                list
            }
            JsonToken.BEGIN_OBJECT -> {
                reader.beginObject()
                var error: String? = null
                val list = mutableListOf<TransactionDto>()
                val names = mutableSetOf<String>()

                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (!names.add(name)) invalidResponse()
                    if (error != null) invalidResponse()
                    when (name) {
                        "error" -> {
                            if (names.size > 1) invalidResponse()
                            error = reader.readString(100)
                        }
                        "transactions" -> {
                            if (reader.peek() != JsonToken.BEGIN_ARRAY) invalidResponse()
                            reader.beginArray()
                            while (reader.hasNext()) {
                                list += readSingleTransactionObject(reader)
                            }
                            reader.endArray()
                        }
                        "amount", "category", "merchant", "date", "note", "is_recurring", "type" -> {
                            list += readRemainingTransactionFields(reader, initialKey = name)
                            break
                        }
                        else -> invalidResponse()
                    }
                }
                reader.endObject()
                if (reader.peek() != JsonToken.END_DOCUMENT) invalidResponse()
                if (error != null) {
                    if (names != setOf("error")) invalidResponse()
                    return@use listOf(TransactionDto(error = error))
                }
                list
            }
            else -> invalidResponse()
        }
    }

    private fun readSingleTransactionObject(reader: JsonReader): TransactionDto {
        reader.beginObject()
        val dto = readRemainingTransactionFields(reader)
        reader.endObject()
        return dto
    }

    private fun readRemainingTransactionFields(reader: JsonReader, initialKey: String? = null): TransactionDto {
        val names = mutableSetOf<String>()
        var amount: Long? = null
        var type: String? = null
        var category: String? = null
        var merchant: String? = null
        var date: String? = null
        var note: String? = null
        var isRecurring: Boolean? = null
        var error: String? = null

        fun processField(name: String) {
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

        if (initialKey != null) {
            processField(initialKey)
        }
        while (reader.hasNext()) {
            val name = reader.nextName()
            processField(name)
        }
        if (error != null) {
            if (names != setOf("error")) invalidResponse()
            return TransactionDto(error = error)
        }
        return TransactionDto(
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
