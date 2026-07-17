package com.example.expense_tracker.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReminderDateCheckerTest {

    private val reminderName = "Internet"
    private val amountStr = "Rp300.000"

    @Test
    fun `first day of month triggers reminder`() {
        // Today is 1st of month
        val today = LocalDate.of(2023, 10, 1)
        val payload = ReminderDateChecker.getNotificationPayload(
            dueDay = 25,
            today = today,
            reminderName = reminderName,
            amountStr = amountStr
        )
        assertNotNull(payload)
        assertEquals("Bill Reminder", payload?.title)
        assertEquals("💡 Reminder: Internet jatuh tempo tgl 25", payload?.messageTemplate)
    }

    @Test
    fun `H-7 triggers reminder`() {
        // dueDay is 25, today is 18
        val today = LocalDate.of(2023, 10, 18)
        val payload = ReminderDateChecker.getNotificationPayload(
            dueDay = 25,
            today = today,
            reminderName = reminderName,
            amountStr = amountStr
        )
        assertNotNull(payload)
        assertEquals("H-7 Payment", payload?.title)
        assertEquals("⚠️ H-7: Internet Rp300.000 jatuh tempo tgl 25", payload?.messageTemplate)
    }

    @Test
    fun `H-3 triggers reminder`() {
        // dueDay is 25, today is 22
        val today = LocalDate.of(2023, 10, 22)
        val payload = ReminderDateChecker.getNotificationPayload(
            dueDay = 25,
            today = today,
            reminderName = reminderName,
            amountStr = amountStr
        )
        assertNotNull(payload)
        assertEquals("H-3 Payment", payload?.title)
        assertEquals("🔴 H-3: Segera bayar Internet Rp300.000!", payload?.messageTemplate)
    }

    @Test
    fun `due day triggers reminder`() {
        // dueDay is 25, today is 25
        val today = LocalDate.of(2023, 10, 25)
        val payload = ReminderDateChecker.getNotificationPayload(
            dueDay = 25,
            today = today,
            reminderName = reminderName,
            amountStr = amountStr
        )
        assertNotNull(payload)
        assertEquals("Payment Due Today", payload?.title)
        assertEquals("🚨 HARI INI: Batas pembayaran Internet!", payload?.messageTemplate)
    }

    @Test
    fun `other days do not trigger reminder`() {
        // dueDay is 25, today is 10
        val today = LocalDate.of(2023, 10, 10)
        val payload = ReminderDateChecker.getNotificationPayload(
            dueDay = 25,
            today = today,
            reminderName = reminderName,
            amountStr = amountStr
        )
        assertNull(payload)
    }

    @Test
    fun `due day clamp to end of month`() {
        // dueDay is 31, but month is February (28 days)
        val today = LocalDate.of(2023, 2, 28) // H-0 (due day)
        val payload = ReminderDateChecker.getNotificationPayload(
            dueDay = 31,
            today = today,
            reminderName = reminderName,
            amountStr = amountStr
        )
        assertNotNull(payload)
        assertEquals("Payment Due Today", payload?.title)

        // H-3 would be Feb 25
        val todayH3 = LocalDate.of(2023, 2, 25)
        val payloadH3 = ReminderDateChecker.getNotificationPayload(
            dueDay = 31,
            today = todayH3,
            reminderName = reminderName,
            amountStr = amountStr
        )
        assertNotNull(payloadH3)
        assertEquals("H-3 Payment", payloadH3?.title)
    }
}
