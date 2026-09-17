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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.ui.theme.spacing
import java.time.Instant
import java.time.ZoneId

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
    val haptic = LocalHapticFeedback.current
    val editingEnabled = !state.isParsing && !state.isSaving && !state.saved

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
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PROMPT, micPrompt)
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {}
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ai_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        enabled = !state.isSaving,
                        onClick = handleBack
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
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val spacing = MaterialTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

            // ── Prompt Container Card ──
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(spacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.space150)
                ) {
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = viewModel::onInputChange,
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

                    // Integrated toolbar: character counter, clear button, and speech mic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = "${state.inputText.length} / 1000",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            if (state.inputText.isNotBlank() && editingEnabled) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.onInputChange("")
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

                        // Speech microphone button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    launchSpeech()
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

                    // Primary Parse Button
                    Button(
                        onClick = {
                            keyboard?.hide()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.parse()
                        },
                        enabled = state.canParse,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isParsing) {
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
                                        state.isParsing -> R.string.ai_parsing
                                        state.draft != null || state.error != null -> R.string.ai_parse_again
                                        else -> R.string.ai_parse
                                    }
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Cancel parsing button if in progress
                    if (state.isParsing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = viewModel::cancelParsing) {
                                Text(stringResource(R.string.ai_cancel_parsing))
                            }
                        }
                    }
                }
            }

            // ── Discreet Trust & Privacy Card ──
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
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

            if (!state.isInitializing && state.error != AiUiError.INITIALIZATION) {
                if (state.wallets.isEmpty()) {
                    Text(
                        stringResource(R.string.ai_no_wallets),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (state.categories.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.ai_no_categories),
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(
                            onClick = viewModel::retryLoad,
                            enabled = editingEnabled
                        ) {
                            Text(stringResource(R.string.ai_retry_load))
                        }
                    }
                }
            }

            state.error?.let { error ->
                AiErrorCard(error = error, onRetryLoad = viewModel::retryLoad)
            }

            state.draft?.let { draft ->
                TransactionPreview(
                    state = state,
                    draft = draft,
                    enabled = editingEnabled,
                    onDraftChange = viewModel::updateDraft,
                    onSave = {
                        keyboard?.hide()
                        viewModel.save()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionPreview(
    state: NaturalLanguageUiState,
    draft: TransactionDraft,
    enabled: Boolean,
    onDraftChange: ((TransactionDraft) -> TransactionDraft) -> Unit,
    onSave: () -> Unit
) {
    val previewStart = remember { BringIntoViewRequester() }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        withFrameNanos { }
        previewStart.bringIntoView()
    }
    val spacing = MaterialTheme.spacing
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(previewStart)
    ) {
        Column(
            modifier = Modifier.padding(spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
        ) {
            Text(
                stringResource(R.string.ai_preview_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.ai_preview_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Segmented Button (Pengeluaran vs Pemasukan)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val isExpense = draft.type == TransactionType.EXPENSE.name
                SegmentedButton(
                    selected = isExpense,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDraftChange { it.copy(type = TransactionType.EXPENSE.name) }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    enabled = enabled
                ) {
                    Text(stringResource(R.string.transaction_expense))
                }
                SegmentedButton(
                    selected = !isExpense,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDraftChange { it.copy(type = TransactionType.INCOME.name) }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    enabled = enabled
                ) {
                    Text(stringResource(R.string.transaction_income))
                }
            }

            // Amount field with prefix
            OutlinedTextField(
                value = draft.amountText,
                onValueChange = { text -> onDraftChange { it.copy(amountText = text) } },
                enabled = enabled,
                label = { Text(stringResource(R.string.ai_amount)) },
                prefix = {
                    Text(
                        "Rp ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                supportingText = { Text(stringResource(R.string.ai_amount_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category choice dropdown
            val categoryChoices = state.categories
                .filter { it.type == draft.type || it.type == "BOTH" }
                .map { it.id to it.name }
            AiChoiceDropdown(
                label = stringResource(R.string.category),
                selectedId = draft.categoryId,
                choices = categoryChoices,
                enabled = enabled,
                onSelect = { id -> onDraftChange { it.copy(categoryId = id) } }
            )

            // Wallet choice dropdown
            AiChoiceDropdown(
                label = stringResource(R.string.input_choose_wallet),
                selectedId = draft.walletId,
                choices = state.wallets.map { it.id to it.name },
                enabled = enabled,
                onSelect = { id -> onDraftChange { it.copy(walletId = id) } }
            )

            // Merchant field
            OutlinedTextField(
                value = draft.merchant,
                onValueChange = { text -> onDraftChange { it.copy(merchant = text) } },
                enabled = enabled,
                label = { Text(stringResource(R.string.ai_merchant)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Date picker
            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                val initialMillis = try {
                    draft.parsedDate()?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli()
                        ?: System.currentTimeMillis()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                                onDraftChange { it.copy(dateText = selectedDate.toString()) }
                            }
                            showDatePicker = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = draft.dateText,
                onValueChange = { text -> onDraftChange { it.copy(dateText = text) } },
                enabled = enabled,
                label = { Text(stringResource(R.string.ai_date)) },
                supportingText = { Text(stringResource(R.string.ai_date_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.choose_date)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Note field
            OutlinedTextField(
                value = draft.note,
                onValueChange = { text -> onDraftChange { it.copy(note = text) } },
                enabled = enabled,
                label = { Text(stringResource(R.string.ai_note)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Recurring toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = draft.isRecurring,
                        enabled = enabled,
                        role = Role.Switch,
                        onValueChange = { checked ->
                            onDraftChange { it.copy(isRecurring = checked) }
                        }
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.ai_recurring),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.ai_recurring_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = draft.isRecurring, onCheckedChange = null, enabled = enabled)
                }
            }

            if (!state.canSave && !state.isSaving && !state.isParsing && !state.saved) {
                Text(
                    stringResource(R.string.ai_validation_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Confirm & Save Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave()
                },
                enabled = state.canSave,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        stringResource(
                            if (state.isSaving) R.string.ai_saving else R.string.ai_confirm_save
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiChoiceDropdown(
    label: String,
    selectedId: Long?,
    choices: List<Pair<Long, String>>,
    enabled: Boolean,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectionEnabled = enabled && choices.isNotEmpty()
    ExposedDropdownMenuBox(
        expanded = expanded && selectionEnabled,
        onExpandedChange = { expanded = it && selectionEnabled }
    ) {
        OutlinedTextField(
            value = choices.firstOrNull { it.first == selectedId }?.second.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = selectionEnabled,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && selectionEnabled)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, selectionEnabled)
        )
        ExposedDropdownMenu(
            expanded = expanded && selectionEnabled,
            onDismissRequest = { expanded = false }
        ) {
            choices.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AiErrorCard(error: AiUiError, onRetryLoad: () -> Unit) {
    val spacing = MaterialTheme.spacing
    val message = when (error) {
        AiUiError.CONFIGURATION -> R.string.ai_error_configuration
        AiUiError.NETWORK -> R.string.ai_error_network
        AiUiError.TIMEOUT -> R.string.ai_error_timeout
        AiUiError.RATE_LIMIT -> R.string.ai_error_rate_limit
        AiUiError.AUTHENTICATION -> R.string.ai_error_authentication
        AiUiError.SERVICE -> R.string.ai_error_service
        AiUiError.INVALID_RESPONSE -> R.string.ai_error_invalid_response
        AiUiError.INVALID_INPUT -> R.string.ai_error_invalid_input
        AiUiError.INITIALIZATION -> R.string.ai_error_initialization
        AiUiError.VALIDATION -> R.string.ai_validation_hint
        AiUiError.SAVE -> R.string.ai_error_save
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(
            modifier = Modifier.padding(spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.space100)
        ) {
            Text(stringResource(message), style = MaterialTheme.typography.bodyMedium)
            if (error == AiUiError.INITIALIZATION) {
                TextButton(onClick = onRetryLoad) {
                    Text(stringResource(R.string.ai_retry_load))
                }
            }
        }
    }
}
