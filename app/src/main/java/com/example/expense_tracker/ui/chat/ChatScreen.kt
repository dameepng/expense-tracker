package com.example.expense_tracker.ui.chat

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.ai.chat.ChatMessage
import com.example.expense_tracker.data.ai.chat.ChatRole
import com.example.expense_tracker.ui.chat.components.ChatBubbleStatus
import com.example.expense_tracker.ui.chat.components.ChatMessageBubble
import com.example.expense_tracker.ui.theme.Expense_trackerTheme

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val leaveScreen = {
        viewModel.cancelRequest()
        onBack()
    }

    BackHandler(onBack = leaveScreen)
    ChatScreenContent(
        state = state,
        onBack = leaveScreen,
        onReset = viewModel::resetSession,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::send,
        onCancelRequest = viewModel::cancelRequest,
        onRetry = viewModel::retryFailedMessage,
        onDiscardFailed = viewModel::discardFailedMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreenContent(
    state: ChatUiState,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancelRequest: () -> Unit,
    onRetry: () -> Unit,
    onDiscardFailed: () -> Unit
) {
    val listState = rememberLazyListState()
    var followsLatest by remember { mutableStateOf(true) }
    var showScopeInfo by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to listState.canScrollForward }
            .collect { (isScrolling, canScrollForward) ->
                if (isScrolling) followsLatest = !canScrollForward
            }
    }
    LaunchedEffect(
        state.messages.size,
        state.isLoading,
        state.error,
        state.isHistoryTruncated
    ) {
        if (followsLatest) {
            withFrameNanos { }
            val lastIndex = listState.layoutInfo.totalItemsCount - 1
            if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(chatBackgroundBrush())
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.chat_title),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showScopeInfo = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.chat_scope_action)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                ChatInputBar(
                    state = state,
                    onReset = onReset,
                    onInputChange = onInputChange,
                    onSend = onSend
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { contentPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentPadding = PaddingValues(horizontal = 17.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.isHistoryTruncated) {
                    item(key = "history-truncated") {
                        ChatNotice(text = stringResource(R.string.chat_history_truncated))
                    }
                }
                if (state.messages.isEmpty()) {
                    item(key = "empty") {
                        ChatEmptyState(onExampleClick = onInputChange)
                    }
                } else {
                    items(state.messages, key = ChatMessage::id) { message ->
                        val status = when (message.id) {
                            state.pendingMessage?.id -> ChatBubbleStatus.SENDING
                            state.failedMessage?.id -> ChatBubbleStatus.FAILED
                            else -> null
                        }
                        ChatMessageBubble(
                            message = message,
                            userLabel = stringResource(R.string.chat_role_user),
                            assistantLabel = stringResource(R.string.chat_role_assistant),
                            sendingLabel = stringResource(R.string.chat_message_sending),
                            failedLabel = stringResource(R.string.chat_message_failed),
                            status = status
                        )
                    }
                }
                if (state.isLoading) {
                    item(key = "loading") {
                        ChatLoading(onCancel = onCancelRequest)
                    }
                }
                state.error?.let { error ->
                    item(key = "error-${error.name}") {
                        ChatErrorCard(
                            error = error,
                            canRetry = state.canRetry,
                            onRetry = onRetry,
                            onDiscard = onDiscardFailed
                        )
                    }
                }
            }
        }

        if (showScopeInfo) {
            ChatScopeDialog(onDismiss = { showScopeInfo = false })
        }
    }
}

@Composable
private fun ChatScopeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
        title = { Text(text = stringResource(R.string.chat_scope_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.chat_scope_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.chat_transmission_notice),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.chat_detail_limit),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.chat_scope_close))
            }
        }
    )
}

