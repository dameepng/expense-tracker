package com.example.expense_tracker.ui.chat

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
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
    onDiscardFailed: () -> Unit
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
    // Dengan reverseLayout = true, item index 0 secara natural terkunci di tepi bawah saat keyboard naik.
    // Jika user sedang berada di paling bawah (isAtBottom), kita pastikan index 0 tetap tampil sempurna.
    LaunchedEffect(keyboardVisible) {
        if (keyboardVisible && isAtBottom && state.messages.isNotEmpty()) {
            withFrameNanos { } // Tunggu 1 frame agar layoutInfo viewport selesai diukur ulang
            listState.animateScrollToItem(0)
        }
    }

    // 2. Auto-scroll saat pesan baru masuk, loading, atau error:
    // HANYA dipicu jika user memang sedang berada di paling bawah (isAtBottom).
    // Jika user sedang scroll ke atas membaca histori, posisinya tidak akan ditarik paksa ke bawah.
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            // WindowInsets(0): Matikan inset handling otomatis Scaffold.
            // Alasan: Jika Scaffold mengurus insets secara otomatis, bottomBar atau contentPadding
            // akan sering mendapat dobel inset (misalnya IME + navigationBars dijumlahkan dua kali).
            // Dengan WindowInsets(0, 0, 0, 0), kita memegang kendali penuh:
            // - TopAppBar menangani statusBars secara internal
            // - ChatInputBar menangani imePadding() & navigationBarsPadding() sendiri secara terisolasi
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.chat_title),
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
                        IconButton(onClick = { showScopeInfo = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.chat_scope_action)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    scrollBehavior = topAppBarScrollBehavior
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
                messages = state.messages,
                pendingMessageId = state.pendingMessage?.id,
                failedMessageId = state.failedMessage?.id,
                isLoading = state.isLoading,
                error = state.error,
                canRetry = state.canRetry,
                isHistoryTruncated = state.isHistoryTruncated,
                onExampleClick = onInputChange,
                onCancelRequest = onCancelRequest,
                onRetry = onRetry,
                onDiscardFailed = onDiscardFailed,
                contentPadding = contentPadding
            )
        }

        if (showScopeInfo) {
            ChatScopeDialog(onDismiss = { showScopeInfo = false })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessagesList(
    listState: LazyListState,
    messages: List<ChatMessage>,
    pendingMessageId: String?,
    failedMessageId: String?,
    isLoading: Boolean,
    error: ChatUiError?,
    canRetry: Boolean,
    isHistoryTruncated: Boolean,
    onExampleClick: (String) -> Unit,
    onCancelRequest: () -> Unit,
    onRetry: () -> Unit,
    onDiscardFailed: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    // Pola Standar Chat (WhatsApp/Telegram):
    // Ketika ada pesan, gunakan reverseLayout = true dengan list dibalik (newest item di index 0).
    // Keuntungan besar:
    // 1. Pesan terbaru "auto-nempel" di bawah secara gratis tanpa perlu kalkulasi scroll manual.
    // 2. Saat keyboard (IME) muncul dan resize viewport, posisi item 0 tetap diam di atas input bar.
    // Saat pesan kosong, gunakan reverseLayout = false agar layout Empty State (welcome card & suggestions)
    // mengalir secara wajar dari atas ke bawah.
    val isReversed = messages.isNotEmpty()
    val messagesReversed = remember(messages) { messages.asReversed() }

    LazyColumn(
        state = listState,
        reverseLayout = isReversed,
        modifier = modifier
            .fillMaxSize()
            // padding(contentPadding): Meneruskan padding Scaffold (topBar & bottomBar height)
            .padding(contentPadding)
            // consumeWindowInsets: Memberitahu Compose insets ini sudah dikonsumsi agar tidak dihitung ganda
            .consumeWindowInsets(contentPadding),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isReversed) {
            // Karena reverseLayout = true, indeks 0 digambar di posisi visual paling bawah.
            // 1. Error card (posisi visual paling bawah jika ada error)
            error?.let { err ->
                item(key = "error-${err.name}") {
                    ChatErrorCard(
                        error = err,
                        canRetry = canRetry,
                        onRetry = onRetry,
                        onDiscard = onDiscardFailed
                    )
                }
            }

            // 2. Loading indicator (posisi visual di bawah pesan terakhir, menunggu jawaban AI)
            if (isLoading) {
                item(key = "loading") {
                    ChatLoading(onCancel = onCancelRequest)
                }
            }

            // 3. Pesan-pesan dalam urutan terbalik:
            // messagesReversed[0] (pesan terbaru) digambar di bawah,
            // messagesReversed[last] (pesan terlama) digambar di atas.
            items(messagesReversed, key = ChatMessage::id) { message ->
                val status = when (message.id) {
                    pendingMessageId -> ChatBubbleStatus.SENDING
                    failedMessageId -> ChatBubbleStatus.FAILED
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

            // 4. Notifikasi riwayat terpotong:
            // Diletakkan di indeks terakhir agar tampil di posisi visual paling atas (sebelum pesan terlama).
            if (isHistoryTruncated) {
                item(key = "history-truncated") {
                    ChatNotice(text = stringResource(R.string.chat_history_truncated))
                }
            }
        } else {
            // Layout normal top-to-bottom saat chat masih kosong
            if (isHistoryTruncated) {
                item(key = "history-truncated") {
                    ChatNotice(text = stringResource(R.string.chat_history_truncated))
                }
            }
            item(key = "empty") {
                ChatEmptyState(onExampleClick = onExampleClick)
            }
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
        color = MaterialTheme.colorScheme.surfaceContainer,
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
    val exFood = stringResource(R.string.chat_example_food_month)
    val exTop = stringResource(R.string.chat_example_top_category)
    val exComp = stringResource(R.string.chat_example_month_comparison)
    val exUnusual = stringResource(R.string.chat_example_unusual)
    val examples = remember(exFood, exTop, exComp, exUnusual) {
        listOf(exFood, exTop, exComp, exUnusual)
    }
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
                color = MaterialTheme.colorScheme.surfaceContainer,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatInputBar(
    inputText: String,
    canSend: Boolean,
    isFailed: Boolean,
    hasInputError: Boolean,
    onReset: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val showSendAction = inputText.isNotEmpty()

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val baseStyle = MaterialTheme.typography.bodyLarge
    val inputTextStyle = remember(baseStyle, onSurfaceColor) {
        baseStyle.copy(color = onSurfaceColor)
    }
    val inputShape = remember { RoundedCornerShape(24.dp) }

    // Best Practice Insets (Google Jetchat Sample):
    // Modifier.imePadding().navigationBarsPadding() dipasang langsung pada root composable input bar.
    // - Saat keyboard tertutup: imePadding() = 0, navigationBarsPadding() memberi ruang pas di atas gesture pill / 3-button nav.
    // - Saat keyboard terbuka: imePadding() mengangkat bar setinggi keyboard. navigationBars yang berada di balik keyboard
    //   otomatis ter-consume sehingga navigationBarsPadding() tidak menambahkan padding ekstra (bebas celah/gap ganda).
    // - Hindari menghitung WindowInsets.navigationBars.asPaddingValues() secara manual karena rawan salah hitung.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        .padding(start = 4.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onReset, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.chat_new_session),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            }
        )
        if (hasInputError || inputText.length >= 900) {
            Text(
                text = stringResource(R.string.chat_character_count, inputText.length),
                style = MaterialTheme.typography.labelSmall,
                color = if (hasInputError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
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
