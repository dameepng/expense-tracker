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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.summary.CashFlowChart
import com.example.expense_tracker.ui.summary.DailyCashFlow
import com.example.expense_tracker.ui.theme.financialNegativeColor
import com.example.expense_tracker.ui.theme.financialNeutralColor
import com.example.expense_tracker.ui.theme.financialPositiveColor
import com.example.expense_tracker.ui.theme.spacing

/**
 * Simplified, calm Cash Flow section.
 * Replaces nested cards with clean whitespace, strong financial typography,
 * paired income/expense indicators, interactive chart, and contextual insights.
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
    modifier: Modifier = Modifier,
    isBalanceVisible: Boolean = true,
    topCategoryName: String? = null,
    topCategoryPercentage: Int? = null
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
            // ── Title & Period Selector ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cash_flow_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SummaryPeriodDropdown(
                    selected = filter,
                    onSelected = onFilterSelected,
                    onCustomClick = onCustomFilterClick
                )
            }

            Spacer(modifier = Modifier.height(spacing.space200))

            // ── Net Cash Flow (Clear typography without nested cards) ──
            Text(
                text = if (isBalanceVisible) CurrencyFormatter.format(netCashFlow) else stringResource(R.string.privacy_hidden_amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(spacing.space25))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.net_cash_flow),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(spacing.space100))
                val isZero = netCashFlow == 0L
                val isPositive = netCashFlow > 0L
                Text(
                    text = when {
                        isZero -> stringResource(R.string.cash_flow_balanced)
                        isPositive -> stringResource(R.string.cash_flow_positive)
                        else -> stringResource(R.string.cash_flow_negative)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isZero -> financialNeutralColor()
                        isPositive -> financialPositiveColor()
                        else -> financialNegativeColor()
                    }
                )
            }

            Spacer(modifier = Modifier.height(spacing.space200))

            // ── Paired Income & Expense Metrics ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(financialPositiveColor(), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.transaction_income),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.space50))
                    Text(
                        text = if (isBalanceVisible) CurrencyFormatter.format(totalIncome) else stringResource(R.string.privacy_hidden_amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = financialPositiveColor()
                    )
                }

                Spacer(modifier = Modifier.width(spacing.space150))

                // Expense
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(financialNegativeColor(), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.transaction_expense),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.space50))
                    Text(
                        text = if (isBalanceVisible) CurrencyFormatter.format(totalExpense) else stringResource(R.string.privacy_hidden_amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = financialNegativeColor()
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.space150))

            // ── Cash Flow Chart ──
            CashFlowChart(
                dailyCashFlow = dailyCashFlow,
                netCashFlow = netCashFlow,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                isBalanceVisible = isBalanceVisible,
                periodDescription = stringResource(filter.labelResId)
            )

            // ── Contextual Insight ──
            val insight = remember(totalIncome, totalExpense, topCategoryName, topCategoryPercentage) {
                calculateContextualInsight(totalIncome, totalExpense, topCategoryName, topCategoryPercentage)
            }

            if (insight != null) {
                Spacer(modifier = Modifier.height(spacing.space150))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

private fun calculateContextualInsight(
    totalIncome: Long,
    totalExpense: Long,
    topCategoryName: String?,
    topCategoryPercentage: Int?
): String? {
    return when {
        totalIncome == 0L && totalExpense == 0L -> null
        totalIncome > 0L && totalExpense == 0L -> "💡 Seluruh arus kas periode ini adalah pemasukan."
        totalExpense > 0L && totalIncome == 0L -> {
            if (topCategoryName != null && topCategoryPercentage != null) {
                "💡 $topCategoryName menjadi kategori pengeluaran terbesar ($topCategoryPercentage%)."
            } else {
                "💡 Belum ada pemasukan yang tercatat pada periode ini."
            }
        }
        totalIncome >= totalExpense * 1.5 -> "💡 Pemasukan periode ini jauh lebih tinggi daripada pengeluaran."
        totalExpense > totalIncome -> "💡 Pengeluaran periode ini melebihi pemasukan."
        topCategoryName != null && topCategoryPercentage != null && topCategoryPercentage >= 30 -> {
            "💡 $topCategoryName menjadi kategori pengeluaran terbesar ($topCategoryPercentage%)."
        }
        else -> "💡 Arus kas stabil, pemasukan dan pengeluaran seimbang."
    }
}
