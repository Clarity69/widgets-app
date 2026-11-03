package com.taro.inkwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews

class SearchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        const val DEFAULT_WIDTH_DP = 250
        const val DEFAULT_HEIGHT_DP = 56

        private const val REQUEST_OPEN_SEARCH = 200
        private const val REQUEST_OPEN_LENS = 201
        private const val REQUEST_OPEN_VOICE = 202

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SearchWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val settings = SettingsStore.loadSearch(context)
            val (widthPx, heightPx) = WidgetSize.px(
                context,
                manager.getAppWidgetOptions(appWidgetId),
                DEFAULT_WIDTH_DP,
                DEFAULT_HEIGHT_DP
            )
            val bitmap = SearchRenderer.render(context, widthPx, heightPx, settings)

            val views = RemoteViews(context.packageName, R.layout.widget_search).apply {
                setImageViewBitmap(R.id.search_image, bitmap)
                setViewVisibility(R.id.mic_button, if (settings.showMic) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.lens_button, if (settings.showLens) View.VISIBLE else View.GONE)
                // The icon areas sit on top of the bar; their own click handlers win.
                setOnClickPendingIntent(
                    R.id.search_bar,
                    LaunchTrampolineActivity.pendingIntent(
                        context,
                        LaunchTrampolineActivity.ACTION_OPEN_SEARCH,
                        REQUEST_OPEN_SEARCH
                    )
                )
                setOnClickPendingIntent(
                    R.id.mic_button,
                    LaunchTrampolineActivity.pendingIntent(
                        context,
                        LaunchTrampolineActivity.ACTION_OPEN_VOICE,
                        REQUEST_OPEN_VOICE
                    )
                )
                setOnClickPendingIntent(
                    R.id.lens_button,
                    LaunchTrampolineActivity.pendingIntent(
                        context,
                        LaunchTrampolineActivity.ACTION_OPEN_LENS,
                        REQUEST_OPEN_LENS
                    )
                )
            }
            manager.updateAppWidget(appWidgetId, views)
        }
    }
}
