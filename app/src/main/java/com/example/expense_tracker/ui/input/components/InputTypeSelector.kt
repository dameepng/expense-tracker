package com.example.expense_tracker.ui.input.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.input.InputTypeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTypeSelector(
    selectedOption: InputTypeOption,
    onOptionSelected: (InputTypeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val options = InputTypeOption.entries

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        options.forEachIndexed { index, option ->
            val label = when (option) {
                InputTypeOption.INCOME -> stringResource(R.string.transaction_income)
                InputTypeOption.EXPENSE -> stringResource(R.string.transaction_expense)
                InputTypeOption.BILL_REMINDER -> stringResource(R.string.bill_reminder)
            }

            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOptionSelected(option)
                },
                selected = selectedOption == option,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(label)
            }
        }
    }
}
