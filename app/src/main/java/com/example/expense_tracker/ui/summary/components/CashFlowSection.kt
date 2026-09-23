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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Cash Flow section matching 1:1 with design spec.
 * Displays net cash flow, status pill, paired income/expense cards, and interactive trend chart.
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBg = if (isDark) Color(0xFF16191E) else MaterialTheme.colorScheme.surfaceContainerLow
    val metricCardBg = if (isDark) Color(0xFF1E222A) else MaterialTheme.colorScheme.surfaceContainer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenMargin),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // ── Title & Period Selector ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cash_flow_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                SummaryPeriodDropdown(
                    selected = filter,
                    onSelected = onFilterSelected,
                    onCustomClick = onCustomFilterClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Net Cash Flow Hero ──
            Text(
                text = if (isBalanceVisible) CurrencyFormatter.format(netCashFlow) else stringResource(R.string.privacy_hidden_amount),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Subtitle (Arus kas bersih + Status) ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.net_cash_flow),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = Color(0xFF8E95A3)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                val isZero = netCashFlow == 0L
                val isPositive = netCashFlow > 0L
                val statusColor = when {
                    isZero -> financialNeutralColor()
                    isPositive -> if (isDark) Color(0xFF22C55E) else Color(0xFF0F8A58)
                    else -> if (isDark) Color(0xFFF87171) else Color(0xFFC5221F)
                }
                Text(
                    text = when {
                        isZero -> stringResource(R.string.cash_flow_balanced)
                        isPositive -> stringResource(R.string.cash_flow_positive)
                        else -> stringResource(R.string.cash_flow_negative)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Paired Income & Expense Metric Cards ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = metricCardBg
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (isDark) Color(0xFF132B22) else Color(0xFFD1FAE5),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF22C55E) else Color(0xFF0F8A58),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.transaction_income),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E95A3)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBalanceVisible) CurrencyFormatter.format(totalIncome) else stringResource(R.string.privacy_hidden_amount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF22C55E) else Color(0xFF0F8A58)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Expense Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = metricCardBg
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (isDark) Color(0xFF331D24) else Color(0xFFFFE4E6),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFF87171) else Color(0xFFC5221F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.transaction_expense),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E95A3)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBalanceVisible) CurrencyFormatter.format(totalExpense) else stringResource(R.string.privacy_hidden_amount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFF87171) else Color(0xFFC5221F)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Cash Flow Chart ──
            CashFlowChart(
                dailyCashFlow = dailyCashFlow,
                netCashFlow = netCashFlow,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                isBalanceVisible = isBalanceVisible,
                periodDescription = stringResource(filter.labelResId)
            )
        }
    }
}

