package com.taro.inkwidgets.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.taro.inkwidgets.ClockSettings
import com.taro.inkwidgets.HexColor
import com.taro.inkwidgets.R
import com.taro.inkwidgets.SearchSettings
import com.taro.inkwidgets.ThemePreset
import com.taro.inkwidgets.ThemeSettings
import com.taro.inkwidgets.ui.components.ButtonRow
import com.taro.inkwidgets.ui.components.ClockPreview
import com.taro.inkwidgets.ui.components.InkButton
import com.taro.inkwidgets.ui.components.InkOutlinedButton
import com.taro.inkwidgets.ui.components.InkRadioRow
import com.taro.inkwidgets.ui.components.InkSegmented
import com.taro.inkwidgets.ui.components.InkSlider
import com.taro.inkwidgets.ui.components.InkSwitchRow
import com.taro.inkwidgets.ui.components.ScreenHeader
import com.taro.inkwidgets.ui.components.SearchPreview
import com.taro.inkwidgets.ui.components.SectionTitle
import com.taro.inkwidgets.ui.theme.Ink
import com.taro.inkwidgets.ui.theme.Muted
import com.taro.inkwidgets.ui.theme.Paper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizeScreen(
    state: UiState,
    contentPadding: PaddingValues,
    onSelectKind: (WidgetKind) -> Unit,
    onDarkPreview: (Boolean) -> Unit,
    onEditClock: ((ClockSettings) -> ClockSettings) -> Unit,
    onEditSearch: ((SearchSettings) -> SearchSettings) -> Unit,
    onReset: (WidgetKind) -> Unit,
    onApply: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .imePadding()
    ) {
        // Fixed top part: title, widget switcher and live preview.
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
            ScreenHeader(title = stringResource(R.string.customize_title))
            InkSegmented(
                options = listOf(
                    stringResource(R.string.customize_tab_clock),
                    stringResource(R.string.customize_tab_search)
                ),
                selectedIndex = state.customizeKind.ordinal,
                onSelect = { onSelectKind(WidgetKind.entries[it]) }
            )
            Box(Modifier.padding(top = 12.dp)) {
                when (state.customizeKind) {
                    WidgetKind.CLOCK -> ClockPreview(state.draftClock, dark = state.darkPreview)
                    WidgetKind.SEARCH -> SearchPreview(state.draftSearch, dark = state.darkPreview)
                }
            }
            InkSwitchRow(
                label = stringResource(R.string.customize_dark_preview),
                checked = state.darkPreview,
                onCheckedChange = onDarkPreview
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Ink)

        // Scrollable options.
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            when (state.customizeKind) {
                WidgetKind.CLOCK -> ClockOptions(state.draftClock, onEditClock)
                WidgetKind.SEARCH -> SearchOptions(state.draftSearch, onEditSearch)
            }
        }

        // Fixed action bar.
        HorizontalDivider(thickness = 1.dp, color = Ink)
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
            if (state.hasUnappliedChanges) {
                Text(
                    stringResource(R.string.customize_unapplied),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            ButtonRow {
                InkOutlinedButton(
                    text = stringResource(R.string.customize_reset),
                    onClick = { onReset(state.customizeKind) },
                    modifier = Modifier.weight(1f)
                )
                InkButton(
                    text = stringResource(R.string.customize_apply),
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ClockOptions(
    s: ClockSettings,
    edit: ((ClockSettings) -> ClockSettings) -> Unit,
) {
    SectionTitle(stringResource(R.string.cfg_section_color))
    ThemePicker(s.theme) { theme -> edit { it.copy(theme = theme) } }

    SectionTitle(stringResource(R.string.cfg_section_size))
    InkSlider(stringResource(R.string.cfg_clock_scale), s.scalePercent, 40..100, "%") { v ->
        edit { it.copy(scalePercent = v) }
    }
    InkSlider(stringResource(R.string.cfg_dot_size), s.dotSizePercent, 30..100, "%") { v ->
        edit { it.copy(dotSizePercent = v) }
    }
    InkSlider(stringResource(R.string.cfg_date_size), s.dateSizePercent, 60..160, "%") { v ->
        edit { it.copy(dateSizePercent = v) }
    }

    SectionTitle(stringResource(R.string.cfg_section_elements))
    InkSwitchRow(stringResource(R.string.cfg_show_date), s.showDate) { v ->
        edit { it.copy(showDate = v) }
    }
    InkSwitchRow(stringResource(R.string.cfg_show_off_dots), s.showOffDots) { v ->
        edit { it.copy(showOffDots = v) }
    }
    InkSwitchRow(stringResource(R.string.cfg_paper_background), s.paperBackground) { v ->
        edit { it.copy(paperBackground = v) }
    }
}

@Composable
private fun SearchOptions(
    s: SearchSettings,
    edit: ((SearchSettings) -> SearchSettings) -> Unit,
) {
    SectionTitle(stringResource(R.string.cfg_section_color))
    ThemePicker(s.theme) { theme -> edit { it.copy(theme = theme) } }

    SectionTitle(stringResource(R.string.cfg_section_size))
    InkSlider(stringResource(R.string.cfg_bar_height), s.heightDp, 32..64, " dp") { v ->
        edit { it.copy(heightDp = v) }
    }
    InkSlider(stringResource(R.string.cfg_border_width), s.borderDp, 0..4, " dp") { v ->
        edit { it.copy(borderDp = v) }
    }
    InkSlider(stringResource(R.string.cfg_g_size), s.gSizeSp, 14..32, " sp") { v ->
        edit { it.copy(gSizeSp = v) }
    }
    InkSlider(stringResource(R.string.cfg_icon_size), s.iconSizeDp, 16..32, " dp") { v ->
        edit { it.copy(iconSizeDp = v) }
    }

    SectionTitle(stringResource(R.string.cfg_section_elements))
    InkSwitchRow(stringResource(R.string.cfg_show_g), s.showG) { v ->
        edit { it.copy(showG = v) }
    }
    InkSwitchRow(stringResource(R.string.cfg_show_mic), s.showMic) { v ->
        edit { it.copy(showMic = v) }
    }
    InkSwitchRow(stringResource(R.string.cfg_show_lens), s.showLens) { v ->
        edit { it.copy(showLens = v) }
    }
    InkSwitchRow(stringResource(R.string.cfg_show_divider), s.showDivider) { v ->
        edit { it.copy(showDivider = v) }
    }
}

/** Preset list plus three hex fields that appear for the custom preset. */
@Composable
private fun ThemePicker(theme: ThemeSettings, onChange: (ThemeSettings) -> Unit) {
    ThemePreset.entries.forEach { preset ->
        InkRadioRow(
            label = stringResource(preset.labelRes),
            selected = theme.preset == preset,
            onClick = {
                if (preset == ThemePreset.CUSTOM && theme.preset != ThemePreset.CUSTOM) {
                    // Start the custom colours from what is on screen now.
                    onChange(theme.copy(preset = preset, custom = theme.resolve()))
                } else {
                    onChange(theme.copy(preset = preset))
                }
            }
        )
    }
    if (theme.preset == ThemePreset.CUSTOM) {
        val custom = theme.custom
        HexField(stringResource(R.string.cfg_color_ink), custom.ink) { c ->
            onChange(theme.copy(custom = custom.copy(ink = c)))
        }
        HexField(stringResource(R.string.cfg_color_paper), custom.paper) { c ->
            onChange(theme.copy(custom = custom.copy(paper = c)))
        }
        HexField(stringResource(R.string.cfg_color_muted), custom.muted) { c ->
            onChange(theme.copy(custom = custom.copy(muted = c)))
        }
        Text(
            stringResource(R.string.cfg_custom_note),
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun HexField(label: String, color: Int, onColor: (Int) -> Unit) {
    // Local text so half-typed values are kept; the colour only changes when valid.
    var text by remember(color) { mutableStateOf(HexColor.format(color)) }
    val valid = HexColor.parse(text) != null
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val cleaned = input.take(7)
                text = cleaned
                HexColor.parse(cleaned)?.let(onColor)
            },
            label = { Text(label) },
            singleLine = true,
            isError = !valid,
            supportingText = {
                if (!valid) Text(stringResource(R.string.cfg_hex_error))
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedBorderColor = Ink,
                unfocusedBorderColor = Muted,
                errorBorderColor = Ink,
                focusedLabelColor = Ink,
                unfocusedLabelColor = Muted,
                errorLabelColor = Ink,
                errorSupportingTextColor = Ink,
                errorTextColor = Ink,
                cursorColor = Ink,
                errorCursorColor = Ink,
                focusedContainerColor = Paper,
                unfocusedContainerColor = Paper,
                errorContainerColor = Paper,
            ),
            modifier = Modifier.weight(1f)
        )
        Box(
            Modifier
                .padding(start = 12.dp)
                .size(40.dp)
                .border(1.dp, Ink, RoundedCornerShape(8.dp))
                .background(Color(color), RoundedCornerShape(8.dp))
        )
    }
}
