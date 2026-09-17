package com.example.expense_tracker.startup.tasks

import android.content.Context
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.startup.IdempotentStartupTask
import com.example.expense_tracker.startup.StartupPhase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pre-warms the Room SQLite database connection in the background.
 *
 * Ensures database file opening, WAL configuration, and schema validation
 * are completed off the main thread before the UI requests transactions.
 */
class DatabasePrewarmStartupTask(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IdempotentStartupTask() {

    override val id: String = "database_prewarm"
    override val phase: StartupPhase = StartupPhase.NORMAL
    override val timeoutMs: Long = 1500L

    override suspend fun execute() {
        withContext(dispatcher) {
            // Touch readable database to eagerly open and initialize connection pool & migrations off Main
            val db = AppDatabase.getInstance(context)
            db.openHelper.readableDatabase
        }
    }
}
