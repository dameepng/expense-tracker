package com.example.expense_tracker.data.ai

import com.google.gson.JsonParseException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class AiErrorMapperTest {
    @Test
    fun `maps HTTP statuses to stable reasons`() {
        mapOf(
            401 to AiError.AUTHENTICATION,
            403 to AiError.AUTHENTICATION,
            408 to AiError.TIMEOUT,
            504 to AiError.TIMEOUT,
            429 to AiError.RATE_LIMIT,
            400 to AiError.SERVER,
            500 to AiError.SERVER,
            529 to AiError.SERVER
        ).forEach { (status, expected) ->
            assertMapped(expected, ClaudeHttpException(status))
        }
    }

    @Test
    fun `maps network timeout response and service failures`() {
        listOf(
            UnknownHostException("sensitive host") to AiError.NETWORK,
            IOException("sensitive network data") to AiError.NETWORK,
            SocketTimeoutException("sensitive timeout data") to AiError.TIMEOUT,
            InterruptedIOException("sensitive timeout data") to AiError.TIMEOUT,
            JsonParseException("sensitive response body") to AiError.INVALID_RESPONSE,
            IllegalStateException("sensitive response body") to AiError.INVALID_RESPONSE,
            IllegalArgumentException("sensitive request data") to AiError.SERVER,
            RuntimeException("sensitive service data") to AiError.SERVER
        ).forEach { (failure, expected) ->
            assertMapped(expected, failure)
        }
    }

    @Test
    fun `preserves an existing stable AI exception`() {
        val existing = AiInputException(AiError.INVALID_INPUT)

        assertSame(existing, AiErrorMapper.toException(existing))
    }

    @Test
    fun `propagates cancellation unchanged`() {
        val cancellation = CancellationException("screen closed")

        try {
            AiErrorMapper.toException(cancellation)
            fail("Cancellation should propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }

    private fun assertMapped(expected: AiError, failure: Exception) {
        val mapped = AiErrorMapper.toException(failure)

        assertEquals(expected, mapped.reason)
        assertEquals(expected.name, mapped.message)
        assertNull(mapped.cause)
    }
}
