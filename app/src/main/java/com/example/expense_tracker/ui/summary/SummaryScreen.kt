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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.WindowInsets

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
import com.example.expense_tracker.data.TransactionType
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.res.stringResource
import com.example.expense_tracker.R

// ── Summary Filter Tabs ────────────────────────────────────────────

@Composable
fun SummaryPeriodDropdown(
    selected: FilterPeriod,
    onSelected: (FilterPeriod, Long?, Long?) -> Unit,
    onCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(selected.labelResId),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.choose_period),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val standardFilters = FilterPeriod.entries.filter { it != FilterPeriod.CUSTOM }
            standardFilters.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(stringResource(filter.labelResId)) },
                    onClick = {
                        expanded = false
                        if (selected != filter) {
                            onSelected(filter, null, null)
                        }
                    },
                    trailingIcon = if (selected == filter) {
                        { Icon(Icons.Default.Check, contentDescription = "Selected") }
                    } else null
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(FilterPeriod.CUSTOM.labelResId)) },
                onClick = {
                    expanded = false
                    if (selected != FilterPeriod.CUSTOM) {
                        onCustomClick()
                    }
                },
                trailingIcon = if (selected == FilterPeriod.CUSTOM) {
                    { Icon(Icons.Default.Check, contentDescription = "Selected") }
                } else null
            )
        }
    }
}

// ── Summary Type Tabs ──────────────────────────────────────────────

