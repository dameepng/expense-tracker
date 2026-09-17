package com.example.expense_tracker.startup

/**
 * Defines the execution phase of a startup task in the application lifecycle,
 * following the Android Startup Pattern (Ehab Elwan).
 */
enum class StartupPhase {
    /**
     * Critical tasks that must execute before or during the initial frame/view setup
     * (e.g. locale guard, essential security or crash-handling configuration).
     */
    CRITICAL,

    /**
     * Normal startup tasks that execute concurrently on boot in the background
     * without blocking the UI thread (e.g. database prewarming, notification channels).
     */
    NORMAL,

    /**
     * Deferred tasks that run after the primary UI is drawn or when the system is idle
     * (e.g. periodic WorkManager task scheduling, background syncing).
     */
    BACKGROUND
}
