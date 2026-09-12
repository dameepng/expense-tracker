package com.example.expense_tracker.data.ai

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal data class ClaudeMessageRequest(
    val model: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeMessage>
)

internal data class ClaudeMessage(val role: String, val content: String)

internal data class ClaudeMessageResponse(
    val content: List<ClaudeContentBlock>? = null,
    @SerializedName("stop_reason") val stopReason: String? = null
)

internal data class ClaudeContentBlock(val type: String? = null, val text: String? = null)

internal interface ClaudeApi {
    suspend fun createMessage(apiKey: String, request: ClaudeMessageRequest): ClaudeMessageResponse
}

internal class ClaudeHttpException(val statusCode: Int) : IOException()

internal class OkHttpClaudeApi(
    private val callFactory: Call.Factory = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
) : ClaudeApi {
    private val gson = GsonBuilder().setStrictness(Strictness.STRICT).create()

    override suspend fun createMessage(
        apiKey: String,
        request: ClaudeMessageRequest
    ): ClaudeMessageResponse = suspendCancellableCoroutine { continuation ->
        val httpRequest = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(gson.toJson(request).toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        val call = callFactory.newCall(httpRequest)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val parsed = response.use {
                        if (!it.isSuccessful) throw ClaudeHttpException(it.code)
                        val body = it.body ?: throw AiInputException(AiError.INVALID_RESPONSE)
                        val source = body.source()
                        // Bound even chunked bodies, without trusting Content-Length.
                        source.request(MAX_BODY_BYTES + 1)
                        if (source.buffer.size > MAX_BODY_BYTES) throw AiInputException(AiError.INVALID_RESPONSE)
                        gson.fromJson(source.readUtf8(), ClaudeMessageResponse::class.java)
                            ?: throw AiInputException(AiError.INVALID_RESPONSE)
                    }
                    if (continuation.isActive) continuation.resume(parsed)
                } catch (exception: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
            }
        })
    }

    private companion object {
        const val MAX_BODY_BYTES = 262_144L
    }
}
