package com.taro.inkwidgets

import android.graphics.Bitmap
import java.util.Calendar

class ClockConfigActivity : BaseConfigActivity() {

    private var settings = ClockSettings()

    override val providerClass = ClockWidgetProvider::class.java
    override val titleRes = R.string.clock_config_title
    override val defaultWidthDp = ClockWidgetProvider.DEFAULT_WIDTH_DP
    override val defaultHeightDp = ClockWidgetProvider.DEFAULT_HEIGHT_DP

    override fun loadSettings() {
        settings = SettingsStore.loadClock(this, appWidgetId)
    }

    override fun buildForm(ui: ConfigUi) {
        ui.section(R.string.cfg_section_color)
        ui.themePicker(settings.theme) { settings = settings.copy(theme = it) }

        ui.section(R.string.cfg_section_size)
        ui.slider(R.string.cfg_clock_scale, 40, 100, settings.scalePercent, "%") {
            settings = settings.copy(scalePercent = it)
        }
        ui.slider(R.string.cfg_dot_size, 30, 100, settings.dotSizePercent, "%") {
            settings = settings.copy(dotSizePercent = it)
        }
        ui.slider(R.string.cfg_date_size, 60, 160, settings.dateSizePercent, "%") {
            settings = settings.copy(dateSizePercent = it)
        }

        ui.section(R.string.cfg_section_elements)
        ui.toggle(R.string.cfg_show_date, settings.showDate) {
            settings = settings.copy(showDate = it)
        }
        ui.toggle(R.string.cfg_show_off_dots, settings.showOffDots) {
            settings = settings.copy(showOffDots = it)
        }
        ui.toggle(R.string.cfg_paper_background, settings.paperBackground) {
            settings = settings.copy(paperBackground = it)
        }
    }

    override fun renderPreview(widthPx: Int, heightPx: Int): Bitmap =
        ClockRenderer.render(widthPx, heightPx, Calendar.getInstance(), settings)

    override fun saveSettings() {
        SettingsStore.saveClock(this, appWidgetId, settings)
    }

    override fun applyToWidget() {
        ClockWidgetProvider.updateAll(this)
    }
}
