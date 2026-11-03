package com.taro.inkwidgets.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taro.inkwidgets.ui.theme.DotOff
import com.taro.inkwidgets.ui.theme.Ink
import com.taro.inkwidgets.ui.theme.Muted
import com.taro.inkwidgets.ui.theme.Paper
import kotlin.math.roundToInt

val CardShape = RoundedCornerShape(16.dp)
val PillShape = RoundedCornerShape(percent = 50)

/** Screen heading (serif) with an optional muted subtitle. */
@Composable
fun ScreenHeader(title: String, subtitle: String? = null) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.displaySmall, color = Ink)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = Ink,
        modifier = modifier.padding(top = 20.dp, bottom = 4.dp)
    )
}

/** Flat Paper card with a thin Ink border; no elevation. */
@Composable
fun InkCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Ink, CardShape)
            .background(Paper, CardShape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun InkButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper),
        elevation = null,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun InkOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = PillShape,
        border = BorderStroke(1.5.dp, Ink),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Paper, contentColor = Ink),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

/** Small bordered pill, e.g. "2 terpasang". */
@Composable
fun InkBadge(text: String, filled: Boolean) {
    Box(
        modifier = Modifier
            .border(1.dp, Ink, PillShape)
            .background(if (filled) Ink else Paper, PillShape)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (filled) Paper else Ink
        )
    }
}

/** Two or more equal segments with an Ink border; the selected one is filled Ink. */
@Composable
fun InkSegmented(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Ink, PillShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .background(if (selected) Ink else Paper, PillShape)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Paper else Ink
                )
            }
        }
    }
}

@Composable
fun InkSlider(
    label: String,
    value: Int,
    range: IntRange,
    unit: String,
    onValueChange: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Text("$value$unit", style = MaterialTheme.typography.bodyLarge, color = Muted)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(range.first, range.last)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Ink,
                activeTrackColor = Ink,
                inactiveTrackColor = DotOff,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

@Composable
fun InkSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Paper,
                checkedTrackColor = Ink,
                checkedBorderColor = Ink,
                uncheckedThumbColor = Ink,
                uncheckedTrackColor = Paper,
                uncheckedBorderColor = Ink,
            ),
        )
    }
}

@Composable
fun InkRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Ink, unselectedColor = Ink),
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

/** Plain text link (underline-free, Ink, bold label style). */
@Composable
fun InkLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = Ink,
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
    )
}

/** Row of buttons that share the width equally. */
@Composable
fun ButtonRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

val ScreenPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
