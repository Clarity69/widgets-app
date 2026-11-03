package com.taro.inkwidgets

import java.util.Locale

/** The three colours every widget is drawn with. */
data class InkColors(val ink: Int, val paper: Int, val muted: Int) {
    /** Unlit dot: the ink colour at ~12% alpha. */
    val dotOff: Int get() = (ink and 0x00FFFFFF) or (0x1F shl 24)
}

enum class ThemePreset(val key: String, val labelRes: Int, val colors: InkColors?) {
    LIGHT(
        "light", R.string.theme_light,
        InkColors(0xFF111111.toInt(), 0xFFF4F1EA.toInt(), 0xFF5A5A5A.toInt())
    ),
    DARK(
        "dark", R.string.theme_dark,
        InkColors(0xFFF4F1EA.toInt(), 0xFF111111.toInt(), 0xFFA39F97.toInt())
    ),
    MONO(
        "mono", R.string.theme_mono,
        InkColors(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF555555.toInt())
    ),
    SEPIA(
        "sepia", R.string.theme_sepia,
        InkColors(0xFF3B2A1A.toInt(), 0xFFEFE2CC.toInt(), 0xFF7A6650.toInt())
    ),
    CUSTOM("custom", R.string.theme_custom, null);

    companion object {
        val DEFAULT_COLORS: InkColors = LIGHT.colors!!

        fun fromKey(key: String?): ThemePreset = entries.firstOrNull { it.key == key } ?: LIGHT
    }
}

/** A chosen preset plus the user's own colours (used when the preset is CUSTOM). */
data class ThemeSettings(
    val preset: ThemePreset = ThemePreset.LIGHT,
    val custom: InkColors = ThemePreset.DEFAULT_COLORS,
) {
    fun resolve(): InkColors = preset.colors ?: custom
}

object HexColor {
    /** Parses "#RRGGBB" or "RRGGBB" into an opaque colour, or null if invalid. */
    fun parse(text: String): Int? {
        val hex = text.trim().removePrefix("#")
        if (hex.length != 6) return null
        if (!hex.all { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' }) return null
        return 0xFF000000.toInt() or hex.toInt(16)
    }

    fun format(color: Int): String = String.format(Locale.ROOT, "#%06X", color and 0xFFFFFF)
}
