package com.example.expense_tracker.data.ai

import com.example.expense_tracker.BuildConfig
import com.example.expense_tracker.data.Category
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AiDependenciesTest {
    private val request = NaturalLanguageRequest(
        text = "makan 25rb",
        categories = listOf(Category(1L, "Makanan")),
        referenceDate = LocalDate.of(2026, 9, 12),
        zoneId = ZoneId.of("Asia/Jakarta")
    )

    @Test
    fun `shared dependencies retain one transport and BuildConfig configuration`() {
        val firstConsumer = AiDependencies.shared
        val secondConsumer = AiDependencies.shared

        assertSame(firstConsumer, secondConsumer)
        assertSame(firstConsumer.claudeApi, secondConsumer.claudeApi)
        assertEquals(BuildConfig.CLAUDE_MODEL, firstConsumer.configuration.model)
        assertTrue(firstConsumer.configuration.apiKey.contentEquals(BuildConfig.ANTHROPIC_API_KEY))
    }

    @Test
    fun `repositories created for different consumers use the injected transport and configuration`() = runTest {
        val api = RecordingClaudeApi()
        val dependencies = AiDependencies(
            configuration = AiConfiguration(apiKey = " test-key ", model = "test-model"),
            claudeApi = api
        )
        val firstConsumer = dependencies.createNaturalLanguageRepository()
        val secondConsumer = dependencies.createNaturalLanguageRepository()

        firstConsumer.parse(request)
        secondConsumer.parse(request)

        assertSame(api, dependencies.claudeApi)
        assertEquals(listOf("test-key", "test-key"), api.apiKeys)
        assertEquals(listOf("test-model", "test-model"), api.requests.map { it.model })
    }

    @Test
    fun `injected blank key fails before shared transport is called`() = runTest {
        val api = RecordingClaudeApi()
        val repository = AiDependencies(
            configuration = AiConfiguration(apiKey = " ", model = "test-model"),
            claudeApi = api
        ).createNaturalLanguageRepository()

        val failure = runCatching { repository.parse(request) }.exceptionOrNull()

        assertTrue(failure is AiInputException)
        assertEquals(AiError.MISSING_API_KEY, (failure as AiInputException).reason)
        assertEquals(0, api.requests.size)
    }

    private class RecordingClaudeApi : ClaudeApi {
        val apiKeys = mutableListOf<String>()
        val requests = mutableListOf<ClaudeMessageRequest>()

        override suspend fun createMessage(
            apiKey: String,
            request: ClaudeMessageRequest
        ): ClaudeMessageResponse {
            apiKeys += apiKey
            requests += request
            return ClaudeMessageResponse(
                content = listOf(
                    ClaudeContentBlock(
                        type = "text",
                        text = """{"amount":25000,"type":"EXPENSE","category":"Makanan","merchant":"","date":"2026-09-12","note":"makan","is_recurring":false}"""
                    )
                ),
                stopReason = "end_turn"
            )
        }
    }
}
