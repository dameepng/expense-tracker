package com.example.expense_tracker.startup

/**
 * Standard contract for a modular startup task in the application.
 *
 * Each task implements a suspend `invoke()` function, enabling cooperative cancellation,
 * timeouts, and main-safe execution off the UI thread.
 */
interface StartupTask {
    /**
     * Unique identifier for the startup task.
     */
    val id: String

    /**
     * The lifecycle phase in which this task should be executed.
     */
    val phase: StartupPhase
        get() = StartupPhase.NORMAL

    /**
     * Maximum execution time allotted before timing out to prevent stalling boot sequence.
     */
    val timeoutMs: Long
        get() = 1000L

    /**
     * Suspends and executes the initialization logic. Must be main-safe.
     */
    suspend operator fun invoke()
}
