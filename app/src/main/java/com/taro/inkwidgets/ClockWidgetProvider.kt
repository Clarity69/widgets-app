package com.taro.inkwidgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import java.util.Calendar

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
        scheduleNextTick(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        scheduleNextTick(context)
    }

    override fun onDisabled(context: Context) {
        cancelTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> updateAll(context)

            else -> super.onReceive(context, intent)
        }
    }

    companion object {
        private const val ACTION_TICK = "com.taro.inkwidgets.action.CLOCK_TICK"

        private const val REQUEST_TICK = 300
        private const val REQUEST_OPEN_CLOCK = 100

        /** Window used when exact alarms are not allowed. */
        private const val INEXACT_WINDOW_MS = 1_000L
        private const val MINUTE_MS = 60_000L

        /** Fallback size (dp) for a 4x2 widget when the launcher reports nothing. */
        const val DEFAULT_WIDTH_DP = 250
        const val DEFAULT_HEIGHT_DP = 110

        /** Redraw every clock widget and arm the next minute tick (or cancel if none). */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            if (ids.isEmpty()) {
                cancelTick(context)
                return
            }
            for (id in ids) {
                updateWidget(context, manager, id)
            }
            scheduleNextTick(context)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val (widthPx, heightPx) = WidgetSize.px(
                context,
                manager.getAppWidgetOptions(appWidgetId),
                DEFAULT_WIDTH_DP,
                DEFAULT_HEIGHT_DP
            )
            val now = Calendar.getInstance()
            val settings = SettingsStore.loadClock(context)
            val bitmap = ClockRenderer.render(widthPx, heightPx, now, settings)

            val views = RemoteViews(context.packageName, R.layout.widget_clock).apply {
                setImageViewBitmap(R.id.clock_image, bitmap)
                setContentDescription(R.id.clock_image, ClockRenderer.describe(now))
                setOnClickPendingIntent(
                    R.id.clock_root,
                    LaunchTrampolineActivity.pendingIntent(
                        context,
                        LaunchTrampolineActivity.ACTION_OPEN_CLOCK,
                        REQUEST_OPEN_CLOCK
                    )
                )
            }
            manager.updateAppWidget(appWidgetId, views)
        }

        /** Arm a one-shot alarm at the start of the next minute. */
        fun scheduleNextTick(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val nextMinute = (System.currentTimeMillis() / MINUTE_MS + 1) * MINUTE_MS
            val pendingIntent = tickPendingIntent(context)

            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            try {
                if (canExact) {
                    // RTC (not _WAKEUP): never wakes the phone; delivered as soon as it is awake.
                    alarmManager.setExact(AlarmManager.RTC, nextMinute, pendingIntent)
                } else {
                    alarmManager.setWindow(AlarmManager.RTC, nextMinute, INEXACT_WINDOW_MS, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.setWindow(AlarmManager.RTC, nextMinute, INEXACT_WINDOW_MS, pendingIntent)
            }
        }

        private fun cancelTick(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            alarmManager.cancel(tickPendingIntent(context))
        }

        private fun tickPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ClockWidgetProvider::class.java).setAction(ACTION_TICK)
            return PendingIntent.getBroadcast(
                context,
                REQUEST_TICK,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
