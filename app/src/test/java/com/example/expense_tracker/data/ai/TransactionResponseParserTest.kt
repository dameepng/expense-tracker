package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TransactionResponseParserTest {
    private val parser = TransactionResponseParser()
    private val request = NaturalLanguageRequest(
        text = "makan siang di warteg 25rb",
        categories = listOf(
            Category(1, "Makanan & Minuman"),
            Category(2, "Transportasi"),
            Category(3, "Hiburan"),
            Category(4, "Lainnya", "BOTH"),
            Category(5, "Gaji", "INCOME")
        ),
        referenceDate = LocalDate.of(2026, 9, 10),
        zoneId = ZoneId.of("Asia/Jakarta")
    )
    private val validJson = """
        {"amount":25000,"category":"Makanan & Minuman","merchant":"Warteg","date":"2026-09-10","note":"makan siang","is_recurring":false}
    """.trimIndent()

    @Test
    fun `parses example into exact typed transaction`() {
        assertEquals(
            ParsedTransaction(25000L, 1L, "Warteg", LocalDate.of(2026, 9, 10), "makan siang", false),
            parser.parse(validJson, request)
        )
    }

    @Test
    fun `subscription response preserves recurrence`() {
        val json = validJson.replace("25000", "120000")
            .replace("Makanan & Minuman", "Hiburan")
            .replace("Warteg", "Netflix")
            .replace("makan siang", "langganan netflix")
            .replace("false", "true")
        val result = parser.parse(json, request.copy(text = "langganan netflix 120rb tiap bulan"))
        assertEquals(120000L, result.amount)
        assertEquals(3L, result.categoryId)
        assertEquals("Netflix", result.merchant)
        assertTrue(result.isRecurring)
    }

    @Test
    fun `shared BOTH category is accepted but mismatched category type is rejected`() {
        val result = parser.parse(validJson.replace("Makanan & Minuman", "Lainnya"), request)
        assertEquals(4L, result.categoryId)
        assertInvalid(validJson.replace("Makanan & Minuman", "Gaji"))
    }

    @Test
    fun `income transaction with income category is parsed successfully`() {
        val incomeJson = """
            {"amount":5729860,"type":"INCOME","category":"Gaji","merchant":"Kantor","date":"2026-09-10","note":"gaji bulanan","is_recurring":true}
        """.trimIndent()
        val result = parser.parse(incomeJson, request.copy(text = "gajian bulan ini 5.729.860"))
        assertEquals(5729860L, result.amount)
        assertEquals("INCOME", result.type)
        assertEquals(5L, result.categoryId)
        assertEquals("Kantor", result.merchant)
        assertEquals(LocalDate.of(2026, 9, 10), result.date)
        assertTrue(result.isRecurring)
    }

    @Test
    fun `category comes from request with exact spelling`() {
        assertInvalid(validJson.replace("Makanan & Minuman", "Restoran Baru"))
        assertInvalid(validJson.replace("Makanan & Minuman", "makanan & minuman"))
        val custom = request.copy(categories = listOf(Category(41, "Kopi Spesial")))
        assertEquals(41L, parser.parse(validJson.replace("Makanan & Minuman", "Kopi Spesial"), custom).categoryId)
    }

    @Test
    fun `amount must be a positive exact JSON integer within Long range`() {
        listOf("0", "-1", "25000.5", "25000.0", "2.5e4", "\"25000\"", "true", "null", "9223372036854775808")
            .forEach { assertInvalid(validJson.replace("25000", it)) }
        assertEquals(Long.MAX_VALUE, parser.parse(validJson.replace("25000", Long.MAX_VALUE.toString()), request).amount)
    }

    @Test
    fun `booleans and strings must have JSON types from schema`() {
        listOf("\"false\"", "0", "null").forEach { assertInvalid(validJson.replace("false", it)) }
        listOf("23", "false", "null").forEach { assertInvalid(validJson.replace("\"Warteg\"", it)) }
    }

    @Test
    fun `missing null duplicate and extra fields are rejected`() {
        assertInvalid(validJson.replace("\"merchant\":\"Warteg\",", ""))
        assertInvalid(validJson.replace("\"Warteg\"", "null"))
        assertInvalid(validJson.replace("{", "{\"amount\":100,"))
        assertInvalid(validJson.replace("{", "{\"extra\":true,"))
        assertInvalid(validJson.replace("{", "{\"error\":\"ambiguous_input\","))
    }

    @Test
    fun `markdown code blocks are cleaned and parsed successfully`() {
        val wrapped = "```json\n$validJson\n```"
        val result = parser.parse(wrapped, request)
        assertEquals(25000L, result.amount)
        assertEquals("EXPENSE", result.type)
    }

    @Test
    fun `invalid JSON comments arrays and trailing objects are rejected`() {
        listOf(
            "not json", "[$validJson]", "$validJson {}",
            validJson.replace("{", "{/* note */"), validJson.replace("}", ",}"),
            validJson.replace("\"amount\"", "amount"), "null", ""
        ).forEach(::assertInvalid)
    }

    @Test
    fun `date must be a real strict ISO calendar date`() {
        listOf("2026-02-30", "2026-13-01", "2026-9-10", "10/09/2026", "kemarin", "0000-01-01")
            .forEach { assertInvalid(validJson.replace("2026-09-10", it)) }
        assertEquals(LocalDate.of(2024, 2, 29), parser.parse(validJson.replace("2026-09-10", "2024-02-29"), request).date)
    }

    @Test
    fun `relative date still requires valid date in response`() {
        val exception = assertThrows(AiInputException::class.java) {
            parser.parse(validJson.replace("2026-09-10", "kemarin"), request.copy(text = "beli bensin 50000 kemarin"))
        }
        assertEquals(AiError.INVALID_RESPONSE, exception.reason)
    }

    @Test
    fun `kemarin overrides mistaken model date across year rollover`() {
        val datedRequest = request.copy(text = "beli bensin 50000 kemarin", referenceDate = LocalDate.of(2026, 1, 1))
        assertEquals(LocalDate.of(2025, 12, 31), parser.parse(validJson, datedRequest).date)
    }

    @Test
    fun `kemarin handles leap day and month rollover`() {
        val datedRequest = request.copy(text = "makan 25rb kemarin", referenceDate = LocalDate.of(2024, 3, 1))
        assertEquals(LocalDate.of(2024, 2, 29), parser.parse(validJson, datedRequest).date)
    }

    @Test
    fun `tadi pagi uses supplied device date`() {
        val datedRequest = request.copy(text = "beli kopi 25rb tadi pagi", referenceDate = LocalDate.of(2026, 10, 1))
        assertEquals(datedRequest.referenceDate, parser.parse(validJson, datedRequest).date)
    }

    @Test
    fun `relative dates use device local date rather than UTC day`() {
        val instant = Instant.parse("2026-09-10T01:00:00Z")
        val jakarta = ZoneId.of("Asia/Jakarta")
        val losAngeles = ZoneId.of("America/Los_Angeles")
        fun parseIn(zone: ZoneId) = parser.parse(
            validJson,
            request.copy(text = "makan 25rb kemarin", referenceDate = instant.atZone(zone).toLocalDate(), zoneId = zone)
        ).date
        assertEquals(LocalDate.of(2026, 9, 9), parseIn(jakarta))
        assertEquals(LocalDate.of(2026, 9, 8), parseIn(losAngeles))
    }

    @Test
    fun `explicit dates are left for model resolution when mixed with relative language`() {
        val datedRequest = request.copy(text = "makan 25rb tanggal 2026-09-10 dari pesanan kemarin")
        assertEquals(LocalDate.of(2026, 9, 10), parser.parse(validJson, datedRequest).date)
    }

    @Test
    fun `compound relative phrases do not override date with yesterday or today`() {
        listOf("minggu kemarin", "bulan kemarin", "tahun kemarin", "senin kemarin", "2 hari tadi", "dua hari kemarin").forEach { phrase ->
            val datedRequest = request.copy(text = "makan 25rb $phrase", referenceDate = LocalDate.of(2026, 10, 1))
            assertEquals(phrase, LocalDate.of(2026, 9, 10), parser.parse(validJson, datedRequest).date)
        }
    }

    @Test
    fun `unsupported income or multiple expenses produces editable input error`() {
        listOf("unsupported_transaction", "ambiguous_input").forEach { reason ->
            val exception = assertThrows(AiInputException::class.java) {
                parser.parse("""{"error":"$reason"}""", request)
            }
            assertEquals(AiError.INVALID_INPUT, exception.reason)
        }
        assertInvalid("""{"error":"arbitrary error"}""")
    }

    @Test
    fun `empty optional descriptions remain valid and no recurrence is invented`() {
        val result = parser.parse(validJson.replace("Warteg", "").replace("makan siang", ""), request)
        assertEquals("", result.merchant)
        assertEquals("", result.note)
        assertFalse(result.isRecurring)
    }

    @Test
    fun `oversized model response or fields are rejected`() {
        assertInvalid(validJson.replace("Warteg", "x".repeat(201)))
        assertInvalid(validJson.replace("makan siang", "x".repeat(1_001)))
        assertInvalid(" ".repeat(16_385) + validJson)
    }

    private fun assertInvalid(json: String) {
        val exception = assertThrows(AiInputException::class.java) { parser.parse(json, request) }
        assertEquals(json.take(100), AiError.INVALID_RESPONSE, exception.reason)
    }
}
