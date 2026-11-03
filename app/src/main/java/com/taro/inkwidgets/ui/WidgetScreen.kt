package com.taro.inkwidgets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taro.inkwidgets.R
import com.taro.inkwidgets.ui.components.ButtonRow
import com.taro.inkwidgets.ui.components.ClockPreview
import com.taro.inkwidgets.ui.components.InkBadge
import com.taro.inkwidgets.ui.components.InkButton
import com.taro.inkwidgets.ui.components.InkCard
import com.taro.inkwidgets.ui.components.InkOutlinedButton
import com.taro.inkwidgets.ui.components.ScreenHeader
import com.taro.inkwidgets.ui.components.ScreenPadding
import com.taro.inkwidgets.ui.components.SearchPreview
import com.taro.inkwidgets.ui.theme.Ink
import com.taro.inkwidgets.ui.theme.Muted
import com.taro.inkwidgets.ui.theme.Paper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetScreen(
    state: UiState,
    contentPadding: PaddingValues,
    onPin: (WidgetKind) -> Boolean,
    onCustomize: (WidgetKind) -> Unit,
    onMessage: (String) -> Unit,
) {
    var manualSheetFor by rememberSaveable { mutableStateOf<WidgetKind?>(null) }
    val pinRequested = stringResource(R.string.widgets_pin_requested)

    fun pin(kind: WidgetKind) {
        if (onPin(kind)) onMessage(pinRequested) else manualSheetFor = kind
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.widgets_title),
            subtitle = stringResource(R.string.widgets_subtitle)
        )

        WidgetCard(
            title = stringResource(R.string.clock_widget_label),
            description = stringResource(R.string.widgets_clock_description),
            count = state.clockCount,
            preview = { ClockPreview(state.savedClock, dark = state.darkPreview) },
            onPin = { pin(WidgetKind.CLOCK) },
            onManual = { manualSheetFor = WidgetKind.CLOCK },
            onCustomize = { onCustomize(WidgetKind.CLOCK) },
        )

        WidgetCard(
            title = stringResource(R.string.search_widget_label),
            description = stringResource(R.string.widgets_search_description),
            count = state.searchCount,
            preview = { SearchPreview(state.savedSearch, dark = state.darkPreview) },
            onPin = { pin(WidgetKind.SEARCH) },
            onManual = { manualSheetFor = WidgetKind.SEARCH },
            onCustomize = { onCustomize(WidgetKind.SEARCH) },
        )
    }

    val sheetKind = manualSheetFor
    if (sheetKind != null) {
        ModalBottomSheet(
            onDismissRequest = { manualSheetFor = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Paper,
            contentColor = Ink,
            tonalElevation = 0.dp,
        ) {
            ManualAddSteps(
                widgetName = stringResource(
                    if (sheetKind == WidgetKind.CLOCK) R.string.clock_widget_label
                    else R.string.search_widget_label
                ),
                onClose = { manualSheetFor = null }
            )
        }
    }
}

@Composable
private fun WidgetCard(
    title: String,
    description: String,
    count: Int,
    preview: @Composable () -> Unit,
    onPin: () -> Unit,
    onManual: () -> Unit,
    onCustomize: () -> Unit,
) {
    InkCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                modifier = Modifier.weight(1f)
            )
            InkBadge(
                text = if (count == 0) stringResource(R.string.widgets_count_none)
                else stringResource(R.string.widgets_count_some, count),
                filled = count > 0
            )
        }
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        preview()
        ButtonRow {
            InkButton(
                text = stringResource(R.string.widgets_add),
                onClick = onPin,
                modifier = Modifier.weight(1f)
            )
        }
        ButtonRow {
            InkOutlinedButton(
                text = stringResource(R.string.widgets_customize),
                onClick = onCustomize,
                modifier = Modifier.weight(1f)
            )
            InkOutlinedButton(
                text = stringResource(R.string.widgets_manual),
                onClick = onManual,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ManualAddSteps(widgetName: String, onClose: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            stringResource(R.string.manual_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )
        Text(
            stringResource(R.string.manual_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )
        val steps = listOf(
            stringResource(R.string.manual_step_1),
            stringResource(R.string.manual_step_2),
            stringResource(R.string.manual_step_3),
            stringResource(R.string.manual_step_4, widgetName),
            stringResource(R.string.manual_step_5),
        )
        steps.forEachIndexed { index, step ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(
                    "${index + 1}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(step, style = MaterialTheme.typography.bodyLarge, color = Ink)
            }
        }
        Spacer(Modifier.height(12.dp))
        InkButton(
            text = stringResource(R.string.manual_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
