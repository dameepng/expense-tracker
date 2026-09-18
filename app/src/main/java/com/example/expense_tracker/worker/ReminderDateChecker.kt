package com.example.expense_tracker.worker

import java.time.LocalDate

data class NotificationPayload(
    val title: String,
    val messageTemplate: String
)

object ReminderDateChecker {

    private const val DAYS_BEFORE_DUE_EARLY = 7
    private const val DAYS_BEFORE_DUE_URGENT = 3

    fun getNotificationPayload(
        dueDay: Int,
        today: LocalDate,
        reminderName: String,
        amountStr: String
    ): NotificationPayload? {
        val dayOfMonth = today.dayOfMonth
        val isFirstDayOfMonth = dayOfMonth == 1
        val actualDueDay = dueDay.coerceAtMost(today.lengthOfMonth())

        return when {
            isFirstDayOfMonth -> NotificationPayload(
                title = "Bill Reminder",
                messageTemplate = "💡 Reminder: $reminderName jatuh tempo tgl $dueDay"
            )
            dayOfMonth == actualDueDay - DAYS_BEFORE_DUE_EARLY -> NotificationPayload(
                title = "H-7 Payment",
                messageTemplate = "⚠️ H-7: $reminderName $amountStr jatuh tempo tgl $dueDay"
            )
            dayOfMonth == actualDueDay - DAYS_BEFORE_DUE_URGENT -> NotificationPayload(
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
