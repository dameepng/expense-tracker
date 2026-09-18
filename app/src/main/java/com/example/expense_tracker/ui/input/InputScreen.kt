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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.ui.input.components.AmountInput
import com.example.expense_tracker.ui.input.components.CategoryGrid
import com.example.expense_tracker.ui.input.components.InputHeader
import com.example.expense_tracker.ui.input.components.InputTypeSelector
import com.example.expense_tracker.ui.input.components.SaveButton
import com.example.expense_tracker.ui.input.components.WalletPicker
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme
import com.example.expense_tracker.ui.theme.spacing

private val MAX_INPUT_CONTAINER_WIDTH = 640.dp

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onSaved: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.saved) {
        onSaved()
    }

    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val handleBack = {
        keyboard?.hide()
        focusManager.clearFocus()
        onNavigateBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboard?.hide()
            focusManager.clearFocus()
        }
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
                .widthIn(max = MAX_INPUT_CONTAINER_WIDTH)
        ) {
            InputHeader(
                inputTypeOption = state.inputTypeOption,
                onNavigateBack = handleBack
            )

            Spacer(modifier = Modifier.height(spacing.itemGap))

            // Only show empty wallet state if loading is finished AND wallets are truly empty
            if (!state.isLoading && state.wallets.isEmpty()) {
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
                InputTypeSelector(
                    selectedOption = state.inputTypeOption,
                    onOptionSelected = viewModel::onInputTypeSelected
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    AmountInput(
                        amountText = state.amountText,
                        onAmountChange = viewModel::onAmountChange
                    )

                    if (state.inputMode == InputMode.BILL_REMINDER) {
                        OutlinedTextField(
                            value = state.billReminderName,
                            onValueChange = viewModel::onBillReminderNameChange,
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
                            onValueChange = viewModel::onBillReminderDueDayChange,
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
                                onCheckedChange = viewModel::onRepeatChange
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = viewModel::onDescriptionChange,
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

                    CategoryGrid(
                        categories = state.categories,
                        selectedId = state.selectedCategoryId,
                        onCategorySelected = viewModel::onCategorySelected
                    )

                    Spacer(modifier = Modifier.height(spacing.itemGap))

                    WalletPicker(
                        wallets = state.wallets,
                        selectedId = state.selectedWalletId,
                        onWalletSelected = viewModel::onWalletSelected
                    )

                    Spacer(modifier = Modifier.height(spacing.sectionGap))
                }

                SaveButton(
                    enabled = state.isSaveEnabled,
                    onClick = viewModel::onSave,
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


@Preview(showBackground = true)
@Composable
fun InputScreenEmptyPreview() {
    ExpenseTrackerTheme {
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
            InputTypeSelector(selectedOption = InputTypeOption.EXPENSE, onOptionSelected = {})
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
    ExpenseTrackerTheme {
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
            InputTypeSelector(selectedOption = InputTypeOption.EXPENSE, onOptionSelected = {})
            AmountInput(amountText = "75000", onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = 1L, onCategorySelected = {})
            SaveButton(enabled = true, onClick = {})
        }
    }
}
