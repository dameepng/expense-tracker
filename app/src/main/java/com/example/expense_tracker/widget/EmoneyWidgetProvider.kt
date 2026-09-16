package com.example.expense_tracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.expense_tracker.R
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.nfc.NfcCardEntity
import com.example.expense_tracker.data.nfc.NfcCardType
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.nfc.NfcQuickScanActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmoneyWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            val latestCard = try {
                AppDatabase.getInstance(context).nfcCardDao().getLatestCard()
            } catch (_: Exception) {
                null
            }

            for (appWidgetId in appWidgetIds) {
                val views = buildRemoteViews(context, latestCard)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun buildRemoteViews(context: Context, card: NfcCardEntity?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_emoney_card)

        // Intent to launch quick scan activity when user taps button or widget
        val scanIntent = Intent(context, NfcQuickScanActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            scanIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_btn_scan, pendingIntent)

        if (card != null) {
            val cardType = try {
                NfcCardType.valueOf(card.cardType)
            } catch (_: Exception) {
                NfcCardType.UNKNOWN
            }

            views.setTextViewText(R.id.widget_card_title, cardType.displayName)
            views.setTextViewText(R.id.widget_balance_amount, CurrencyFormatter.format(card.balance))

            // Mask card number
            val clean = card.cardNumber.replace("\\s".toRegex(), "")
            val masked = if (clean.length >= 8) {
                "•••• " + clean.takeLast(4)
            } else {
                "•••• ••••"
            }
            views.setTextViewText(R.id.widget_card_number, masked)

            // Format last scanned time
            val timeStr = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(card.lastScannedAt))
            views.setTextViewText(R.id.widget_last_scanned, context.getString(R.string.nfc_last_scanned, timeStr))
        } else {
            views.setTextViewText(R.id.widget_card_title, context.getString(R.string.widget_emoney_name))
            views.setTextViewText(R.id.widget_balance_amount, "Rp 0")
            views.setTextViewText(R.id.widget_card_number, "•••• ••••")
            views.setTextViewText(R.id.widget_last_scanned, context.getString(R.string.nfc_no_card_scanned))
        }

        return views
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, EmoneyWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, EmoneyWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
