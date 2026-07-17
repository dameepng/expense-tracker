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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import com.example.expense_tracker.data.TransactionType
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.example.expense_tracker.data.Category

import com.example.expense_tracker.ui.theme.Expense_trackerTheme

// ── Custom Header ───────────────────────────────────────────────────

@Composable
fun InputHeader(
    transactionType: TransactionType,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (transactionType == TransactionType.INCOME) "Tambah Pemasukan" else "Tambah Pengeluaran"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
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
    transactionType: TransactionType,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefix = if (transactionType == TransactionType.INCOME) "+Rp " else "-Rp "
    val displayText = if (amountText.isEmpty()) {
        "Rp 0"
    } else {
        prefix + formatWithDots(amountText)
    }
    
    val textColor = if (amountText.isEmpty()) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    } else {
        if (transactionType == TransactionType.INCOME) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground
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
                color = Color.Transparent // Hide the raw text
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent), // Hide cursor (we show formatted text instead)
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Visible formatted text — always perfectly centered
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
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
    Column(modifier = modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Pilih Kategori",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories, key = { it.id }) { category ->
                val isSelected = category.id == selectedId
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category.id) },
                    label = { 
                        Text(
                            text = category.name, 
                            modifier = Modifier.fillMaxWidth(), 
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.height(48.dp)
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
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .height(64.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "Simpan",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "Simpan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Transaction Type Toggle ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTypeToggle(
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        val types = TransactionType.entries
        types.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                onClick = { onTypeChange(type) },
                selected = selectedType == type,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = if (type == TransactionType.INCOME) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer,
                    activeContentColor = if (type == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(if (type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran")
            }
        }
    }
}

// ── Input Screen ───────────────────────────────────────────────────

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onSaved: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate back on save
    if (state.saved) {
        onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        InputHeader(
            transactionType = state.transactionType,
            onNavigateBack = onNavigateBack
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TransactionTypeToggle(
            selectedType = state.transactionType,
            onTypeChange = { viewModel.onTransactionTypeChange(it) }
        )
        
        // Amount input
        AmountInput(
            amountText = state.amountText,
            transactionType = state.transactionType,
            onAmountChange = { viewModel.onAmountChange(it) }
        )

        // Description Input
        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text("Deskripsi Singkat (opsional)") },
            placeholder = { Text("Misal: Nasi Goreng, Bensin, Netflix") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Wallet picker
        WalletPicker(
            wallets = state.wallets,
            selectedId = state.selectedWalletId,
            onWalletSelected = { viewModel.onWalletSelected(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category grid
        CategoryGrid(
            categories = state.categories,
            selectedId = state.selectedCategoryId,
            onCategorySelected = { viewModel.onCategorySelected(it) },
            modifier = Modifier.weight(1f)
        )

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            InputHeader(transactionType = TransactionType.EXPENSE, onNavigateBack = {})
            Spacer(modifier = Modifier.height(16.dp))
            TransactionTypeToggle(selectedType = TransactionType.EXPENSE, onTypeChange = {})
            AmountInput(amountText = "", transactionType = TransactionType.EXPENSE, onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = null, onCategorySelected = {}, modifier = Modifier.weight(1f))
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
            InputHeader(transactionType = TransactionType.EXPENSE, onNavigateBack = {})
            Spacer(modifier = Modifier.height(16.dp))
            TransactionTypeToggle(selectedType = TransactionType.EXPENSE, onTypeChange = {})
            AmountInput(amountText = "75000", transactionType = TransactionType.EXPENSE, onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = 1L, onCategorySelected = {}, modifier = Modifier.weight(1f))
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

    Column(modifier = modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Pilih Wallet",
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
                    onClick = { onWalletSelected(wallet.id) },
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
