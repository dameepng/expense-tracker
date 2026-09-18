package com.example.expense_tracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.theme.spacing

/**
 * M3 Expressive Navigation Rail untuk ukuran layar Medium (600dp - 839dp).
 * Cocok untuk Foldable unfolded, tablet potret, dan smartphone dalam mode lanskap.
 */
@Composable
fun AppNavigationRail(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val itemColors = NavigationRailItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onSurface,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        header = {
            Spacer(modifier = Modifier.height(spacing.space100))
            FloatingActionButton(
                onClick = {
                    onNavigate(NavRoutes.inputRoute(null))
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.nav_add),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(spacing.space200))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            // Home
            NavigationRailItem(
                selected = currentRoute == NavRoutes.HOME,
                onClick = {
                    onNavigate(NavRoutes.HOME)
                },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_home), fontWeight = FontWeight.Medium) },
                colors = itemColors
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Wallet
            NavigationRailItem(
                selected = currentRoute == NavRoutes.WALLET,
                onClick = {
                    onNavigate(NavRoutes.WALLET)
                },
                icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_wallet), fontWeight = FontWeight.Medium) },
                colors = itemColors
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Summary
            NavigationRailItem(
                selected = currentRoute == NavRoutes.SUMMARY,
                onClick = {
                    onNavigate(NavRoutes.SUMMARY)
                },
                icon = { Icon(Icons.Filled.PieChart, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_summary), fontWeight = FontWeight.Medium) },
                colors = itemColors
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Profile
            NavigationRailItem(
                selected = currentRoute == NavRoutes.PROFILE,
                onClick = {
                    onNavigate(NavRoutes.PROFILE)
                },
                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_profile), fontWeight = FontWeight.Medium) },
                colors = itemColors
            )
        }
    }
}

/**
 * M3 Expressive Permanent Navigation Drawer untuk ukuran layar Expanded (≥ 840dp).
 * Cocok untuk tablet lanskap, desktop, dan Chromebook.
 */
@Composable
fun AppNavigationDrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val spacing = MaterialTheme.spacing
    PermanentDrawerSheet(
        modifier = modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = spacing.screenMargin, vertical = spacing.sectionGap)
                .verticalScroll(rememberScrollState())
        ) {
            // App Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space150),
                modifier = Modifier.padding(horizontal = spacing.space150, vertical = spacing.space100)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Smart Financial Tracker",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.sectionGap))

            // Primary Hero Action Button (M3 Expressive)
            Button(
                onClick = {
                    onNavigate(NavRoutes.inputRoute(null))
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(spacing.space100))
                Text(
                    text = stringResource(R.string.nav_add),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(spacing.space350))

            // Navigation Destinations
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_home), fontWeight = FontWeight.SemiBold) },
                selected = currentRoute == NavRoutes.HOME,
                onClick = {
                    onNavigate(NavRoutes.HOME)
                },
                shape = RoundedCornerShape(16.dp),
                colors = itemColors,
                modifier = Modifier.padding(vertical = spacing.space50)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_wallet), fontWeight = FontWeight.SemiBold) },
                selected = currentRoute == NavRoutes.WALLET,
                onClick = {
                    onNavigate(NavRoutes.WALLET)
                },
                shape = RoundedCornerShape(16.dp),
                colors = itemColors,
                modifier = Modifier.padding(vertical = spacing.space50)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.PieChart, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_summary), fontWeight = FontWeight.SemiBold) },
                selected = currentRoute == NavRoutes.SUMMARY,
                onClick = {
                    onNavigate(NavRoutes.SUMMARY)
                },
                shape = RoundedCornerShape(16.dp),
                colors = itemColors,
                modifier = Modifier.padding(vertical = spacing.space50)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_profile), fontWeight = FontWeight.SemiBold) },
                selected = currentRoute == NavRoutes.PROFILE,
                onClick = {
                    onNavigate(NavRoutes.PROFILE)
                },
                shape = RoundedCornerShape(16.dp),
                colors = itemColors,
                modifier = Modifier.padding(vertical = spacing.space50)
            )
        }
    }
}
