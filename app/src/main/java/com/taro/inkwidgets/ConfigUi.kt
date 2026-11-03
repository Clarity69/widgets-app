package com.taro.inkwidgets

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import kotlin.math.roundToInt

/** Small helpers that build the settings form in code and append it to [container]. */
class ConfigUi(
    private val activity: Activity,
    private val container: LinearLayout,
    private val onChange: () -> Unit
) {
    private val density = activity.resources.displayMetrics.density
    private val ink = activity.getColor(R.color.ink)
    private val muted = activity.getColor(R.color.muted)

    fun dp(value: Int): Int = (value * density).roundToInt()

    private fun matchWrap(topDp: Int = 0) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(topDp) }

    fun section(titleRes: Int) {
        val title = TextView(activity).apply {
            setText(titleRes)
            setTextColor(ink)
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
        }
        container.addView(title, matchWrap(28))
    }

    fun slider(
        labelRes: Int,
        minValue: Int,
        maxValue: Int,
        value: Int,
        unit: String,
        onValue: (Int) -> Unit
    ) {
        val label = activity.getString(labelRes)
        val text = TextView(activity).apply {
            setTextColor(ink)
            textSize = 15f
        }
        fun show(v: Int) {
            text.text = "$label: $v$unit"
        }
        val bar = SeekBar(activity).apply {
            max = maxValue
            min = minValue
            progress = value.coerceIn(minValue, maxValue)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    show(progress)
                    if (fromUser) {
                        onValue(progress)
                        onChange()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
        show(bar.progress)
        container.addView(text, matchWrap(14))
        container.addView(bar, matchWrap(4))
    }

    fun toggle(labelRes: Int, checked: Boolean, onValue: (Boolean) -> Unit) {
        val switch = Switch(activity).apply {
            setText(labelRes)
            setTextColor(ink)
            textSize = 15f
            isChecked = checked
            minHeight = dp(48)
            setOnCheckedChangeListener { _, isOn ->
                onValue(isOn)
                onChange()
            }
        }
        container.addView(switch, matchWrap(4))
    }

    /** Preset radio list plus three hex inputs shown only for the custom preset. */
    fun themePicker(current: ThemeSettings, onValue: (ThemeSettings) -> Unit) {
        var theme = current
        var filling = false

        val customBox = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            visibility = if (current.preset == ThemePreset.CUSTOM) View.VISIBLE else View.GONE
        }
        val inkRow = hexRow(customBox, R.string.cfg_color_ink, theme.custom.ink) { c ->
            if (!filling) {
                theme = theme.copy(custom = theme.custom.copy(ink = c))
                onValue(theme)
                onChange()
            }
        }
        val paperRow = hexRow(customBox, R.string.cfg_color_paper, theme.custom.paper) { c ->
            if (!filling) {
                theme = theme.copy(custom = theme.custom.copy(paper = c))
                onValue(theme)
                onChange()
            }
        }
        val mutedRow = hexRow(customBox, R.string.cfg_color_muted, theme.custom.muted) { c ->
            if (!filling) {
                theme = theme.copy(custom = theme.custom.copy(muted = c))
                onValue(theme)
                onChange()
            }
        }

        val group = RadioGroup(activity).apply { orientation = RadioGroup.VERTICAL }
        val idToPreset = HashMap<Int, ThemePreset>()
        for (preset in ThemePreset.entries) {
            val button = RadioButton(activity).apply {
                id = View.generateViewId()
                setText(preset.labelRes)
                setTextColor(ink)
                textSize = 15f
                minHeight = dp(44)
            }
            idToPreset[button.id] = preset
            group.addView(button)
            if (preset == current.preset) button.isChecked = true
        }
        group.setOnCheckedChangeListener { _, checkedId ->
            val preset = idToPreset[checkedId] ?: return@setOnCheckedChangeListener
            theme = if (preset == ThemePreset.CUSTOM && theme.preset != ThemePreset.CUSTOM) {
                // Start the custom colours from whatever is on screen now.
                theme.copy(preset = preset, custom = theme.resolve())
            } else {
                theme.copy(preset = preset)
            }
            if (preset == ThemePreset.CUSTOM) {
                filling = true
                inkRow.setText(HexColor.format(theme.custom.ink))
                paperRow.setText(HexColor.format(theme.custom.paper))
                mutedRow.setText(HexColor.format(theme.custom.muted))
                filling = false
            }
            customBox.visibility = if (preset == ThemePreset.CUSTOM) View.VISIBLE else View.GONE
            onValue(theme)
            onChange()
        }

        container.addView(group, matchWrap(8))
        container.addView(customBox, matchWrap(4))
    }

    private fun hexRow(
        parent: LinearLayout,
        labelRes: Int,
        initial: Int,
        onColor: (Int) -> Unit
    ): EditText {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val label = TextView(activity).apply {
            setText(labelRes)
            setTextColor(ink)
            textSize = 15f
        }
        val swatchShape = GradientDrawable().apply {
            cornerRadius = dp(6).toFloat()
            setColor(initial)
            setStroke(dp(1), muted)
        }
        val swatch = View(activity).apply { background = swatchShape }
        val input = EditText(activity).apply {
            setText(HexColor.format(initial))
            setTextColor(ink)
            setHintTextColor(muted)
            hint = "#RRGGBB"
            textSize = 15f
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(7))
        }
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val color = HexColor.parse(s?.toString().orEmpty())
                if (color == null) {
                    input.error = activity.getString(R.string.cfg_hex_error)
                } else {
                    input.error = null
                    swatchShape.setColor(color)
                    onColor(color)
                }
            }
        })

        row.addView(label, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(input, LinearLayout.LayoutParams(dp(120), LinearLayout.LayoutParams.WRAP_CONTENT))
        row.addView(swatch, LinearLayout.LayoutParams(dp(28), dp(28)).apply { marginStart = dp(8) })
        parent.addView(row, matchWrap(4))
        return input
    }
}
