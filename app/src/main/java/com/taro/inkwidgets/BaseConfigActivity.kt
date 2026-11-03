package com.taro.inkwidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Shared settings screen: title, live preview, form (from the subclass) and a save button.
 * Opened by the launcher when a widget is placed, or from MainActivity later.
 */
abstract class BaseConfigActivity : Activity() {

    protected var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        private set

    private lateinit var ui: ConfigUi
    private lateinit var previewImage: ImageView
    private lateinit var previewShape: GradientDrawable
    private var darkBackdrop = false

    protected abstract val providerClass: Class<out AppWidgetProvider>
    protected abstract val titleRes: Int
    protected abstract val defaultWidthDp: Int
    protected abstract val defaultHeightDp: Int

    protected abstract fun loadSettings()
    protected abstract fun buildForm(ui: ConfigUi)
    protected abstract fun renderPreview(widthPx: Int, heightPx: Int): Bitmap
    protected abstract fun saveSettings()
    protected abstract fun applyToWidget()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)
        if (info != null && info.provider != ComponentName(this, providerClass)) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Keep the widget even if the user backs out: the defaults are fine.
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        loadSettings()
        applyToWidget()
        setContentView(buildLayout())
        refreshPreview()
    }

    private fun buildLayout(): View {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        ui = ConfigUi(this, column, ::refreshPreview)
        val pad = ui.dp(24)
        column.setPadding(pad, pad, pad, pad)

        column.addView(TextView(this).apply {
            setText(titleRes)
            setTextColor(getColor(R.color.ink))
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        })

        previewShape = GradientDrawable().apply { cornerRadius = ui.dp(16).toFloat() }
        previewImage = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val previewFrame = FrameLayout(this).apply {
            background = previewShape
            val p = ui.dp(12)
            setPadding(p, p, p, p)
            addView(
                previewImage,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
        column.addView(
            previewFrame,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = ui.dp(16) }
        )
        updateBackdrop()

        ui.toggle(R.string.cfg_dark_backdrop, darkBackdrop) {
            darkBackdrop = it
            updateBackdrop()
        }

        buildForm(ui)

        val save = Button(this).apply {
            setText(R.string.cfg_save)
            setTextColor(getColor(R.color.ink))
            textSize = 16f
            isAllCaps = false
            stateListAnimator = null
            setBackgroundResource(R.drawable.button_outline)
            minHeight = ui.dp(52)
            setOnClickListener {
                saveSettings()
                applyToWidget()
                setResult(
                    RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
                finish()
            }
        }
        column.addView(
            save,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = ui.dp(28) }
        )

        return ScrollView(this).apply {
            fitsSystemWindows = true
            setBackgroundColor(getColor(R.color.paper))
            addView(column)
        }
    }

    private fun updateBackdrop() {
        if (darkBackdrop) {
            previewShape.setColor(getColor(R.color.ink))
            previewShape.setStroke(0, getColor(R.color.ink))
        } else {
            previewShape.setColor(getColor(R.color.paper))
            previewShape.setStroke(ui.dp(1), getColor(R.color.muted))
        }
    }

    /** Re-render the preview at the widget's real pixel size; the ImageView scales it down. */
    private fun refreshPreview() {
        val options = AppWidgetManager.getInstance(this).getAppWidgetOptions(appWidgetId)
        val (w, h) = WidgetSize.px(this, options, defaultWidthDp, defaultHeightDp)
        previewImage.setImageBitmap(renderPreview(w, h))
    }
}
