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

/**
 * The single place where widget customisation is stored (SharedPreferences).
 * One settings set per widget type; every placed widget of that type uses it.
 *
 * Version 1 stored settings per widget ("clock_<id>.*"); [migrateIfNeeded] copies the
 * first such set into the shared keys ("clock.*") and removes the old keys.
 */
object SettingsStore {

    private const val PREFS = "widget_settings"
    private const val CLOCK = "clock"
    private const val SEARCH = "search"
    private const val KEY_SCHEMA = "schema_version"
    private const val SCHEMA = 2

    private fun prefs(context: Context): SharedPreferences {
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        migrateIfNeeded(p)
        return p
    }

    fun loadClock(context: Context): ClockSettings = readClock(prefs(context), CLOCK)

    fun saveClock(context: Context, settings: ClockSettings) {
        val e = prefs(context).edit()
        writeClock(e, CLOCK, settings)
        e.apply()
    }

    fun loadSearch(context: Context): SearchSettings = readSearch(prefs(context), SEARCH)

    fun saveSearch(context: Context, settings: SearchSettings) {
        val e = prefs(context).edit()
        writeSearch(e, SEARCH, settings)
        e.apply()
    }

    // --- migration ---------------------------------------------------------------------

    private val LEGACY_KEY = Regex("""^(clock|search)_(\d+)\.theme$""")

    private fun migrateIfNeeded(p: SharedPreferences) {
        if (p.getInt(KEY_SCHEMA, 1) >= SCHEMA) return
        val keys = p.all.keys.toList()
        val firstLegacy = HashMap<String, String>() // type -> legacy prefix
        for (key in keys.sorted()) {
            val match = LEGACY_KEY.matchEntire(key) ?: continue
            val type = match.groupValues[1]
            if (type !in firstLegacy) firstLegacy[type] = "${type}_${match.groupValues[2]}"
        }
        val e = p.edit()
        firstLegacy[CLOCK]?.let { writeClock(e, CLOCK, readClock(p, it)) }
        firstLegacy[SEARCH]?.let { writeSearch(e, SEARCH, readSearch(p, it)) }
        for (key in keys) {
            if (key.startsWith("${CLOCK}_") || key.startsWith("${SEARCH}_")) e.remove(key)
        }
        e.putInt(KEY_SCHEMA, SCHEMA)
        e.apply()
    }

    // --- serialisation -----------------------------------------------------------------

    private fun readClock(p: SharedPreferences, k: String): ClockSettings {
        val d = ClockSettings()
        return ClockSettings(
            theme = readTheme(p, k),
            scalePercent = p.getInt("$k.scale", d.scalePercent),
            dotSizePercent = p.getInt("$k.dot", d.dotSizePercent),
            dateSizePercent = p.getInt("$k.date", d.dateSizePercent),
            showDate = p.getBoolean("$k.showDate", d.showDate),
            showOffDots = p.getBoolean("$k.showOff", d.showOffDots),
            paperBackground = p.getBoolean("$k.paperBg", d.paperBackground),
        )
    }

    private fun writeClock(e: SharedPreferences.Editor, k: String, s: ClockSettings) {
        writeTheme(e, k, s.theme)
        e.putInt("$k.scale", s.scalePercent)
        e.putInt("$k.dot", s.dotSizePercent)
        e.putInt("$k.date", s.dateSizePercent)
        e.putBoolean("$k.showDate", s.showDate)
        e.putBoolean("$k.showOff", s.showOffDots)
        e.putBoolean("$k.paperBg", s.paperBackground)
    }

    private fun readSearch(p: SharedPreferences, k: String): SearchSettings {
        val d = SearchSettings()
        return SearchSettings(
            theme = readTheme(p, k),
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

    private fun writeSearch(e: SharedPreferences.Editor, k: String, s: SearchSettings) {
        writeTheme(e, k, s.theme)
        e.putInt("$k.height", s.heightDp)
        e.putInt("$k.border", s.borderDp)
        e.putInt("$k.gSize", s.gSizeSp)
        e.putInt("$k.iconSize", s.iconSizeDp)
        e.putBoolean("$k.showG", s.showG)
        e.putBoolean("$k.showMic", s.showMic)
        e.putBoolean("$k.showLens", s.showLens)
        e.putBoolean("$k.showDivider", s.showDivider)
    }

    private fun readTheme(p: SharedPreferences, k: String): ThemeSettings {
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

    private fun writeTheme(e: SharedPreferences.Editor, k: String, t: ThemeSettings) {
        e.putString("$k.theme", t.preset.key)
        e.putInt("$k.ink", t.custom.ink)
        e.putInt("$k.paper", t.custom.paper)
        e.putInt("$k.muted", t.custom.muted)
    }
}
