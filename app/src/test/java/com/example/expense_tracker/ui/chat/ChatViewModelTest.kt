package com.example.expense_tracker.ui.chat

import com.example.expense_tracker.data.ai.chat.ChatError
import com.example.expense_tracker.data.ai.chat.ChatException
import com.example.expense_tracker.data.ai.chat.ChatMessage
import com.example.expense_tracker.data.ai.chat.ChatRepository
import com.example.expense_tracker.data.ai.chat.ChatRequest
import com.example.expense_tracker.data.ai.chat.ChatResult
import com.example.expense_tracker.data.ai.chat.ChatRole
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var nextUserId = 0

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank oversized and double send never create duplicate requests or user bubbles`() {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeChatRepository { request ->
            gate.await()
            result("answer-${request.question}")
        }
        val viewModel = viewModel(repository)

        viewModel.onInputChange("  ")
        viewModel.send()
        assertEquals(ChatUiError.INVALID_INPUT, viewModel.uiState.value.error)
        assertTrue(repository.requests.isEmpty())
        assertTrue(viewModel.uiState.value.messages.isEmpty())

        viewModel.onInputChange("x".repeat(1_001))
        viewModel.send()
        assertEquals(ChatUiError.INPUT_LIMIT, viewModel.uiState.value.error)
        assertTrue(repository.requests.isEmpty())

        viewModel.onInputChange("  valid question  ")
        assertTrue(viewModel.uiState.value.canSend)
        viewModel.send()
        viewModel.send()
        dispatcher.scheduler.runCurrent()

        assertEquals(1, repository.requests.size)
        assertEquals("valid question", repository.requests.single().question)
        assertEquals(1, viewModel.uiState.value.messages.size)
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.canSend)

        gate.complete(Unit)
        dispatcher.scheduler.runCurrent()
        assertEquals(2, viewModel.uiState.value.messages.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `success appends assistant identity and follow up carries completed conversation`() {
        var responseIndex = 0
        val repository = FakeChatRepository {
            responseIndex += 1
            result(
                content = "answer-$responseIndex",
                id = "assistant-$responseIndex",
                timestamp = responseIndex.toLong()
            )
        }
        val viewModel = viewModel(repository)

        viewModel.onInputChange("first")
        viewModel.send()
        dispatcher.scheduler.runCurrent()
        viewModel.onInputChange("follow up")
        viewModel.send()
        dispatcher.scheduler.runCurrent()

        assertEquals(2, repository.requests.size)
        assertTrue(repository.requests.first().history.isEmpty())
        assertEquals(
            listOf(ChatRole.USER, ChatRole.ASSISTANT),
            repository.requests[1].history.map { it.role }
        )
        assertEquals(
            listOf("first", "answer-1"),
            repository.requests[1].history.map { it.content }
        )
        assertEquals(
            listOf("first", "answer-1", "follow up", "answer-2"),
            viewModel.uiState.value.messages.map { it.content }
        )
        val assistant = viewModel.uiState.value.messages.last()
        assertEquals("assistant-2", assistant.id)
        assertEquals(2L, assistant.timestampEpochMillis)
        assertEquals(ChatRole.ASSISTANT, assistant.role)
        assertNull(viewModel.uiState.value.pendingMessage)
        assertNull(viewModel.uiState.value.failedMessage)
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `failure keeps user bubble and retry succeeds without duplicating it`() {
        var attempt = 0
        val repository = FakeChatRepository {
            attempt += 1
            if (attempt == 1) throw ChatException(ChatError.NETWORK)
            result("recovered")
        }
        val viewModel = viewModel(repository)

        viewModel.onInputChange("retry me")
        viewModel.send()
        dispatcher.scheduler.runCurrent()

        val failed = requireNotNull(viewModel.uiState.value.failedMessage)
        assertEquals(ChatUiError.NETWORK, viewModel.uiState.value.error)
        assertEquals(listOf("retry me"), viewModel.uiState.value.messages.map { it.content })
        assertTrue(viewModel.uiState.value.canRetry)

        viewModel.retryFailedMessage()
        dispatcher.scheduler.runCurrent()

        assertEquals(2, repository.requests.size)
        assertTrue(repository.requests[1].history.isEmpty())
        assertEquals("retry me", repository.requests[1].question)
        assertEquals(1, viewModel.uiState.value.messages.count { it.id == failed.id })
        assertEquals(listOf("retry me", "recovered"), viewModel.uiState.value.messages.map { it.content })
        assertNull(viewModel.uiState.value.failedMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `active and failed requests block new send until failed bubble is discarded`() {
        var attempt = 0
        val firstGate = CompletableDeferred<Unit>()
        val repository = FakeChatRepository {
            attempt += 1
            if (attempt == 1) {
                firstGate.await()
                throw ChatException(ChatError.TIMEOUT)
            }
            result("new answer")
        }
        val viewModel = viewModel(repository)

        viewModel.onInputChange("first")
        viewModel.send()
        viewModel.onInputChange("new draft")
        viewModel.send()
        dispatcher.scheduler.runCurrent()
        assertEquals(1, repository.requests.size)
        assertEquals("new draft", viewModel.uiState.value.inputText)

        firstGate.complete(Unit)
        dispatcher.scheduler.runCurrent()
        viewModel.send()
        dispatcher.scheduler.runCurrent()
        assertEquals(1, repository.requests.size)
        assertEquals(ChatUiError.TIMEOUT, viewModel.uiState.value.error)

        viewModel.discardFailedMessage()
        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertNull(viewModel.uiState.value.error)
        viewModel.send()
        dispatcher.scheduler.runCurrent()

        assertEquals(2, repository.requests.size)
        assertEquals("new draft", repository.requests.last().question)
        assertEquals(listOf("new draft", "new answer"), viewModel.uiState.value.messages.map { it.content })
    }

    @Test
    fun `cancel ends loading and ignores a non cooperative late response`() {
        val repository = NonCancellableRepository()
        val viewModel = viewModel(repository)

        viewModel.onInputChange("cancel me")
        viewModel.send()
        dispatcher.scheduler.runCurrent()
        val pending = requireNotNull(viewModel.uiState.value.pendingMessage)

        viewModel.cancelRequest()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(pending, viewModel.uiState.value.failedMessage)
        assertTrue(viewModel.uiState.value.canRetry)

        repository.complete(result("late answer"))
        dispatcher.scheduler.runCurrent()

        assertEquals(listOf("cancel me"), viewModel.uiState.value.messages.map { it.content })
        assertEquals(pending, viewModel.uiState.value.failedMessage)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `reset clears session and ignores completion from previous generation`() {
        val repository = NonCancellableRepository()
        val viewModel = viewModel(repository)

        viewModel.onInputChange("old question")
        viewModel.send()
        dispatcher.scheduler.runCurrent()
        viewModel.onInputChange("draft after send")
        viewModel.resetSession()

        assertEquals(ChatUiState(), viewModel.uiState.value)
        repository.complete(result("late answer"))
        dispatcher.scheduler.runCurrent()
        assertEquals(ChatUiState(), viewModel.uiState.value)
    }

    @Test
    fun `long session retains six whole pairs plus pending draft and truncation notice`() {
        val eighthGate = CompletableDeferred<Unit>()
        var requestCount = 0
        val repository = FakeChatRepository { request ->
            requestCount += 1
            if (requestCount == 8) eighthGate.await()
            result("answer-${request.question}")
        }
        val viewModel = viewModel(repository)

        (1..7).forEach { index ->
            viewModel.onInputChange("question-$index")
            viewModel.send()
            dispatcher.scheduler.runCurrent()
        }

        val afterSeven = viewModel.uiState.value
        assertEquals(12, afterSeven.messages.size)
        assertEquals("question-2", afterSeven.messages.first().content)
        assertEquals("answer-question-7", afterSeven.messages.last().content)
        assertTrue(afterSeven.isHistoryTruncated)

        viewModel.onInputChange("question-8")
        viewModel.send()
        dispatcher.scheduler.runCurrent()
        viewModel.onInputChange("next draft")

        val pending = viewModel.uiState.value
        assertEquals(13, pending.messages.size)
        assertEquals("question-8", pending.pendingMessage?.content)
        assertEquals("next draft", pending.inputText)
        assertTrue(pending.isHistoryTruncated)
        assertEquals(12, repository.requests.last().history.size)

        viewModel.cancelRequest()
        eighthGate.complete(Unit)
        dispatcher.scheduler.runCurrent()
    }

    @Test
    fun `all repository error groups map deterministically and end loading`() {
        val mappings = mapOf(
            ChatError.MISSING_API_KEY to ChatUiError.CONFIGURATION,
            ChatError.INVALID_INPUT to ChatUiError.INVALID_INPUT,
            ChatError.DATA_UNAVAILABLE to ChatUiError.DATA_LOADING,
            ChatError.INVALID_PREFERENCES to ChatUiError.DATA_LOADING,
            ChatError.INVALID_CONTEXT to ChatUiError.DATA_LOADING,
            ChatError.CONTEXT_TOO_LARGE to ChatUiError.CONTEXT_LIMIT,
            ChatError.NETWORK to ChatUiError.NETWORK,
            ChatError.TIMEOUT to ChatUiError.TIMEOUT,
            ChatError.RATE_LIMIT to ChatUiError.RATE_LIMIT,
            ChatError.AUTHENTICATION to ChatUiError.AUTHENTICATION,
            ChatError.SERVER to ChatUiError.SERVICE,
            ChatError.EMPTY_RESPONSE to ChatUiError.INVALID_RESPONSE,
            ChatError.INVALID_RESPONSE to ChatUiError.INVALID_RESPONSE,
            ChatError.UNSUPPORTED_RESPONSE to ChatUiError.INVALID_RESPONSE,
            ChatError.TRUNCATED_RESPONSE to ChatUiError.INVALID_RESPONSE
        )

        mappings.forEach { (repositoryError, expectedUiError) ->
            val repository = FakeChatRepository { throw ChatException(repositoryError) }
            val viewModel = viewModel(repository)
            viewModel.onInputChange("question")
            viewModel.send()
            dispatcher.scheduler.runCurrent()

            assertEquals(repositoryError.name, expectedUiError, viewModel.uiState.value.error)
            assertFalse(repositoryError.name, viewModel.uiState.value.isLoading)
            assertEquals(repositoryError.name, "question", viewModel.uiState.value.failedMessage?.content)
            assertEquals(repositoryError.name, 1, repository.requests.size)
        }
    }

    @Test
    fun `repository cancellation and unexpected failure both end loading`() {
        val cancellationRepository = FakeChatRepository {
            throw kotlinx.coroutines.CancellationException("cancelled upstream")
        }
        val cancelled = viewModel(cancellationRepository)
        cancelled.onInputChange("question")
        cancelled.send()
        dispatcher.scheduler.runCurrent()
        assertFalse(cancelled.uiState.value.isLoading)
        assertEquals("question", cancelled.uiState.value.failedMessage?.content)
        assertNull(cancelled.uiState.value.error)

        val failed = viewModel(FakeChatRepository { error("unexpected") })
        failed.onInputChange("question")
        failed.send()
        dispatcher.scheduler.runCurrent()
        assertFalse(failed.uiState.value.isLoading)
        assertEquals(ChatUiError.SERVICE, failed.uiState.value.error)
    }

    @Test
    fun `factory creates chat view model with injected repository`() {
        val repository = FakeChatRepository { result("answer") }

        val created = ChatViewModelFactory.create(repository).create(ChatViewModel::class.java)
        created.onInputChange("question")
        created.send()
        dispatcher.scheduler.runCurrent()

        assertEquals(1, repository.requests.size)
        assertEquals(listOf("question", "answer"), created.uiState.value.messages.map { it.content })
    }

    private fun viewModel(repository: ChatRepository) = ChatViewModel(
        repository = repository,
        clock = Clock.fixed(USER_TIME, ZoneOffset.UTC),
        idProvider = { "user-${++nextUserId}" }
    )

    private class FakeChatRepository(
        private val handler: suspend (ChatRequest) -> ChatResult
    ) : ChatRepository {
        val requests = mutableListOf<ChatRequest>()

        override suspend fun send(request: ChatRequest): ChatResult {
            requests += request
            return handler(request)
        }
    }

    private class NonCancellableRepository : ChatRepository {
        private var continuation: Continuation<ChatResult>? = null

        override suspend fun send(request: ChatRequest): ChatResult = suspendCoroutine {
            continuation = it
        }

        fun complete(result: ChatResult) {
            requireNotNull(continuation).resume(result)
            continuation = null
        }
    }

    private companion object {
        val USER_TIME: Instant = Instant.parse("2026-09-13T08:00:00Z")

        fun result(
            content: String,
            id: String = "assistant-id",
            timestamp: Long = 2L
        ) = ChatResult(
            ChatMessage(
                id = id,
                role = ChatRole.ASSISTANT,
                content = content,
                timestampEpochMillis = timestamp
            )
        )
    }
}
