package com.example.expense_tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Repeat
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.TimeFormatter
import com.example.expense_tracker.ui.theme.spacing

@Composable
fun TransactionListItem(
    transaction: ExpenseWithCategory,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
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

    val cardShape = RoundedCornerShape(20.dp)
    val containerModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .clip(cardShape)
    }

    val title = transaction.merchant.takeIf { it.isNotBlank() }
        ?: transaction.description.takeIf { it.isNotBlank() }
        ?: transaction.categoryName

    val formattedDate = TimeFormatter.formatDate(transaction.timestamp)
    val subtitleText = if (title == transaction.categoryName) {
        formattedDate
    } else {
        "${transaction.categoryName} • $formattedDate"
    }

    Surface(
        modifier = containerModifier,
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        ListItem(
            modifier = Modifier.padding(horizontal = spacing.space50, vertical = spacing.space25),
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space50),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (transaction.isRecurring) Modifier.weight(1f, fill = false) else Modifier
                    )
                    if (transaction.isRecurring) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.transaction_recurring_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
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
                Text(
                    text = amountPrefix + CurrencyFormatter.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

