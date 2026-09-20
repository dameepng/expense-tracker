package com.example.expense_tracker.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.ui.components.TransactionListItem
import com.example.expense_tracker.ui.theme.motionScheme

private val SWIPE_EDIT_COLOR = Color(0xFF4CAF50)

/**
 * List item wrapping TransactionListItem with SwipeToDismissBox gestures for quick edit (left-to-right) and delete (right-to-left).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazyItemScope.SwipeableTransactionItem(
    expense: ExpenseWithCategory,
    onDismiss: (ExpenseWithCategory, SwipeToDismissBoxValue) -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentExpense by rememberUpdatedState(expense)
    val confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = remember(expense.id) {
        { dismissValue -> onDismiss(currentExpense, dismissValue) }
    }

    @Suppress("DEPRECATION")
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = confirmValueChange
    )

    val swipeShape = remember { RoundedCornerShape(20.dp) }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeDismissBackground(
                direction = dismissState.dismissDirection,
                shape = swipeShape
            )
        },
        modifier = modifier
            .animateItem(
                fadeInSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            )
            .fillMaxWidth()
            .clip(swipeShape)
    ) {
        TransactionListItem(
            transaction = expense,
            onClick = onClick
        )
    }
}

@Composable
private fun SwipeDismissBackground(
    direction: SwipeToDismissBoxValue,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    val color = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> SWIPE_EDIT_COLOR
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
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
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(color, shape)
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
}
