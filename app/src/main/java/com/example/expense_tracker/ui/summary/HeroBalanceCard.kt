package com.example.expense_tracker.ui.summary

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme
import com.example.expense_tracker.ui.theme.financialNegativeColor
import com.example.expense_tracker.ui.theme.financialNeutralColor
import com.example.expense_tracker.ui.theme.financialPositiveColor
import com.example.expense_tracker.ui.theme.spacing
import java.util.Locale
import kotlin.math.abs

/**
 * Premium quiet hero card displaying total balance with high typographic hierarchy,
 * hoisted balance privacy toggle, semantic trend delta, and subtle secondary iconography.
 */
@Composable
fun HeroBalanceCard(
    totalBalance: Long,
    percentageChange: Float,
    modifier: Modifier = Modifier,
    isBalanceVisible: Boolean = true,
    onToggleBalanceVisibility: () -> Unit = {}
) {
    val spacing = MaterialTheme.spacing
    val isZeroDelta = abs(percentageChange) < 0.05f
    val isPositiveDelta = percentageChange > 0f && !isZeroDelta

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
            // Header: Section label + Privacy toggle & subtle icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total_balance).uppercase(Locale.forLanguageTag("id-ID")),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleBalanceVisibility,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(R.string.privacy_toggle_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.space100))

            // Primary Financial Value: Large, scannable, high contrast
            Text(
                text = if (isBalanceVisible) CurrencyFormatter.format(totalBalance) else stringResource(R.string.privacy_hidden_amount),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(spacing.space150))

            // Semantic trend row: neutral zero delta handling
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isZeroDelta -> {
                        Text(
                            text = stringResource(R.string.balance_no_change),
                            color = financialNeutralColor(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    isPositiveDelta -> {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = financialPositiveColor(),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(Locale.forLanguageTag("id-ID"), "%.1f", percentageChange)}% ${stringResource(R.string.vs_last_period)}",
                            color = financialPositiveColor(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = financialNegativeColor(),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(Locale.forLanguageTag("id-ID"), "%.1f", abs(percentageChange))}% ${stringResource(R.string.vs_last_period)}",
                            color = financialNegativeColor(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeroBalanceCardPositivePreview() {
    ExpenseTrackerTheme {
        HeroBalanceCard(
            totalBalance = 200_084_000L,
            percentageChange = 12.4f
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeroBalanceCardZeroPreview() {
    ExpenseTrackerTheme {
        HeroBalanceCard(
            totalBalance = 200_084_000L,
            percentageChange = 0.0f
        )
    }
}
