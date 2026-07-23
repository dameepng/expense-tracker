package com.example.expense_tracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.expense_tracker.data.dataStore
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.expense_tracker.utils.AuthManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val userPrefsRepo = remember(context) {
                com.example.expense_tracker.data.UserPreferencesRepositoryImpl(context.dataStore)
            }
            val themeMode by userPrefsRepo.themeModeFlow.collectAsState(initial = "System Default")
            
            val isBiometricsEnabledState = userPrefsRepo.isBiometricsEnabledFlow.collectAsState(initial = null)
            val isBiometricsEnabled = isBiometricsEnabledState.value
            
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "Dark Mode" -> true
                "Light Mode" -> false
                else -> isSystemDark
            }
            
            val isAuthenticated by AuthManager.isAuthenticated.collectAsState()
            val lifecycleOwner = LocalLifecycleOwner.current
            
            DisposableEffect(lifecycleOwner, isBiometricsEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        if (isBiometricsEnabled == true) {
                            AuthManager.lastBackgroundTime = System.currentTimeMillis()
                        }
                    } else if (event == Lifecycle.Event.ON_START) {
                        if (isBiometricsEnabled == true) {
                            val timeElapsed = System.currentTimeMillis() - AuthManager.lastBackgroundTime
                            if (AuthManager.lastBackgroundTime > 0 && timeElapsed > 5 * 60 * 1000) {
                                AuthManager.lock()
                            }
                            
                            if (!AuthManager.isAuthenticated.value) {
                                val fragmentActivity = context as? FragmentActivity
                                if (fragmentActivity != null) {
                                    com.example.expense_tracker.utils.BiometricHelper.authenticate(
                                        activity = fragmentActivity,
                                        title = "Kasflow Terkunci",
                                        subtitle = "Gunakan sidik jari untuk membuka Kasflow",
                                        onSuccess = { AuthManager.unlock() },
                                        onError = {}
                                    )
                                }
                            }
                        } else if (isBiometricsEnabled == false) {
                            AuthManager.unlock()
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            
            if (isBiometricsEnabled == null) {
                // Return empty screen while reading DataStore to prevent bypass
                return@setContent
            }

            Expense_trackerTheme(darkTheme = darkTheme) {
                if (isBiometricsEnabled == true && !isAuthenticated) {
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(64.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Aplikasi Terkunci", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(32.dp))
                                androidx.compose.material3.Button(onClick = {
                                    val fragmentActivity = context as? FragmentActivity
                                    if (fragmentActivity != null) {
                                        com.example.expense_tracker.utils.BiometricHelper.authenticate(
                                            activity = fragmentActivity,
                                            title = "Kasflow Terkunci",
                                            subtitle = "Gunakan sidik jari untuk membuka Kasflow",
                                            onSuccess = { AuthManager.unlock() },
                                            onError = {}
                                        )
                                    }
                                }) {
                                    Text("Buka Kunci")
                                }
                            }
                        }
                    }
                } else {
                    ExpenseTrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp() {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    
    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = HomeViewModelFactory.create(app))
    val streakViewModel: StreakCounterViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = StreakViewModelFactory.create(app))
    val summaryViewModel: SummaryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = SummaryViewModelFactory.create(app))
    val walletViewModel: com.example.expense_tracker.ui.wallet.WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.expense_tracker.ui.wallet.WalletViewModelFactory.create(app))
    val profileViewModel: com.example.expense_tracker.ui.profile.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.expense_tracker.ui.profile.ProfileViewModelFactory.create(app))
    val reminderListViewModel: com.example.expense_tracker.ui.reminder.ReminderListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.expense_tracker.ui.reminder.ReminderListViewModelFactory(app))
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission granted or denied if needed
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            com.example.expense_tracker.ui.navigation.BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == currentRoute || (route.startsWith("input") && currentRoute?.startsWith("input") == true)) {
                        return@BottomNavBar
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME, // We could make this dynamic based on preferences later, but HOME is fine for now
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { slideInHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> fullWidth / 3 }) + fadeIn(animationSpec = tween(200)) },
            exitTransition = { slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> -fullWidth / 3 }) + fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { slideInHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> -fullWidth / 3 }) + fadeIn(animationSpec = tween(200)) },
            popExitTransition = { slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> fullWidth / 3 }) + fadeOut(animationSpec = tween(200)) }
        ) {
            composable(
                route = NavRoutes.ONBOARDING,
                enterTransition = { fadeIn(animationSpec = tween(150)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) }
            ) {
                com.example.expense_tracker.ui.onboarding.OnboardingScreen(
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = NavRoutes.HOME,
                enterTransition = { fadeIn(animationSpec = tween(150)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) }
            ) { backStackEntry ->
                val shouldRefresh by backStackEntry.savedStateHandle.getStateFlow("refresh_home", false).collectAsState()
                LaunchedEffect(shouldRefresh) {
                    if (shouldRefresh) {
                        // homeViewModel.refresh()
                        backStackEntry.savedStateHandle.set("refresh_home", false)
                    }
                }
                
                HomeScreen(
                    viewModel = homeViewModel,
                    streakViewModel = streakViewModel,
                    onNavigateToInput = { id -> navController.navigate(NavRoutes.inputRoute(id)) },
                    onNavigateToSummary = {
                        navController.navigate(NavRoutes.SUMMARY) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToReminder = { navController.navigate(NavRoutes.REMINDER_LIST) }
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
                    onSaved = { 
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh_home", true)
                        navController.popBackStack() 
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.SUMMARY,
                enterTransition = { fadeIn(animationSpec = tween(150)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) }
            ) {
                SummaryScreen(
                    viewModel = summaryViewModel
                )
            }
            
            composable(
                route = NavRoutes.WALLET,
                enterTransition = { fadeIn(animationSpec = tween(150)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) }
            ) {
                com.example.expense_tracker.ui.wallet.WalletListScreen(
                    viewModel = walletViewModel,
                    onSelectWallet = { walletId ->
                        walletViewModel.selectWallet(walletId)
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.PROFILE,
                enterTransition = { fadeIn(animationSpec = tween(150)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) }
            ) {
                com.example.expense_tracker.ui.profile.ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutSuccess = {
                        navController.navigate(NavRoutes.ONBOARDING) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(NavRoutes.REMINDER_LIST) {
                com.example.expense_tracker.ui.reminder.ReminderListScreen(
                    viewModel = reminderListViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun applicationContext() =
    androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
