package com.example.expense_tracker.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.foundation.layout.WindowInsets
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
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.TimeFormatter
import com.example.expense_tracker.ui.theme.Expense_trackerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource
import com.example.expense_tracker.R

// ── Custom Header ───────────────────────────────────────────────────

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    userName: String = "Pelanggan",
    userPhotoUri: String? = null,
    activeRemindersCount: Int = 0,
    onNavigateToReminder: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                if (userPhotoUri != null && userPhotoUri.isNotEmpty()) {
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
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        IconButton(
            onClick = onNavigateToReminder,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
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
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Balance Card ───────────────────────────────────────────────────

@Composable
fun BalanceCard(
    totalBalance: Long,
    selectedWalletName: String,
    wallets: List<com.example.expense_tracker.data.Wallet> = emptyList(),
    selectedWalletId: Long? = null,
    onWalletSelected: (Long?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedWallet = wallets.find { it.id == selectedWalletId }
    val gradient = if (selectedWallet != null) {
        com.example.expense_tracker.ui.wallet.CardGradients.getGradient(selectedWallet.color).brush
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(
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

    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .aspectRatio(1.8f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(24.dp)
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Income Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = "Income",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.transaction_income),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AutoResizeText(
                        text = CurrencyFormatter.format(totalIncome),
                        style = MaterialTheme.typography.titleMedium,
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
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = "Expense",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.transaction_expense),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AutoResizeText(
                        text = CurrencyFormatter.format(totalExpense),
                        style = MaterialTheme.typography.titleMedium,
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

@Composable
fun WalletOverview(
    wallets: List<com.example.expense_tracker.data.Wallet>,
    onWalletClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (wallets.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.wallets),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = wallets,
                key = { it.id }
            ) { wallet ->
                Card(
                    onClick = { onWalletClick(wallet.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.width(140.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = wallet.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = CurrencyFormatter.format(wallet.balance),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ── Transaction List Item ────────────────────────────────────────────

@Composable
fun TransactionListItem(
    transaction: ExpenseWithCategory,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == com.example.expense_tracker.data.TransactionType.INCOME.name
    val amountPrefix = if (isIncome) "+" else "-"
    val amountColor = if (isIncome) Color(0xFF2E8B57) else MaterialTheme.colorScheme.error

    ListItem(
        modifier = modifier.padding(vertical = 4.dp),
        headlineContent = {
            Text(
                text = transaction.categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = TimeFormatter.formatTime(transaction.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingContent = {
            if (isIncome) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = "Income",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = com.example.expense_tracker.ui.theme.categoryColor(transaction.categoryId.toInt()).copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = com.example.expense_tracker.ui.theme.categoryColor(transaction.categoryId.toInt()),
                            modifier = Modifier.size(16.dp)
                        ) {}
                    }
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountPrefix + CurrencyFormatter.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
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
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(R.string.no_recent_transactions),
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

// ── Home Screen ────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    streakViewModel: StreakCounterViewModel? = null,
    onNavigateToInput: (Long?) -> Unit = {},
    onNavigateToSummary: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    
    val streakState by (streakViewModel?.uiState ?: MutableStateFlow(StreakCounterUiState())).collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(state.transactions.firstOrNull()?.id) {
        if (state.transactions.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val currentOnNavigateToInput by rememberUpdatedState(onNavigateToInput)
    val onDismissTransaction = remember(viewModel, coroutineScope, snackbarHostState) {
        { expense: ExpenseWithCategory, dismissValue: SwipeToDismissBoxValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    viewModel.deleteExpense(expense)
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = context.getString(R.string.transaction_deleted),
                            actionLabel = context.getString(R.string.cancel),
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteExpense(expense)
                        }
                    }
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
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
                onNavigateToReminder = onNavigateToReminder,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        
        Spacer(modifier = Modifier.height(8.dp))

        // Balance Card
        BalanceCard(
            totalBalance = state.totalAmount,
            selectedWalletName = state.selectedWalletName,
            wallets = state.wallets,
            selectedWalletId = state.selectedWalletId,
            onWalletSelected = { viewModel.selectWallet(it) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Income / Expense Summary
        IncomeExpenseSummary(
            totalIncome = state.totalIncome,
            totalExpense = state.totalExpense
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Transactions Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                onClick = onNavigateToSummary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = stringResource(R.string.view_all),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Expense list or empty state
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
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
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .background(color, RoundedCornerShape(12.dp))
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
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            TransactionListItem(
                                transaction = expense,
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

// ── Helper ─────────────────────────────────────────────────────────

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
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(
                        items = fakeState.transactions,
                        key = { it.id }
                    ) { expense ->
                        TransactionListItem(transaction = expense, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}
