package com.example.expense_tracker.data.ai

import com.google.gson.JsonParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class OkHttpClaudeApiTest {
    private val request = ClaudeMessageRequest("claude-sonnet-4-6", 1024, "JSON ONLY", listOf(ClaudeMessage("user", "makan 25rb")))

    @Test
    fun `transport sends Messages API headers payload and reads typed response`() = runTest {
        val factory = FakeCallFactory()
        val pending = async { OkHttpClaudeApi(factory).createMessage("test-key", request) }
        runCurrent()
        val call = requireNotNull(factory.call)
        val sent = call.request()
        assertEquals("https://api.anthropic.com/v1/messages", sent.url.toString())
        assertEquals("POST", sent.method)
        assertEquals("test-key", sent.header("x-api-key"))
        assertEquals("2023-06-01", sent.header("anthropic-version"))
        assertEquals("application/json; charset=utf-8", sent.body!!.contentType().toString())
        val buffer = Buffer()
        sent.body!!.writeTo(buffer)
        val payload = JsonParser.parseString(buffer.readUtf8()).asJsonObject
        assertEquals("claude-sonnet-4-6", payload["model"].asString)
        assertEquals(1024, payload["max_tokens"].asInt)
        assertEquals("JSON ONLY", payload["system"].asString)
        assertEquals("makan 25rb", payload["messages"].asJsonArray[0].asJsonObject["content"].asString)
        call.respond(200, """{"content":[{"type":"text","text":"{}"}],"stop_reason":"end_turn"}""")
        assertEquals(ClaudeMessageResponse(listOf(ClaudeContentBlock("text", "{}")), "end_turn"), pending.await())
    }

    @Test
    fun `coroutine cancellation cancels the underlying HTTP call`() = runTest {
        val factory = FakeCallFactory()
        val pending = async { OkHttpClaudeApi(factory).createMessage("test-key", request) }
        runCurrent()
        val call = requireNotNull(factory.call)
        assertFalse(call.isCanceled())
        pending.cancel()
        runCurrent()
        assertTrue(call.isCanceled())
        // A callback arriving after cancellation is ignored, rather than surfacing a UI error.
        call.fail(IOException("Canceled"))
        assertTrue(pending.isCancelled)
    }

    @Test
    fun `non successful status is preserved without returning server error body`() = runTest {
        val factory = FakeCallFactory()
        val pending = async {
            try {
                OkHttpClaudeApi(factory).createMessage("test-key", request)
                fail("Expected HTTP error")
                null
            } catch (exception: ClaudeHttpException) {
                exception
            }
        }
        runCurrent()
        factory.call!!.respond(429, "sensitive server diagnostic")
        val exception = requireNotNull(pending.await())
        assertEquals(429, exception.statusCode)
        assertEquals(null, exception.message)
    }

    @Test
    fun `oversized response is rejected even without checking content length`() = runTest {
        val factory = FakeCallFactory()
        val pending = async {
            try {
                OkHttpClaudeApi(factory).createMessage("test-key", request)
                fail("Expected bounded response error")
                null
            } catch (exception: AiInputException) {
                exception.reason
            }
        }
        runCurrent()
        factory.call!!.respond(200, " ".repeat(262_145))
        assertEquals(AiError.INVALID_RESPONSE, pending.await())
    }

    private class FakeCallFactory : Call.Factory {
        var call: FakeCall? = null
        override fun newCall(request: Request): Call = FakeCall(request).also { call = it }
    }

    private class FakeCall(private val httpRequest: Request) : Call {
        private var callback: Callback? = null
        private var canceled = false
        override fun request(): Request = httpRequest
        override fun execute(): Response = error("Synchronous network call is not expected")
        override fun enqueue(responseCallback: Callback) { callback = responseCallback }
        override fun cancel() { canceled = true }
        override fun isExecuted(): Boolean = callback != null
        override fun isCanceled(): Boolean = canceled
        override fun timeout(): Timeout = Timeout.NONE
        override fun clone(): Call = FakeCall(httpRequest)

        fun respond(status: Int, body: String) {
            callback!!.onResponse(
                this,
                Response.Builder().request(httpRequest).protocol(Protocol.HTTP_1_1)
                    .code(status).message("test response")
                    .body(body.toResponseBody("application/json".toMediaType())).build()
            )
        }

        fun fail(exception: IOException) { callback!!.onFailure(this, exception) }
    }
}
