package com.example.expense_tracker.data.ai.chat

import com.example.expense_tracker.data.ai.AiConfiguration
import com.example.expense_tracker.data.ai.AiDependencies
import com.example.expense_tracker.data.ai.ClaudeApi
import com.example.expense_tracker.data.ai.ClaudeContentBlock
import com.example.expense_tracker.data.ai.ClaudeHttpException
import com.example.expense_tracker.data.ai.ClaudeMessageRequest
import com.example.expense_tracker.data.ai.ClaudeMessageResponse
import com.example.expense_tracker.data.analytics.CategoryExpenseTotal
import com.example.expense_tracker.data.analytics.ExpenseComparison
import com.example.expense_tracker.data.analytics.FinancialPeriod
import com.example.expense_tracker.data.analytics.FinancialPeriodKind
import com.example.expense_tracker.data.analytics.PeriodExpenseSummary
import com.google.gson.Gson
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudeChatRepositoryTest {
    @Test
    fun `first question sends one normalized user message and returns stable assistant message`() =
        runBlocking {
            val api = FakeClaudeApi()
            val source = FakeContextSource(listOf(context()))
            val repository = repository(api = api, contextSource = source)

            val result = repository.send(ChatRequest("  Berapa pengeluaran saya?\n"))

            val call = api.calls.single()
            assertEquals(API_KEY, call.apiKey)
            assertEquals(MODEL, call.request.model)
            assertEquals(1_024, call.request.maxTokens)
            assertEquals(listOf("user"), call.request.messages.map { it.role })
            assertEquals("Berapa pengeluaran saya?", call.request.messages.single().content)
            assertEquals(1, call.request.system.countOccurrences("\"schema_version\""))
            val serializedPayload = Gson().toJson(call.request)
            assertFalse(serializedPayload.contains("timestampEpochMillis"))
            assertFalse(serializedPayload.contains("ASSISTANT-old answer"))
            assertFalse(serializedPayload.contains("\"merchant\""))
            assertFalse(serializedPayload.contains("\"note\""))
            assertFalse(serializedPayload.contains("\"cardNumber\""))
            assertEquals(1, source.callCount)
            assertEquals("assistant-id", result.assistantMessage.id)
            assertEquals(ChatRole.ASSISTANT, result.assistantMessage.role)
            assertEquals("Jawaban aman", result.assistantMessage.content)
            assertEquals(RESPONSE_TIME.toEpochMilli(), result.assistantMessage.timestampEpochMillis)
        }

    @Test
    fun `follow up sends completed history in order and current question exactly once`() = runBlocking {
        val api = FakeClaudeApi()
        val history = listOf(
            message(ChatRole.ASSISTANT, "orphan"),
            message(ChatRole.USER, "Pertanyaan pertama"),
            message(ChatRole.ASSISTANT, "Jawaban pertama"),
            message(ChatRole.USER, "gagal tanpa jawaban")
        )

        repository(api = api).send(ChatRequest("Lalu bulan lalu?", history))

        val messages = api.calls.single().request.messages
        assertEquals(listOf("user", "assistant", "user"), messages.map { it.role })
        assertEquals(
            listOf("Pertanyaan pertama", "Jawaban pertama", "Lalu bulan lalu?"),
            messages.map { it.content }
        )
        assertEquals(1, messages.count { it.content == "Lalu bulan lalu?" })
    }

    @Test
    fun `each request uses a fresh context and never repeats an old context in system prompt`() =
        runBlocking {
            val source = FakeContextSource(
                listOf(context(currency = "IDR"), context(currency = "USD"))
            )
            val api = FakeClaudeApi()
            val repository = repository(api = api, contextSource = source)

            repository.send(ChatRequest("Pertama"))
            repository.send(ChatRequest("Kedua", history = completedPair()))

            assertEquals(2, source.callCount)
            assertTrue(api.calls[0].request.system.contains("\"currency\":\"IDR\""))
            assertTrue(api.calls[1].request.system.contains("\"currency\":\"USD\""))
            assertFalse(api.calls[1].request.system.contains("\"currency\":\"IDR\""))
            assertEquals(1, api.calls[1].request.system.countOccurrences("\"schema_version\""))
            assertTrue(api.calls[1].request.system.contains("newest financial_context"))
        }

    @Test
    fun `invalid input and missing key stop before context or API`() {
        listOf("   ", "x".repeat(1_001)).forEach { invalidQuestion ->
            val api = FakeClaudeApi()
            val source = FakeContextSource(listOf(context()))
            val failure = assertThrows(ChatException::class.java) {
                runBlocking { repository(api = api, contextSource = source).send(ChatRequest(invalidQuestion)) }
            }

            assertEquals(ChatError.INVALID_INPUT, failure.reason)
            assertEquals(0, source.callCount)
            assertTrue(api.calls.isEmpty())
        }

        val api = FakeClaudeApi()
        val source = FakeContextSource(listOf(context()))
        val failure = assertThrows(ChatException::class.java) {
            runBlocking {
                repository(api = api, contextSource = source, apiKey = "  ")
                    .send(ChatRequest("Valid"))
            }
        }
        assertEquals(ChatError.MISSING_API_KEY, failure.reason)
        assertEquals(0, source.callCount)
        assertTrue(api.calls.isEmpty())
    }

    @Test
    fun `accepts a trimmed one thousand character question`() = runBlocking {
        val api = FakeClaudeApi()
        val question = "x".repeat(1_000)

        repository(api = api).send(ChatRequest(" \n$question\t"))

        assertEquals(question, api.calls.single().request.messages.single().content)
    }

    @Test
    fun `maps context failures before API and keeps successful empty data distinct`() = runBlocking {
        val reasons = mapOf(
            ChatContextError.DATA_UNAVAILABLE to ChatError.DATA_UNAVAILABLE,
            ChatContextError.INVALID_PREFERENCES to ChatError.INVALID_PREFERENCES,
            ChatContextError.INVALID_CONTEXT to ChatError.INVALID_CONTEXT
        )
        reasons.forEach { (contextReason, expected) ->
            val api = FakeClaudeApi()
            val failure = assertThrows(ChatException::class.java) {
                runBlocking {
                    repository(
                        api = api,
                        contextSource = ChatContextSource {
                            throw ChatContextException(contextReason)
                        }
                    ).send(ChatRequest("Valid"))
                }
            }
            assertEquals(expected, failure.reason)
            assertTrue(api.calls.isEmpty())
        }

        val oversizedApi = FakeClaudeApi()
        val oversized = assertThrows(ChatException::class.java) {
            runBlocking {
                repository(
                    api = oversizedApi,
                    contextBuilder = ChatContextBuilder(maxContextCharacters = 1)
                ).send(ChatRequest("Valid"))
            }
        }
        assertEquals(ChatError.CONTEXT_TOO_LARGE, oversized.reason)
        assertTrue(oversizedApi.calls.isEmpty())

        val emptyApi = FakeClaudeApi()
        repository(api = emptyApi, contextSource = FakeContextSource(listOf(emptyContext())))
            .send(ChatRequest("Valid"))
        assertTrue(emptyApi.calls.single().request.system.contains("\"data_status\":\"empty\""))
    }

    @Test
    fun `joins complete text blocks and rejects empty malformed unsupported or truncated output`() =
        runBlocking {
            val validApi = FakeClaudeApi(
                response = ClaudeMessageResponse(
                    content = listOf(
                        ClaudeContentBlock(type = "text", text = "Bagian "),
                        ClaudeContentBlock(type = "text", text = "kedua")
                    ),
                    stopReason = "end_turn"
                )
            )
            val valid = repository(api = validApi).send(ChatRequest("Valid"))
            assertEquals("Bagian kedua", valid.assistantMessage.content)

            val cases = listOf(
                ClaudeMessageResponse(content = emptyList(), stopReason = "end_turn") to
                    ChatError.EMPTY_RESPONSE,
                ClaudeMessageResponse(
                    content = listOf(ClaudeContentBlock(type = "text", text = " \n")),
                    stopReason = "end_turn"
                ) to ChatError.EMPTY_RESPONSE,
                ClaudeMessageResponse(content = null, stopReason = "end_turn") to
                    ChatError.INVALID_RESPONSE,
                ClaudeMessageResponse(
                    content = listOf(ClaudeContentBlock(type = "text", text = null)),
                    stopReason = "end_turn"
                ) to ChatError.INVALID_RESPONSE,
                ClaudeMessageResponse(
                    content = listOf(ClaudeContentBlock(type = "tool_use", text = "ignored")),
                    stopReason = "end_turn"
                ) to ChatError.UNSUPPORTED_RESPONSE,
                ClaudeMessageResponse(
                    content = listOf(ClaudeContentBlock(type = "text", text = "partial")),
                    stopReason = "max_tokens"
                ) to ChatError.TRUNCATED_RESPONSE,
                ClaudeMessageResponse(
                    content = listOf(ClaudeContentBlock(type = "text", text = "text")),
                    stopReason = "tool_use"
                ) to ChatError.INVALID_RESPONSE
            )
            cases.forEach { (response, expected) ->
                val failure = assertThrows(ChatException::class.java) {
                    runBlocking {
                        repository(api = FakeClaudeApi(response = response))
                            .send(ChatRequest("Valid"))
                    }
                }
                assertEquals(expected, failure.reason)
            }
        }

    @Test
    fun `maps transport failures without exposing raw details`() {
        val failures = listOf(
            IOException("private request") to ChatError.NETWORK,
            SocketTimeoutException("private request") to ChatError.TIMEOUT,
            ClaudeHttpException(401) to ChatError.AUTHENTICATION,
            ClaudeHttpException(429) to ChatError.RATE_LIMIT,
            ClaudeHttpException(500) to ChatError.SERVER
        )

        failures.forEach { (transportFailure, expected) ->
            val api = FakeClaudeApi(failure = transportFailure)
            val failure = assertThrows(ChatException::class.java) {
                runBlocking { repository(api = api).send(ChatRequest("Valid")) }
            }

            assertEquals(expected, failure.reason)
            assertEquals(expected.name, failure.message)
            assertNull(failure.cause)
            assertEquals(1, api.calls.size)
        }
    }

    @Test
    fun `propagates cancellation unchanged and never retries automatically`() {
        val cancellation = CancellationException("screen closed")
        val api = FakeClaudeApi(failure = cancellation)

        try {
            runBlocking { repository(api = api).send(ChatRequest("Valid")) }
            throw AssertionError("Cancellation should propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
        assertEquals(1, api.calls.size)
    }

    @Test
    fun `dependency provider gives chat repository the shared client and configuration`() = runBlocking {
        val api = FakeClaudeApi()
        val dependencies = AiDependencies(
            configuration = AiConfiguration(API_KEY, MODEL),
            claudeApi = api
        )

        val repository = dependencies.createChatRepository(FakeContextSource(listOf(context())))
        repository.send(ChatRequest("Valid"))

        assertSame(api, dependencies.claudeApi)
        assertEquals(API_KEY, api.calls.single().apiKey)
        assertEquals(MODEL, api.calls.single().request.model)
    }

    private fun repository(
        api: ClaudeApi = FakeClaudeApi(),
        contextSource: ChatContextSource = FakeContextSource(listOf(context())),
        contextBuilder: ChatContextBuilder = ChatContextBuilder(),
        apiKey: String = API_KEY
    ) = ClaudeChatRepository(
        configuration = AiConfiguration(apiKey = apiKey, model = MODEL),
        api = api,
        contextSource = contextSource,
        contextBuilder = contextBuilder,
        clock = Clock.fixed(RESPONSE_TIME, ZoneOffset.UTC),
        idProvider = { "assistant-id" }
    )

    private fun completedPair() = listOf(
        message(ChatRole.USER, "old question"),
        message(ChatRole.ASSISTANT, "old answer")
    )

    private fun message(role: ChatRole, content: String) = ChatMessage(
        id = "$role-$content",
        role = role,
        content = content,
        timestampEpochMillis = 1L
    )

    private fun context(currency: String = "IDR") = ChatContext(
        snapshotEpochMillis = SNAPSHOT.toEpochMilli(),
        zoneId = "UTC",
        currency = currency,
        preferredLocale = "id-ID",
        walletScope = WalletScope.ALL_WALLETS,
        currentWeek = summary(
            FinancialPeriodKind.CURRENT_WEEK,
            "2026-09-06T00:00:00Z",
            SNAPSHOT.toString(),
            complete = false,
            total = 10_000L
        ),
        currentMonth = summary(
            FinancialPeriodKind.CURRENT_MONTH,
            "2026-09-01T00:00:00Z",
            SNAPSHOT.toString(),
            complete = false,
            total = 10_000L
        ),
        previousMonth = summary(
            FinancialPeriodKind.PREVIOUS_MONTH,
            "2026-08-01T00:00:00Z",
            "2026-09-01T00:00:00Z",
            complete = true,
            total = 5_000L
        ),
        monthComparison = ExpenseComparison(5_000L, java.math.BigDecimal("100"))
    )

    private fun emptyContext(): ChatContext {
        val context = context()
        return context.copy(
            currentWeek = context.currentWeek.copy(totalExpense = 0L, categories = emptyList()),
            currentMonth = context.currentMonth.copy(totalExpense = 0L, categories = emptyList()),
            previousMonth = context.previousMonth.copy(totalExpense = 0L, categories = emptyList()),
            monthComparison = ExpenseComparison(0L, null)
        )
    }

    private fun summary(
        kind: FinancialPeriodKind,
        start: String,
        end: String,
        complete: Boolean,
        total: Long
    ) = PeriodExpenseSummary(
        period = FinancialPeriod(
            kind = kind,
            startInclusiveEpochMillis = Instant.parse(start).toEpochMilli(),
            endExclusiveEpochMillis = Instant.parse(end).toEpochMilli(),
            isComplete = complete
        ),
        totalExpense = total,
        categories = listOf(CategoryExpenseTotal(1L, "Food", total))
    )

    private class FakeContextSource(
        private val contexts: List<ChatContext>
    ) : ChatContextSource {
        var callCount = 0

        override suspend fun loadContext(): ChatContext {
            val index = callCount.coerceAtMost(contexts.lastIndex)
            callCount += 1
            return contexts[index]
        }
    }

    private class FakeClaudeApi(
        private val response: ClaudeMessageResponse = ClaudeMessageResponse(
            content = listOf(ClaudeContentBlock(type = "text", text = "Jawaban aman")),
            stopReason = "end_turn"
        ),
        private val failure: Exception? = null
    ) : ClaudeApi {
        val calls = mutableListOf<ApiCall>()

        override suspend fun createMessage(
            apiKey: String,
            request: ClaudeMessageRequest
        ): ClaudeMessageResponse {
            calls += ApiCall(apiKey, request)
            failure?.let { throw it }
            return response
        }
    }

    private data class ApiCall(
        val apiKey: String,
        val request: ClaudeMessageRequest
    )

    private fun String.countOccurrences(value: String): Int =
        windowed(value.length).count { it == value }

    private companion object {
        const val API_KEY = "test-key"
        const val MODEL = "test-model"
        val SNAPSHOT: Instant = Instant.parse("2026-09-12T10:00:00Z")
        val RESPONSE_TIME: Instant = Instant.parse("2026-09-12T10:00:05Z")
    }
}
