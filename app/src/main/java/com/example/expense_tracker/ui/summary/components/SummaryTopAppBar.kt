package com.example.expense_tracker.ui.summary.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.expense_tracker.R
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme

/**
 * Top app bar for summary screen containing title, custom date filter button, and optional wallet selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryTopAppBar(
    currentFilter: FilterPeriod,
    wallets: List<Wallet>,
    selectedWalletId: Long?,
    onCustomFilterClick: () -> Unit,
    onWalletSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_summary),
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = onCustomFilterClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.filter_custom),
                    tint = if (currentFilter == FilterPeriod.CUSTOM) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            WalletDropdownAction(
                wallets = wallets,
                selectedWalletId = selectedWalletId,
                onWalletSelected = onWalletSelected
            )
        },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    )
}

@Composable
private fun WalletDropdownAction(
    wallets: List<Wallet>,
    selectedWalletId: Long?,
    onWalletSelected: (Long?) -> Unit
) {
    var walletMenuExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { walletMenuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = stringResource(R.string.input_choose_wallet),
                tint = if (selectedWalletId != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        DropdownMenu(
            expanded = walletMenuExpanded,
            onDismissRequest = { walletMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.all_wallets)) },
                onClick = {
                    walletMenuExpanded = false
                    if (selectedWalletId != null) {
                        onWalletSelected(null)
                    }
                },
                trailingIcon = if (selectedWalletId == null) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
            wallets.forEach { wallet ->
                DropdownMenuItem(
                    text = { Text(wallet.name) },
                    onClick = {
                        walletMenuExpanded = false
                        if (selectedWalletId != wallet.id) {
                            onWalletSelected(wallet.id)
                        }
                    },
                    trailingIcon = if (selectedWalletId == wallet.id) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryTopAppBarPreview() {
    ExpenseTrackerTheme {
        val sampleWallets = listOf(
            Wallet(id = 1, name = "Dompet Utama", balance = 5_000_000L),
            Wallet(id = 2, name = "BCA", balance = 12_500_000L)
        )
        SummaryTopAppBar(
            currentFilter = FilterPeriod.MONTH,
            wallets = sampleWallets,
            selectedWalletId = null,
            onCustomFilterClick = {},
            onWalletSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryTopAppBarSelectedWalletPreview() {
    ExpenseTrackerTheme {
        val sampleWallets = listOf(
            Wallet(id = 1, name = "Dompet Utama", balance = 5_000_000L),
            Wallet(id = 2, name = "BCA", balance = 12_500_000L)
        )
        SummaryTopAppBar(
            currentFilter = FilterPeriod.CUSTOM,
            wallets = sampleWallets,
            selectedWalletId = 2L,
            onCustomFilterClick = {},
            onWalletSelected = {}
        )
    }
}

