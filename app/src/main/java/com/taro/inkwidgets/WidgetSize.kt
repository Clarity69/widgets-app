package com.taro.inkwidgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import kotlin.math.roundToInt

object WidgetSize {

    /** RemoteViews bitmaps travel through Binder; keep them reasonably small. */
    private const val MAX_SIDE_PX = 1600

    /** Current widget size in pixels from the launcher-reported dp size (with fallbacks). */
    fun px(
        context: Context,
        options: Bundle?,
        defaultWidthDp: Int,
        defaultHeightDp: Int
    ): Pair<Int, Int> {
        val landscape = context.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        // Portrait: narrowest width and tallest height; landscape: the opposite.
        val widthDp = options?.getInt(
            if (landscape) AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
            else AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
        ) ?: 0
        val heightDp = options?.getInt(
            if (landscape) AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
            else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
        ) ?: 0

        val density = context.resources.displayMetrics.density
        val w = (if (widthDp > 0) widthDp else defaultWidthDp) * density
        val h = (if (heightDp > 0) heightDp else defaultHeightDp) * density

        val scale = minOf(1f, MAX_SIDE_PX / maxOf(w, h))
        return Pair(
            (w * scale).roundToInt().coerceAtLeast(1),
            (h * scale).roundToInt().coerceAtLeast(1)
        )
    }
}
