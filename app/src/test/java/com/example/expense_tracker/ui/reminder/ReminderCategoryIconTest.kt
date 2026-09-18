package com.example.expense_tracker.ui.reminder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.expense_tracker.data.BillReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderCategoryIconTest {

    @Test
    fun getCategoryIcon_returnsCorrectIconsForKnownIds() {
        assertEquals(Icons.Default.Restaurant, getCategoryIcon(1L))
        assertEquals(Icons.Default.DirectionsCar, getCategoryIcon(2L))
        assertEquals(Icons.Default.ShoppingCart, getCategoryIcon(3L))
        assertEquals(Icons.Default.Movie, getCategoryIcon(4L))
        assertEquals(Icons.Default.Receipt, getCategoryIcon(5L))
        assertEquals(Icons.Default.LocalHospital, getCategoryIcon(6L))
    }

    @Test
    fun getCategoryIcon_returnsNotificationIconForUnknownIds() {
        assertEquals(Icons.Default.Notifications, getCategoryIcon(0L))
        assertEquals(Icons.Default.Notifications, getCategoryIcon(999L))
        assertEquals(Icons.Default.Notifications, getCategoryIcon(-1L))
    }

    @Test
    fun reminderItemUiState_isPaid_forRecurringReminders() {
        val baseReminder = BillReminder(
            id = 1L,
            name = "Internet",
            amount = 350_000L,
            dueDay = 15,
            categoryId = 5L,
            walletId = 1L,
            isRepeat = true,
            isActive = true
        )

        val paidThisMonth = ReminderItemUiState(
            reminder = baseReminder,
            categoryName = "Tagihan",
            walletName = "BCA",
            isPaidThisMonth = true
        )
        assertTrue(paidThisMonth.isPaid)

        val notPaidThisMonth = ReminderItemUiState(
            reminder = baseReminder,
            categoryName = "Tagihan",
            walletName = "BCA",
            isPaidThisMonth = false
        )
        assertFalse(notPaidThisMonth.isPaid)
    }

    @Test
    fun reminderItemUiState_isPaid_forOneTimeReminders() {
        val inactiveOneTime = BillReminder(
            id = 2L,
            name = "Doctor Bill",
            amount = 200_000L,
            dueDay = 10,
            categoryId = 6L,
            walletId = 1L,
            isRepeat = false,
            isActive = false
        )

        val completedOneTime = ReminderItemUiState(
            reminder = inactiveOneTime,
            categoryName = "Kesehatan",
            walletName = "BCA",
            isPaidThisMonth = false
        )
        assertTrue(completedOneTime.isPaid)

        val activeOneTime = inactiveOneTime.copy(isActive = true)
        val pendingOneTime = ReminderItemUiState(
            reminder = activeOneTime,
            categoryName = "Kesehatan",
            walletName = "BCA",
            isPaidThisMonth = false
        )
        assertFalse(pendingOneTime.isPaid)
    }
}
