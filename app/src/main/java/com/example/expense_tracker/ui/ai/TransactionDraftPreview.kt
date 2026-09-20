package com.example.expense_tracker.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.components.ChoiceDropdown
import com.example.expense_tracker.ui.theme.spacing
import java.time.Instant
import java.time.ZoneId

private const val CURRENCY_PREFIX = "Rp "
private const val CATEGORY_TYPE_BOTH = "BOTH"
private const val UTC_ZONE_ID = "UTC"

/**
 * Preview section for single or multiple transaction drafts detected from natural language input.
 */
@Composable
fun NaturalLanguageDraftPreviewSection(
    state: NaturalLanguageUiState,
    editingEnabled: Boolean,
    onUpdateDraft: (Int, (TransactionDraft) -> TransactionDraft) -> Unit,
    onRemoveDraft: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showValidationHint = !state.canSave && !state.isSaving && !state.isParsing && !state.saved

    when {
        state.drafts.size == 1 -> {
            SingleTransactionPreview(
                draft = state.drafts.first(),
                categories = state.categories,
                wallets = state.wallets,
                enabled = editingEnabled,
                canSave = state.canSave,
                isSaving = state.isSaving,
                showValidationHint = showValidationHint,
                onDraftChange = { transform -> onUpdateDraft(0, transform) },
                onSave = onSave,
                modifier = modifier
            )
        }
        state.drafts.size > 1 -> {
            MultiTransactionPreview(
                drafts = state.drafts,
                categories = state.categories,
                wallets = state.wallets,
                enabled = editingEnabled,
                canSave = state.canSave,
                isSaving = state.isSaving,
                showValidationHint = showValidationHint,
                onDraftChange = onUpdateDraft,
                onRemoveDraft = onRemoveDraft,
                onSave = onSave,
                modifier = modifier
            )
        }
    }
}

@Composable
fun MultiTransactionPreview(
    drafts: List<TransactionDraft>,
    categories: List<Category>,
    wallets: List<Wallet>,
    enabled: Boolean,
    canSave: Boolean,
    isSaving: Boolean,
    showValidationHint: Boolean,
    onDraftChange: (Int, (TransactionDraft) -> TransactionDraft) -> Unit,
    onRemoveDraft: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewStart = remember { BringIntoViewRequester() }
    val haptic = LocalHapticFeedback.current
    val spacing = MaterialTheme.spacing
    val totalAmount = remember(drafts) {
        drafts.sumOf { it.amountText.toLongOrNull() ?: 0L }
    }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        previewStart.bringIntoView()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(previewStart),
        verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
    ) {
        MultiDraftSummaryHeader(
            draftCount = drafts.size,
            totalAmount = totalAmount
        )

        drafts.forEachIndexed { index, draft ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(spacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ai_item_number, index + 1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRemoveDraft(index)
                            },
                            enabled = enabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.ai_delete_draft),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    TransactionDraftFields(
                        draft = draft,
                        categories = categories,
                        wallets = wallets,
                        enabled = enabled,
                        onDraftChange = { transform -> onDraftChange(index, transform) }
                    )
                }
            }
        }

        DraftValidationHint(visible = showValidationHint)

        SaveDraftButton(
            isSaving = isSaving,
            canSave = canSave,
            text = stringResource(
                if (isSaving) R.string.ai_saving else R.string.ai_save_all,
                drafts.size
            ),
            onSave = onSave
        )
    }
}

@Composable
fun MultiDraftSummaryHeader(
    draftCount: Int,
    totalAmount: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ai_multiple_found, draftCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.ai_total_preview, CurrencyFormatter.format(totalAmount)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SingleTransactionPreview(
    draft: TransactionDraft,
    categories: List<Category>,
    wallets: List<Wallet>,
    enabled: Boolean,
    canSave: Boolean,
    isSaving: Boolean,
    showValidationHint: Boolean,
    onDraftChange: ((TransactionDraft) -> TransactionDraft) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewStart = remember { BringIntoViewRequester() }
    val spacing = MaterialTheme.spacing

    LaunchedEffect(Unit) {
        withFrameNanos { }
        previewStart.bringIntoView()
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
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

            TransactionDraftFields(
                draft = draft,
                categories = categories,
                wallets = wallets,
                enabled = enabled,
                onDraftChange = onDraftChange
            )

            DraftValidationHint(visible = showValidationHint)

            SaveDraftButton(
                isSaving = isSaving,
                canSave = canSave,
                text = stringResource(
                    if (isSaving) R.string.ai_saving else R.string.ai_confirm_save
                ),
                onSave = onSave
            )
        }
    }
}

