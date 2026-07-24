package com.example.expense_tracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        // Home
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentRoute == NavRoutes.HOME,
            onClick = { onNavigate(NavRoutes.HOME) }
        )
        // Wallet
        NavigationBarItem(
            icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_wallet)) },
            selected = currentRoute == NavRoutes.WALLET,
            onClick = { onNavigate(NavRoutes.WALLET) }
        )
        // Add
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.nav_add),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            selected = currentRoute?.startsWith("input") == true,
            onClick = { onNavigate(NavRoutes.inputRoute(null)) }
        )
        // Summary
        NavigationBarItem(
            icon = { Icon(Icons.Filled.PieChart, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_summary)) },
            selected = currentRoute == NavRoutes.SUMMARY,
            onClick = { onNavigate(NavRoutes.SUMMARY) }
        )
        // Profile
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_profile)) },
            selected = currentRoute == NavRoutes.PROFILE,
            onClick = { onNavigate(NavRoutes.PROFILE) }
        )
    }
}
