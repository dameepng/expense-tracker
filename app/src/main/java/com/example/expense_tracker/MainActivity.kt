package com.example.expense_tracker

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Color
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.UserPreferencesRepositoryImpl
import com.example.expense_tracker.data.ai.AiDependencies
import com.example.expense_tracker.data.ai.RoomTransactionDraftRepository
import com.example.expense_tracker.data.ai.chat.ChatContextProvider
import com.example.expense_tracker.data.ai.receipt.ReceiptImageProcessor
import com.example.expense_tracker.data.analytics.RoomFinancialSummarySource
import com.example.expense_tracker.data.dataStore
import com.example.expense_tracker.startup.StartupManager
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.ai.NaturalLanguageScreen
import com.example.expense_tracker.ui.ai.NaturalLanguageViewModel
import com.example.expense_tracker.ui.ai.NaturalLanguageViewModelFactory
import com.example.expense_tracker.ui.chat.ChatScreen
import com.example.expense_tracker.ui.chat.ChatViewModel
import com.example.expense_tracker.ui.chat.ChatViewModelFactory
import com.example.expense_tracker.ui.home.HomeScreen
import com.example.expense_tracker.ui.home.HomeViewModel
import com.example.expense_tracker.ui.home.HomeViewModelFactory
import com.example.expense_tracker.ui.input.InputScreen
import com.example.expense_tracker.ui.input.InputViewModel
import com.example.expense_tracker.ui.input.InputViewModelFactory
import com.example.expense_tracker.ui.navigation.AppNavigationDrawerContent
import com.example.expense_tracker.ui.navigation.AppNavigationRail
import com.example.expense_tracker.ui.navigation.BottomNavBar
import com.example.expense_tracker.ui.navigation.LocalNavAnimatedVisibilityScope
import com.example.expense_tracker.ui.navigation.LocalSharedTransitionScope
import com.example.expense_tracker.ui.navigation.NavMotion
import com.example.expense_tracker.ui.navigation.NavRoutes
import com.example.expense_tracker.ui.nfc.NfcQuickScanActivity
import com.example.expense_tracker.ui.onboarding.OnboardingScreen
import com.example.expense_tracker.ui.profile.HelpFaqScreen
import com.example.expense_tracker.ui.profile.PrivacyPolicyScreen
import com.example.expense_tracker.ui.profile.ProfileScreen
import com.example.expense_tracker.ui.profile.ProfileViewModel
import com.example.expense_tracker.ui.profile.ProfileViewModelFactory
import com.example.expense_tracker.ui.receipt.ReceiptPickerScreen
import com.example.expense_tracker.ui.receipt.ReceiptReviewScreen
import com.example.expense_tracker.ui.receipt.ReceiptScanViewModel
import com.example.expense_tracker.ui.receipt.ReceiptScanViewModelFactory
import com.example.expense_tracker.ui.reminder.ReminderListScreen
import com.example.expense_tracker.ui.reminder.ReminderListViewModel
import com.example.expense_tracker.ui.reminder.ReminderListViewModelFactory
import com.example.expense_tracker.ui.summary.SummaryScreen
import com.example.expense_tracker.ui.summary.SummaryViewModel
import com.example.expense_tracker.ui.summary.SummaryViewModelFactory
import com.example.expense_tracker.ui.summary.categorydetail.CategoryDetailScreen
import com.example.expense_tracker.ui.summary.categorydetail.CategoryDetailViewModel
import com.example.expense_tracker.ui.summary.categorydetail.CategoryDetailViewModelFactory
import com.example.expense_tracker.ui.theme.Expense_trackerTheme
import com.example.expense_tracker.ui.theme.MaterialMotionTokens
import com.example.expense_tracker.ui.wallet.WalletListScreen
import com.example.expense_tracker.ui.wallet.WalletViewModel
import com.example.expense_tracker.ui.wallet.WalletViewModelFactory
import com.example.expense_tracker.utils.AuthManager
import com.example.expense_tracker.utils.BiometricHelper

