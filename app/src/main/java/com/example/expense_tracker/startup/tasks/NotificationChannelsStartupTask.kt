package com.example.expense_tracker.startup.tasks

import android.content.Context
import com.example.expense_tracker.startup.IdempotentStartupTask
import com.example.expense_tracker.startup.StartupPhase
import com.example.expense_tracker.utils.NotificationHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Initializes notification channels asynchronously on boot.
 */
class NotificationChannelsStartupTask(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : IdempotentStartupTask() {

    override val id: String = "notification_channels"
    override val phase: StartupPhase = StartupPhase.NORMAL
    override val timeoutMs: Long = 800L

    override suspend fun execute() {
        withContext(dispatcher) {
            NotificationHelper.createNotificationChannel(context)
        }
    }
}
