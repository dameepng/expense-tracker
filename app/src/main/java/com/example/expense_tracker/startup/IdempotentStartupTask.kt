package com.example.expense_tracker.startup

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe base implementation for startup tasks that must be idempotent.
 *
 * Ensures that [execute] only runs once throughout the process lifecycle,
 * even if invoked concurrently or repeatedly from multiple startup triggers.
 */
abstract class IdempotentStartupTask : StartupTask {

    private val mutex = Mutex()

    @Volatile
    private var isCompleted = false

    final override suspend fun invoke() {
        if (isCompleted) return
        mutex.withLock {
            if (isCompleted) return
            execute()
            isCompleted = true
        }
    }

    /**
     * Executes the actual initialization work.
     */
    protected abstract suspend fun execute()
}
