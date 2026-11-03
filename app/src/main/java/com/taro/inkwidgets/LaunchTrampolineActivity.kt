package com.taro.inkwidgets

import android.app.Activity
import android.app.PendingIntent
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.speech.RecognizerIntent

/**
 * Invisible activity that widgets point their PendingIntents at.
 * It tries each candidate intent in order until one starts, then finishes immediately.
 */
class LaunchTrampolineActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val candidates = when (intent?.action) {
            ACTION_OPEN_LENS -> lensCandidates() + searchCandidates()
            ACTION_OPEN_VOICE -> voiceCandidates() + searchCandidates()
            ACTION_OPEN_CLOCK -> clockCandidates()
            else -> searchCandidates()
        }
        candidates.any { build -> tryStart(build()) }
        finish()
    }

    private fun searchCandidates(): List<() -> Intent?> = listOf<() -> Intent?>(
        { Intent().setComponent(ComponentName(GOOGLE_APP, "$GOOGLE_APP.SearchActivity")) },
        { Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, "") },
        { Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")) },
    )

    private fun lensCandidates(): List<() -> Intent?> = listOf<() -> Intent?>(
        { packageManager.getLaunchIntentForPackage(LENS_APP) },
        { Intent().setComponent(ComponentName(GOOGLE_APP, LENS_ACTIVITY)) },
        { Intent(Intent.ACTION_VIEW, Uri.parse("googleapp://lens")) },
    )

    private fun voiceCandidates(): List<() -> Intent?> = listOf<() -> Intent?>(
        { Intent(RecognizerIntent.ACTION_WEB_SEARCH).setPackage(GOOGLE_APP) },
        { Intent(RecognizerIntent.ACTION_WEB_SEARCH) },
        { Intent(Intent.ACTION_VOICE_COMMAND) },
    )

    private fun clockCandidates(): List<() -> Intent?> = listOf<() -> Intent?>(
        { Intent(AlarmClock.ACTION_SHOW_ALARMS) },
        { packageManager.getLaunchIntentForPackage("com.android.deskclock") },
        { packageManager.getLaunchIntentForPackage("com.google.android.deskclock") },
    )

    private fun tryStart(target: Intent?): Boolean {
        if (target == null) return false
        return try {
            startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    companion object {
        const val ACTION_OPEN_SEARCH = "com.taro.inkwidgets.action.OPEN_SEARCH"
        const val ACTION_OPEN_LENS = "com.taro.inkwidgets.action.OPEN_LENS"
        const val ACTION_OPEN_VOICE = "com.taro.inkwidgets.action.OPEN_VOICE"
        const val ACTION_OPEN_CLOCK = "com.taro.inkwidgets.action.OPEN_CLOCK"

        private const val GOOGLE_APP = "com.google.android.googlequicksearchbox"
        private const val LENS_APP = "com.google.ar.lens"
        private const val LENS_ACTIVITY = "com.google.android.apps.search.lens.LensExportedActivity"

        fun pendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, LaunchTrampolineActivity::class.java)
                .setAction(action)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
