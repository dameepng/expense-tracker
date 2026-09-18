package com.example.expense_tracker.startup

import android.content.Context
import android.util.Log
import com.example.expense_tracker.data.UserPreferencesRepositoryImpl
import com.example.expense_tracker.data.dataStore
import com.example.expense_tracker.startup.tasks.DatabasePrewarmStartupTask
import com.example.expense_tracker.startup.tasks.LocaleGuardStartupTask
import com.example.expense_tracker.startup.tasks.NotificationChannelsStartupTask
import com.example.expense_tracker.startup.tasks.WorkManagerStartupTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Orchestrator for the Android Startup Pattern.
 *
 * Dispatches and manages startup tasks according to their [StartupPhase],
 * preventing God-class initializations in the Application class and keeping
 * the UI thread unblocked during cold start.
 */
class StartupManager(
    private val tasks: List<StartupTask>,
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    companion object {
        private const val TAG = "StartupManager"

        @Volatile
        private var instance: StartupManager? = null

        fun getInstance(context: Context): StartupManager {
            return instance ?: synchronized(this) {
                instance ?: createDefault(context.applicationContext).also { instance = it }
            }
        }

        private fun createDefault(appContext: Context): StartupManager {
            val userPrefsRepo = UserPreferencesRepositoryImpl(appContext.dataStore)
            val defaultTasks = listOf(
                LocaleGuardStartupTask(userPrefsRepo),
                NotificationChannelsStartupTask(appContext),
                DatabasePrewarmStartupTask(appContext),
                WorkManagerStartupTask(appContext)
            )
            return StartupManager(defaultTasks)
        }
    }

    /**
     * Executes all CRITICAL phase tasks.
     * Can be suspended during initial frame preparation or splash screen with strict timeout bounds.
     */
    suspend fun runCriticalTasks() = coroutineScope {
        val criticalTasks = tasks.filter { it.phase == StartupPhase.CRITICAL }
        criticalTasks.map { task ->
            async(defaultDispatcher) {
                runTaskSafely(task)
            }
        }.awaitAll()
    }

    /**
     * Executes NORMAL phase tasks concurrently in the background.
     * Fires immediately on boot without stalling the main thread.
     */
    fun runNormalTasks() {
        val normalTasks = tasks.filter { it.phase == StartupPhase.NORMAL }
        normalTasks.forEach { task ->
            appScope.launch(defaultDispatcher) {
                runTaskSafely(task)
            }
        }
    }

    /**
     * Executes BACKGROUND phase tasks deferred after UI has rendered or during idle.
     */
    fun runDeferredTasks() {
        val backgroundTasks = tasks.filter { it.phase == StartupPhase.BACKGROUND }
        backgroundTasks.forEach { task ->
            appScope.launch(defaultDispatcher) {
                runTaskSafely(task)
            }
        }
    }

    private suspend fun runTaskSafely(task: StartupTask) {
        try {
            val result = withTimeoutOrNull(task.timeoutMs) {
                task()
            }
            if (result == null) {
                logWarning("Startup task '${task.id}' timed out after ${task.timeoutMs}ms")
            }
        } catch (e: Exception) {
            logError("Error executing startup task '${task.id}'", e)
        }
    }

    private fun logWarning(message: String) {
        try {
            Log.w(TAG, message)
        } catch (_: Throwable) {
            println(message)
        }
    }

    private fun logError(message: String, throwable: Throwable) {
        try {
            Log.e(TAG, message, throwable)
        } catch (_: Throwable) {
            println("$message: ${throwable.message}")
        }
    }
}
