package com.example.expense_tracker.ui.chat

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.ai.chat.ChatMessage
import com.example.expense_tracker.data.ai.chat.ChatRole
import com.example.expense_tracker.ui.chat.components.ChatBubbleStatus
import com.example.expense_tracker.ui.chat.components.ChatEmptyState
import com.example.expense_tracker.ui.chat.components.ChatErrorCard
import com.example.expense_tracker.ui.chat.components.ChatInputBar
import com.example.expense_tracker.ui.chat.components.ChatMessageBubble
import com.example.expense_tracker.ui.chat.components.ChatScopeDialog
import com.example.expense_tracker.ui.theme.Expense_trackerTheme
import com.example.expense_tracker.ui.theme.spacing
import kotlinx.coroutines.launch

private val MAX_CHAT_SCREEN_WIDTH = 768.dp

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val leaveScreen = {
        keyboard?.hide()
        focusManager.clearFocus()
        viewModel.cancelRequest()
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelRequest()
            keyboard?.hide()
            focusManager.clearFocus()
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ChatScreenContent(
    state: ChatUiState,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancelRequest: () -> Unit,
    onRetry: () -> Unit,
    onDiscardFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    )
    val coroutineScope = rememberCoroutineScope()

    // Status apakah user sedang berada di posisi paling bawah chat (pesan terbaru).
    // Pada reverseLayout = true, item index 0 adalah posisi visual paling bawah.
    val isAtBottom by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    var showScopeInfo by remember { mutableStateOf(false) }

    @Suppress("OPT_IN_USAGE")
    val keyboardVisible = WindowInsets.isImeVisible

    // 1. Auto-scroll saat keyboard dibuka:
    LaunchedEffect(keyboardVisible) {
        if (keyboardVisible && isAtBottom && state.messages.isNotEmpty()) {
            withFrameNanos { } // Tunggu 1 frame agar layoutInfo viewport selesai diukur ulang
            listState.animateScrollToItem(0)
        }
    }

    // 2. Auto-scroll saat pesan baru masuk, loading, atau error:
    LaunchedEffect(
        state.messages.size,
        state.isLoading,
        state.error,
        state.isHistoryTruncated
    ) {
        if (isAtBottom && state.messages.isNotEmpty()) {
            withFrameNanos { } // Tunggu 1 frame agar item baru selesai dikomposisi dan masuk layoutInfo
            listState.animateScrollToItem(0)
        }
    }

    // Saat user sendiri yang mengirim pesan, langsung picu scroll ke pesan terbaru
    val handleSend = remember(onSend, coroutineScope, listState) {
        {
            onSend()
            coroutineScope.launch {
                withFrameNanos { }
                if (listState.layoutInfo.totalItemsCount > 0) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = MAX_CHAT_SCREEN_WIDTH)
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                ChatTopAppBar(
                    scrollBehavior = topAppBarScrollBehavior,
                    onBack = onBack,
                    onOpenScopeInfo = { showScopeInfo = true }
                )
            },
            bottomBar = {
                ChatInputBar(
                    inputText = state.inputText,
                    canSend = state.canSend,
                    isFailed = state.failedMessage != null,
                    hasInputError = state.error == ChatUiError.INVALID_INPUT ||
                        state.error == ChatUiError.INPUT_LIMIT,
                    onReset = onReset,
                    onInputChange = onInputChange,
                    onSend = { handleSend() }
                )
            },
            containerColor = Color.Transparent
        ) { contentPadding ->
            ChatMessagesList(
                listState = listState,
                state = state,
                contentPadding = contentPadding,
                onExampleClick = onInputChange,
                onCancelRequest = onCancelRequest,
                onRetry = onRetry,
                onDiscardFailed = onDiscardFailed
            )
        }

        if (showScopeInfo) {
            ChatScopeDialog(onDismiss = { showScopeInfo = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
    onOpenScopeInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.chat_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            IconButton(onClick = onOpenScopeInfo) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.chat_scope_action)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        windowInsets = WindowInsets(0, 0, 0, 0),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessagesList(
    listState: LazyListState,
    state: ChatUiState,
    contentPadding: PaddingValues,
    onExampleClick: (String) -> Unit,
    onCancelRequest: () -> Unit,
    onRetry: () -> Unit,
    onDiscardFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReversed = state.messages.isNotEmpty()
    val messagesReversed = remember(state.messages) { state.messages.asReversed() }

    val spacing = MaterialTheme.spacing
    LazyColumn(
        state = listState,
        reverseLayout = isReversed,
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding),
        contentPadding = PaddingValues(horizontal = spacing.screenMargin, vertical = spacing.itemGap),
        verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
    ) {
        if (isReversed) {
            // 1. Error card (posisi visual paling bawah jika ada error)
            state.error?.let { err ->
                item(key = "error-${err.name}") {
                    ChatErrorCard(
                        error = err,
                        canRetry = state.canRetry,
                        onRetry = onRetry,
                        onDiscard = onDiscardFailed
                    )
                }
            }

            // 2. Loading indicator
            if (state.isLoading) {
                item(key = "loading") {
                    ChatLoading(onCancel = onCancelRequest)
                }
            }

            // 3. Pesan-pesan dalam urutan terbalik
            items(messagesReversed, key = ChatMessage::id) { message ->
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

            // 4. Notifikasi riwayat terpotong
            truncatedHistoryNoticeItem(state.isHistoryTruncated)
        } else {
            // Layout normal top-to-bottom saat chat masih kosong
            truncatedHistoryNoticeItem(state.isHistoryTruncated)
            item(key = "empty") {
                ChatEmptyState(onExampleClick = onExampleClick)
            }
        }
    }
}

private fun LazyListScope.truncatedHistoryNoticeItem(isHistoryTruncated: Boolean) {
    if (isHistoryTruncated) {
        item(key = "history-truncated") {
            ChatNotice(text = stringResource(R.string.chat_history_truncated))
        }
    }
}

@Composable
private fun ChatNotice(
    text: String,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(
            modifier = Modifier.padding(spacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing.space125),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ChatLoading(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(spacing.space150),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
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
