package com.example.expense_tracker.ui.summary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.summary.BreakdownItem
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme
import com.example.expense_tracker.ui.theme.categoryColor
import com.example.expense_tracker.ui.theme.spacing

/**
 * Card item representing a category breakdown with expressive squircle icon, progress bar, percentage pill, and amount.
 */
@Composable
fun BreakdownCardItem(
    item: BreakdownItem,
    modifier: Modifier = Modifier,
    isIncome: Boolean = false,
    onClick: () -> Unit = {}
) {
    val spacing = MaterialTheme.spacing
    val icon = when (item.categoryId) {
        1L -> Icons.Default.Restaurant
        2L -> Icons.Default.DirectionsCar
        3L -> Icons.Default.ShoppingCart
        4L -> Icons.Default.Movie
        5L -> Icons.Default.Receipt
        6L -> Icons.Default.LocalHospital
        else -> Icons.Default.MoreHoriz
    }

    val itemCategoryColor = categoryColor(item.categoryId.toInt(), isIncome)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expressive Squircle Leading Category Icon
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = itemCategoryColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.categoryName,
                        tint = itemCategoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(spacing.space150))

            // Center Content: Category Name, Progress Bar, and Percentage Pill
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space75)
                ) {
                    Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = itemCategoryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${(item.percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = itemCategoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.space100))

                LinearProgressIndicator(
                    progress = { item.percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.space75),
                    color = itemCategoryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.width(spacing.space150))

            // Trailing Content: Amount and Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyFormatter.format(item.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(spacing.space50))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BreakdownCardItemPreview() {
    ExpenseTrackerTheme {
        BreakdownCardItem(
            item = BreakdownItem(1, "Makanan", 50_000L, 0.33f)
        )
    }
}
