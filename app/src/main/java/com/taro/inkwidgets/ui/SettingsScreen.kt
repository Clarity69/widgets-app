package com.taro.inkwidgets.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taro.inkwidgets.BuildConfig
import com.taro.inkwidgets.R
import com.taro.inkwidgets.ui.components.InkBadge
import com.taro.inkwidgets.ui.components.InkButton
import com.taro.inkwidgets.ui.components.InkCard
import com.taro.inkwidgets.ui.components.InkLink
import com.taro.inkwidgets.ui.components.InkOutlinedButton
import com.taro.inkwidgets.ui.components.ScreenHeader
import com.taro.inkwidgets.ui.components.ScreenPadding
import com.taro.inkwidgets.ui.theme.Ink
import com.taro.inkwidgets.ui.theme.Muted

const val GITHUB_URL = "https://github.com/Clarity69/widgets-app"

@Composable
fun SettingsScreen(
    state: UiState,
    contentPadding: PaddingValues,
    onRefreshWidgets: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = stringResource(R.string.settings_title))

        // --- Keep widgets updating ----------------------------------------------------
        InkCard {
            Text(
                stringResource(R.string.settings_update_title),
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Text(
                stringResource(R.string.settings_update_body),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                modifier = Modifier.padding(top = 6.dp)
            )
            InkOutlinedButton(
                text = stringResource(R.string.settings_open_app_details),
                onClick = { openAppDetails(context) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.settings_exact_alarm),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                InkBadge(
                    text = stringResource(
                        if (state.exactAlarmAllowed) R.string.settings_exact_alarm_allowed
                        else R.string.settings_exact_alarm_denied
                    ),
                    filled = state.exactAlarmAllowed
                )
            }
            Text(
                stringResource(
                    if (state.exactAlarmAllowed) R.string.settings_exact_alarm_allowed_hint
                    else R.string.settings_exact_alarm_denied_hint
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (!state.exactAlarmAllowed) {
                InkOutlinedButton(
                    text = stringResource(R.string.settings_open_alarm_settings),
                    onClick = { openExactAlarmSettings(context) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
            }
        }

        InkButton(
            text = stringResource(R.string.settings_refresh_all),
            onClick = onRefreshWidgets,
            modifier = Modifier.fillMaxWidth()
        )

        // --- About -----------------------------------------------------------------
        InkCard {
            Text(
                stringResource(R.string.settings_about_title),
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
            Text(
                stringResource(R.string.settings_made_by),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.settings_offline),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(top = 2.dp)
            )
            InkLink(
                text = stringResource(R.string.settings_github),
                // runCatching: no browser installed throws; nothing else to do then.
                onClick = { runCatching { uriHandler.openUri(GITHUB_URL) } },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun openAppDetails(context: Context) {
    val details = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    startSafely(context, details) || startSafely(context, Intent(Settings.ACTION_SETTINGS))
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.fromParts("package", context.packageName, null)
        )
        if (startSafely(context, intent)) return
    }
    openAppDetails(context)
}

private fun startSafely(context: Context, intent: Intent): Boolean = try {
    context.startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    false
}
