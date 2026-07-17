package com.example.expense_tracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        // Home
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            selected = currentRoute == NavRoutes.HOME,
            onClick = { onNavigate(NavRoutes.HOME) }
        )
        // Summary
        NavigationBarItem(
            icon = { Icon(Icons.Filled.PieChart, contentDescription = "Summary") },
            selected = currentRoute == NavRoutes.SUMMARY,
            onClick = { onNavigate(NavRoutes.SUMMARY) }
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
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            selected = currentRoute?.startsWith("input") == true,
            onClick = { onNavigate(NavRoutes.inputRoute(null)) }
        )
        // Profile
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            selected = false,
            onClick = { /* TODO: Profile Screen */ }
        )
    }
}
