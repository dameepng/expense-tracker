package com.example.expense_tracker.ui.home

import com.example.expense_tracker.ui.components.TransactionListItem
import com.example.expense_tracker.ui.theme.spacing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.Expense_trackerTheme
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource
import com.example.expense_tracker.R

// ── Custom Header ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    userName: String = "Pelanggan",
    userPhotoUri: String? = null,
    activeRemindersCount: Int = 0,
    onNavigateToReminder: () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    if (!userPhotoUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = userPhotoUri,
                            contentDescription = "Profil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            modifier = Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.welcome_back),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onNavigateToReminder) {
                if (activeRemindersCount > 0) {
                    androidx.compose.material3.BadgedBox(
                        badge = {
                            androidx.compose.material3.Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(text = activeRemindersCount.toString())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.bill_reminder),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.bill_reminder),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── Balance Card ───────────────────────────────────────────────────

@Composable
fun BalanceCard(
    totalBalance: Long,
    selectedWalletName: String,
    modifier: Modifier = Modifier,
    wallets: List<com.example.expense_tracker.data.Wallet> = emptyList(),
    selectedWalletId: Long? = null,
    onWalletSelected: (Long?) -> Unit = {}
) {
    val selectedWallet = wallets.find { it.id == selectedWalletId }
    val gradient = if (selectedWallet != null) {
        com.example.expense_tracker.ui.wallet.CardGradients.getGradient(selectedWallet.color).brush
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF2D2D3A),
                Color(0xFF1A1A2E)
            )
        )
    }

    val maskedCardNumber = if (selectedWallet != null && selectedWallet.cardNumber.isNotBlank()) {
        val digits = selectedWallet.cardNumber.filter { it.isDigit() }
        val masked = if (digits.length >= 4) {
            "•".repeat((digits.length - 4).coerceAtLeast(0)) + digits.takeLast(4)
        } else {
            digits
        }
        val padded = masked.padEnd(16, '•')
        padded.chunked(4).joinToString(" ")
    } else {
        "•••• •••• •••• 4020"
    }

    val spacing = MaterialTheme.spacing
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenMargin)
            .aspectRatio(1.8f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(spacing.cardPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Wallet Name & More Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = selectedWalletName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 8.dp, y = (-8).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.all_wallets)) },
                                onClick = {
                                    expanded = false
                                    if (selectedWalletId != null) {
                                        onWalletSelected(null)
                                    }
                                },
                                leadingIcon = {
                                    if (selectedWalletId == null) {
                                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                                    }
                                }
                            )
                            wallets.forEach { wallet ->
                                DropdownMenuItem(
                                    text = { Text(wallet.name) },
                                    onClick = {
                                        expanded = false
                                        if (selectedWalletId != wallet.id) {
                                            onWalletSelected(wallet.id)
                                        }
                                    },
                                    leadingIcon = {
                                        if (selectedWalletId == wallet.id) {
                                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Middle & Bottom Row Combined
                Column {
                    AutoResizeText(
                        text = CurrencyFormatter.format(totalBalance),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.total_balance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Bottom Row: Card number & Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = maskedCardNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            letterSpacing = 2.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        // Decorative mastercard-style circles
                        Box(modifier = Modifier.width(40.dp).height(24.dp)) {
                            Surface(
                                modifier = Modifier.size(24.dp).align(Alignment.CenterStart),
                                shape = CircleShape,
                                color = Color(0xFFEA001B).copy(alpha = 0.8f)
                            ) {}
                            Surface(
                                modifier = Modifier.size(24.dp).align(Alignment.CenterEnd),
                                shape = CircleShape,
                                color = Color(0xFFF79E1B).copy(alpha = 0.8f)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

// ── Income / Expense Summary ───────────────────────────────────────

@Composable
fun IncomeExpenseSummary(
    totalIncome: Long,
    totalExpense: Long,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenMargin),
        horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)
    ) {
        // Income Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier.padding(spacing.cardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = "Income",
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(spacing.space150))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.transaction_income),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AutoResizeText(
                        text = CurrencyFormatter.format(totalIncome),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }

        // Expense Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier.padding(spacing.cardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = "Expense",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(spacing.space150))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.transaction_expense),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AutoResizeText(
                        text = CurrencyFormatter.format(totalExpense),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ── Wallet Overview ───────────────────────────────────────────────

// ── Transaction List Item ────────────────────────────────────────────

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_recent_transactions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Home Screen ────────────────────────────────────────────────────

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

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    LocalContext.current

    LaunchedEffect(state.transactions.firstOrNull()?.id) {
        if (state.transactions.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val currentOnNavigateToInput by rememberUpdatedState(onNavigateToInput)
    val deletedMessage = stringResource(R.string.transaction_deleted)
    val cancelLabel = stringResource(R.string.cancel)
    val haptic = LocalHapticFeedback.current
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
        val spacing = MaterialTheme.spacing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
        
        Spacer(modifier = Modifier.height(spacing.sectionGap))

        // Balance Card
        BalanceCard(
            totalBalance = state.totalAmount,
            selectedWalletName = state.selectedWalletName,
            wallets = state.wallets,
            selectedWalletId = state.selectedWalletId,
            onWalletSelected = { viewModel.selectWallet(it) }
        )
        
        Spacer(modifier = Modifier.height(spacing.sectionGap))

        // Income / Expense Summary
        IncomeExpenseSummary(
            totalIncome = state.totalIncome,
            totalExpense = state.totalExpense
        )

        Spacer(modifier = Modifier.height(spacing.sectionGap))

        // Transactions Section Header
        Row(
            modifier = Modifier
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
                onClick = { onNavigateToSummary(state.selectedWalletId) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.view_all),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Expense list or empty state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.transactions.isEmpty()) {
                EmptyState(periodLabel = state.periodLabel)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(top = spacing.space100),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(
                        items = state.transactions,
                        key = { it.id }
                    ) { expense ->
                        val currentExpense by rememberUpdatedState(expense)
                        val confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = remember {
                            { dismissValue -> onDismissTransaction(currentExpense, dismissValue) }
                        }
                        
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = confirmValueChange
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val direction = dismissState.dismissDirection
                                val color = when (direction) {
                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green for edit
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error // Red for delete
                                    else -> Color.Transparent
                                }
                                val icon = when (direction) {
                                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                    else -> null
                                }
                                val alignment = when (direction) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    else -> Alignment.Center
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    if (icon != null) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TransactionListItem(
                                transaction = expense,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNavigateToInput(expense.id)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
    }
    }
}

// ── Helper ─────────────────────────────────────────────────────────

@Suppress("SameParameterValue")
@Composable
fun AutoResizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified
) {
    var textStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = textStyle,
        fontWeight = fontWeight,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { result ->
            if (result.didOverflowWidth || result.didOverflowHeight) {
                textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        }
    )
}

// ── Previews ───────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun BalanceCardPreview() {
    Expense_trackerTheme {
        BalanceCard(totalBalance = 150_000L, selectedWalletName = "All Wallets")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeHeaderPreview() {
    Expense_trackerTheme {
        HeaderSection()
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListItemPreview() {
    Expense_trackerTheme {
        Column {
            TransactionListItem(
                transaction = ExpenseWithCategory(
                    id = 1,
                    amount = 50_000L,
                    categoryId = 1,
                    categoryName = "Makanan",
                    description = "Makan Siang",
                    timestamp = System.currentTimeMillis(),
                    type = com.example.expense_tracker.data.TransactionType.EXPENSE.name
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
                    type = com.example.expense_tracker.data.TransactionType.INCOME.name
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_withData() {
    Expense_trackerTheme {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            HeaderSection()
            Spacer(modifier = Modifier.height(8.dp))
            BalanceCard(
                totalBalance = fakeState.totalAmount, 
                selectedWalletName = fakeState.selectedWalletName,
                wallets = emptyList(),
                selectedWalletId = null,
                onWalletSelected = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            IncomeExpenseSummary(totalIncome = fakeState.totalIncome, totalExpense = fakeState.totalExpense)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_transactions),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(
                        items = fakeState.transactions,
                        key = { it.id }
                    ) { expense ->
                        TransactionListItem(transaction = expense)
                    }
                }
            }
        }
    }
}
