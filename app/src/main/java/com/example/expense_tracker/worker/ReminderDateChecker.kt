package com.example.expense_tracker.worker

import java.time.LocalDate
import java.time.YearMonth

data class NotificationPayload(
    val title: String,
    val messageTemplate: String
)

object ReminderDateChecker {

    fun getNotificationPayload(
        dueDay: Int,
        today: LocalDate,
        reminderName: String,
        amountStr: String
    ): NotificationPayload? {
        val dayOfMonth = today.dayOfMonth
        val isFirstDayOfMonth = dayOfMonth == 1
        
        val lengthOfMonth = YearMonth.of(today.year, today.month).lengthOfMonth()
        val actualDueDay = if (dueDay > lengthOfMonth) lengthOfMonth else dueDay

        return when {
            isFirstDayOfMonth -> NotificationPayload(
                title = "Bill Reminder",
                messageTemplate = "💡 Reminder: $reminderName jatuh tempo tgl $dueDay"
            )
            dayOfMonth == actualDueDay - 7 -> NotificationPayload(
                title = "H-7 Payment",
                messageTemplate = "⚠️ H-7: $reminderName $amountStr jatuh tempo tgl $dueDay"
            )
            dayOfMonth == actualDueDay - 3 -> NotificationPayload(
                title = "H-3 Payment",
                messageTemplate = "🔴 H-3: Segera bayar $reminderName $amountStr!"
            )
            dayOfMonth == actualDueDay -> NotificationPayload(
                title = "Payment Due Today",
                messageTemplate = "🚨 HARI INI: Batas pembayaran $reminderName!"
            )
            else -> null
        }
    }
}
