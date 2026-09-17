package com.example.expense_tracker.startup.tasks

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expense_tracker.startup.IdempotentStartupTask
import com.example.expense_tracker.startup.StartupPhase
import com.example.expense_tracker.worker.BillReminderWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Enqueues periodic WorkManager background sync/reminders asynchronously.
 * Phase is BACKGROUND so it runs deferred after the UI is rendered, preventing
 * SQLite lock contention on initial app launch.
 */
class WorkManagerStartupTask(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : IdempotentStartupTask() {

    override val id: String = "work_manager_periodic_reminders"
    override val phase: StartupPhase = StartupPhase.BACKGROUND
    override val timeoutMs: Long = 2000L

    override suspend fun execute() {
        withContext(dispatcher) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "BillReminderWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }
    }
}
