package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.Category
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.ZoneId

class ClaudeNaturalLanguageRepositoryTest {
    private val request = NaturalLanguageRequest(
        text = "makan 25rb kemarin",
        categories = listOf(Category(1, "Makanan"), Category(2, "Lainnya", "BOTH"), Category(3, "Gaji", "INCOME")),
        referenceDate = LocalDate.of(2026, 1, 1),
        zoneId = ZoneId.of("Asia/Jakarta")
    )
    private val response = ClaudeMessageResponse(
        content = listOf(ClaudeContentBlock("text", """{"amount":25000,"category":"Makanan","merchant":"","date":"2025-12-31","note":"makan","is_recurring":false}""")),
        stopReason = "end_turn"
    )

    @Test
    fun `sends typed request with live categories and device date timezone`() = runTest {
        var captured: ClaudeMessageRequest? = null
        val api = object : ClaudeApi {
            override suspend fun createMessage(apiKey: String, request: ClaudeMessageRequest): ClaudeMessageResponse {
                assertEquals("test-key", apiKey)
                captured = request
                return response
            }
        }
        val result = ClaudeNaturalLanguageRepository("test-key", "claude-sonnet-4-6", api).parse(request)
        val sent = requireNotNull(captured)
        assertEquals("claude-sonnet-4-6", sent.model)
        assertEquals(listOf(ClaudeMessage("user", request.text)), sent.messages)
        assertTrue(sent.system.contains("JSON ONLY"))
        assertTrue(sent.system.contains("unsupported_transaction"))
        val context = JsonParser.parseString(sent.system.substringAfter("Context (JSON data):").trim()).asJsonObject
        assertEquals("2026-01-01", context["current_date"].asString)
        assertEquals("Asia/Jakarta", context["time_zone"].asString)
        assertEquals("2025-12-31", context["resolved_relative_date"].asString)
        val categoryNames = context["categories"].asJsonArray.map { it.asJsonObject["name"].asString }
        assertEquals(listOf("Makanan", "Lainnya", "Gaji"), categoryNames)
        assertTrue(context["categories"].toString().contains("Gaji"))
        assertEquals(25000L, result.single().amount)
        assertEquals(LocalDate.of(2025, 12, 31), result.single().date)
    }

    @Test
    fun `empty key fails before network call`() = runTest {
        expectReason(AiError.MISSING_API_KEY) {
            ClaudeNaturalLanguageRepository(" ", "model", neverCalledApi()).parse(request)
        }
    }

    @Test
    fun `blank oversized or empty categories fails before network call`() = runTest {
        val repository = ClaudeNaturalLanguageRepository("key", "model", neverCalledApi())
        listOf(request.copy(text = " "), request.copy(text = "x".repeat(1_001)), request.copy(categories = emptyList()))
            .forEach { invalid -> expectReason(AiError.INVALID_INPUT) { repository.parse(invalid) } }
    }

    @Test
    fun `network timeout and status failures retain clear reasons`() = runTest {
        val cases = listOf(
            UnknownHostException() to AiError.NETWORK,
            SocketTimeoutException() to AiError.TIMEOUT,
            InterruptedIOException("timeout") to AiError.TIMEOUT,
            ClaudeHttpException(401) to AiError.AUTHENTICATION,
            ClaudeHttpException(403) to AiError.AUTHENTICATION,
            ClaudeHttpException(408) to AiError.TIMEOUT,
            ClaudeHttpException(504) to AiError.TIMEOUT,
            ClaudeHttpException(429) to AiError.RATE_LIMIT,
            ClaudeHttpException(500) to AiError.SERVER,
            ClaudeHttpException(529) to AiError.SERVER
        )
        cases.forEach { (failure, reason) ->
            val api = object : ClaudeApi {
                override suspend fun createMessage(apiKey: String, request: ClaudeMessageRequest): ClaudeMessageResponse = throw failure
            }
            expectReason(reason) { ClaudeNaturalLanguageRepository("key", "model", api).parse(request) }
        }
    }

    @Test
    fun `cancellation is propagated without converting to user error`() = runTest {
        val cancellation = CancellationException("screen closed")
        val api = object : ClaudeApi {
            override suspend fun createMessage(apiKey: String, request: ClaudeMessageRequest): ClaudeMessageResponse = throw cancellation
        }
        try {
            ClaudeNaturalLanguageRepository("key", "model", api).parse(request)
            fail("Cancellation should propagate")
        } catch (exception: CancellationException) {
            assertSame(cancellation, exception)
        }
    }

    @Test
    fun `malformed truncated refused and unexpected content is rejected`() = runTest {
        listOf(
            response.copy(stopReason = "max_tokens"),
            response.copy(stopReason = "refusal"),
            response.copy(stopReason = null),
            response.copy(content = emptyList()),
            response.copy(content = null),
            response.copy(content = listOf(ClaudeContentBlock("tool_use", "{}"))),
            response.copy(content = listOf(ClaudeContentBlock("text", "not json"))),
            response.copy(content = response.content!! + response.content)
        ).forEach { invalid ->
            val api = object : ClaudeApi {
                override suspend fun createMessage(apiKey: String, request: ClaudeMessageRequest) = invalid
            }
            expectReason(AiError.INVALID_RESPONSE) { ClaudeNaturalLanguageRepository("key", "model", api).parse(request) }
        }
    }

    private fun neverCalledApi() = object : ClaudeApi {
        override suspend fun createMessage(apiKey: String, request: ClaudeMessageRequest): ClaudeMessageResponse {
            error("No network call expected")
        }
    }

    private suspend fun expectReason(reason: AiError, action: suspend () -> Unit) {
        try {
            action()
            fail("Expected $reason")
        } catch (exception: AiInputException) {
            assertEquals(reason, exception.reason)
            assertEquals(reason.name, exception.message)
        }
    }
}
