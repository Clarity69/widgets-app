package com.taro.inkwidgets.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/** Every surface is Paper, every foreground is Ink: no tints, no tonal elevation. */
private val InkColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = Ink,
    onPrimaryContainer = Paper,
    inversePrimary = Paper,
    secondary = Ink,
    onSecondary = Paper,
    secondaryContainer = Ink,
    onSecondaryContainer = Paper,
    tertiary = Ink,
    onTertiary = Paper,
    tertiaryContainer = Ink,
    onTertiaryContainer = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Muted,
    surfaceTint = Paper,
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    error = Ink,
    onError = Paper,
    errorContainer = Paper,
    onErrorContainer = Ink,
    outline = Ink,
    outlineVariant = Muted,
    scrim = Ink,
    surfaceBright = Paper,
    surfaceDim = Paper,
    surfaceContainer = Paper,
    surfaceContainerHigh = Paper,
    surfaceContainerHighest = Paper,
    surfaceContainerLow = Paper,
    surfaceContainerLowest = Paper,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InkWidgetsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = InkColorScheme,
        typography = InkTypography,
    ) {
        // E-ink feel: no ripple animation on press.
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            content()
        }
    }
}
