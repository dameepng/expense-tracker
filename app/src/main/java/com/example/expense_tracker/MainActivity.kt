package com.example.expense_tracker

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
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
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.UserPreferencesRepositoryImpl
import com.example.expense_tracker.data.dataStore
import com.example.expense_tracker.startup.StartupManager
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.components.BiometricLockScreen
import com.example.expense_tracker.ui.home.HomeViewModel
import com.example.expense_tracker.ui.home.HomeViewModelFactory
import com.example.expense_tracker.ui.navigation.AppNavHost
import com.example.expense_tracker.ui.navigation.AppNavigationDrawerContent
import com.example.expense_tracker.ui.navigation.AppNavigationRail
import com.example.expense_tracker.ui.navigation.BottomNavBar
import com.example.expense_tracker.ui.navigation.NavRoutes
import com.example.expense_tracker.ui.navigation.rememberIsReduceMotion
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme
import com.example.expense_tracker.ui.theme.MaterialMotionTokens
import com.example.expense_tracker.utils.AuthManager
import com.example.expense_tracker.utils.BiometricHelper

private const val EXTRA_NAV_ROUTE = "EXTRA_NAV_ROUTE"
private const val EXTRA_CARD_NOTE = "EXTRA_CARD_NOTE"
private const val EXTRA_CARD_AMOUNT = "EXTRA_CARD_AMOUNT"
private const val THEME_DARK_MODE = "Dark Mode"
private const val THEME_LIGHT_MODE = "Light Mode"
private const val THEME_SYSTEM_DEFAULT = "System Default"
private const val LANGUAGE_ENGLISH = "English"
private const val LOCALE_TAG_EN = "en"
private const val LOCALE_TAG_ID = "id"
private const val BIOMETRIC_PROMPT_TITLE = "Kasflow Terkunci"
private const val BIOMETRIC_PROMPT_SUBTITLE = "Gunakan sidik jari untuk membuka Kasflow"

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {

    private val pendingNavRoute = mutableStateOf<Triple<String, String?, Long?>?>(null)

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory.create(application)
    }

    private val userPrefsRepo by lazy {
        UserPreferencesRepositoryImpl(applicationContext.dataStore)
    }

    @Volatile
    private var isPreferencesLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        // Transparent system bars initialized immediately to prevent navigation bar flash
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        // Pre-warm preferences asynchronously so initial destination is 100% correct before splash drops
        lifecycleScope.launch {
            userPrefsRepo.isBiometricsEnabledFlow.first()
            isPreferencesLoaded = true
        }

        // Splash ONLY holds for the minimal state required to determine the initial destination.
        // Home content loads progressively underneath with stable skeleton placeholders.
        splashScreen.setKeepOnScreenCondition {
            !isPreferencesLoaded
        }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val widthSizeClass = windowSizeClass.widthSizeClass
            val context = LocalContext.current
            val themeMode by userPrefsRepo.themeModeFlow.collectAsState(initial = THEME_SYSTEM_DEFAULT)

            LaunchedEffect(userPrefsRepo) {
                userPrefsRepo.currencyFlow.collect { curr ->
                    CurrencyFormatter.setCurrency(curr)
                }
            }

            LaunchedEffect(userPrefsRepo) {
                userPrefsRepo.languageFlow.collect { lang ->
                    if (lang.isNotBlank()) {
                        val localeStr = if (lang == LANGUAGE_ENGLISH) LOCALE_TAG_EN else LOCALE_TAG_ID
                        val currentLocales = AppCompatDelegate.getApplicationLocales()
                        if (currentLocales.toLanguageTags() != localeStr) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(localeStr)
                            )
                        }
                    }
                }
            }

            val isBiometricsEnabled by userPrefsRepo.isBiometricsEnabledFlow.collectAsState(initial = false)

            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                THEME_DARK_MODE -> true
                THEME_LIGHT_MODE -> false
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
                                promptBiometricAuth(context)
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

            ExpenseTrackerTheme(darkTheme = darkTheme) {
                if (isBiometricsEnabled && !isAuthenticated) {
                    BiometricLockScreen(
                        onUnlockClick = { promptBiometricAuth(context) }
                    )
                } else {
                    ExpenseTrackerApp(
                        homeViewModel = homeViewModel,
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

    private fun promptBiometricAuth(context: Context) {
        val fragmentActivity = context as? FragmentActivity ?: return
        BiometricHelper.authenticate(
            activity = fragmentActivity,
            title = BIOMETRIC_PROMPT_TITLE,
            subtitle = BIOMETRIC_PROMPT_SUBTITLE,
            onSuccess = { AuthManager.unlock() },
            onError = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    homeViewModel: HomeViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    pendingNavRoute: MutableState<Triple<String, String?, Long?>?> = remember { mutableStateOf(null) }
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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

    when {
        showNavigation && widthSizeClass == WindowWidthSizeClass.Expanded -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    AppNavigationDrawerContent(
                        currentRoute = currentRoute,
                        onNavigate = onNavigateTo
                    )
                }
            ) {
                AppScaffold(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    userPreferencesRepository = userPreferencesRepository,
                    isReduceMotion = isReduceMotion,
                    showNavigation = false,
                    currentRoute = currentRoute,
                    onNavigateTo = onNavigateTo,
                    onNavigateToInput = onNavigateToInput,
                    onNavigateToSummary = onNavigateToSummary,
                    onNavigateToReminder = onNavigateToReminder,
                    onNavigateToAiInput = onNavigateToAiInput,
                    onNavigateToReceipt = onNavigateToReceipt,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateBack = onNavigateBack
                )
            }
        }
        showNavigation && widthSizeClass == WindowWidthSizeClass.Medium -> {
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail(
                    currentRoute = currentRoute,
                    onNavigate = onNavigateTo
                )
                Box(modifier = Modifier.weight(1f)) {
                    AppScaffold(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        userPreferencesRepository = userPreferencesRepository,
                        isReduceMotion = isReduceMotion,
                        showNavigation = false,
                        currentRoute = currentRoute,
                        onNavigateTo = onNavigateTo,
                        onNavigateToInput = onNavigateToInput,
                        onNavigateToSummary = onNavigateToSummary,
                        onNavigateToReminder = onNavigateToReminder,
                        onNavigateToAiInput = onNavigateToAiInput,
                        onNavigateToReceipt = onNavigateToReceipt,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
        else -> {
            AppScaffold(
                navController = navController,
                homeViewModel = homeViewModel,
                userPreferencesRepository = userPreferencesRepository,
                isReduceMotion = isReduceMotion,
                showNavigation = showNavigation && widthSizeClass == WindowWidthSizeClass.Compact,
                currentRoute = currentRoute,
                onNavigateTo = onNavigateTo,
                onNavigateToInput = onNavigateToInput,
                onNavigateToSummary = onNavigateToSummary,
                onNavigateToReminder = onNavigateToReminder,
                onNavigateToAiInput = onNavigateToAiInput,
                onNavigateToReceipt = onNavigateToReceipt,
                onNavigateToChat = onNavigateToChat,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun AppScaffold(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    isReduceMotion: Boolean,
    showNavigation: Boolean,
    currentRoute: String?,
    onNavigateTo: (String) -> Unit,
    onNavigateToInput: (Long?) -> Unit,
    onNavigateToSummary: (Long?) -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToAiInput: () -> Unit,
    onNavigateToReceipt: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            AppNavHost(
                navController = navController,
                homeViewModel = homeViewModel,
                userPreferencesRepository = userPreferencesRepository,
                isReduceMotion = isReduceMotion,
                onNavigateToInput = onNavigateToInput,
                onNavigateToSummary = onNavigateToSummary,
                onNavigateToReminder = onNavigateToReminder,
                onNavigateToAiInput = onNavigateToAiInput,
                onNavigateToReceipt = onNavigateToReceipt,
                onNavigateToChat = onNavigateToChat,
                onNavigateBack = onNavigateBack
            )

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
