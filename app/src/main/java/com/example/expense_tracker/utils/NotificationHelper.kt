package com.example.expense_tracker.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.expense_tracker.MainActivity
import com.example.expense_tracker.R

object NotificationHelper {

    private const val CHANNEL_ID = "bill_reminders_channel"
    private const val CHANNEL_NAME = "Bill Reminders"
    private const val CHANNEL_DESC = "Notifications for upcoming bill due dates"

    private const val AI_CHANNEL_ID = "ai_transactions_channel"

    private const val EXTRA_DESTINATION = "destination"
    private const val DESTINATION_REMINDER_LIST = "reminder_list"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createBillChannel(notificationManager)
            createAiChannel(context, notificationManager)
        }
    }

    private fun createBillChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val billChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(billChannel)
        }
    }

    private fun createAiChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aiChannelName = try {
                context.getString(R.string.nl_quick_channel_name)
            } catch (_: Exception) {
                "Catat Transaksi AI"
            }
            val aiChannelDesc = try {
                context.getString(R.string.nl_quick_channel_desc)
            } catch (_: Exception) {
                "Notifikasi konfirmasi transaksi yang dicatat lewat AI Widget"
            }

            val aiChannel = NotificationChannel(
                AI_CHANNEL_ID,
                aiChannelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = aiChannelDesc
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(aiChannel)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_DESTINATION, DESTINATION_REMINDER_LIST)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!hasNotificationPermission(context)) {
            return
        }

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // Ignored if permission was revoked concurrently
        }
    }

    @SuppressLint("MissingPermission")
    fun showTransactionSuccessNotification(
        context: Context,
        title: String,
        message: String,
        subText: String? = null
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 100000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, AI_CHANNEL_ID)
            .setSmallIcon(com.example.expense_tracker.R.drawable.ic_widget_sparkle)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!subText.isNullOrBlank()) {
            builder.setSubText(subText)
        }

        if (!hasNotificationPermission(context)) {
            return
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // Ignored if permission was revoked concurrently
        }
    }
}
