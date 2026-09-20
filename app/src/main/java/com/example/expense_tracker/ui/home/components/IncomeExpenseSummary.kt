package com.example.expense_tracker.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.components.AutoResizeText
import com.example.expense_tracker.ui.theme.spacing

private val INCOME_CONTAINER_COLOR = Color(0xFFE8F5E9)
private val INCOME_ICON_COLOR = Color(0xFF1B5E20)

/**
 * Dual summary cards displaying total income and total expense for the selected period.
 */
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
        SummaryMetricCard(
            label = stringResource(R.string.transaction_income),
            amount = totalIncome,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconContainerColor = INCOME_CONTAINER_COLOR,
            iconTint = INCOME_ICON_COLOR,
            modifier = Modifier.weight(1f)
        )

        SummaryMetricCard(
            label = stringResource(R.string.transaction_expense),
            amount = totalExpense,
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            iconContainerColor = MaterialTheme.colorScheme.errorContainer,
            iconTint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    amount: Long,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconContainerColor,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.space150))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AutoResizeText(
                    text = CurrencyFormatter.format(amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}
