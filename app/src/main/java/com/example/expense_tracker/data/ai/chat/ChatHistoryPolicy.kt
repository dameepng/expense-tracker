package com.example.expense_tracker.data.ai.chat

internal class ChatHistoryPolicy(
    private val maxCompletedPairs: Int = MAX_COMPLETED_PAIRS,
    private val maxCharacters: Int = MAX_HISTORY_CHARACTERS
) {
    init {
        require(maxCompletedPairs > 0) { "Completed pair limit must be positive" }
        require(maxCharacters > 0) { "History character limit must be positive" }
    }

    fun select(history: List<ChatMessage>): List<ChatMessage> {
        val pairs = completedPairs(history).takeLast(maxCompletedPairs).toMutableList()
        var characterCount = pairs.sumOf { (user, assistant) ->
            user.content.length + assistant.content.length
        }
        while (characterCount > maxCharacters && pairs.isNotEmpty()) {
            val (user, assistant) = pairs.removeAt(0)
            characterCount -= user.content.length + assistant.content.length
        }
        return pairs.flatMap { (user, assistant) -> listOf(user, assistant) }
    }

    private fun completedPairs(history: List<ChatMessage>): List<Pair<ChatMessage, ChatMessage>> {
        val pairs = mutableListOf<Pair<ChatMessage, ChatMessage>>()
        var index = 0
        while (index < history.size) {
            val user = history[index]
            val assistant = history.getOrNull(index + 1)
            if (user.role == ChatRole.USER &&
                assistant?.role == ChatRole.ASSISTANT &&
                user.content.isNotBlank() &&
                assistant.content.isNotBlank()
            ) {
                pairs += user to assistant
                index += 2
            } else {
                index += 1
            }
        }
        return pairs
    }

    private companion object {
        const val MAX_COMPLETED_PAIRS = 6
        const val MAX_HISTORY_CHARACTERS = 12_000
    }
}
