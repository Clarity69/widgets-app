package com.taro.inkwidgets

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.open_settings_button).setOnClickListener {
            openAppSettings()
        }

        // Opening the app re-arms the minute alarm in case MIUI killed it.
        ClockWidgetProvider.updateAll(this)
    }

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
