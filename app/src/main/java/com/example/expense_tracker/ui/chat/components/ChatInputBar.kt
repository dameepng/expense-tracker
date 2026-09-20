package com.example.expense_tracker.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.theme.spacing

private const val CHAT_INPUT_WARN_THRESHOLD = 900

/**
 * Bottom chat input bar handling text input, new session reset, voice placeholder, send action, and keyboard insets.
 */
@Composable
fun ChatInputBar(
    inputText: String,
    canSend: Boolean,
    isFailed: Boolean,
    hasInputError: Boolean,
    onReset: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val showSendAction = inputText.isNotEmpty()

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val baseStyle = MaterialTheme.typography.bodyLarge
    val inputTextStyle = remember(baseStyle, onSurfaceColor) {
        baseStyle.copy(color = onSurfaceColor)
    }
    val inputShape = remember { RoundedCornerShape(24.dp) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = spacing.space150, vertical = spacing.space100),
        verticalArrangement = Arrangement.spacedBy(spacing.space50)
    ) {
        BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            enabled = !isFailed,
            textStyle = inputTextStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            minLines = 1,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, inputShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, inputShape)
                        .padding(start = spacing.space50, end = spacing.space125, top = spacing.space100, bottom = spacing.space100),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChatResetSessionButton(onReset = onReset)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = spacing.space100),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.chat_input_placeholder),
                                style = inputTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }

                    ChatActionButtons(
                        showSendAction = showSendAction,
                        canSend = canSend,
                        onSend = onSend
                    )
                }
            }
        )

        ChatCharacterCounter(
            charCount = inputText.length,
            visible = hasInputError || inputText.length >= CHAT_INPUT_WARN_THRESHOLD,
            hasInputError = hasInputError
        )
    }
}

@Composable
private fun ChatResetSessionButton(
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onReset,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.chat_new_session),
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun ChatActionButtons(
    showSendAction: Boolean,
    canSend: Boolean,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val spacing = MaterialTheme.spacing

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.space200),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.MicNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Surface(
            shape = CircleShape,
            color = if (showSendAction && canSend) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            contentColor = if (showSendAction && canSend) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ) {
            IconButton(
                onClick = {
                    keyboard?.hide()
                    onSend()
                },
                enabled = showSendAction && canSend,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (showSendAction) {
                        Icons.Default.ArrowUpward
                    } else {
                        Icons.Rounded.GraphicEq
                    },
                    contentDescription = stringResource(
                        if (showSendAction) R.string.chat_send
                        else R.string.chat_record_audio
                    ),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatCharacterCounter(
    charCount: Int,
    visible: Boolean,
    hasInputError: Boolean,
    modifier: Modifier = Modifier
) {
    if (visible) {
        Text(
            text = stringResource(R.string.chat_character_count, charCount),
            style = MaterialTheme.typography.labelSmall,
            color = if (hasInputError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = modifier.padding(horizontal = 16.dp)
        )
    }
}
