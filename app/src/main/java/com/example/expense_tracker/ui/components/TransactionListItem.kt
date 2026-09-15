package com.example.expense_tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.TimeFormatter
import com.example.expense_tracker.ui.theme.spacing

@Composable
fun TransactionListItem(
    transaction: ExpenseWithCategory,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val isIncome = transaction.type == com.example.expense_tracker.data.TransactionType.INCOME.name
    val amountPrefix = if (isIncome) "+" else "-"
    val amountColor = if (isIncome) Color(0xFF2E8B57) else MaterialTheme.colorScheme.error

    val iconBg = if (isIncome) {
        Color(0xFFE8F5E9)
    } else {
        com.example.expense_tracker.ui.theme.categoryColor(transaction.categoryId.toInt()).copy(alpha = 0.15f)
    }
    val iconTint = if (isIncome) {
        Color(0xFF1B5E20)
    } else {
        com.example.expense_tracker.ui.theme.categoryColor(transaction.categoryId.toInt())
    }

    val iconVector = if (isIncome) {
        Icons.AutoMirrored.Filled.TrendingUp
    } else {
        when (transaction.categoryId) {
            1L -> Icons.Default.Restaurant
            2L -> Icons.Default.DirectionsCar
            3L -> Icons.Default.ShoppingCart
            4L -> Icons.Default.Movie
            5L -> Icons.Default.Receipt
            6L -> Icons.Default.LocalHospital
            else -> Icons.Default.MoreHoriz
        }
    }

    ListItem(
        modifier = modifier.padding(vertical = spacing.space50),
        headlineContent = {
            Text(
                text = transaction.categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Column {
                if (transaction.merchant.isNotBlank()) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                if (transaction.isRecurring) {
                    Text(
                        text = stringResource(R.string.ai_recurring),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = TimeFormatter.formatTime(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        },
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = iconBg,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = transaction.categoryName,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
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

