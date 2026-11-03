package com.taro.inkwidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private lateinit var widgetList: LinearLayout
    private lateinit var widgetListEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        widgetList = findViewById(R.id.widget_list)
        widgetListEmpty = findViewById(R.id.widget_list_empty)
        findViewById<Button>(R.id.open_settings_button).setOnClickListener {
            openAppSettings()
        }

        // Opening the app re-arms the minute alarm in case MIUI killed it.
        ClockWidgetProvider.updateAll(this)
        SearchWidgetProvider.updateAll(this)
    }

    override fun onResume() {
        super.onResume()
        populateWidgetList()
    }

    /** One "Atur ..." button per widget currently on the home screen. */
    private fun populateWidgetList() {
        widgetList.removeAllViews()
        val manager = AppWidgetManager.getInstance(this)
        val entries = listOf(
            Triple(ClockWidgetProvider::class.java, ClockConfigActivity::class.java, R.string.clock_widget_label),
            Triple(SearchWidgetProvider::class.java, SearchConfigActivity::class.java, R.string.search_widget_label),
        )
        var count = 0
        for ((provider, config, labelRes) in entries) {
            val ids = manager.getAppWidgetIds(ComponentName(this, provider))
            ids.forEachIndexed { index, id ->
                val button = Button(this).apply {
                    text = getString(R.string.main_configure_widget, getString(labelRes), index + 1)
                    setTextColor(getColor(R.color.ink))
                    textSize = 15f
                    isAllCaps = false
                    stateListAnimator = null
                    setBackgroundResource(R.drawable.button_outline)
                    minHeight = dp(48)
                    setOnClickListener {
                        startActivity(
                            Intent(this@MainActivity, config)
                                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        )
                    }
                }
                widgetList.addView(
                    button,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(10) }
                )
                count++
            }
        }
        widgetListEmpty.visibility = if (count == 0) View.VISIBLE else View.GONE
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun openAppSettings() {
        val details = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        try {
            startActivity(details)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (ignored: ActivityNotFoundException) {
                // No settings app at all; nothing else to do.
            }
        }
    }
}
