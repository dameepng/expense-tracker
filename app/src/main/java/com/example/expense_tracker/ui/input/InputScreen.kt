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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.Expense_trackerTheme

// ── Custom Header ───────────────────────────────────────────────────

@Composable
fun InputHeader(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = "Tambah Pengeluaran",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

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
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = amountText,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = { text ->
                val prefix = "Rp "
                val out = prefix + text.text
                val offsetMapping = object : OffsetMapping {
                    override fun originalToTransformed(offset: Int) = offset + prefix.length
                    override fun transformedToOriginal(offset: Int) = if (offset < prefix.length) 0 else offset - prefix.length
                }
                TransformedText(AnnotatedString(out), offsetMapping)
            },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (amountText.isEmpty()) {
                        Text(
                            text = "Rp 0",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
        
        if (amountText.isNotEmpty() && amountText.toLongOrNull() != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                Text(
                    text = CurrencyFormatter.format(amountText.toLong()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
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
        InputHeader(onNavigateBack = onNavigateBack)
        
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
            InputHeader(onNavigateBack = {})
            AmountInput(amountText = "", onAmountChange = {})
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
            InputHeader(onNavigateBack = {})
            AmountInput(amountText = "75000", onAmountChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            CategoryGrid(categories = categories, selectedId = 1L, onCategorySelected = {}, modifier = Modifier.weight(1f))
            SaveButton(enabled = true, onClick = {})
        }
    }
}

