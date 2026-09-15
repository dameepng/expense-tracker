package com.example.expense_tracker.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import com.example.expense_tracker.ui.theme.spacing
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.example.expense_tracker.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.data.Category

import com.example.expense_tracker.ui.theme.Expense_trackerTheme

// ── Custom Header ───────────────────────────────────────────────────

@Composable
fun InputHeader(
    inputTypeOption: InputTypeOption,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (inputTypeOption) {
        InputTypeOption.INCOME -> stringResource(R.string.input_add_income)
        InputTypeOption.EXPENSE -> stringResource(R.string.input_add_expense)
        InputTypeOption.BILL_REMINDER -> stringResource(R.string.input_add_bill)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// ── Amount Input ───────────────────────────────────────────────────

private fun formatWithDots(value: String): String {
    val number = value.toLongOrNull() ?: return value
    return number.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

@Composable
fun AmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefix = "Rp "
    val displayText = if (amountText.isEmpty()) {
        "Rp 0"
    } else {
        prefix + formatWithDots(amountText)
    }
    
    val textColor = if (amountText.isEmpty()) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    val dynamicFontSize = when {
        displayText.length > 20 -> 20.sp
        displayText.length > 16 -> 24.sp
        displayText.length > 13 -> 28.sp
        displayText.length > 10 -> 34.sp
        else -> 44.sp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = amountText,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) onAmountChange(newValue)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.displayLarge.copy(
                color = Color.Transparent, // Hide the raw text
                fontSize = dynamicFontSize
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent), // Hide cursor (we show formatted text instead)
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Visible formatted text — always perfectly centered and responsive
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = dynamicFontSize
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Invisible text field (still needed for keyboard input)
                    innerTextField()
                }
            }
        )
    }
}

// ── Category Grid ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedId: Long?,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Column(modifier = modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.input_choose_category),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val chunkedCategories = categories.chunked(3)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            chunkedCategories.forEach { rowCategories ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowCategories.forEach { category ->
                        val isSelected = category.id == selectedId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCategorySelected(category.id)
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null,
                            label = { 
                                Text(
                                    text = category.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

// ── Save Button ────────────────────────────────────────────────────

@Composable
fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    label: String = stringResource(R.string.save),
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val spacing = MaterialTheme.spacing
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenMargin, vertical = spacing.screenMargin)
            .height(56.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = stringResource(R.string.save)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Input Type Toggle ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTypeSegmentedButton(
    selectedOption: InputTypeOption,
    onOptionSelected: (InputTypeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        val options = InputTypeOption.entries
        options.forEachIndexed { index, option ->
            val label = when (option) {
                InputTypeOption.INCOME -> stringResource(R.string.transaction_income)
                InputTypeOption.EXPENSE -> stringResource(R.string.transaction_expense)
                InputTypeOption.BILL_REMINDER -> stringResource(R.string.bill_reminder)
            }
            
            val colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOptionSelected(option)
                },
                selected = selectedOption == option,
                colors = colors
            ) {
                Text(label)
            }
        }
    }
}