@Composable
fun SaveDraftButton(
    isSaving: Boolean,
    canSave: Boolean,
    text: String,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSave()
        },
        enabled = canSave,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DraftValidationHint(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (visible) {
        Text(
            text = stringResource(R.string.ai_validation_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    }
}

@Composable
fun TransactionDraftFields(
    draft: TransactionDraft,
    categories: List<Category>,
    wallets: List<Wallet>,
    enabled: Boolean,
    onDraftChange: ((TransactionDraft) -> TransactionDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.itemGap)
    ) {
        DraftTypeSegmentedButton(
            selectedType = draft.type,
            enabled = enabled,
            onTypeChange = { newType -> onDraftChange { it.copy(type = newType) } }
        )

        DraftAmountField(
            amountText = draft.amountText,
            enabled = enabled,
            onAmountChange = { text -> onDraftChange { it.copy(amountText = text) } }
        )

        DraftCategoryDropdown(
            selectedCategoryId = draft.categoryId,
            draftType = draft.type,
            categories = categories,
            enabled = enabled,
            onCategorySelect = { id -> onDraftChange { it.copy(categoryId = id) } }
        )

        DraftWalletDropdown(
            selectedWalletId = draft.walletId,
            wallets = wallets,
            enabled = enabled,
            onWalletSelect = { id -> onDraftChange { it.copy(walletId = id) } }
        )

        DraftMerchantField(
            merchant = draft.merchant,
            enabled = enabled,
            onMerchantChange = { text -> onDraftChange { it.copy(merchant = text) } }
        )

        DraftDateField(
            dateText = draft.dateText,
            parsedDateSupplier = { draft.parsedDate() },
            enabled = enabled,
            onDateSelect = { dateString -> onDraftChange { it.copy(dateText = dateString) } }
        )

        DraftNoteField(
            note = draft.note,
            enabled = enabled,
            onNoteChange = { text -> onDraftChange { it.copy(note = text) } }
        )

        DraftRecurringSwitch(
            isRecurring = draft.isRecurring,
            enabled = enabled,
            onRecurringChange = { checked -> onDraftChange { it.copy(isRecurring = checked) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftTypeSegmentedButton(
    selectedType: String,
    enabled: Boolean,
    onTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isExpense = selectedType == TransactionType.EXPENSE.name
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = isExpense,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTypeChange(TransactionType.EXPENSE.name)
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
                onTypeChange(TransactionType.INCOME.name)
            },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            enabled = enabled
        ) {
            Text(stringResource(R.string.transaction_income))
        }
    }
}

@Composable
fun DraftAmountField(
    amountText: String,
    enabled: Boolean,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = amountText,
        onValueChange = onAmountChange,
        enabled = enabled,
        label = { Text(stringResource(R.string.ai_amount)) },
        prefix = {
            Text(
                CURRENCY_PREFIX,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        supportingText = { Text(stringResource(R.string.ai_amount_hint)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DraftCategoryDropdown(
    selectedCategoryId: Long?,
    draftType: String,
    categories: List<Category>,
    enabled: Boolean,
    onCategorySelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryChoices = remember(categories, draftType) {
        categories
            .filter { it.type == draftType || it.type == CATEGORY_TYPE_BOTH }
            .map { it.id to it.name }
    }
    ChoiceDropdown(
        label = stringResource(R.string.category),
        selectedId = selectedCategoryId,
        choices = categoryChoices,
        enabled = enabled,
        onSelect = onCategorySelect,
        modifier = modifier
    )
}

@Composable
fun DraftWalletDropdown(
    selectedWalletId: Long?,
    wallets: List<Wallet>,
    enabled: Boolean,
    onWalletSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val walletChoices = remember(wallets) {
        wallets.map { it.id to it.name }
    }
    ChoiceDropdown(
        label = stringResource(R.string.input_choose_wallet),
        selectedId = selectedWalletId,
        choices = walletChoices,
        enabled = enabled,
        onSelect = onWalletSelect,
        modifier = modifier
    )
}

@Composable
fun DraftMerchantField(
    merchant: String,
    enabled: Boolean,
    onMerchantChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = merchant,
        onValueChange = onMerchantChange,
        enabled = enabled,
        label = { Text(stringResource(R.string.ai_merchant)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftDateField(
    dateText: String,
    parsedDateSupplier: () -> java.time.LocalDate?,
    enabled: Boolean,
    onDateSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val initialMillis = remember(dateText) {
            try {
                parsedDateSupplier()?.atStartOfDay(ZoneId.of(UTC_ZONE_ID))?.toInstant()?.toEpochMilli()
                    ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of(UTC_ZONE_ID))
                            .toLocalDate()
                        onDateSelect(selectedDate.toString())
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
        value = dateText,
        onValueChange = onDateSelect,
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
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DraftNoteField(
    note: String,
    enabled: Boolean,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        enabled = enabled,
        label = { Text(stringResource(R.string.ai_note)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DraftRecurringSwitch(
    isRecurring: Boolean,
    enabled: Boolean,
    onRecurringChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isRecurring,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onRecurringChange
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
            Switch(checked = isRecurring, onCheckedChange = null, enabled = enabled)
        }
    }
}
