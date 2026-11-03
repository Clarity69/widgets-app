package com.taro.inkwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class SearchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val views = buildViews(context)
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    companion object {
        private const val REQUEST_OPEN_SEARCH = 200
        private const val REQUEST_OPEN_LENS = 201

        private fun buildViews(context: Context): RemoteViews =
            RemoteViews(context.packageName, R.layout.widget_search).apply {
                // The camera button sits inside the bar; its own click handler wins.
                setOnClickPendingIntent(
                    R.id.search_bar,
                    LaunchTrampolineActivity.pendingIntent(
                        context,
                        LaunchTrampolineActivity.ACTION_OPEN_SEARCH,
                        REQUEST_OPEN_SEARCH
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
    }
}