// ── Input Screen ───────────────────────────────────────────────────

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onSaved: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToAiInput: () -> Unit = {},
    onNavigateToReceipt: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate back on save
    if (state.saved) {
        onSaved()
    }

    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            InputHeader(
                inputTypeOption = state.inputTypeOption,
                onNavigateBack = onNavigateBack
            )
        
        Spacer(modifier = Modifier.height(spacing.sectionGap))
        
        if (state.wallets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(spacing.screenMargin)
                ) {
                    Text(
                        text = "Tidak Ada Dompet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(spacing.space100))
                    Text(
                        text = "Tambahkan wallet terlebih dahulu untuk mencatat transaksi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(spacing.sectionGap))
                    Button(
                        onClick = onNavigateToWallet,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tambah Wallet")
                    }
                }
            }
        } else {
            InputTypeSegmentedButton(
                selectedOption = state.inputTypeOption,
                onOptionSelected = { viewModel.onInputTypeSelected(it) }
            )

            if (state.inputMode == InputMode.TRANSACTION) {
                Spacer(modifier = Modifier.height(spacing.itemGap))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.screenMargin),
                    horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)
                ) {
                    Button(
                        onClick = onNavigateToAiInput,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.home_ai_action), maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onNavigateToReceipt,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.home_receipt_action), maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
            // Amount input
        AmountInput(
            amountText = state.amountText,
            onAmountChange = { viewModel.onAmountChange(it) }
        )

        if (state.inputMode == InputMode.BILL_REMINDER) {
            OutlinedTextField(
                value = state.billReminderName,
                onValueChange = { viewModel.onBillReminderNameChange(it) },
                label = { Text(stringResource(R.string.bill_name)) },
                placeholder = { Text(stringResource(R.string.bill_name_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenMargin),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(spacing.itemGap))

            OutlinedTextField(
                value = state.billReminderDueDay,
                onValueChange = { viewModel.onBillReminderDueDayChange(it) },
                label = { Text(stringResource(R.string.due_date)) },
                placeholder = { Text(stringResource(R.string.due_date_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenMargin),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
            
            Spacer(modifier = Modifier.height(spacing.itemGap))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenMargin),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.repeat_every_month),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.bill_auto_repeat_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isRepeat,
                    onCheckedChange = { viewModel.onRepeatChange(it) }
                )
            }
        } else {
            // Description Input
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text(stringResource(R.string.input_note)) },
                placeholder = { Text(stringResource(R.string.input_note_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenMargin),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(spacing.itemGap))

        // Category grid
        CategoryGrid(
            categories = state.categories,
            selectedId = state.selectedCategoryId,
            onCategorySelected = { viewModel.onCategorySelected(it) }
        )

        Spacer(modifier = Modifier.height(spacing.itemGap))

        // Wallet picker
        WalletPicker(
            wallets = state.wallets,
            selectedId = state.selectedWalletId,
            onWalletSelected = { viewModel.onWalletSelected(it) }
        )
        
        Spacer(modifier = Modifier.height(spacing.sectionGap))
        }

        // Save button
        SaveButton(
            enabled = state.isSaveEnabled,
            onClick = { viewModel.onSave() },
            label = when (state.inputTypeOption) {
                InputTypeOption.INCOME -> stringResource(R.string.input_save_income)
                InputTypeOption.BILL_REMINDER -> stringResource(R.string.input_save_bill)
                InputTypeOption.EXPENSE -> stringResource(R.string.input_save_expense)
            },
            modifier = Modifier.imePadding()
        )
        }
        }
    }
}

// ── Previews ───────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun InputScreenEmptyPreview() {
    Expense_trackerTheme {
        val categories = listOf(
            Category(1, "Makanan"),
            Category(2, "Transport"),
            Category(3, "Belanja"),
            Category(4, "Hiburan"),
            Category(5, "Tagihan"),
            Category(6, "Kesehatan"),
            Category(7, "Lainnya"),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            InputHeader(inputTypeOption = InputTypeOption.EXPENSE, onNavigateBack = {})
            Spacer(modifier = Modifier.height(16.dp))
            InputTypeSegmentedButton(selectedOption = InputTypeOption.EXPENSE, onOptionSelected = {})
            AmountInput(amountText = "", onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = null, onCategorySelected = {})
            SaveButton(enabled = false, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputScreenFilledPreview() {
    Expense_trackerTheme {
        val categories = listOf(
            Category(1, "Makanan"),
            Category(2, "Transport"),
            Category(3, "Belanja"),
            Category(4, "Hiburan"),
            Category(5, "Tagihan"),
            Category(6, "Kesehatan"),
            Category(7, "Lainnya"),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            InputHeader(inputTypeOption = InputTypeOption.EXPENSE, onNavigateBack = {})
            Spacer(modifier = Modifier.height(16.dp))
            InputTypeSegmentedButton(selectedOption = InputTypeOption.EXPENSE, onOptionSelected = {})
            AmountInput(amountText = "75000", onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = 1L, onCategorySelected = {})
            SaveButton(enabled = true, onClick = {})
        }
    }
}

// ── Wallet Picker ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletPicker(
    wallets: List<com.example.expense_tracker.data.Wallet>,
    selectedId: Long?,
    onWalletSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (wallets.size <= 1) return
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.input_choose_wallet),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(wallets, key = { it.id }) { wallet ->
                val isSelected = wallet.id == selectedId
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onWalletSelected(wallet.id)
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null,
                    label = { 
                        Text(
                            text = wallet.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

