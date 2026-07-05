package com.example.expense_tracker.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.Expense_trackerTheme
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties

// ── Summary Filter Tabs ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryFilterTabs(
    selected: FilterPeriod,
    onSelected: (FilterPeriod, Long?, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.weight(1f)
        ) {
            val standardFilters = FilterPeriod.entries.filter { it != FilterPeriod.CUSTOM }
            standardFilters.forEachIndexed { index, filter ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = standardFilters.size),
                    onClick = { onSelected(filter, null, null) },
                    selected = selected == filter,
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(filter.label)
                }
            }
        }

        IconButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Custom Date",
                tint = if (selected == FilterPeriod.CUSTOM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(16.dp),
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            onSelected(FilterPeriod.CUSTOM, start, end)
                        } else if (start != null) {
                            onSelected(FilterPeriod.CUSTOM, start, start) // Same day if only one selected
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f),
                title = {
                    Text(
                        text = "Pilih Tanggal",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Tentukan rentang tanggal filter",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                    )
                },
                showModeToggle = false
            )
        }
    }
}

// ── Breakdown Card Item ──────────────────────────────────────────────

@Composable
fun BreakdownCardItem(
    item: BreakdownItem,
    modifier: Modifier = Modifier
) {
    val icon = when (item.categoryId) {
        1L -> Icons.Default.Restaurant
        2L -> Icons.Default.DirectionsCar
        3L -> Icons.Default.ShoppingCart
        4L -> Icons.Default.Movie
        5L -> Icons.Default.Receipt
        6L -> Icons.Default.LocalHospital
        else -> Icons.Default.MoreHoriz
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: Icon and Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.categoryName,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom row: Colored Dot and Percentage
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = com.example.expense_tracker.ui.theme.categoryColor(item.categoryId.toInt()),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(item.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.format(item.amount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Empty State ────────────────────────────────────────────────────

@Composable
fun SummaryEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📊",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada data pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Total Footer ───────────────────────────────────────────────────

@Composable
fun SummaryTotalFooter(
    totalAmount: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Total",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = CurrencyFormatter.format(totalAmount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Summary Screen ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ringkasan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

        // Filter tabs
        SummaryFilterTabs(
            selected = state.filter,
            onSelected = { filter, start, end -> viewModel.onFilterSelected(filter, start, end) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Breakdown list or empty state
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            SummaryEmptyState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    // Donut Chart placed at the top of the grid
                    DonutChart(
                        items = state.items,
                        totalAmount = state.totalAmount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
                items(state.items, key = { it.categoryId }) { item ->
                    BreakdownCardItem(item = item)
                }
            }
            // Total footer
            SummaryTotalFooter(totalAmount = state.totalAmount)
        }
    }
    }
}

// ── Previews ───────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun SummaryScreenEmptyPreview() {
    Expense_trackerTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            SummaryFilterTabs(selected = FilterPeriod.TODAY, onSelected = { _, _, _ -> })
            Spacer(modifier = Modifier.height(16.dp))
            SummaryEmptyState()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenWithDataPreview() {
    Expense_trackerTheme {
        val items = listOf(
            BreakdownItem(1, "Makanan", 60_000L, 0.40f),
            BreakdownItem(2, "Transport", 45_000L, 0.30f),
            BreakdownItem(3, "Belanja", 30_000L, 0.20f),
            BreakdownItem(4, "Hiburan", 15_000L, 0.10f),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            SummaryFilterTabs(selected = FilterPeriod.TODAY, onSelected = { _, _, _ -> })
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items, key = { it.categoryId }) { item ->
                    BreakdownCardItem(item = item)
                }
            }
            SummaryTotalFooter(totalAmount = 150_000L)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BreakdownCardItemPreview() {
    Expense_trackerTheme {
        BreakdownCardItem(
            item = BreakdownItem(1, "Makanan", 50_000L, 0.33f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryTotalFooterPreview() {
    Expense_trackerTheme {
        SummaryTotalFooter(totalAmount = 150_000L)
    }
}
