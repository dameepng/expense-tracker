package com.example.expense_tracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.ai.quick.NaturalLanguageQuickActivity

class NaturalLanguageWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = buildRemoteViews(context)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_natural_language)

        val textPendingIntent = createPendingIntent(context, autoSpeech = false, REQUEST_CODE_TEXT)
        val micPendingIntent = createPendingIntent(context, autoSpeech = true, REQUEST_CODE_MIC)

        // Bind PendingIntent to every clickable element so no area falls back to opening the main app
        views.setOnClickPendingIntent(R.id.widget_nl_root, textPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_nl_icon, textPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_nl_title, textPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_nl_prompt, textPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_nl_btn_mic, micPendingIntent)

        return views
    }

    private fun createPendingIntent(
        context: Context,
        autoSpeech: Boolean,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, NaturalLanguageQuickActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_AUTO_SPEECH, autoSpeech)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_AUTO_SPEECH = "extra_auto_speech"
        const val EXTRA_WALLET_ID = "extra_wallet_id"
        const val PREFS_NAME = "nl_widget_prefs"
        const val KEY_SELECTED_WALLET_ID = "selected_wallet_id"

        private const val REQUEST_CODE_TEXT = 101
        private const val REQUEST_CODE_MIC = 102

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, NaturalLanguageWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, NaturalLanguageWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
