package com.taro.inkwidgets

import android.graphics.Bitmap

class SearchConfigActivity : BaseConfigActivity() {

    private var settings = SearchSettings()

    override val providerClass = SearchWidgetProvider::class.java
    override val titleRes = R.string.search_config_title
    override val defaultWidthDp = SearchWidgetProvider.DEFAULT_WIDTH_DP
    override val defaultHeightDp = SearchWidgetProvider.DEFAULT_HEIGHT_DP

    override fun loadSettings() {
        settings = SettingsStore.loadSearch(this, appWidgetId)
    }

    override fun buildForm(ui: ConfigUi) {
        ui.section(R.string.cfg_section_color)
        ui.themePicker(settings.theme) { settings = settings.copy(theme = it) }

        ui.section(R.string.cfg_section_size)
        ui.slider(R.string.cfg_bar_height, 32, 64, settings.heightDp, " dp") {
            settings = settings.copy(heightDp = it)
        }
        ui.slider(R.string.cfg_border_width, 0, 4, settings.borderDp, " dp") {
            settings = settings.copy(borderDp = it)
        }
        ui.slider(R.string.cfg_g_size, 14, 32, settings.gSizeSp, " sp") {
            settings = settings.copy(gSizeSp = it)
        }
        ui.slider(R.string.cfg_icon_size, 16, 32, settings.iconSizeDp, " dp") {
            settings = settings.copy(iconSizeDp = it)
        }

        ui.section(R.string.cfg_section_elements)
        ui.toggle(R.string.cfg_show_g, settings.showG) {
            settings = settings.copy(showG = it)
        }
        ui.toggle(R.string.cfg_show_mic, settings.showMic) {
            settings = settings.copy(showMic = it)
        }
        ui.toggle(R.string.cfg_show_lens, settings.showLens) {
            settings = settings.copy(showLens = it)
        }
        ui.toggle(R.string.cfg_show_divider, settings.showDivider) {
            settings = settings.copy(showDivider = it)
        }
    }

    override fun renderPreview(widthPx: Int, heightPx: Int): Bitmap =
        SearchRenderer.render(this, widthPx, heightPx, settings)

    override fun saveSettings() {
        SettingsStore.saveSearch(this, appWidgetId, settings)
    }

    override fun applyToWidget() {
        SearchWidgetProvider.updateAll(this)
    }
}
