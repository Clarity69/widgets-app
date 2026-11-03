package com.taro.inkwidgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.TypedValue
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Draws the slim search capsule: "G" on the left, mic and camera on the right.
 *
 * The icon zones must line up with the transparent click areas in widget_search.xml:
 * from the right edge there is [END_MARGIN_DP][lens ICON_ZONE_DP][mic ICON_ZONE_DP].
 */
object SearchRenderer {

    const val ICON_ZONE_DP = 48f
    const val END_MARGIN_DP = 4f
    private const val SIDE_MARGIN_DP = 4f
    private const val G_START_DP = 18f
    private const val DIVIDER_WIDTH_DP = 1f
    private const val DIVIDER_HEIGHT_DP = 20f

    fun render(context: Context, widthPx: Int, heightPx: Int, s: SearchSettings): Bitmap {
        val width = widthPx.coerceAtLeast(1)
        val height = heightPx.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val colors = s.theme.resolve()
        val dm = context.resources.displayMetrics
        fun dp(v: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, dm)

        // Capsule
        val border = dp(s.borderDp.toFloat())
        val capsuleHeight = min(dp(s.heightDp.toFloat()), height.toFloat())
        val capsule = RectF(
            dp(SIDE_MARGIN_DP),
            (height - capsuleHeight) / 2f,
            width - dp(SIDE_MARGIN_DP),
            (height + capsuleHeight) / 2f
        )
        capsule.inset(border / 2f, border / 2f)
        val corner = capsule.height() / 2f
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.paper }
        canvas.drawRoundRect(capsule, corner, corner, fill)
        if (border > 0f) {
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.ink
                style = Paint.Style.STROKE
                strokeWidth = border
            }
            canvas.drawRoundRect(capsule, corner, corner, stroke)
        }
        val centerY = capsule.centerY()

        // "G"
        if (s.showG) {
            val gPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.ink
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, s.gSizeSp.toFloat(), dm
                )
            }
            val bounds = Rect()
            gPaint.getTextBounds("G", 0, 1, bounds)
            canvas.drawText(
                "G",
                capsule.left + dp(G_START_DP) - bounds.left,
                centerY - bounds.exactCenterY(),
                gPaint
            )
        }

        // Right-hand icons: lens is right-most, mic sits to its left.
        val iconSize = dp(s.iconSizeDp.toFloat())
        val zone = dp(ICON_ZONE_DP)
        var zoneRight = width - dp(END_MARGIN_DP)
        var leftMostZone = Float.NaN
        if (s.showLens) {
            drawIcon(context, canvas, R.drawable.ic_lens_camera, zoneRight - zone / 2f, centerY, iconSize, colors.ink)
            zoneRight -= zone
            leftMostZone = zoneRight
        }
        if (s.showMic) {
            drawIcon(context, canvas, R.drawable.ic_mic, zoneRight - zone / 2f, centerY, iconSize, colors.ink)
            zoneRight -= zone
            leftMostZone = zoneRight
        }

        if (s.showDivider && !leftMostZone.isNaN()) {
            val divider = Paint().apply { color = colors.ink }
            val half = dp(DIVIDER_WIDTH_DP) / 2f
            val dividerHeight = min(dp(DIVIDER_HEIGHT_DP), capsule.height() * 0.5f)
            canvas.drawRect(
                leftMostZone - half,
                centerY - dividerHeight / 2f,
                leftMostZone + half,
                centerY + dividerHeight / 2f,
                divider
            )
        }

        return bitmap
    }

    private fun drawIcon(
        context: Context,
        canvas: Canvas,
        resId: Int,
        cx: Float,
        cy: Float,
        size: Float,
        color: Int
    ) {
        val drawable = context.getDrawable(resId)?.mutate() ?: return
        drawable.setTint(color)
        val half = size / 2f
        drawable.setBounds(
            (cx - half).roundToInt(),
            (cy - half).roundToInt(),
            (cx + half).roundToInt(),
            (cy + half).roundToInt()
        )
        drawable.draw(canvas)
    }
}
