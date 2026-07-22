package com.example.expense_tracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomBillReminderRepository
import com.example.expense_tracker.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class BillReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val repository = RoomBillReminderRepository(database.billReminderDao())

            val activeReminders = repository.getActiveReminders().first()
            if (activeReminders.isEmpty()) {
                return@withContext Result.success()
            }

            val today = LocalDate.now()
            val dayOfMonth = today.dayOfMonth
            val isFirstDayOfMonth = dayOfMonth == 1
            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            for (reminder in activeReminders) {
                val amountStr = currencyFormat.format(reminder.amount)
                val notificationId = reminder.id.toInt()
                
                val payload = ReminderDateChecker.getNotificationPayload(
                    dueDay = reminder.dueDay,
                    today = today,
                    reminderName = reminder.name,
                    amountStr = amountStr
                )

                if (payload != null) {
                    NotificationHelper.showNotification(
                        applicationContext,
                        notificationId,
                        payload.title,
                        payload.messageTemplate
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
