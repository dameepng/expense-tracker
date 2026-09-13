package com.example.expense_tracker.data.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.InvocationTargetException

class ClaudeMultimodalRequestTest {
    @Test fun multimodalMessageUsesAnthropicImageBlockShape() {
        val api = OkHttpClaudeApi()
        val method = api.javaClass.getDeclaredMethod("requestJson", ClaudeMessageRequest::class.java).apply { isAccessible = true }
        val json = method.invoke(api, ClaudeMessageRequest("model", 100, "system", listOf(
            ClaudeMessage("user", "JSON ONLY", ClaudeImage("image/jpeg", "abc123"))
        ))).toString()
        assertTrue(json.contains("\"type\":\"image\""))
        assertTrue(json.contains("\"media_type\":\"image/jpeg\""))
        assertTrue(json.contains("\"data\":\"abc123\""))
        assertTrue(json.contains("\"type\":\"text\""))
    }

    @Test fun textOnlyMessageKeepsStringContent() {
        val api = OkHttpClaudeApi()
        val method = api.javaClass.getDeclaredMethod("requestJson", ClaudeMessageRequest::class.java).apply { isAccessible = true }
        val json = method.invoke(api, ClaudeMessageRequest("model", 100, "system", listOf(ClaudeMessage("user", "hello")))).toString()
        assertTrue(json.contains("\"content\":\"hello\""))
        assertEquals(0, "\"type\":\"image\"".toRegex().findAll(json).count())
    }

    @Test fun oversizedImageIsRejectedBeforeSerialization() {
        val api = OkHttpClaudeApi()
        val method = api.javaClass.getDeclaredMethod("requestJson", ClaudeMessageRequest::class.java).apply { isAccessible = true }
        val thrown = try {
            method.invoke(api, ClaudeMessageRequest("model", 100, "system", listOf(ClaudeMessage("user", "x", ClaudeImage("image/jpeg", "a".repeat(240_001))))))
            null
        } catch (e: InvocationTargetException) { e.targetException }
        assertTrue(thrown is AiInputException)
        assertEquals(AiError.INVALID_INPUT, (thrown as AiInputException).reason)
    }
}