@Composable
private fun ChatNotice(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ChatEmptyState(onExampleClick: (String) -> Unit) {
    val examples = listOf(
        stringResource(R.string.chat_example_food_month),
        stringResource(R.string.chat_example_top_category),
        stringResource(R.string.chat_example_month_comparison),
        stringResource(R.string.chat_example_unusual)
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ChatMessageBubble(
            message = previewMessage(
                id = "assistant-welcome",
                role = ChatRole.ASSISTANT,
                content = stringResource(R.string.chat_welcome)
            ),
            userLabel = stringResource(R.string.chat_role_user),
            assistantLabel = stringResource(R.string.chat_role_assistant),
            sendingLabel = stringResource(R.string.chat_message_sending),
            failedLabel = stringResource(R.string.chat_message_failed),
            status = null
        )
        ChatNotice(text = stringResource(R.string.chat_privacy_summary))
        Text(
            text = stringResource(R.string.chat_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        examples.forEach { example ->
            Surface(
                onClick = { onExampleClick(example) },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 17.dp, vertical = 13.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatLoading(onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Text(
            text = stringResource(R.string.chat_loading),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onCancel) {
            Text(stringResource(R.string.chat_cancel_request))
        }
    }
}

@Composable
private fun ChatErrorCard(
    error: ChatUiError,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDiscard: () -> Unit
) {
    val message = stringResource(error.messageResource())
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.chat_error_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = message, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (canRetry) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDiscard) {
                        Text(stringResource(R.string.chat_discard_failed))
                    }
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.chat_retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    state: ChatUiState,
    onReset: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val inputError = state.error == ChatUiError.INVALID_INPUT ||
        state.error == ChatUiError.INPUT_LIMIT
    val canReset = state.messages.isNotEmpty() || state.inputText.isNotEmpty()

    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Surface(
                onClick = onReset,
                enabled = canReset,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 5.dp
            ) {
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.chat_reset_session),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(29.dp),
                shadowElevation = 5.dp,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(start = 17.dp, top = 4.dp, end = 5.dp, bottom = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BasicTextField(
                            value = state.inputText,
                            onValueChange = onInputChange,
                            enabled = state.failedMessage == null,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            minLines = 1,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (state.canSend) {
                                        keyboard?.hide()
                                        onSend()
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.heightIn(min = 46.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (state.inputText.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.chat_input_placeholder),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.MicNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Surface(
                            onClick = {
                                keyboard?.hide()
                                onSend()
                            },
                            enabled = state.canSend,
                            shape = CircleShape,
                            color = if (state.canSend) {
                                Color(0xFF202126)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
                            },
                            contentColor = Color.White
                        ) {
                            Box(
                                modifier = Modifier.size(46.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = stringResource(R.string.chat_send),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    if (inputError || state.inputText.length >= 900) {
                        Text(
                            text = stringResource(
                                R.string.chat_character_count,
                                state.inputText.length
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (inputError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(start = 2.dp, end = 8.dp, bottom = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun chatBackgroundBrush(): Brush {
    val colors = if (isSystemInDarkTheme()) {
        listOf(
            Color(0xFF181820),
            Color(0xFF201C2A),
            Color(0xFF2A1F35)
        )
    } else {
        listOf(
            Color(0xFFF6F6FB),
            Color(0xFFF3EDF9),
            Color(0xFFEAD8F8)
        )
    }
    return Brush.verticalGradient(colors)
}

private fun ChatUiError.messageResource(): Int = when (this) {
    ChatUiError.CONFIGURATION -> R.string.chat_error_configuration
    ChatUiError.NETWORK -> R.string.chat_error_network
    ChatUiError.TIMEOUT -> R.string.chat_error_timeout
    ChatUiError.RATE_LIMIT -> R.string.chat_error_rate_limit
    ChatUiError.AUTHENTICATION -> R.string.chat_error_authentication
    ChatUiError.SERVICE -> R.string.chat_error_service
    ChatUiError.INVALID_RESPONSE -> R.string.chat_error_invalid_response
    ChatUiError.DATA_LOADING -> R.string.chat_error_data_loading
    ChatUiError.CONTEXT_LIMIT -> R.string.chat_error_context_limit
    ChatUiError.INVALID_INPUT -> R.string.chat_error_invalid_input
    ChatUiError.INPUT_LIMIT -> R.string.chat_error_input_limit
}

@Preview(name = "Chat Empty - Light", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun EmptyChatPreview() {
    ChatPreview(state = ChatUiState(), darkTheme = false)
}

@Preview(
    name = "Chat Conversation - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ConversationChatPreview() {
    ChatPreview(state = previewConversation(), darkTheme = true)
}

@Preview(name = "Chat Long Text - Large Font", widthDp = 320, heightDp = 700, fontScale = 1.5f)
@Composable
private fun LargeFontChatPreview() {
    ChatPreview(
        state = previewConversation().copy(
            messages = previewConversation().messages + previewMessage(
                "assistant-long",
                ChatRole.ASSISTANT,
                "Pengeluaran bulan berjalan adalah Rp1.250.000 untuk seluruh wallet. Periode ini masih berjalan, sehingga perbandingan dengan bulan lalu perlu dibaca sebagai indikasi sementara."
            )
        ),
        darkTheme = false
    )
}

@Preview(name = "Chat Loading - Keyboard Height", widthDp = 360, heightDp = 420)
@Composable
private fun LoadingChatPreview() {
    val pending = previewMessage("user-pending", ChatRole.USER, "Kategori apa yang paling boros?")
    ChatPreview(
        state = ChatUiState(
            messages = previewConversation().messages + pending,
            isLoading = true,
            pendingMessage = pending
        ),
        darkTheme = false
    )
}

@Preview(name = "Chat Error", widthDp = 360, heightDp = 720)
@Composable
private fun ErrorChatPreview() {
    val failed = previewMessage("user-failed", ChatRole.USER, "Bandingkan dengan bulan lalu")
    ChatPreview(
        state = ChatUiState(
            messages = previewConversation().messages + failed,
            error = ChatUiError.NETWORK,
            failedMessage = failed
        ),
        darkTheme = false
    )
}

@Preview(name = "Chat History Truncated", widthDp = 360, heightDp = 720)
@Composable
private fun TruncatedChatPreview() {
    ChatPreview(
        state = previewConversation().copy(isHistoryTruncated = true),
        darkTheme = false
    )
}

@Composable
private fun ChatPreview(state: ChatUiState, darkTheme: Boolean) {
    Expense_trackerTheme(darkTheme = darkTheme, dynamicColor = false) {
        ChatScreenContent(
            state = state,
            onBack = {},
            onReset = {},
            onInputChange = {},
            onSend = {},
            onCancelRequest = {},
            onRetry = {},
            onDiscardFailed = {}
        )
    }
}

private fun previewConversation() = ChatUiState(
    messages = listOf(
        previewMessage("user-1", ChatRole.USER, "Berapa pengeluaran makan bulan ini?"),
        previewMessage(
            "assistant-1",
            ChatRole.ASSISTANT,
            "Pengeluaran kategori Makanan bulan berjalan adalah Rp450.000 untuk seluruh wallet."
        )
    )
)

private fun previewMessage(id: String, role: ChatRole, content: String) = ChatMessage(
    id = id,
    role = role,
    content = content,
    timestampEpochMillis = 0L
)
