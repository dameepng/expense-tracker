package com.example.expense_tracker.ui.summary.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.expense_tracker.R

/**
 * Dialog wrapper for selecting custom start and end date range.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryDateRangePickerDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (startMillis: Long, endMillis: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.padding(16.dp),
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        onDateRangeSelected(start, end)
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.weight(1f),
            title = {
                Text(
                    text = stringResource(R.string.choose_date),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)
                )
            },
            headline = {
                Text(
                    text = stringResource(R.string.choose_date_desc),
                    modifier = Modifier.padding(16.dp)
                )
            },
            showModeToggle = false
        )
    }
}
