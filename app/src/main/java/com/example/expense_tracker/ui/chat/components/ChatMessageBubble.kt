package com.example.expense_tracker.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.data.ai.chat.ChatMessage
import com.example.expense_tracker.data.ai.chat.ChatRole
import com.example.expense_tracker.ui.theme.spacing

internal enum class ChatBubbleStatus {
    SENDING,
    FAILED
}

@Composable
internal fun ChatMessageBubble(
    message: ChatMessage,
    userLabel: String,
    assistantLabel: String,
    sendingLabel: String,
    failedLabel: String,
    status: ChatBubbleStatus?,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == ChatRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val arrangement = if (isUser) Arrangement.End else Arrangement.Start
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    } else {
        RoundedCornerShape(topStart = 6.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    }
    val spacing = MaterialTheme.spacing
    val roleLabel = if (isUser) userLabel else assistantLabel

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$roleLabel: ${message.content}"
            },
        horizontalArrangement = arrangement
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = bubbleShape,
            border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.82f else 0.88f)
                .clip(bubbleShape)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = spacing.space200, vertical = spacing.space150),
                horizontalAlignment = alignment,
                verticalArrangement = Arrangement.spacedBy(spacing.space50)
            ) {
                SelectionContainer {
                    if (isUser) {
                        // Bubble user menggunakan Text biasa (plain text) tanpa markdown parsing
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Bubble assistant dirender dengan parser markdown native Compose
                        AssistantMarkdownText(
                            markdown = message.content,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                status?.let {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (it == ChatBubbleStatus.FAILED) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (it == ChatBubbleStatus.SENDING) sendingLabel else failedLabel,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
