package com.example.expense_tracker.ui.ai

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.theme.spacing

private val MAX_NL_SCREEN_WIDTH = 640.dp
private const val MAX_INPUT_CHAR_COUNT = 1000
private const val SPEECH_RECOGNITION_LANGUAGE = "id-ID"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaturalLanguageScreen(
    viewModel: NaturalLanguageViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currentOnSaved by rememberUpdatedState(onSaved)
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val handleBack = {
        keyboard?.hide()
        focusManager.clearFocus()
        viewModel.cancelParsing()
        onBack()
    }

    LaunchedEffect(state.saved) {
        if (state.saved) currentOnSaved()
    }

    // Only intercept back gesture when saving to prevent corrupting state/accidental back during persistence
    BackHandler(enabled = state.isSaving) {
        // Prevent back during save
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelParsing()
            keyboard?.hide()
            focusManager.clearFocus()
        }
    }

    val micPrompt = stringResource(R.string.nl_quick_mic_prompt)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onInputChange(spokenText)
            }
        }
    }

    val launchSpeech = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, SPEECH_RECOGNITION_LANGUAGE)
            putExtra(RecognizerIntent.EXTRA_PROMPT, micPrompt)
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {}
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    NaturalLanguageContent(
        state = state,
        scrollBehavior = scrollBehavior,
        onBack = handleBack,
        onInputChange = viewModel::onInputChange,
        onLaunchSpeech = launchSpeech,
        onParse = {
            keyboard?.hide()
            viewModel.parse()
        },
        onCancelParsing = viewModel::cancelParsing,
        onRetryLoad = viewModel::retryLoad,
        onUpdateDraft = viewModel::updateDraft,
        onRemoveDraft = viewModel::removeDraft,
        onSave = {
            keyboard?.hide()
            viewModel.save()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NaturalLanguageContent(
    state: NaturalLanguageUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onLaunchSpeech: () -> Unit,
    onParse: () -> Unit,
    onCancelParsing: () -> Unit,
    onRetryLoad: () -> Unit,
    onUpdateDraft: (Int, (TransactionDraft) -> TransactionDraft) -> Unit,
    onRemoveDraft: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val editingEnabled = !state.isParsing && !state.isSaving && !state.saved

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NaturalLanguageTopAppBar(
                isSaving = state.isSaving,
                scrollBehavior = scrollBehavior,
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = MAX_NL_SCREEN_WIDTH)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenMargin, vertical = spacing.itemGap),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
            ) {
                Text(
                    text = stringResource(R.string.ai_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NaturalLanguagePromptCard(
                    inputText = state.inputText,
                    canParse = state.canParse,
                    isParsing = state.isParsing,
                    hasDraftsOrError = state.drafts.isNotEmpty() || state.error != null,
                    editingEnabled = editingEnabled,
                    onInputChange = onInputChange,
                    onLaunchSpeech = onLaunchSpeech,
                    onParse = onParse,
                    onCancelParsing = onCancelParsing
                )

                NaturalLanguageTrustCard()

                NaturalLanguageEmptyState(
                    isInitializing = state.isInitializing,
                    error = state.error,
                    walletsEmpty = state.wallets.isEmpty(),
                    categoriesEmpty = state.categories.isEmpty(),
                    editingEnabled = editingEnabled,
                    onRetryLoad = onRetryLoad
                )

                state.error?.let { error ->
                    AiErrorCard(error = error, onRetryLoad = onRetryLoad)
                }

                NaturalLanguageDraftPreviewSection(
                    state = state,
                    editingEnabled = editingEnabled,
                    onUpdateDraft = onUpdateDraft,
                    onRemoveDraft = onRemoveDraft,
                    onSave = onSave
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NaturalLanguageTopAppBar(
    isSaving: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.ai_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(
                enabled = !isSaving,
                onClick = onBack
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        windowInsets = WindowInsets(0, 0, 0, 0),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun NaturalLanguagePromptCard(
    inputText: String,
    canParse: Boolean,
    isParsing: Boolean,
    hasDraftsOrError: Boolean,
    editingEnabled: Boolean,
    onInputChange: (String) -> Unit,
    onLaunchSpeech: () -> Unit,
    onParse: () -> Unit,
    onCancelParsing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.space150)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                enabled = editingEnabled,
                label = { Text(stringResource(R.string.ai_sentence_label)) },
                placeholder = {
                    Text(
                        stringResource(R.string.ai_sentence_example),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                minLines = 4,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            PromptInputToolbar(
                inputTextLength = inputText.length,
                showClearButton = inputText.isNotBlank() && editingEnabled,
                editingEnabled = editingEnabled,
                onClearText = { onInputChange("") },
                onLaunchSpeech = onLaunchSpeech
            )

            PromptParseButton(
                canParse = canParse,
                isParsing = isParsing,
                hasDraftsOrError = hasDraftsOrError,
                onParse = onParse
            )

            if (isParsing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancelParsing) {
                        Text(stringResource(R.string.ai_cancel_parsing))
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptInputToolbar(
    inputTextLength: Int,
    showClearButton: Boolean,
    editingEnabled: Boolean,
    onClearText: () -> Unit,
    onLaunchSpeech: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "$inputTextLength / $MAX_INPUT_CHAR_COUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            if (showClearButton) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClearText()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.ai_clear_tooltip),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLaunchSpeech()
                },
                enabled = editingEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.ai_mic_tooltip),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PromptParseButton(
    canParse: Boolean,
    isParsing: Boolean,
    hasDraftsOrError: Boolean,
    onParse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onParse()
        },
        enabled = canParse,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isParsing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                stringResource(
                    when {
                        isParsing -> R.string.ai_parsing
                        hasDraftsOrError -> R.string.ai_parse_again
                        else -> R.string.ai_parse
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NaturalLanguageTrustCard(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ai_trust_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.ai_trust_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun NaturalLanguageEmptyState(
    isInitializing: Boolean,
    error: AiUiError?,
    walletsEmpty: Boolean,
    categoriesEmpty: Boolean,
    editingEnabled: Boolean,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isInitializing || error == AiUiError.INITIALIZATION) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (walletsEmpty) {
            Text(
                stringResource(R.string.ai_no_wallets),
                color = MaterialTheme.colorScheme.error
            )
        }
        if (categoriesEmpty) {
            Text(
                stringResource(R.string.ai_no_categories),
                color = MaterialTheme.colorScheme.error
            )
            TextButton(
                onClick = onRetryLoad,
                enabled = editingEnabled
            ) {
                Text(stringResource(R.string.ai_retry_load))
            }
        }
    }
}
