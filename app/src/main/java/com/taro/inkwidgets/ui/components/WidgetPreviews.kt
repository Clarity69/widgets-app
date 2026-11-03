package com.taro.inkwidgets.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.taro.inkwidgets.ClockRenderer
import com.taro.inkwidgets.ClockSettings
import com.taro.inkwidgets.ClockWidgetProvider
import com.taro.inkwidgets.SearchRenderer
import com.taro.inkwidgets.SearchSettings
import com.taro.inkwidgets.SearchWidgetProvider
import com.taro.inkwidgets.ui.theme.Ink
import com.taro.inkwidgets.ui.theme.Muted
import com.taro.inkwidgets.ui.theme.Paper
import kotlinx.coroutines.delay
import java.util.Calendar

private val BackdropShape = RoundedCornerShape(12.dp)

/** Light (Paper) or dark (Ink) "wallpaper" behind a preview. */
private fun Modifier.backdrop(dark: Boolean): Modifier =
    this
        .fillMaxWidth()
        .border(1.dp, if (dark) Ink else Muted, BackdropShape)
        .background(if (dark) Ink else Paper, BackdropShape)
        .padding(12.dp)

/** Minute value that ticks over exactly at each minute boundary. */
@Composable
private fun currentMinute(): Long {
    val minute by produceState(initialValue = System.currentTimeMillis() / 60_000L) {
        while (true) {
            val now = System.currentTimeMillis()
            delay(60_000L - now % 60_000L)
            value = System.currentTimeMillis() / 60_000L
        }
    }
    return minute
}

/** The dot-matrix clock, drawn by the same [ClockRenderer] the widget uses. */
@Composable
fun ClockPreview(settings: ClockSettings, dark: Boolean, modifier: Modifier = Modifier) {
    val minute = currentMinute()
    BoxWithConstraints(modifier.backdrop(dark)) {
        val widthPx = constraints.maxWidth.coerceAtLeast(1)
        val heightPx = (widthPx * ClockWidgetProvider.DEFAULT_HEIGHT_DP /
            ClockWidgetProvider.DEFAULT_WIDTH_DP.toFloat()).toInt().coerceAtLeast(1)
        val bitmap = remember(widthPx, heightPx, settings, minute) {
            ClockRenderer.render(widthPx, heightPx, Calendar.getInstance(), settings).asImageBitmap()
        }
        val heightDp = with(LocalDensity.current) { heightPx.toDp() }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(heightDp)
        )
    }
}

/** The slim search bar, drawn by the same [SearchRenderer] the widget uses. */
@Composable
fun SearchPreview(settings: SearchSettings, dark: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    BoxWithConstraints(modifier.backdrop(dark)) {
        val widthPx = constraints.maxWidth.coerceAtLeast(1)
        val heightDp = SearchWidgetProvider.DEFAULT_HEIGHT_DP.dp
        val heightPx = with(density) { heightDp.roundToPx() }.coerceAtLeast(1)
        val bitmap = remember(widthPx, heightPx, settings) {
            SearchRenderer.render(context, widthPx, heightPx, settings).asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(heightDp)
        )
    }
}
