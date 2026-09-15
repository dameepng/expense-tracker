package com.example.expense_tracker.ui.ai

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.expense_tracker.data.TransactionType
import androidx.compose.runtime.Composable
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
    val editingEnabled = !state.isParsing && !state.isSaving && !state.saved

    LaunchedEffect(state.saved) {
        if (state.saved) currentOnSaved()
    }
    // Keep the form in place while the confirmed transaction is being committed.
    BackHandler(enabled = state.isSaving) {}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.ai_title), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(
                        enabled = !state.isSaving,
                        onClick = {
                            viewModel.cancelParsing()
                            onBack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                stringResource(R.string.ai_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = viewModel::onInputChange,
                        enabled = editingEnabled,
                        label = { Text(stringResource(R.string.ai_sentence_label)) },
                        placeholder = { Text(stringResource(R.string.ai_sentence_example)) },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        supportingText = {
                            Text(stringResource(R.string.ai_character_count, state.inputText.length))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        stringResource(R.string.ai_transmission_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            keyboard?.hide()
                            viewModel.parse()
                        },
                        enabled = state.canParse,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
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
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            }
                            Text(
                                stringResource(
                                    when {
                                        state.isParsing -> R.string.ai_parsing
                                        state.draft != null || state.error != null -> R.string.ai_parse_again
                                        else -> R.string.ai_parse
                                    }
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (state.isParsing) {
                        TextButton(
                            onClick = viewModel::cancelParsing,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.ai_cancel_parsing))
                        }
                    } else if (state.draft == null) {
                        TextButton(
                            onClick = viewModel::startManualEntry,
                            enabled = editingEnabled && !state.isInitializing,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.ai_manual_entry))
                        }
                    }
                }
            }

            if (state.isInitializing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(stringResource(R.string.ai_loading_choices))
                }
            } else if (state.error != AiUiError.INITIALIZATION) {
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

@Composable
private fun TransactionPreview(
    state: NaturalLanguageUiState,
    draft: TransactionDraft,
    enabled: Boolean,
    onDraftChange: ((TransactionDraft) -> TransactionDraft) -> Unit,
    onSave: () -> Unit
) {
    val previewStart = remember { BringIntoViewRequester() }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        previewStart.bringIntoView()
    }
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.ai_preview_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.bringIntoViewRequester(previewStart)
            )
            Text(
                stringResource(R.string.ai_preview_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val isExpense = draft.type == TransactionType.EXPENSE.name
                SegmentedButton(
                    selected = isExpense,
                    onClick = { onDraftChange { it.copy(type = TransactionType.EXPENSE.name) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    enabled = enabled
                ) {
                    Text(stringResource(R.string.transaction_expense))
                }
                SegmentedButton(
                    selected = !isExpense,
                    onClick = { onDraftChange { it.copy(type = TransactionType.INCOME.name) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    enabled = enabled
                ) {
                    Text(stringResource(R.string.transaction_income))
                }
            }
            OutlinedTextField(
                value = draft.amountText,
                onValueChange = { text -> onDraftChange { it.copy(amountText = text) } },
                enabled = enabled,
                label = { Text(stringResource(R.string.ai_amount)) },
                supportingText = { Text(stringResource(R.string.ai_amount_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
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
            AiChoiceDropdown(
                label = stringResource(R.string.input_choose_wallet),
                selectedId = draft.walletId,
                choices = state.wallets.map { it.id to it.name },
                enabled = enabled,
                onSelect = { id -> onDraftChange { it.copy(walletId = id) } }
            )
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
            OutlinedTextField(
                value = draft.dateText,
                onValueChange = { text -> onDraftChange { it.copy(dateText = text) } },
                enabled = enabled,
                label = { Text(stringResource(R.string.ai_date)) },
                supportingText = { Text(stringResource(R.string.ai_date_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = draft.note,
                onValueChange = { text -> onDraftChange { it.copy(note = text) } },
                enabled = enabled,
                label = { Text(stringResource(R.string.ai_note)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 2,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                Text(
                    stringResource(R.string.ai_recurring),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = draft.isRecurring, onCheckedChange = null, enabled = enabled)
            }
            Text(
                stringResource(R.string.ai_recurring_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!state.canSave && !state.isSaving && !state.isParsing && !state.saved) {
                Text(
                    stringResource(R.string.ai_validation_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = onSave,
                enabled = state.canSave,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        stringResource(
                            if (state.isSaving) R.string.ai_saving else R.string.ai_confirm_save
                        ),
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
