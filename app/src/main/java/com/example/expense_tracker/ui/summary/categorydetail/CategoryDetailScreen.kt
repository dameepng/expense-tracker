package com.example.expense_tracker.ui.summary.categorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.ui.components.TransactionListItem
import com.example.expense_tracker.ui.theme.motionScheme
import com.example.expense_tracker.ui.theme.spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToInput: (Long?) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = state.category?.name ?: stringResource(R.string.category),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_transactions),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val currentOnNavigateToInput by rememberUpdatedState(onNavigateToInput)
                val deletedMessage = stringResource(R.string.transaction_deleted)
                val cancelLabel = stringResource(R.string.cancel)
                val onDismissTransaction = remember(viewModel, coroutineScope, snackbarHostState, deletedMessage, cancelLabel) {
                    { expense: ExpenseWithCategory, dismissValue: SwipeToDismissBoxValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.EndToStart -> {
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
                                currentOnNavigateToInput(expense.id)
                                false
                            }
                            else -> false
                        }
                    }
                }

                val spacing = MaterialTheme.spacing
                val swipeShape = RoundedCornerShape(SWIPE_SHAPE_RADIUS)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacing.space100),
                    contentPadding = PaddingValues(bottom = 80.dp, top = spacing.space100, start = spacing.screenMargin, end = spacing.screenMargin)
                ) {
                    items(
                        items = state.transactions,
                        key = { it.id }
                    ) { expense ->
                        val currentExpense by rememberUpdatedState(expense)
                        val confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = remember(expense.id) {
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
                                    SwipeToDismissBoxValue.StartToEnd -> SWIPE_EDIT_COLOR // Green for edit
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
                                        .clip(swipeShape)
                                        .background(color, swipeShape)
                                        .padding(horizontal = 24.dp),
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
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                                    fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                                    placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                )
                                .fillMaxWidth()
                                .clip(swipeShape)
                        ) {
                            val onItemClick = remember(expense.id) {
                                { onNavigateToInput(expense.id) }
                            }
                            TransactionListItem(
                                transaction = expense,
                                onClick = onItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}

private val SWIPE_SHAPE_RADIUS = 20.dp
private val SWIPE_EDIT_COLOR = Color(0xFF4CAF50)

