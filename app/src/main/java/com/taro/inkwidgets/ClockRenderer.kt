package com.taro.inkwidgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

/**
 * Draws the dot-matrix clock (HH:mm) plus the date line onto a transparent bitmap.
 *
 * Layout in "dot units" (1 unit = distance between two dot centres):
 *   [digit 5][gap 1][digit 5][gap 1][colon 1][gap 1][digit 5][gap 1][digit 5] = 25 columns
 *   7 rows of dots, then (optionally) a gap and one line of date text.
 * The whole block is scaled uniformly to fit the widget and centred both ways.
 */
object ClockRenderer {

    private const val GRID_COLS = 25
    private const val DATE_GAP_UNITS = 1.4f
    private const val DATE_LINE_UNITS = 1.9f
    private const val DATE_TEXT_UNITS = 1.5f
    private const val DATE_LETTER_SPACING = 0.12f
    private const val PAPER_PADDING_UNITS = 1.5f
    private const val PAPER_RADIUS_UNITS = 1.8f

    private val DIGIT_START_COLUMNS = intArrayOf(0, 6, 14, 20)
    private const val COLON_COLUMN = 12
    private val COLON_ROWS = intArrayOf(2, 4)

    fun render(
        widthPx: Int,
        heightPx: Int,
        time: Calendar,
        settings: ClockSettings
    ): Bitmap {
        val width = widthPx.coerceAtLeast(1)
        val height = heightPx.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val colors = settings.theme.resolve()

        val dateScale = settings.dateSizePercent / 100f
        val dateLineUnits = DATE_LINE_UNITS * dateScale
        val pad = if (settings.paperBackground) PAPER_PADDING_UNITS else 0f
        val totalCols = GRID_COLS + 2 * pad
        val totalRows = DotFont.ROWS +
            (if (settings.showDate) DATE_GAP_UNITS + dateLineUnits else 0f) +
            2 * pad

        val fitPitch = min(width / totalCols, height / totalRows)
        val pitch = fitPitch * (settings.scalePercent.coerceIn(10, 100) / 100f)
        val blockLeft = (width - pitch * totalCols) / 2f
        val blockTop = (height - pitch * totalRows) / 2f

        if (settings.paperBackground) {
            val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.paper }
            val radius = pitch * PAPER_RADIUS_UNITS
            canvas.drawRoundRect(
                RectF(blockLeft, blockTop, blockLeft + pitch * totalCols, blockTop + pitch * totalRows),
                radius,
                radius,
                paperPaint
            )
        }

        val left = blockLeft + pad * pitch
        val top = blockTop + pad * pitch
        val radius = pitch * settings.dotSizePercent.coerceIn(10, 100) / 200f

        val onPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.ink }
        val offPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.dotOff }

        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val digits = intArrayOf(hour / 10, hour % 10, minute / 10, minute % 10)

        for (i in digits.indices) {
            val startCol = DIGIT_START_COLUMNS[i]
            for (row in 0 until DotFont.ROWS) {
                for (col in 0 until DotFont.COLS) {
                    val lit = DotFont.isOn(digits[i], row, col)
                    if (!lit && !settings.showOffDots) continue
                    canvas.drawCircle(
                        left + (startCol + col + 0.5f) * pitch,
                        top + (row + 0.5f) * pitch,
                        radius,
                        if (lit) onPaint else offPaint
                    )
                }
            }
        }

        for (row in COLON_ROWS) {
            canvas.drawCircle(
                left + (COLON_COLUMN + 0.5f) * pitch,
                top + (row + 0.5f) * pitch,
                radius,
                onPaint
            )
        }

        if (settings.showDate) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.muted
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                textSize = pitch * DATE_TEXT_UNITS * dateScale
                letterSpacing = DATE_LETTER_SPACING
            }
            val text = formatDate(time)
            val maxWidth = width * 0.95f
            val measured = paint.measureText(text)
            if (measured > maxWidth && measured > 0f) {
                paint.textSize = paint.textSize * (maxWidth / measured)
            }
            val centerY = top + (DotFont.ROWS + DATE_GAP_UNITS + dateLineUnits / 2f) * pitch
            val metrics = paint.fontMetrics
            canvas.drawText(text, width / 2f, centerY - (metrics.ascent + metrics.descent) / 2f, paint)
        }

        return bitmap
    }

    /** e.g. "SEL 3 NOV" for Indonesian, "TUE 3 NOV" for English. */
    fun formatDate(time: Calendar, locale: Locale = Locale.getDefault()): String =
        SimpleDateFormat("EEE d MMM", locale)
            .format(time.time)
            .replace(".", "")
            .uppercase(locale)

    /** Spoken description for accessibility, e.g. "09:41, Selasa 3 November". */
    fun describe(time: Calendar, locale: Locale = Locale.getDefault()): String =
        SimpleDateFormat("HH:mm, EEEE d MMMM", locale).format(time.time)
}
