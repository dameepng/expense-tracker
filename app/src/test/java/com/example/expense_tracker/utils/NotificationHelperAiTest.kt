package com.example.expense_tracker.utils

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class NotificationHelperAiTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        context = app
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Test
    fun createNotificationChannel_createsAiChannel() {
        NotificationHelper.createNotificationChannel(context)

        val channel = notificationManager.getNotificationChannel("ai_transactions_channel")
        assertNotNull("AI transaction channel should not be null", channel)
        assertEquals("Catat Transaksi AI", channel.name)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
    }

    @Test
    fun showTransactionSuccessNotification_postsNotification() {
        NotificationHelper.showTransactionSuccessNotification(
            context = context,
            title = "Transaksi Berhasil Dicatat! ✨",
            message = "Rp 25.000 • Makanan",
            subText = "Dompet Utama"
        )

        val notifications = notificationManager.activeNotifications
        assertNotNull(notifications)
        val aiNotification = notifications.find {
            it.notification.channelId == "ai_transactions_channel"
        }
        assertNotNull("Should find posted AI notification", aiNotification)
        assertEquals("Transaksi Berhasil Dicatat! ✨", aiNotification!!.notification.extras.getString("android.title"))
    }
}
