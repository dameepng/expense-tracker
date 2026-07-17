package com.example.expense_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.getValue
import com.example.expense_tracker.ui.home.HomeScreen
import com.example.expense_tracker.ui.home.HomeViewModel
import com.example.expense_tracker.ui.home.HomeViewModelFactory
import com.example.expense_tracker.ui.home.StreakCounterViewModel
import com.example.expense_tracker.ui.home.StreakViewModelFactory
import com.example.expense_tracker.ui.input.InputScreen
import com.example.expense_tracker.ui.input.InputViewModel
import com.example.expense_tracker.ui.input.InputViewModelFactory
import com.example.expense_tracker.ui.navigation.NavRoutes
import com.example.expense_tracker.ui.summary.SummaryScreen
import com.example.expense_tracker.ui.summary.SummaryViewModel
import com.example.expense_tracker.ui.summary.SummaryViewModelFactory
import com.example.expense_tracker.ui.theme.Expense_trackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Expense_trackerTheme {
                ExpenseTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            com.example.expense_tracker.ui.navigation.BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route.startsWith("input")) {
                        navController.navigate(route)
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(NavRoutes.HOME) {
                val homeViewModel: HomeViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(factory = HomeViewModelFactory.create(applicationContext()))
                val streakViewModel: StreakCounterViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(factory = StreakViewModelFactory.create(applicationContext()))
                HomeScreen(
                    viewModel = homeViewModel,
                    streakViewModel = streakViewModel,
                    onNavigateToInput = { id -> navController.navigate(NavRoutes.inputRoute(id)) },
                    onNavigateToSummary = { navController.navigate(NavRoutes.SUMMARY) }
                )
            }

            composable(
                route = NavRoutes.INPUT,
                arguments = listOf(navArgument("expenseId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId")?.toLongOrNull()
                val inputViewModel: InputViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(factory = InputViewModelFactory.create(applicationContext(), expenseId))
                InputScreen(
                    viewModel = inputViewModel,
                    onSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.SUMMARY) {
                val summaryViewModel: SummaryViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(factory = SummaryViewModelFactory.create(applicationContext()))
                SummaryScreen(
                    viewModel = summaryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(NavRoutes.WALLET) {
                val walletViewModel: com.example.expense_tracker.ui.wallet.WalletViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.expense_tracker.ui.wallet.WalletViewModelFactory.create(applicationContext()))
                com.example.expense_tracker.ui.wallet.WalletListScreen(
                    viewModel = walletViewModel
                )
            }
        }
    }
}

@Composable
private fun applicationContext() =
    androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
