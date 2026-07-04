package com.example.expense_tracker.ui.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.Expense_trackerTheme

// ── Amount Input ───────────────────────────────────────────────────

@Composable
fun AmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            placeholder = { Text("0", style = MaterialTheme.typography.headlineMedium) },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (amountText.isNotEmpty() && amountText.toLongOrNull() != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.format(amountText.toLong()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Category Grid ──────────────────────────────────────────────────

@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedId: Long?,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Kategori",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories, key = { it.id }) { category ->
                FilterChip(
                    selected = category.id == selectedId,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
            }
        }
    }
}

// ── Save Button ────────────────────────────────────────────────────

@Composable
fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(56.dp)
    ) {
        Text(
            text = "Simpan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Input Screen ───────────────────────────────────────────────────

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onSaved: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate back on save
    if (state.saved) {
        onSaved()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(24.dp))

        // Amount input
        AmountInput(
            amountText = state.amountText,
            onAmountChange = { viewModel.onAmountChange(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category grid
        CategoryGrid(
            categories = state.categories,
            selectedId = state.selectedCategoryId,
            onCategorySelected = { viewModel.onCategorySelected(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save button
        SaveButton(
            enabled = state.isSaveEnabled,
            onClick = { viewModel.onSave() }
        )
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
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            AmountInput(amountText = "", onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = null, onCategorySelected = {})
            Spacer(modifier = Modifier.weight(1f))
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
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            AmountInput(amountText = "75000", onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = 1L, onCategorySelected = {})
            Spacer(modifier = Modifier.weight(1f))
            SaveButton(enabled = true, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AmountInputPreview() {
    Expense_trackerTheme {
        AmountInput(amountText = "50000", onAmountChange = {})
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryGridPreview() {
    Expense_trackerTheme {
        CategoryGrid(
            categories = listOf(
                Category(1, "Makanan"),
                Category(2, "Transport"),
                Category(3, "Belanja"),
                Category(4, "Hiburan"),
                Category(5, "Tagihan"),
                Category(6, "Kesehatan"),
                Category(7, "Lainnya"),
            ),
            selectedId = 1L,
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SaveButtonEnabledPreview() {
    Expense_trackerTheme {
        SaveButton(enabled = true, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun SaveButtonDisabledPreview() {
    Expense_trackerTheme {
        SaveButton(enabled = false, onClick = {})
    }
}
