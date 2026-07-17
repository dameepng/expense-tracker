package com.example.expense_tracker

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expense_tracker.utils.NotificationHelper
import com.example.expense_tracker.worker.BillReminderWorker
import java.util.concurrent.TimeUnit

class ExpenseTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        NotificationHelper.createNotificationChannel(this)
        
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
            
        val dailyWorkRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BillReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}
