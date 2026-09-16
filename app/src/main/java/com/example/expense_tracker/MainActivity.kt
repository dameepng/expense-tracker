package com.example.expense_tracker

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset
import com.example.expense_tracker.ui.theme.MaterialMotionTokens
import com.example.expense_tracker.ui.theme.motionScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.example.expense_tracker.ui.navigation.AppNavigationDrawerContent
import com.example.expense_tracker.ui.navigation.AppNavigationRail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import com.example.expense_tracker.ui.navigation.NavMotion
import com.example.expense_tracker.ui.navigation.LocalSharedTransitionScope
import com.example.expense_tracker.ui.navigation.LocalNavAnimatedVisibilityScope
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.ai.AiDependencies
import com.example.expense_tracker.data.ai.chat.ChatContextProvider
import com.example.expense_tracker.data.analytics.RoomFinancialSummarySource
import com.example.expense_tracker.data.dataStore
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.chat.ChatScreen
import com.example.expense_tracker.ui.chat.ChatViewModel
import com.example.expense_tracker.ui.chat.ChatViewModelFactory
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
import com.example.expense_tracker.utils.AuthManager
import java.util.Locale

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val widthSizeClass = windowSizeClass.widthSizeClass
            val context = LocalContext.current
            val userPrefsRepo = remember(context) {
                com.example.expense_tracker.data.UserPreferencesRepositoryImpl(context.dataStore)
            }
            val themeMode by userPrefsRepo.themeModeFlow.collectAsState(initial = "System Default")
            val currency by userPrefsRepo.currencyFlow.collectAsState(initial = "IDR")
            val language by userPrefsRepo.languageFlow.collectAsState(initial = null)
            
            LaunchedEffect(currency) {
                CurrencyFormatter.setCurrency(currency)
            }
            
            LaunchedEffect(language) {
                if (language != null) {
                    val localeStr = if (language == "English") "en" else "id"
                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                        androidx.core.os.LocaleListCompat.forLanguageTags(localeStr)
                    )
                }
            }
            
            val isBiometricsEnabledState = userPrefsRepo.isBiometricsEnabledFlow.collectAsState(initial = null)
            val isBiometricsEnabled = isBiometricsEnabledState.value ?: return@setContent
            
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "Dark Mode" -> true
                "Light Mode" -> false
                else -> isSystemDark
            }

            SideEffect {
                val transparent = android.graphics.Color.TRANSPARENT
                val systemBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(transparent)
                } else {
                    SystemBarStyle.light(transparent, transparent)
                }
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle,
                    navigationBarStyle = systemBarStyle
                )
            }
            
            val isAuthenticated by AuthManager.isAuthenticated.collectAsState()
            val lifecycleOwner = LocalLifecycleOwner.current
            
            DisposableEffect(lifecycleOwner, isBiometricsEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        if (isBiometricsEnabled) {
                            AuthManager.lastBackgroundTime = System.currentTimeMillis()
                        }
                    } else if (event == Lifecycle.Event.ON_START) {
                        if (isBiometricsEnabled) {
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
                        } else {
                            AuthManager.unlock()
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            Expense_trackerTheme(darkTheme = darkTheme) {
                if (isBiometricsEnabled && !isAuthenticated) {
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
                    ExpenseTrackerApp(
                        userPreferencesRepository = userPrefsRepo,
                        widthSizeClass = widthSizeClass
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    userPreferencesRepository: UserPreferencesRepository,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    
    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = HomeViewModelFactory.create(app))
    androidx.lifecycle.viewmodel.compose.viewModel<StreakCounterViewModel>(factory = StreakViewModelFactory.create(app))
    val summaryViewModel: SummaryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = SummaryViewModelFactory.create(app))
    val walletViewModel: com.example.expense_tracker.ui.wallet.WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.expense_tracker.ui.wallet.WalletViewModelFactory.create(app))
    val profileViewModel: com.example.expense_tracker.ui.profile.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.expense_tracker.ui.profile.ProfileViewModelFactory.create(app))
    val reminderListViewModel: com.example.expense_tracker.ui.reminder.ReminderListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.expense_tracker.ui.reminder.ReminderListViewModelFactory(app))
    val receiptViewModel: com.example.expense_tracker.ui.receipt.ReceiptScanViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.example.expense_tracker.ui.receipt.ReceiptScanViewModelFactory.create(
                repository = AiDependencies.shared.createReceiptRepository(
                    com.example.expense_tracker.data.ai.receipt.ReceiptImageProcessor(app.contentResolver)
                ),
                draftRepository = com.example.expense_tracker.data.ai.RoomTransactionDraftRepository(
                    AppDatabase.getInstance(app)
                )
            )
        )
    
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

    val isReduceMotion = rememberIsReduceMotion()
    val showNavigation = NavRoutes.shouldShowBottomBar(currentRoute)
    val onNavigateTo: (String) -> Unit = { route ->
        if (route == currentRoute || (route.startsWith("input") && currentRoute != null && currentRoute.startsWith("input"))) {
            // no-op
        } else {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }

    val contentScaffold = @Composable {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            @OptIn(ExperimentalSharedTransitionApi::class)
            SharedTransitionLayout {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.HOME,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { NavMotion.enterTransition(this, isReduceMotion) },
                        exitTransition = { NavMotion.exitTransition(this, isReduceMotion) },
                        popEnterTransition = { NavMotion.popEnterTransition(this, isReduceMotion) },
                        popExitTransition = { NavMotion.popExitTransition(this, isReduceMotion) }
                    ) {
            composable(route = NavRoutes.ONBOARDING) {
                com.example.expense_tracker.ui.onboarding.OnboardingScreen(
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = NavRoutes.HOME) { backStackEntry ->
                val shouldRefresh by backStackEntry.savedStateHandle.getStateFlow("refresh_home", false).collectAsState()
                LaunchedEffect(shouldRefresh) {
                    if (shouldRefresh) {
                        // homeViewModel.refresh()
                        backStackEntry.savedStateHandle["refresh_home"] = false
                    }
                }
                
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToInput = { id -> navController.navigate(NavRoutes.inputRoute(id)) },
                    onNavigateToSummary = { walletId ->
                        navController.navigate(NavRoutes.SUMMARY) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.let { it["summary_wallet_id"] = walletId }
                    },
                    onNavigateToReminder = { navController.navigate(NavRoutes.REMINDER_LIST) },
                    onNavigateToAiInput = { if (navController.currentDestination?.route != NavRoutes.AI_INPUT) navController.navigate(NavRoutes.AI_INPUT) },
                    onNavigateToReceipt = {
                        receiptViewModel.reset()
                        if (navController.currentDestination?.route != NavRoutes.RECEIPT_PICKER) {
                            navController.navigate(NavRoutes.RECEIPT_PICKER) { launchSingleTop = true }
                        }
                    },
                    onNavigateToChat = {
                        if (navController.currentDestination?.route != NavRoutes.CHAT) {
                            navController.navigate(NavRoutes.CHAT) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(route = NavRoutes.AI_INPUT) { backStackEntry ->
                val aiViewModel: com.example.expense_tracker.ui.ai.NaturalLanguageViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = remember(backStackEntry) {
                            com.example.expense_tracker.ui.ai.NaturalLanguageViewModelFactory.create(app)
                        }
                    )
                com.example.expense_tracker.ui.ai.NaturalLanguageScreen(
                    viewModel = aiViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(route = NavRoutes.RECEIPT_PICKER) {
                CompositionLocalProvider(
                    LocalNavAnimatedVisibilityScope provides this@composable
                ) {
                    com.example.expense_tracker.ui.receipt.ReceiptPickerScreen(
                        onBack = {
                            receiptViewModel.reset()
                            navController.popBackStack()
                        },
                        onScan = { uri ->
                            receiptViewModel.selectImage(uri)
                            receiptViewModel.startScan()
                            navController.navigate(NavRoutes.RECEIPT_REVIEW) { launchSingleTop = true }
                        }
                    )
                }
            }

            composable(route = NavRoutes.RECEIPT_REVIEW) {
                CompositionLocalProvider(
                    LocalNavAnimatedVisibilityScope provides this@composable
                ) {
                    com.example.expense_tracker.ui.receipt.ReceiptReviewScreen(
                        viewModel = receiptViewModel,
                        onBack = {
                            receiptViewModel.reset()
                            navController.popBackStack()
                        },
                        onManualInput = { navController.navigate(NavRoutes.AI_INPUT) { launchSingleTop = true } },
                        onSaved = {
                            receiptViewModel.reset()
                            navController.popBackStack(NavRoutes.HOME, inclusive = false)
                        }
                    )
                }
            }

            composable(route = NavRoutes.CHAT) { backStackEntry ->
                val viewModelFactory = remember(backStackEntry, userPreferencesRepository) {
                    val database = AppDatabase.getInstance(app)
                    val contextSource = ChatContextProvider(
                        financialSummarySource = RoomFinancialSummarySource(database.expenseDao()),
                        userPreferencesRepository = userPreferencesRepository
                    )
                    ChatViewModelFactory.create(
                        aiDependencies = AiDependencies.shared,
                        contextSource = contextSource
                    )
                }
                val chatViewModel: ChatViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = viewModelFactory
                    )
                ChatScreen(
                    viewModel = chatViewModel,
                    onBack = { navController.popBackStack() }
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
                val factory = remember(expenseId) {
                    InputViewModelFactory.create(app, expenseId)
                }
                val inputViewModel: InputViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = factory
                    )
                InputScreen(
                    viewModel = inputViewModel,
                    onSaved = { 
                        navController.previousBackStackEntry?.savedStateHandle?.let { it["refresh_home"] = true }
                        navController.popBackStack() 
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWallet = {
                        navController.navigate(NavRoutes.WALLET) {
                            popUpTo(NavRoutes.INPUT) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = NavRoutes.SUMMARY) { backStackEntry ->
                val walletId = backStackEntry.savedStateHandle.get<Long?>("summary_wallet_id")
                if (walletId != null) {
                    summaryViewModel.onWalletSelected(walletId)
                    backStackEntry.savedStateHandle.remove<Long?>("summary_wallet_id")
                }
                
                SummaryScreen(
                    viewModel = summaryViewModel,
                    onCategoryClick = { categoryId, walletId, startTime, endTime ->
                        navController.navigate(
                            NavRoutes.categoryDetailRoute(categoryId, walletId, startTime, endTime)
                        )
                    }
                )
            }
            
            composable(
                route = NavRoutes.CATEGORY_DETAIL,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType },
                    navArgument("walletId") { type = NavType.StringType; nullable = true },
                    navArgument("startTime") { type = NavType.StringType },
                    navArgument("endTime") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val factory = remember(backStackEntry) {
                    com.example.expense_tracker.ui.summary.categorydetail.CategoryDetailViewModelFactory(app)
                }
                val viewModel: com.example.expense_tracker.ui.summary.categorydetail.CategoryDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = factory
                )
                com.example.expense_tracker.ui.summary.categorydetail.CategoryDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToInput = { id -> navController.navigate(NavRoutes.inputRoute(id)) }
                )
            }
            
            composable(route = NavRoutes.WALLET) {
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

            composable(route = NavRoutes.PROFILE) {
                com.example.expense_tracker.ui.profile.ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToHelpFaq = { navController.navigate(NavRoutes.HELP_FAQ) },
                    onNavigateToPrivacyPolicy = { navController.navigate(NavRoutes.PRIVACY_POLICY) },
                    onNavigateToChat = {
                        if (navController.currentDestination?.route != NavRoutes.CHAT) {
                            navController.navigate(NavRoutes.CHAT) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToAiInput = {
                        if (navController.currentDestination?.route != NavRoutes.AI_INPUT) {
                            navController.navigate(NavRoutes.AI_INPUT) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToReceipt = {
                        receiptViewModel.reset()
                        if (navController.currentDestination?.route != NavRoutes.RECEIPT_PICKER) {
                            navController.navigate(NavRoutes.RECEIPT_PICKER) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            
            composable(NavRoutes.HELP_FAQ) {
                com.example.expense_tracker.ui.profile.HelpFaqScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.PRIVACY_POLICY) {
                com.example.expense_tracker.ui.profile.PrivacyPolicyScreen(
                    onNavigateBack = { navController.popBackStack() }
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


        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            AnimatedVisibility(
                visible = showNavigation,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    animationSpec = tween(
                        durationMillis = MaterialMotionTokens.DurationShort4,
                        easing = MaterialMotionTokens.EmphasizedDecelerate
                    ),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(MaterialMotionTokens.DurationShort4)),
                exit = slideOutVertically(
                    animationSpec = tween(
                        durationMillis = MaterialMotionTokens.DurationShort4,
                        easing = MaterialMotionTokens.EmphasizedAccelerate
                    ),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(MaterialMotionTokens.DurationShort4))
            ) {
                com.example.expense_tracker.ui.navigation.BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = onNavigateTo
                )
            }
        }
    }
}
}

    if (showNavigation && widthSizeClass == WindowWidthSizeClass.Expanded) {
        PermanentNavigationDrawer(
            drawerContent = {
                AppNavigationDrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = onNavigateTo
                )
            }
        ) {
            contentScaffold()
        }
    } else if (showNavigation && widthSizeClass == WindowWidthSizeClass.Medium) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(
                currentRoute = currentRoute,
                onNavigate = onNavigateTo
            )
            Box(modifier = Modifier.weight(1f)) {
                contentScaffold()
            }
        }
    } else {
        contentScaffold()
    }
}

@Composable
private fun applicationContext() =
    LocalContext.current.applicationContext as android.app.Application

@Composable
private fun rememberIsReduceMotion(): Boolean {
    val context = LocalContext.current
    var isReduceMotion by remember {
        mutableStateOf(checkReduceMotion(context))
    }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isReduceMotion = checkReduceMotion(context)
            }
        }
        val uri1 = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        val uri2 = Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE)
        context.contentResolver.registerContentObserver(uri1, false, observer)
        context.contentResolver.registerContentObserver(uri2, false, observer)
        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
    return isReduceMotion
}

private fun checkReduceMotion(context: Context): Boolean {
    return try {
        val animatorScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        val transitionScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        animatorScale == 0f || transitionScale == 0f
    } catch (_: Exception) {
        false
    }
}
