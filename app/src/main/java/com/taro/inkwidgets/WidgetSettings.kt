package com.taro.inkwidgets

import android.content.Context
import android.content.SharedPreferences

data class ClockSettings(
    val theme: ThemeSettings = ThemeSettings(),
    /** Overall clock size as % of the space available in the widget. */
    val scalePercent: Int = 100,
    /** Dot diameter as % of the distance between dot centres. */
    val dotSizePercent: Int = 80,
    /** Date text size relative to the default. */
    val dateSizePercent: Int = 100,
    val showDate: Boolean = true,
    val showOffDots: Boolean = true,
    val paperBackground: Boolean = false,
)

data class SearchSettings(
    val theme: ThemeSettings = ThemeSettings(),
    val heightDp: Int = 44,
    val borderDp: Int = 2,
    val gSizeSp: Int = 22,
    val iconSizeDp: Int = 22,
    val showG: Boolean = true,
    val showMic: Boolean = true,
    val showLens: Boolean = true,
    val showDivider: Boolean = false,
)

/** Per-widget settings, keyed by appWidgetId, in one SharedPreferences file. */
object SettingsStore {

    private const val PREFS = "widget_settings"
    private const val CLOCK = "clock"
    private const val SEARCH = "search"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun loadClock(context: Context, appWidgetId: Int): ClockSettings {
        val p = prefs(context)
        val k = "${CLOCK}_$appWidgetId"
        val d = ClockSettings()
        return ClockSettings(
            theme = loadTheme(p, k),
            scalePercent = p.getInt("$k.scale", d.scalePercent),
            dotSizePercent = p.getInt("$k.dot", d.dotSizePercent),
            dateSizePercent = p.getInt("$k.date", d.dateSizePercent),
            showDate = p.getBoolean("$k.showDate", d.showDate),
            showOffDots = p.getBoolean("$k.showOff", d.showOffDots),
            paperBackground = p.getBoolean("$k.paperBg", d.paperBackground),
        )
    }

    fun saveClock(context: Context, appWidgetId: Int, s: ClockSettings) {
        val k = "${CLOCK}_$appWidgetId"
        val e = prefs(context).edit()
        saveTheme(e, k, s.theme)
        e.putInt("$k.scale", s.scalePercent)
        e.putInt("$k.dot", s.dotSizePercent)
        e.putInt("$k.date", s.dateSizePercent)
        e.putBoolean("$k.showDate", s.showDate)
        e.putBoolean("$k.showOff", s.showOffDots)
        e.putBoolean("$k.paperBg", s.paperBackground)
        e.apply()
    }

    fun loadSearch(context: Context, appWidgetId: Int): SearchSettings {
        val p = prefs(context)
        val k = "${SEARCH}_$appWidgetId"
        val d = SearchSettings()
        return SearchSettings(
            theme = loadTheme(p, k),
            heightDp = p.getInt("$k.height", d.heightDp),
            borderDp = p.getInt("$k.border", d.borderDp),
            gSizeSp = p.getInt("$k.gSize", d.gSizeSp),
            iconSizeDp = p.getInt("$k.iconSize", d.iconSizeDp),
            showG = p.getBoolean("$k.showG", d.showG),
            showMic = p.getBoolean("$k.showMic", d.showMic),
            showLens = p.getBoolean("$k.showLens", d.showLens),
            showDivider = p.getBoolean("$k.showDivider", d.showDivider),
        )
    }

    fun saveSearch(context: Context, appWidgetId: Int, s: SearchSettings) {
        val k = "${SEARCH}_$appWidgetId"
        val e = prefs(context).edit()
        saveTheme(e, k, s.theme)
        e.putInt("$k.height", s.heightDp)
        e.putInt("$k.border", s.borderDp)
        e.putInt("$k.gSize", s.gSizeSp)
        e.putInt("$k.iconSize", s.iconSizeDp)
        e.putBoolean("$k.showG", s.showG)
        e.putBoolean("$k.showMic", s.showMic)
        e.putBoolean("$k.showLens", s.showLens)
        e.putBoolean("$k.showDivider", s.showDivider)
        e.apply()
    }

    fun deleteClock(context: Context, appWidgetIds: IntArray) = delete(context, CLOCK, appWidgetIds)

    fun deleteSearch(context: Context, appWidgetIds: IntArray) = delete(context, SEARCH, appWidgetIds)

    private fun delete(context: Context, type: String, appWidgetIds: IntArray) {
        val p = prefs(context)
        val prefixes = appWidgetIds.map { "${type}_$it." }
        val e = p.edit()
        for (key in p.all.keys) {
            if (prefixes.any { key.startsWith(it) }) e.remove(key)
        }
        e.apply()
    }

    private fun loadTheme(p: SharedPreferences, k: String): ThemeSettings {
        val d = ThemePreset.DEFAULT_COLORS
        return ThemeSettings(
            preset = ThemePreset.fromKey(p.getString("$k.theme", null)),
            custom = InkColors(
                ink = p.getInt("$k.ink", d.ink),
                paper = p.getInt("$k.paper", d.paper),
                muted = p.getInt("$k.muted", d.muted),
            ),
        )
    }

    private fun saveTheme(e: SharedPreferences.Editor, k: String, t: ThemeSettings) {
        e.putString("$k.theme", t.preset.key)
        e.putInt("$k.ink", t.custom.ink)
        e.putInt("$k.paper", t.custom.paper)
        e.putInt("$k.muted", t.custom.muted)
    }
}
