package com.example.expense_tracker.ui.reminder

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.BillReminder
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.MaterialMotionTokens
import com.example.expense_tracker.ui.theme.categoryColor
import com.example.expense_tracker.ui.theme.motionScheme
import com.example.expense_tracker.ui.theme.spacing
import kotlinx.coroutines.launch

private enum class ReminderContentState {
    Loading,
    Empty,
    Content
}

private val CardShape = RoundedCornerShape(22.dp)
private val SquircleShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(8.dp)
private val StatusPillShape = RoundedCornerShape(10.dp)
private val ButtonShape = RoundedCornerShape(12.dp)
private val OverviewShape = RoundedCornerShape(24.dp)
private val EmptyStateShape = RoundedCornerShape(28.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderListViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.bill_deleted)
    val undoLabel = stringResource(R.string.bill_undo)
    val haptic = LocalHapticFeedback.current

    val onMarkAsPaid: (BillReminder) -> Unit = remember(viewModel) {
        { reminder -> viewModel.markAsPaid(reminder) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.bill_reminders_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
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
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val contentState = when {
            uiState.isLoading && uiState.activeReminders.isEmpty() -> ReminderContentState.Loading
            uiState.activeReminders.isEmpty() -> ReminderContentState.Empty
            else -> ReminderContentState.Content
        }

        Crossfade(
            targetState = contentState,
            animationSpec = tween(
                durationMillis = 200,
                easing = MaterialMotionTokens.EmphasizedDecelerate
            ),
            label = "reminder_content_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { state ->
            when (state) {
                ReminderContentState.Loading -> {
                    ExpressiveReminderLoadingState()
                }
                ReminderContentState.Empty -> {
                    BillEmptyState()
                }
                ReminderContentState.Content -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = MAX_REMINDER_LIST_WIDTH)
                                .padding(horizontal = spacing.screenMargin),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(
                                top = spacing.space100,
                                bottom = spacing.space400
                            )
                        ) {
                            // Hero Overview Card (Google I/O Material 3 Expressive)
                            item(key = "bill_overview_header", contentType = "bill_overview_header") {
                                BillOverviewCard(
                                    totalAmount = uiState.totalAmount,
                                    unpaidCount = uiState.unpaidCount,
                                    paidCount = uiState.paidCount
                                )
                            }

                            items(
                                items = uiState.activeReminders,
                                key = { it.reminder.id },
                                contentType = { "reminder_card" }
                            ) { item ->
                                val currentItem by rememberUpdatedState(item)
                                val confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = remember(item.reminder.id) {
                                    { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val deletedReminder = currentItem.reminder
                                            viewModel.deleteReminder(deletedReminder)
                                            coroutineScope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = deletedMessage,
                                                    actionLabel = undoLabel,
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.insertReminder(deletedReminder)
                                                }
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                }

                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = confirmValueChange
                                )

                                val onItemMarkPaid: () -> Unit = remember(item.reminder.id) {
                                    { onMarkAsPaid(item.reminder) }
                                }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CardShape)
                                                    .background(MaterialTheme.colorScheme.error, shape = CardShape)
                                                    .padding(end = 24.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.onError
                                                )
                                            }
                                        }
                                    },
                                    enableDismissFromStartToEnd = false,
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                                            placementSpec = null
                                        )
                                        .fillMaxWidth()
                                        .clip(CardShape)
                                ) {
                                    ReminderItemCard(
                                        item = item,
                                        onClickMarkAsPaid = onItemMarkPaid
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

/**
 * Material 3 Expressive Loading State for Reminder List.
 * Uses the official Material 3 Contained Loading Indicator with fluid shape morphing
 * and warm typography (https://m3.material.io/components/loading-indicator/overview).
 */
@Composable
fun ExpressiveReminderLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Material 3 Expressive Contained Loading Indicator
            com.example.expense_tracker.ui.components.MaterialContainedLoadingIndicator(
                containerSize = 64.dp,
                indicatorSize = 40.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                indicatorColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 2.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Informative Expressive Title
            Text(
                text = stringResource(R.string.bill_loading_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Description
            Text(
                text = stringResource(R.string.bill_loading_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Hero Overview Card displaying total monthly recurring bills and count of pending vs paid bills.
 */
@Composable
fun BillOverviewCard(
    totalAmount: Long,
    unpaidCount: Int,
    paidCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OverviewShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.bill_total_monthly).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = CurrencyFormatter.format(totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pending status badge
                val pendingBg = if (unpaidCount > 0) {
                    if (isDark) Color(0xFF382320) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
                val pendingTint = if (unpaidCount > 0) {
                    if (isDark) Color(0xFFFFB4AB) else MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    shape = StatusPillShape,
                    color = pendingBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = pendingTint,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.bill_pending_status, unpaidCount),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = pendingTint
                        )
                    }
                }

                // Completed status badge
                val paidBg = if (paidCount > 0) {
                    if (isDark) Color(0xFF163A27) else Color(0xFFE8F5E9)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
                val paidTint = if (paidCount > 0) {
                    if (isDark) Color(0xFF4ADE80) else Color(0xFF1B5E20)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    shape = StatusPillShape,
                    color = paidBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = paidTint,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.bill_paid_status, paidCount),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = paidTint
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2-Tier Material 3 Expressive Reminder Card.
 * Eliminates cramped single-line wrapping and eliminates duplicate disabled buttons.
 */
@Composable
fun ReminderItemCard(
    item: ReminderItemUiState,
    onClickMarkAsPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val iconTint = remember(item.reminder.categoryId) {
        categoryColor(item.reminder.categoryId.toInt())
    }
    val iconBg = remember(iconTint, isDark) {
        iconTint.copy(alpha = if (isDark) 0.22f else 0.14f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Upper Tier: Icon, Details, Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Squircle Category Badge
                Surface(
                    shape = SquircleShape,
                    color = iconBg,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(item.reminder.categoryId),
                            contentDescription = item.categoryName,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.reminder.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.categoryName} • ${item.walletName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = CurrencyFormatter.format(item.reminder.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.8.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Lower Tier: Metadata Pills (Left) & Action/Status (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Metadata Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Due Date Pill
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.bill_due_date_format, item.reminder.dueDay),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Repeat Pill
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.reminder.isRepeat) stringResource(R.string.bill_monthly_repeat) else stringResource(R.string.bill_one_time),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Right Status / Action Button
                if (item.isPaid) {
                    val paidBg = if (isDark) Color(0xFF163A27) else Color(0xFFE8F5E9)
                    val paidText = if (isDark) Color(0xFF4ADE80) else Color(0xFF1B5E20)
                    Surface(
                        shape = StatusPillShape,
                        color = paidBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = paidText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.bill_status_paid),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = paidText,
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClickMarkAsPaid()
                        },
                        shape = ButtonShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.bill_action_pay),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Expressive Empty State for Bill Reminders.
 */
@Composable
fun BillEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Surface(
                shape = EmptyStateShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.bill_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.bill_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

internal fun getCategoryIcon(categoryId: Long): ImageVector = when (categoryId) {
    1L -> Icons.Default.Restaurant
    2L -> Icons.Default.DirectionsCar
    3L -> Icons.Default.ShoppingCart
    4L -> Icons.Default.Movie
    5L -> Icons.Default.Receipt
    6L -> Icons.Default.LocalHospital
    else -> Icons.Default.Notifications
}

private val MAX_REMINDER_LIST_WIDTH = 840.dp