@Composable
fun SummaryTypeTabs(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    val selectedTabIndex = tabs.indexOf(selectedType)
    
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, type ->
            val label = if (type == TransactionType.EXPENSE) stringResource(R.string.transaction_expense) else stringResource(R.string.transaction_income)
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTypeSelected(type) },
                text = { Text(label, fontWeight = FontWeight.SemiBold) },
                selectedContentColor = if (type == TransactionType.INCOME) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Wallet Filter Chips ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletFilterChips(
    wallets: List<com.example.expense_tracker.data.Wallet>,
    selectedWalletId: Long?,
    onWalletSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (wallets.size <= 1) return // No need to show filter if 0 or 1 wallet

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedWalletId == null,
            onClick = { onWalletSelected(null) },
            label = { Text(stringResource(R.string.all_wallets)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        wallets.forEach { wallet ->
            FilterChip(
                selected = selectedWalletId == wallet.id,
                onClick = { onWalletSelected(wallet.id) },
                label = { Text(wallet.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

// ── Breakdown Card Item ──────────────────────────────────────────────

@Composable
fun BreakdownCardItem(
    item: BreakdownItem,
    isIncome: Boolean = false,
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

    val categoryColor = com.example.expense_tracker.ui.theme.categoryColor(item.categoryId.toInt(), isIncome)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.05f) // Subtle tint
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Accent Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(categoryColor, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            // Icon and Text Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.categoryName,
                        tint = categoryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { item.percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = categoryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Amount and Percentage Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(item.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(item.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Empty State ────────────────────────────────────────────────────

@Composable
fun SummaryEmptyState(
    isIncome: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isIncome) "💰" else "📊",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = if (isIncome) stringResource(R.string.no_income_data) else stringResource(R.string.no_expense_data),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Summary Screen ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }
    val isIncome = state.transactionType == TransactionType.INCOME
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_summary)) },
                actions = {
                    IconButton(onClick = { showDateRangePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.filter_custom),
                            tint = if (state.filter == FilterPeriod.CUSTOM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.wallets.size > 1) {
                        var walletMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { walletMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Pilih Wallet",
                                    tint = if (state.selectedWalletId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = walletMenuExpanded,
                                onDismissRequest = { walletMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.all_wallets)) },
                                    onClick = {
                                        walletMenuExpanded = false
                                        if (state.selectedWalletId != null) {
                                            viewModel.onWalletSelected(null)
                                        }
                                    },
                                    trailingIcon = if (state.selectedWalletId == null) {
                                        { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                    } else null
                                )
                                state.wallets.forEach { wallet ->
                                    DropdownMenuItem(
                                        text = { Text(wallet.name) },
                                        onClick = {
                                            walletMenuExpanded = false
                                            if (state.selectedWalletId != wallet.id) {
                                                viewModel.onWalletSelected(wallet.id)
                                            }
                                        },
                                        trailingIcon = if (state.selectedWalletId == wallet.id) {
                                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item(key = "hero_balance") {
                Spacer(modifier = Modifier.height(16.dp))

                // Hero Balance Card
                HeroBalanceCard(
                    totalBalance = state.totalBalance,
                    percentageChange = state.balancePercentageChange
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item(key = "cash_flow") {
                // Cash Flow Insight Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .then(
                            if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
                                Modifier.shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x33000000))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 2.dp else 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.cash_flow_insight),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            SummaryPeriodDropdown(
                                selected = state.filter,
                                onSelected = { filter, start, end -> viewModel.onFilterSelected(filter, start, end) },
                                onCustomClick = { showDateRangePicker = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Income / Expenses / Net cash flow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(CurrencyFormatter.format(state.totalIncome), style = MaterialTheme.typography.titleMedium, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.transaction_income), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(CurrencyFormatter.format(state.totalExpense), style = MaterialTheme.typography.titleMedium, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.transaction_expense), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(CurrencyFormatter.format(state.netCashFlow), style = MaterialTheme.typography.titleMedium, color = if (state.netCashFlow >= 0) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.net_cash_flow), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        CashFlowChart(
                            dailyCashFlow = state.dailyCashFlow
                        )
                    }
                }

            }
            
            item(key = "spending_category") {
                Spacer(modifier = Modifier.height(24.dp))

                // Spending By Category Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .then(
                            if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
                                Modifier.shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x33000000))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 2.dp else 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spending by category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Type switcher (Expense vs Income Dropdown)
                            var typeMenuExpanded by remember { mutableStateOf(false) }
                            Box {
                                Row(
                                    modifier = Modifier.clickable { typeMenuExpanded = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (state.transactionType == TransactionType.INCOME) stringResource(R.string.transaction_income) else stringResource(R.string.transaction_expense),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Pilih Tipe",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = typeMenuExpanded,
                                    onDismissRequest = { typeMenuExpanded = false }
                                ) {
                                    TransactionType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(if (type == TransactionType.INCOME) stringResource(R.string.transaction_income) else stringResource(R.string.transaction_expense)) },
                                            onClick = {
                                                typeMenuExpanded = false
                                                if (state.transactionType != type) {
                                                    viewModel.onTransactionTypeSelected(type)
                                                }
                                            },
                                            trailingIcon = if (state.transactionType == type) {
                                                { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (state.items.isEmpty()) {
                            SummaryEmptyState(isIncome = isIncome, modifier = Modifier.height(150.dp))
                        } else {
                            // Donut Chart and Legend Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DonutChart(
                                    items = state.items,
                                    totalAmount = state.totalAmount,
                                    isIncome = isIncome,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Compact Legend
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.items.take(5).forEach { item ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(
                                                        color = com.example.expense_tracker.ui.theme.categoryColor(item.categoryId.toInt(), isIncome),
                                                        shape = CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.categoryName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${(item.percentage * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (state.items.size > 5) {
                                        Text(
                                            text = "+ ${state.items.size - 5} ${stringResource(R.string.others)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 18.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Total spending footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.total_spending),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyFormatter.format(state.totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!state.isLoading && state.items.isNotEmpty()) {
                items(state.items, key = { "${state.transactionType}_${it.categoryId}" }) { item ->
                    BreakdownCardItem(item = item, isIncome = isIncome, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }
        }
    }

    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(16.dp),
            confirmButton = {
                TextButton(onClick = {
                    dateRangePickerState.selectedStartDateMillis?.let { start ->
                        dateRangePickerState.selectedEndDateMillis?.let { end ->
                            viewModel.onFilterSelected(FilterPeriod.CUSTOM, start, end)
                            showDateRangePicker = false
                        }
                    }
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
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
}

// ── Previews ───────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun SummaryScreenEmptyPreview() {
    Expense_trackerTheme {
        Column(modifier = Modifier.fillMaxSize()) {
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
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items, key = { it.categoryId }) { item ->
                    BreakdownCardItem(item = item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }
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

