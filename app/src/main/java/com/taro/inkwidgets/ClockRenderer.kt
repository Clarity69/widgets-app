package com.taro.inkwidgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
 *   7 rows of dots, a gap, then one line of date text.
 * The whole block is scaled uniformly to fit the widget and centred both ways.
 */
object ClockRenderer {

    private const val GRID_COLS = 25
    private const val DATE_GAP_UNITS = 1.4f
    private const val DATE_LINE_UNITS = 1.9f
    private const val TOTAL_ROW_UNITS = DotFont.ROWS + DATE_GAP_UNITS + DATE_LINE_UNITS
    private const val DOT_RADIUS_UNITS = 0.4f
    private const val DATE_TEXT_UNITS = 1.5f
    private const val DATE_LETTER_SPACING = 0.12f

    private val DIGIT_START_COLUMNS = intArrayOf(0, 6, 14, 20)
    private const val COLON_COLUMN = 12
    private val COLON_ROWS = intArrayOf(2, 4)

    fun render(context: Context, widthPx: Int, heightPx: Int, time: Calendar): Bitmap {
        val width = widthPx.coerceAtLeast(1)
        val height = heightPx.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val pitch = min(width / GRID_COLS.toFloat(), height / TOTAL_ROW_UNITS)
        val left = (width - pitch * GRID_COLS) / 2f
        val top = (height - pitch * TOTAL_ROW_UNITS) / 2f
        val radius = pitch * DOT_RADIUS_UNITS

        val onPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = context.getColor(R.color.ink) }
        val offPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = context.getColor(R.color.dot_off) }

        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val digits = intArrayOf(hour / 10, hour % 10, minute / 10, minute % 10)

        for (i in digits.indices) {
            val startCol = DIGIT_START_COLUMNS[i]
            for (row in 0 until DotFont.ROWS) {
                for (col in 0 until DotFont.COLS) {
                    val paint = if (DotFont.isOn(digits[i], row, col)) onPaint else offPaint
                    canvas.drawCircle(
                        left + (startCol + col + 0.5f) * pitch,
                        top + (row + 0.5f) * pitch,
                        radius,
                        paint
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

        drawDate(context, canvas, width, top, pitch, time)
        return bitmap
    }

    private fun drawDate(
        context: Context,
        canvas: Canvas,
        width: Int,
        top: Float,
        pitch: Float,
        time: Calendar
    ) {
        val text = formatDate(time)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.muted)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            textSize = pitch * DATE_TEXT_UNITS
            letterSpacing = DATE_LETTER_SPACING
        }
        val maxWidth = width * 0.95f
        val measured = paint.measureText(text)
        if (measured > maxWidth && measured > 0f) {
            paint.textSize = paint.textSize * (maxWidth / measured)
        }
        val centerY = top + (DotFont.ROWS + DATE_GAP_UNITS + DATE_LINE_UNITS / 2f) * pitch
        val metrics = paint.fontMetrics
        val baseline = centerY - (metrics.ascent + metrics.descent) / 2f
        canvas.drawText(text, width / 2f, baseline, paint)
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
