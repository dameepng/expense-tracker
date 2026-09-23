package com.example.expense_tracker.ui.home

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.ui.components.TransactionListItem
import com.example.expense_tracker.ui.home.components.BalanceCard
import com.example.expense_tracker.ui.home.components.EmptyState
import com.example.expense_tracker.ui.home.components.HeaderSection
import com.example.expense_tracker.ui.home.components.IncomeExpenseSummary
import com.example.expense_tracker.ui.home.components.SwipeableTransactionItem
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme
import com.example.expense_tracker.ui.theme.spacing
import kotlinx.coroutines.launch

private val MAX_ADAPTIVE_WIDTH = 840.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToInput: (Long?) -> Unit = {},
    onNavigateToSummary: (Long?) -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToAiInput: () -> Unit = {},
    onNavigateToReceipt: () -> Unit = {},
    onNavigateToChat: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    ReportDrawnWhen { !state.isLoading }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var isFirstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(state.transactions.firstOrNull()?.id) {
        if (isFirstLoad) {
            isFirstLoad = false
            return@LaunchedEffect
        }
        if (state.transactions.isNotEmpty() && listState.firstVisibleItemIndex > 0) {
            listState.animateScrollToItem(0)
        }
    }

    val currentOnNavigateToInput by rememberUpdatedState(onNavigateToInput)
    val deletedMessage = stringResource(R.string.transaction_deleted)
    val cancelLabel = stringResource(R.string.cancel)

    val onDismissTransaction = remember(viewModel, coroutineScope, snackbarHostState, deletedMessage, cancelLabel, haptic) {
        { expense: ExpenseWithCategory, dismissValue: SwipeToDismissBoxValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteExpense(expense)
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = deletedMessage,
                            actionLabel = cancelLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteExpense(expense)
                        }
                    }
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    currentOnNavigateToInput(expense.id)
                    false
                }
                else -> false
            }
        }
    }

    val onWalletSelected = remember(viewModel) {
        { walletId: Long? -> viewModel.selectWallet(walletId) }
    }

    Scaffold(
        topBar = {
            HeaderSection(
                userName = state.userName,
                userPhotoUri = state.userPhotoUri,
                activeRemindersCount = state.activeRemindersCount,
                onNavigateToReminder = onNavigateToReminder
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        HomeScreenContent(
            state = state,
            onNavigateToInput = onNavigateToInput,
            onNavigateToSummary = onNavigateToSummary,
            onWalletSelected = onWalletSelected,
            onDismissTransaction = onDismissTransaction,
            listState = listState,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
internal fun HomeScreenContent(
    state: HomeUiState,
    onNavigateToInput: (Long?) -> Unit,
    onNavigateToSummary: (Long?) -> Unit,
    onWalletSelected: (Long?) -> Unit,
    onDismissTransaction: (ExpenseWithCategory, SwipeToDismissBoxValue) -> Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = MAX_ADAPTIVE_WIDTH)
        ) {
            Spacer(modifier = Modifier.height(spacing.sectionGap))

            BalanceCard(
                totalBalance = state.totalAmount,
                selectedWalletName = state.selectedWalletName,
                wallets = state.wallets,
                selectedWalletId = state.selectedWalletId,
                onWalletSelected = onWalletSelected
            )

            Spacer(modifier = Modifier.height(spacing.sectionGap))

            IncomeExpenseSummary(
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense
            )

            Spacer(modifier = Modifier.height(spacing.sectionGap))

            RecentTransactionsHeader(
                onViewAllClick = { onNavigateToSummary(state.selectedWalletId) }
            )

            HomeTransactionsList(
                isLoading = state.isLoading,
                transactions = state.transactions,
                periodLabel = state.periodLabel,
                listState = listState,
                onDismissTransaction = onDismissTransaction,
                onTransactionClick = onNavigateToInput,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun RecentTransactionsHeader(
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenMargin),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.recent_transactions),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        FilledTonalButton(
            onClick = onViewAllClick,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.view_all),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun HomeTransactionsList(
    isLoading: Boolean,
    transactions: List<ExpenseWithCategory>,
    periodLabel: String,
    listState: LazyListState,
    onDismissTransaction: (ExpenseWithCategory, SwipeToDismissBoxValue) -> Boolean,
    onTransactionClick: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (transactions.isEmpty()) {
            EmptyState(periodLabel = periodLabel)
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.space100),
                contentPadding = PaddingValues(
                    top = spacing.space100,
                    bottom = 96.dp,
                    start = spacing.screenMargin,
                    end = spacing.screenMargin
                )
            ) {
                items(
                    items = transactions,
                    key = { it.id },
                    contentType = { "transaction_item" }
                ) { expense ->
                    SwipeableTransactionItem(
                        expense = expense,
                        onDismiss = onDismissTransaction,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTransactionClick(expense.id)
                        }
                    )
                }
            }
        }
    }
}

// ── Previews ───────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun BalanceCardPreview() {
    ExpenseTrackerTheme {
        BalanceCard(totalBalance = 150_000L, selectedWalletName = "All Wallets")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeHeaderPreview() {
    ExpenseTrackerTheme {
        HeaderSection()
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListItemPreview() {
    ExpenseTrackerTheme {
        Column {
            TransactionListItem(
                transaction = ExpenseWithCategory(
                    id = 1,
                    amount = 50_000L,
                    categoryId = 1,
                    categoryName = "Makanan",
                    description = "Makan Siang",
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.EXPENSE.name
                )
            )
            TransactionListItem(
                transaction = ExpenseWithCategory(
                    id = 2,
                    amount = 5_000_000L,
                    categoryId = 0,
                    categoryName = "Gaji",
                    description = "Gaji Bulan Ini",
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.INCOME.name
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_withData() {
    ExpenseTrackerTheme {
        val fakeState = HomeUiState(
            periodLabel = "Hari Ini",
            totalAmount = 150_000L,
            totalIncome = 300_000L,
            totalExpense = 150_000L,
            transactions = listOf(
                ExpenseWithCategory(1, 50_000L, 1, "Makanan", "Baso", System.currentTimeMillis(), "EXPENSE"),
                ExpenseWithCategory(2, 35_000L, 2, "Transport", "", System.currentTimeMillis() - 3600_000, "EXPENSE"),
                ExpenseWithCategory(3, 65_000L, 3, "Belanja", "", System.currentTimeMillis() - 7200_000, "EXPENSE"),
            )
        )
        HomeScreenContent(
            state = fakeState,
            onNavigateToInput = {},
            onNavigateToSummary = {},
            onWalletSelected = {},
            onDismissTransaction = { _, _ -> false }
        )
    }
}
