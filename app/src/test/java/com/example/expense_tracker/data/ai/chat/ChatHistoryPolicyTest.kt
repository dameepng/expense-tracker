package com.example.expense_tracker.data.ai.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatHistoryPolicyTest {
    @Test
    fun `keeps only completed adjacent user assistant pairs`() {
        val history = listOf(
            message(ChatRole.ASSISTANT, "orphan assistant"),
            message(ChatRole.USER, "unanswered user"),
            message(ChatRole.USER, "question one"),
            message(ChatRole.ASSISTANT, "answer one"),
            message(ChatRole.USER, "question two"),
            message(ChatRole.ASSISTANT, "answer two"),
            message(ChatRole.USER, "pending")
        )

        val selected = ChatHistoryPolicy().select(history)

        assertEquals(
            listOf("question one", "answer one", "question two", "answer two"),
            selected.map { it.content }
        )
        assertEquals(
            listOf(ChatRole.USER, ChatRole.ASSISTANT, ChatRole.USER, ChatRole.ASSISTANT),
            selected.map { it.role }
        )
    }

    @Test
    fun `keeps the six newest complete pairs in chronological order`() {
        val history = (1..8).flatMap { index ->
            listOf(
                message(ChatRole.USER, "user-$index"),
                message(ChatRole.ASSISTANT, "assistant-$index")
            )
        }

        val selected = ChatHistoryPolicy().select(history)

        assertEquals(12, selected.size)
        assertEquals("user-3", selected.first().content)
        assertEquals("assistant-8", selected.last().content)
    }

    @Test
    fun `removes oldest whole pairs until history fits character budget`() {
        val history = listOf(
            message(ChatRole.USER, "aaaa"),
            message(ChatRole.ASSISTANT, "bb"),
            message(ChatRole.USER, "ccc"),
            message(ChatRole.ASSISTANT, "ddd")
        )

        val selected = ChatHistoryPolicy(maxCharacters = 10).select(history)

        assertEquals(listOf("ccc", "ddd"), selected.map { it.content })
        assertEquals(6, selected.sumOf { it.content.length })
    }

    @Test
    fun `omits history when newest pair alone exceeds character budget`() {
        val selected = ChatHistoryPolicy(maxCharacters = 4).select(
            listOf(
                message(ChatRole.USER, "three"),
                message(ChatRole.ASSISTANT, "three")
            )
        )

        assertTrue(selected.isEmpty())
    }

    @Test
    fun `default policy enforces twelve thousand history characters`() {
        val exactlyAtLimit = listOf(
            message(ChatRole.USER, "u".repeat(6_000)),
            message(ChatRole.ASSISTANT, "a".repeat(6_000))
        )
        val overLimit = listOf(
            message(ChatRole.USER, "u".repeat(6_000)),
            message(ChatRole.ASSISTANT, "a".repeat(6_001))
        )

        assertEquals(2, ChatHistoryPolicy().select(exactlyAtLimit).size)
        assertTrue(ChatHistoryPolicy().select(overLimit).isEmpty())
    }

    private fun message(role: ChatRole, content: String) = ChatMessage(
        id = "$role-$content",
        role = role,
        content = content,
        timestampEpochMillis = 1L
    )
}
