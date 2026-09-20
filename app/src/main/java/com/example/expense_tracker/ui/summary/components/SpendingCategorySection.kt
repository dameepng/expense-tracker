package com.example.expense_tracker.ui.summary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.summary.BreakdownItem
import com.example.expense_tracker.ui.summary.DonutChart
import com.example.expense_tracker.ui.theme.categoryColor
import com.example.expense_tracker.ui.theme.spacing

private const val MAX_LEGEND_ITEMS = 5
private const val SPENDING_BY_CATEGORY_TITLE = "Spending by category"
private const val CHOOSE_TYPE_DESCRIPTION = "Pilih Tipe"

/**
 * Card section displaying spending/income by category with DonutChart, legend, and total spending footer.
 */
@Composable
fun SpendingCategorySection(
    transactionType: TransactionType,
    items: List<BreakdownItem>,
    isLoading: Boolean,
    totalAmount: Long,
    onTransactionTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val isIncome = transactionType == TransactionType.INCOME

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
                    text = SPENDING_BY_CATEGORY_TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TransactionTypeSwitcher(
                    currentType = transactionType,
                    onTypeSelected = onTransactionTypeSelected
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                items.isEmpty() -> {
                    SummaryEmptyState(isIncome = isIncome, modifier = Modifier.height(150.dp))
                }
                else -> {
                    // Donut Chart and Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutChart(
                            items = items,
                            isIncome = isIncome,
                            modifier = Modifier.weight(1f)
                        )

                        // Compact Legend
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items.take(MAX_LEGEND_ITEMS).forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                color = categoryColor(item.categoryId.toInt(), isIncome),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.categoryName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${(item.percentage * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (items.size > MAX_LEGEND_ITEMS) {
                                Text(
                                    text = "+ ${items.size - MAX_LEGEND_ITEMS} ${stringResource(R.string.others)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Total spending footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.total_spending),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionTypeSwitcher(
    currentType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }
    val currentLabel = if (currentType == TransactionType.INCOME) {
        stringResource(R.string.transaction_income)
    } else {
        stringResource(R.string.transaction_expense)
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { typeMenuExpanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = CHOOSE_TYPE_DESCRIPTION,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = typeMenuExpanded,
            onDismissRequest = { typeMenuExpanded = false }
        ) {
            TransactionType.entries.forEach { type ->
                val typeLabel = if (type == TransactionType.INCOME) {
                    stringResource(R.string.transaction_income)
                } else {
                    stringResource(R.string.transaction_expense)
                }
                DropdownMenuItem(
                    text = { Text(typeLabel) },
                    onClick = {
                        typeMenuExpanded = false
                        if (currentType != type) {
                            onTypeSelected(type)
                        }
                    },
                    trailingIcon = if (currentType == type) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}
