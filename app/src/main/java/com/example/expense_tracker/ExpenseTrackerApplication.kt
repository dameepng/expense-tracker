package com.example.expense_tracker

import android.app.Application
import com.example.expense_tracker.startup.StartupManager

/**
 * Application class decoupled from God-class initialization logic.
 *
 * Adheres to the Android Startup Pattern (Ehab Elwan), delegating initialization
 * tasks to [StartupManager] to prevent blocking the UI thread on cold start.
 */
class ExpenseTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Launch NORMAL boot tasks concurrently in the background (notification channels, DB prewarm)
        StartupManager.getInstance(this).runNormalTasks()
    }
}
