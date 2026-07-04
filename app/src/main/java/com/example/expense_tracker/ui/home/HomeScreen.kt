package com.example.expense_tracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.TimeFormatter
import com.example.expense_tracker.ui.theme.Expense_trackerTheme

// ── Filter Tabs ───────────────────────────────────────────────────

@Composable
fun FilterTabs(
    selected: FilterPeriod,
    onSelected: (FilterPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPeriod.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ── Total Display ──────────────────────────────────────────────────

@Composable
fun TotalDisplay(
    amount: Long,
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = CurrencyFormatter.format(amount),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Total $periodLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Expense List Item ──────────────────────────────────────────────

@Composable
fun ExpenseListItem(
    expense: ExpenseWithCategory,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = expense.categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = TimeFormatter.formatTime(expense.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = CurrencyFormatter.format(expense.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Empty State ────────────────────────────────────────────────────

@Composable
fun EmptyState(
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📭",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Streak Counter ────────────────────────────────────────────────

@Composable
fun StreakCounter(
    streak: Int,
    encouragementText: String,
    modifier: Modifier = Modifier
) {
    val isActive = streak > 0
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Surface(
            color = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = encouragementText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Home Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    streakViewModel: StreakCounterViewModel? = null
) {
    val state by viewModel.uiState.collectAsState()
    val streakStateFlow = streakViewModel?.uiState
        ?: MutableStateFlow(StreakCounterUiState())
    val streakState by streakStateFlow.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Streak counter (US5)
        StreakCounter(
            streak = streakState.streak,
            encouragementText = streakState.encouragementText
        )

        // Filter tabs
        FilterTabs(
            selected = state.filter,
            onSelected = { viewModel.onFilterSelected(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Total display
        TotalDisplay(
            amount = state.totalAmount,
            periodLabel = state.periodLabel
        )

        // Expense list or empty state
        if (state.expenses.isEmpty()) {
            EmptyState(periodLabel = state.periodLabel)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.expenses, key = { it.id }) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        }
    }
}

// ── Previews ───────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun FilterTabsPreview() {
    Expense_trackerTheme {
        FilterTabs(
            selected = FilterPeriod.TODAY,
            onSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TotalDisplayPreview() {
    Expense_trackerTheme {
        TotalDisplay(amount = 150_000L, periodLabel = "Hari Ini")
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseListItemPreview() {
    Expense_trackerTheme {
        ExpenseListItem(
            expense = ExpenseWithCategory(
                id = 1,
                amount = 50_000L,
                categoryId = 1,
                categoryName = "Makanan",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    Expense_trackerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            EmptyState(periodLabel = "hari ini")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_empty() {
    Expense_trackerTheme {
        // Static preview: empty state
        val fakeState = HomeUiState(filter = FilterPeriod.TODAY, periodLabel = "Hari Ini")
        Column(modifier = Modifier.fillMaxSize()) {
            StreakCounter(streak = 0, encouragementText = "Mulai streak kamu hari ini!")
            FilterTabs(selected = fakeState.filter, onSelected = {})
            Spacer(modifier = Modifier.height(8.dp))
            TotalDisplay(amount = fakeState.totalAmount, periodLabel = fakeState.periodLabel)
            EmptyState(periodLabel = fakeState.periodLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_withData() {
    Expense_trackerTheme {
        val fakeState = HomeUiState(
            filter = FilterPeriod.TODAY,
            periodLabel = "Hari Ini",
            totalAmount = 150_000L,
            expenses = listOf(
                ExpenseWithCategory(1, 50_000L, 1, "Makanan", System.currentTimeMillis()),
                ExpenseWithCategory(2, 35_000L, 2, "Transport", System.currentTimeMillis() - 3600_000),
                ExpenseWithCategory(3, 65_000L, 3, "Belanja", System.currentTimeMillis() - 7200_000),
            )
        )
        Column(modifier = Modifier.fillMaxSize()) {
            StreakCounter(streak = 3, encouragementText = "3 hari berturut-turut!")
            FilterTabs(selected = fakeState.filter, onSelected = {})
            Spacer(modifier = Modifier.height(8.dp))
            TotalDisplay(amount = fakeState.totalAmount, periodLabel = fakeState.periodLabel)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(fakeState.expenses, key = { it.id }) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StreakCounterActivePreview() {
    Expense_trackerTheme {
        StreakCounter(streak = 5, encouragementText = "5 hari berturut-turut!")
    }
}

@Preview(showBackground = true)
@Composable
fun StreakCounterInactivePreview() {
    Expense_trackerTheme {
        StreakCounter(streak = 0, encouragementText = "Mulai streak kamu hari ini!")
    }
}

@Preview(showBackground = true)
@Composable
fun StreakCounterOneDayPreview() {
    Expense_trackerTheme {
        StreakCounter(streak = 1, encouragementText = "1 hari berturut-turut!")
    }
}
