package com.example.expense_tracker.ui.navigation

import android.app.Application
import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.ai.AiDependencies
import com.example.expense_tracker.data.ai.RoomTransactionDraftRepository
import com.example.expense_tracker.data.ai.chat.ChatContextProvider
import com.example.expense_tracker.data.ai.receipt.ReceiptImageProcessor
import com.example.expense_tracker.data.analytics.RoomFinancialSummarySource
import com.example.expense_tracker.ui.ai.NaturalLanguageScreen
import com.example.expense_tracker.ui.ai.NaturalLanguageViewModel
import com.example.expense_tracker.ui.ai.NaturalLanguageViewModelFactory
import com.example.expense_tracker.ui.chat.ChatScreen
import com.example.expense_tracker.ui.chat.ChatViewModel
import com.example.expense_tracker.ui.chat.ChatViewModelFactory
import com.example.expense_tracker.ui.home.HomeScreen
import com.example.expense_tracker.ui.home.HomeViewModel
import com.example.expense_tracker.ui.input.InputScreen
import com.example.expense_tracker.ui.input.InputViewModel
import com.example.expense_tracker.ui.input.InputViewModelFactory
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
import com.example.expense_tracker.ui.wallet.WalletListScreen
import com.example.expense_tracker.ui.wallet.WalletViewModel
import com.example.expense_tracker.ui.wallet.WalletViewModelFactory

private const val ARG_EXPENSE_ID = "expenseId"
private const val ARG_CATEGORY_ID = "categoryId"
private const val ARG_WALLET_ID = "walletId"
private const val ARG_START_TIME = "startTime"
private const val ARG_END_TIME = "endTime"
private const val KEY_REFRESH_HOME = "refresh_home"
private const val KEY_INITIAL_NOTE = "initial_note"
private const val KEY_INITIAL_AMOUNT = "initial_amount"
private const val KEY_SUMMARY_WALLET_ID = "summary_wallet_id"

/**
 * Root navigation host managing all application screen destinations, shared transitions, and view model scopes.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    isReduceMotion: Boolean,
    onNavigateToInput: (Long?) -> Unit,
    onNavigateToSummary: (Long?) -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToAiInput: () -> Unit,
    onNavigateToReceipt: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    SharedTransitionLayout(modifier = modifier) {
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
                    val shouldRefresh by backStackEntry.savedStateHandle.getStateFlow(KEY_REFRESH_HOME, false).collectAsState()
                    LaunchedEffect(shouldRefresh) {
                        if (shouldRefresh) {
                            backStackEntry.savedStateHandle[KEY_REFRESH_HOME] = false
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
                            createReceiptScanViewModelFactory(app)
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
                            createReceiptScanViewModelFactory(app)
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
                    arguments = listOf(navArgument(ARG_EXPENSE_ID) {
                        type = NavType.StringType
                        nullable = true
                    })
                ) { backStackEntry ->
                    val expenseId = backStackEntry.arguments?.getString(ARG_EXPENSE_ID)?.toLongOrNull()
                    val initialNote = backStackEntry.savedStateHandle.get<String>(KEY_INITIAL_NOTE)
                    val initialAmount = backStackEntry.savedStateHandle.get<String>(KEY_INITIAL_AMOUNT)
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
                            navController.previousBackStackEntry?.savedStateHandle?.let { it[KEY_REFRESH_HOME] = true }
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
                    val walletId = backStackEntry.savedStateHandle.get<Long?>(KEY_SUMMARY_WALLET_ID)
                    if (walletId != null) {
                        summaryViewModel.onWalletSelected(walletId)
                        backStackEntry.savedStateHandle.remove<Long?>(KEY_SUMMARY_WALLET_ID)
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
                        navArgument(ARG_CATEGORY_ID) { type = NavType.StringType },
                        navArgument(ARG_WALLET_ID) { type = NavType.StringType; nullable = true },
                        navArgument(ARG_START_TIME) { type = NavType.StringType },
                        navArgument(ARG_END_TIME) { type = NavType.StringType }
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
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToAiInput = onNavigateToAiInput,
                        onNavigateToReceipt = onNavigateToReceipt,
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

                composable(
                    route = NavRoutes.REMINDER_LIST,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) { backStackEntry ->
                    val reminderListViewModel: ReminderListViewModel = viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = remember(backStackEntry) {
                            ReminderListViewModelFactory(app)
                        }
                    )
                    ReminderListScreen(
                        viewModel = reminderListViewModel,
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }
}

private fun createReceiptScanViewModelFactory(app: Application): ViewModelProvider.Factory {
    return ReceiptScanViewModelFactory.create(
        repository = AiDependencies.shared.createReceiptRepository(
            ReceiptImageProcessor(app.contentResolver)
        ),
        draftRepository = RoomTransactionDraftRepository(
            AppDatabase.getInstance(app)
        )
    )
}
