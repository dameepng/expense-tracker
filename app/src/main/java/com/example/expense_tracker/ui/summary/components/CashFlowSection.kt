package com.example.expense_tracker.ui.summary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.summary.CashFlowChart
import com.example.expense_tracker.ui.summary.DailyCashFlow
import com.example.expense_tracker.ui.theme.spacing

private val CASH_FLOW_POSITIVE_COLOR = Color(0xFF10B981)
private val CASH_FLOW_NEGATIVE_COLOR = Color(0xFFEF4444)
private val CASH_FLOW_POSITIVE_BG_LIGHT = Color(0xFFECFDF5)
private val CASH_FLOW_NEGATIVE_BG_LIGHT = Color(0xFFFEF2F2)

/**
 * Card section presenting cash flow insights, net cash flow hero box, income/expense breakdown, and daily cash flow chart.
 */
@Composable
fun CashFlowSection(
    filter: FilterPeriod,
    netCashFlow: Long,
    totalIncome: Long,
    totalExpense: Long,
    dailyCashFlow: List<DailyCashFlow>,
    onFilterSelected: (FilterPeriod, Long?, Long?) -> Unit,
    onCustomFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenMargin),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.cardPadding)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cash_flow_insight),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                SummaryPeriodDropdown(
                    selected = filter,
                    onSelected = onFilterSelected,
                    onCustomClick = onCustomFilterClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero: Net Cash Flow
            NetCashFlowHero(netCashFlow = netCashFlow)

            Spacer(modifier = Modifier.height(12.dp))

            // Breakdown: Income & Expense (2 columns)
            IncomeExpenseBreakdownRow(
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )

            CashFlowChart(
                dailyCashFlow = dailyCashFlow
            )
        }
    }
}

@Composable
private fun NetCashFlowHero(
    netCashFlow: Long,
    modifier: Modifier = Modifier
) {
    val isDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isPositive = netCashFlow >= 0
    val heroBackground = if (isDarkMode) {
        if (isPositive) CASH_FLOW_POSITIVE_COLOR.copy(alpha = 0.12f) else CASH_FLOW_NEGATIVE_COLOR.copy(alpha = 0.12f)
    } else {
        if (isPositive) CASH_FLOW_POSITIVE_BG_LIGHT else CASH_FLOW_NEGATIVE_BG_LIGHT
    }
    val heroTextColor = if (isPositive) CASH_FLOW_POSITIVE_COLOR else CASH_FLOW_NEGATIVE_COLOR

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(heroBackground, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.net_cash_flow),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.format(netCashFlow),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = heroTextColor
            )
        }
    }
}

@Composable
private fun IncomeExpenseBreakdownRow(
    totalIncome: Long,
    totalExpense: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Income
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(CASH_FLOW_POSITIVE_COLOR, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.transaction_income),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(totalIncome),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CASH_FLOW_POSITIVE_COLOR
                )
            }
        }
        // Expense
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(CASH_FLOW_NEGATIVE_COLOR, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.transaction_expense),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(totalExpense),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CASH_FLOW_NEGATIVE_COLOR
                )
            }
        }
    }
}
