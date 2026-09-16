package com.example.expense_tracker.ui.input.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.input.InputTypeOption

@OptIn(ExperimentalMaterial3Api::class)
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
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        modifier = modifier
    )
}