private const val EXTRA_NAV_ROUTE = "EXTRA_NAV_ROUTE"
private const val EXTRA_CARD_NOTE = "EXTRA_CARD_NOTE"
private const val EXTRA_CARD_AMOUNT = "EXTRA_CARD_AMOUNT"

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {

    private val pendingNavRoute = mutableStateOf<Triple<String, String?, Long?>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var isAppReady by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !isAppReady }
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val widthSizeClass = windowSizeClass.widthSizeClass
            val context = LocalContext.current
            val userPrefsRepo = remember(context) {
                UserPreferencesRepositoryImpl(context.dataStore)
            }
            val themeMode by userPrefsRepo.themeModeFlow.collectAsState(initial = "System Default")
            
            LaunchedEffect(userPrefsRepo) {
                userPrefsRepo.currencyFlow.collect { curr ->
                    CurrencyFormatter.setCurrency(curr)
                }
            }
            
            LaunchedEffect(userPrefsRepo) {
                userPrefsRepo.languageFlow.collect { lang ->
                    if (lang.isNotBlank()) {
                        val localeStr = if (lang == "English") "en" else "id"
                        val currentLocales = AppCompatDelegate.getApplicationLocales()
                        if (currentLocales.toLanguageTags() != localeStr) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(localeStr)
                            )
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                isAppReady = true
            }

            val isBiometricsEnabled by userPrefsRepo.isBiometricsEnabledFlow.collectAsState(initial = false)
            
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "Dark Mode" -> true
                "Light Mode" -> false
                else -> isSystemDark
            }

            DisposableEffect(darkTheme) {
                val transparent = Color.TRANSPARENT
                val systemBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(transparent)
                } else {
                    SystemBarStyle.light(transparent, transparent)
                }
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle,
                    navigationBarStyle = systemBarStyle
                )
                onDispose {}
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
                            if (AuthManager.lastBackgroundTime > 0 && timeElapsed > AuthManager.DEFAULT_TIMEOUT_MILLIS) {
                                AuthManager.lock()
                            }
                            
                            if (!AuthManager.isAuthenticated.value) {
                                val fragmentActivity = context as? FragmentActivity
                                if (fragmentActivity != null) {
                                    BiometricHelper.authenticate(
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
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Aplikasi Terkunci", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(onClick = {
                                    val fragmentActivity = context as? FragmentActivity
                                    if (fragmentActivity != null) {
                                        BiometricHelper.authenticate(
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
                        widthSizeClass = widthSizeClass,
                        pendingNavRoute = pendingNavRoute
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val route = intent?.getStringExtra(EXTRA_NAV_ROUTE)
        val note = intent?.getStringExtra(EXTRA_CARD_NOTE)
        val amount = intent?.getLongExtra(EXTRA_CARD_AMOUNT, 0L)?.takeIf { it > 0 }
        if (route != null) {
            pendingNavRoute.value = Triple(route, note, amount)
            intent.removeExtra(EXTRA_NAV_ROUTE)
            intent.removeExtra(EXTRA_CARD_NOTE)
            intent.removeExtra(EXTRA_CARD_AMOUNT)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    userPreferencesRepository: UserPreferencesRepository,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    pendingNavRoute: MutableState<Triple<String, String?, Long?>?> = remember { mutableStateOf(null) }
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    
    val homeViewModel: HomeViewModel = viewModel(
        factory = remember { HomeViewModelFactory.create(app) }
    )
    val reminderListViewModel: ReminderListViewModel = viewModel(
        factory = remember { ReminderListViewModelFactory(app) }
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
        // Run deferred background startup tasks once first frame is mounted
        StartupManager.getInstance(app).runDeferredTasks()
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navTarget by pendingNavRoute
    LaunchedEffect(navTarget) {
        val target = navTarget
        if (target != null) {
            val (route, note, amount) = target
            navController.navigate(route) {
                launchSingleTop = true
            }
            if (!note.isNullOrBlank()) {
                navController.currentBackStackEntry?.savedStateHandle?.set("initial_note", note)
            }
            if (amount != null && amount > 0) {
                navController.currentBackStackEntry?.savedStateHandle?.set("initial_amount", amount.toString())
            }
            pendingNavRoute.value = null
        }
    }

    val isReduceMotion = rememberIsReduceMotion()
    val showNavigation = NavRoutes.shouldShowBottomBar(currentRoute)
    val onNavigateTo: (String) -> Unit = remember(navController, currentRoute) {
        { route ->
            if (route == currentRoute || (route.startsWith("input") && currentRoute != null && currentRoute.startsWith("input"))) {
                // no-op
            } else {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
    }

    val onNavigateToInput = remember(navController) {
        { id: Long? -> navController.navigate(NavRoutes.inputRoute(id)) }
    }
    val onNavigateToSummary: (Long?) -> Unit = remember(navController) {
        { walletId: Long? ->
            navController.navigate(NavRoutes.SUMMARY) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("summary_wallet_id", walletId)
        }
    }
    val onNavigateToReminder: () -> Unit = remember(navController) {
        {
            navController.navigate(NavRoutes.REMINDER_LIST) {
                launchSingleTop = true
            }
        }
    }
    val onNavigateToAiInput: () -> Unit = remember(navController) {
        {
            if (navController.currentDestination?.route != NavRoutes.AI_INPUT) {
                navController.navigate(NavRoutes.AI_INPUT)
            }
        }
    }
    val onNavigateToReceipt: () -> Unit = remember(navController) {
        {
            if (navController.currentDestination?.route != NavRoutes.RECEIPT_PICKER) {
                navController.navigate(NavRoutes.RECEIPT_PICKER) { launchSingleTop = true }
            }
        }
    }
    val onNavigateToChat: () -> Unit = remember(navController) {
        {
            if (navController.currentDestination?.route != NavRoutes.CHAT) {
                navController.navigate(NavRoutes.CHAT) {
                    launchSingleTop = true
                }
            }
        }
    }
    val onNavigateBack: () -> Unit = remember(navController) {
        { navController.popBackStack() }
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
                                OnboardingScreen(
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
                                    onNavigateToInput = onNavigateToInput,
                                    onNavigateToSummary = onNavigateToSummary,
                                    onNavigateToReminder = onNavigateToReminder,
                                    onNavigateToAiInput = onNavigateToAiInput,
                                    onNavigateToReceipt = onNavigateToReceipt,
                                    onNavigateToChat = onNavigateToChat
                                )
                            }

                            composable(route = NavRoutes.AI_INPUT) { backStackEntry ->
                                val aiViewModel: NaturalLanguageViewModel = viewModel(
                                    viewModelStoreOwner = backStackEntry,
                                    factory = remember(backStackEntry) {
                                        NaturalLanguageViewModelFactory.create(app)
                                    }
                                )
                                NaturalLanguageScreen(
                                    viewModel = aiViewModel,
                                    onBack = { navController.popBackStack() },
                                    onSaved = { navController.popBackStack() }
                                )
                            }

                            composable(route = NavRoutes.RECEIPT_PICKER) { backStackEntry ->
                                val receiptViewModel: ReceiptScanViewModel = viewModel(
                                    viewModelStoreOwner = backStackEntry,
                                    factory = remember(backStackEntry) {
                                        ReceiptScanViewModelFactory.create(
                                            repository = AiDependencies.shared.createReceiptRepository(
                                                ReceiptImageProcessor(app.contentResolver)
                                            ),
                                            draftRepository = RoomTransactionDraftRepository(
                                                AppDatabase.getInstance(app)
                                            )
                                        )
                                    }
                                )
                                CompositionLocalProvider(
                                    LocalNavAnimatedVisibilityScope provides this@composable
                                ) {
                                    ReceiptPickerScreen(
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

                            composable(route = NavRoutes.RECEIPT_REVIEW) { backStackEntry ->
                                val parentEntry = remember(backStackEntry) {
                                    runCatching { navController.getBackStackEntry(NavRoutes.RECEIPT_PICKER) }.getOrNull() ?: backStackEntry
                                }
                                val receiptViewModel: ReceiptScanViewModel = viewModel(
                                    viewModelStoreOwner = parentEntry,
                                    factory = remember(parentEntry) {
                                        ReceiptScanViewModelFactory.create(
                                            repository = AiDependencies.shared.createReceiptRepository(
                                                ReceiptImageProcessor(app.contentResolver)
                                            ),
                                            draftRepository = RoomTransactionDraftRepository(
                                                AppDatabase.getInstance(app)
                                            )
                                        )
                                    }
                                )
                                CompositionLocalProvider(
                                    LocalNavAnimatedVisibilityScope provides this@composable
                                ) {
                                    ReceiptReviewScreen(
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
                                val chatViewModel: ChatViewModel = viewModel(
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
                                val initialNote = backStackEntry.savedStateHandle.get<String>("initial_note")
                                val initialAmount = backStackEntry.savedStateHandle.get<String>("initial_amount")
                                val factory = remember(expenseId, initialNote, initialAmount) {
                                    InputViewModelFactory.create(app, expenseId, initialNote, initialAmount)
                                }
                                val inputViewModel: InputViewModel = viewModel(
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
                                val summaryViewModel: SummaryViewModel = viewModel(
                                    viewModelStoreOwner = backStackEntry,
                                    factory = remember(backStackEntry) { SummaryViewModelFactory.create(app) }
                                )
                                val walletId = backStackEntry.savedStateHandle.get<Long?>("summary_wallet_id")
                                if (walletId != null) {
                                    summaryViewModel.onWalletSelected(walletId)
                                    backStackEntry.savedStateHandle.remove<Long?>("summary_wallet_id")
                                }
                                
                                SummaryScreen(
                                    viewModel = summaryViewModel,
                                    onCategoryClick = { categoryId, selectedWalletId, startTime, endTime ->
                                        navController.navigate(
                                            NavRoutes.categoryDetailRoute(categoryId, selectedWalletId, startTime, endTime)
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
                                    CategoryDetailViewModelFactory(app)
                                }
                                val viewModel: CategoryDetailViewModel = viewModel(
                                    viewModelStoreOwner = backStackEntry,
                                    factory = factory
                                )
                                CategoryDetailScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToInput = { id -> navController.navigate(NavRoutes.inputRoute(id)) }
                                )
                            }
                            
                            composable(route = NavRoutes.WALLET) { backStackEntry ->
                                val walletViewModel: WalletViewModel = viewModel(
                                    viewModelStoreOwner = backStackEntry,
                                    factory = remember(backStackEntry) {
                                        WalletViewModelFactory.create(app)
                                    }
                                )
                                WalletListScreen(
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

                            composable(route = NavRoutes.PROFILE) { backStackEntry ->
                                val profileViewModel: ProfileViewModel = viewModel(
                                    viewModelStoreOwner = backStackEntry,
                                    factory = remember(backStackEntry) {
                                        ProfileViewModelFactory.create(app)
                                    }
                                )
                                ProfileScreen(
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
                                        if (navController.currentDestination?.route != NavRoutes.RECEIPT_PICKER) {
                                            navController.navigate(NavRoutes.RECEIPT_PICKER) {
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    onNavigateToNfcScan = {
                                        val nfcIntent = Intent(context, NfcQuickScanActivity::class.java)
                                        context.startActivity(nfcIntent)
                                    }
                                )
                            }
                            
                            composable(NavRoutes.HELP_FAQ) {
                                HelpFaqScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(NavRoutes.PRIVACY_POLICY) {
                                PrivacyPolicyScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable(NavRoutes.REMINDER_LIST) {
                                ReminderListScreen(
                                    viewModel = reminderListViewModel,
                                    onNavigateBack = onNavigateBack
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
                                durationMillis = MaterialMotionTokens.DurationMedium2,
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
                        BottomNavBar(
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
private fun rememberIsReduceMotion(): Boolean {
    val context = LocalContext.current
    var isReduceMotion by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        isReduceMotion = checkReduceMotion(context)
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
