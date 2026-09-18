package com.example.expense_tracker.ui.chat

import androidx.lifecycle.ViewModel
import com.example.expense_tracker.data.ai.chat.ChatMessage
import com.example.expense_tracker.data.ai.chat.ChatRepository
import com.example.expense_tracker.data.ai.chat.ChatRequest
import com.example.expense_tracker.data.ai.chat.ChatResult
import com.example.expense_tracker.data.ai.chat.ChatRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatUiStateTest {

    private val dummyMessage = ChatMessage(
        id = "msg-1",
        role = ChatRole.USER,
        content = "Halo",
        timestampEpochMillis = 1000L
    )

    @Test
    fun `canSend returns true only when input is valid, not loading, and no failed message`() {
        val defaultState = ChatUiState()
        assertFalse("Default state with empty input cannot send", defaultState.canSend)

        val stateWithText = defaultState.copy(inputText = "Berapa pengeluaran saya?")
        assertTrue("State with valid input text can send", stateWithText.canSend)

        val loadingState = stateWithText.copy(isLoading = true)
        assertFalse("Loading state cannot send", loadingState.canSend)

        val failedState = stateWithText.copy(failedMessage = dummyMessage)
        assertFalse("State with failed message cannot send until retried or discarded", failedState.canSend)

        val blankState = defaultState.copy(inputText = "   ")
        assertFalse("Blank input cannot send", blankState.canSend)

        val oversizedState = defaultState.copy(inputText = "a".repeat(ChatUiState.MAX_INPUT_CHARACTERS + 1))
        assertFalse("Oversized input cannot send", oversizedState.canSend)
    }

    @Test
    fun `canRetry returns true only when failed message exists and not loading`() {
        val defaultState = ChatUiState()
        assertFalse(defaultState.canRetry)

        val failedState = defaultState.copy(failedMessage = dummyMessage)
        assertTrue(failedState.canRetry)

        val loadingFailedState = failedState.copy(isLoading = true)
        assertFalse(loadingFailedState.canRetry)
    }

    @Test
    fun `MAX_INPUT_CHARACTERS is 1000`() {
        assertEquals(1_000, ChatUiState.MAX_INPUT_CHARACTERS)
    }

    @Test
    fun `ChatViewModelFactory creates ChatViewModel successfully`() {
        val fakeRepo = object : ChatRepository {
            override suspend fun send(request: ChatRequest): ChatResult {
                throw UnsupportedOperationException()
            }
        }
        val factory = ChatViewModelFactory.create(fakeRepo)
        val viewModel = factory.create(ChatViewModel::class.java)

        assertNotNull(viewModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ChatViewModelFactory throws IllegalArgumentException for unknown class`() {
        val fakeRepo = object : ChatRepository {
            override suspend fun send(request: ChatRequest): ChatResult {
                throw UnsupportedOperationException()
            }
        }
        val factory = ChatViewModelFactory.create(fakeRepo)
        class UnknownViewModel : ViewModel()

        factory.create(UnknownViewModel::class.java)
    }
}
