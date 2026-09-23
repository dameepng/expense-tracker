package com.example.expense_tracker.ui.summary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TimeRangeCalculator
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.ui.summary.components.BreakdownCardItem
import com.example.expense_tracker.ui.summary.components.CashFlowSection
import com.example.expense_tracker.ui.summary.components.SpendingCategorySection
import com.example.expense_tracker.ui.summary.components.SummaryDateRangePickerDialog
import com.example.expense_tracker.ui.summary.components.SummaryTopAppBar
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme
import com.example.expense_tracker.ui.theme.motionScheme
import com.example.expense_tracker.ui.theme.spacing

private val MAX_SUMMARY_WIDTH = 840.dp
private const val MILLIS_PER_DAY = 86_400_000L

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onCategoryClick: (categoryId: Long, walletId: Long?, startTime: Long, endTime: Long) -> Unit = { _, _, _, _ -> }
) {
    val state by viewModel.uiState.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SummaryTopAppBar(
                currentFilter = state.filter,
                wallets = state.wallets,
                selectedWalletId = state.selectedWalletId,
                onCustomFilterClick = { showDateRangePicker = true },
                onWalletSelected = { viewModel.onWalletSelected(it) }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        SummaryScreenContent(
            state = state,
            onFilterSelected = { filter, start, end -> viewModel.onFilterSelected(filter, start, end) },
            onCustomFilterClick = { showDateRangePicker = true },
            onWalletSelected = { viewModel.onWalletSelected(it) },
            onTransactionTypeSelected = { viewModel.onTransactionTypeSelected(it) },
            onCategoryClick = onCategoryClick,
            modifier = Modifier.padding(paddingValues)
        )
    }

    if (showDateRangePicker) {
        SummaryDateRangePickerDialog(
            onDismiss = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.onFilterSelected(FilterPeriod.CUSTOM, start, end)
            }
        )
    }
}

@Composable
internal fun SummaryScreenContent(
    state: SummaryUiState,
    onFilterSelected: (FilterPeriod, Long?, Long?) -> Unit,
    onCustomFilterClick: () -> Unit,
    onWalletSelected: (Long?) -> Unit,
    onTransactionTypeSelected: (TransactionType) -> Unit,
    onCategoryClick: (categoryId: Long, walletId: Long?, startTime: Long, endTime: Long) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val spacing = MaterialTheme.spacing
    val isIncome = state.transactionType == TransactionType.INCOME

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = MAX_SUMMARY_WIDTH),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item(key = "hero_balance") {
                Spacer(modifier = Modifier.height(spacing.sectionGap))
                HeroBalanceCard(
                    totalBalance = state.totalBalance,
                    percentageChange = state.balancePercentageChange
                )
                Spacer(modifier = Modifier.height(spacing.sectionGap))
            }

            item(key = "cash_flow") {
                CashFlowSection(
                    filter = state.filter,
                    netCashFlow = state.netCashFlow,
                    totalIncome = state.totalIncome,
                    totalExpense = state.totalExpense,
                    dailyCashFlow = state.dailyCashFlow,
                    onFilterSelected = onFilterSelected,
                    onCustomFilterClick = onCustomFilterClick
                )
            }

            item(key = "spending_category") {
                Spacer(modifier = Modifier.height(spacing.sectionGap))
                SpendingCategorySection(
                    transactionType = state.transactionType,
                    items = state.items,
                    isLoading = state.isLoading,
                    totalAmount = state.totalAmount,
                    onTransactionTypeSelected = onTransactionTypeSelected
                )
                Spacer(modifier = Modifier.height(spacing.sectionGap))
            }

            if (!state.isLoading && state.items.isNotEmpty()) {
                items(
                    items = state.items,
                    key = { it.categoryId },
                    contentType = { "breakdown_item" }
                ) { item ->
                    BreakdownCardItem(
                        item = item,
                        isIncome = isIncome,
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                                fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                            )
                            .padding(
                                horizontal = spacing.screenMargin,
                                vertical = spacing.space75
                            ),
                        onClick = {
                            val (start, end) = calculateCategoryDateRange(state)
                            onCategoryClick(item.categoryId, state.selectedWalletId, start, end)
                        }
                    )
                }
            }
        }
    }
}

private fun calculateCategoryDateRange(state: SummaryUiState): Pair<Long, Long> {
    val startDate = state.customStartDate
    val endDate = state.customEndDate
    return if (state.filter == FilterPeriod.CUSTOM && startDate != null && endDate != null) {
        Pair(startDate, endDate + MILLIS_PER_DAY)
    } else {
        TimeRangeCalculator.calculateRange(state.filter)
    }
}

// ── Previews ───────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun SummaryScreenEmptyPreview() {
    ExpenseTrackerTheme {
        SummaryScreenContent(
            state = SummaryUiState(isLoading = false, items = emptyList()),
            onFilterSelected = { _, _, _ -> },
            onCustomFilterClick = {},
            onWalletSelected = {},
            onTransactionTypeSelected = {},
            onCategoryClick = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenWithDataPreview() {
    ExpenseTrackerTheme {
        val items = listOf(
            BreakdownItem(1, "Makanan", 60_000L, 0.40f),
            BreakdownItem(2, "Transport", 45_000L, 0.30f),
            BreakdownItem(3, "Belanja", 30_000L, 0.20f),
            BreakdownItem(4, "Hiburan", 15_000L, 0.10f),
        )
        SummaryScreenContent(
            state = SummaryUiState(
                isLoading = false,
                totalBalance = 1_500_000L,
                totalIncome = 2_000_000L,
                totalExpense = 500_000L,
                netCashFlow = 1_500_000L,
                totalAmount = 150_000L,
                items = items
            ),
            onFilterSelected = { _, _, _ -> },
            onCustomFilterClick = {},
            onWalletSelected = {},
            onTransactionTypeSelected = {},
            onCategoryClick = { _, _, _, _ -> }
        )
    }
}
