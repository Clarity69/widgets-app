package com.taro.inkwidgets.ui

import android.app.AlarmManager
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.taro.inkwidgets.ClockSettings
import com.taro.inkwidgets.ClockWidgetProvider
import com.taro.inkwidgets.SearchSettings
import com.taro.inkwidgets.SearchWidgetProvider
import com.taro.inkwidgets.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class WidgetKind { CLOCK, SEARCH }

data class UiState(
    /** Settings the widgets currently use. */
    val savedClock: ClockSettings,
    val savedSearch: SearchSettings,
    /** Settings being edited on the Kustomisasi tab (not yet applied). */
    val draftClock: ClockSettings,
    val draftSearch: SearchSettings,
    val customizeKind: WidgetKind = WidgetKind.CLOCK,
    val darkPreview: Boolean = false,
    val clockCount: Int = 0,
    val searchCount: Int = 0,
    val pinSupported: Boolean = false,
    val exactAlarmAllowed: Boolean = true,
) {
    val hasUnappliedChanges: Boolean
        get() = draftClock != savedClock || draftSearch != savedSearch
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application get() = getApplication()

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private fun initialState(): UiState {
        val clock = SettingsStore.loadClock(app)
        val search = SettingsStore.loadSearch(app)
        return UiState(
            savedClock = clock,
            savedSearch = search,
            draftClock = clock,
            draftSearch = search,
        )
    }

    /** Re-read things that can change while the app is in the background. */
    fun refreshSystemState() {
        val manager = AppWidgetManager.getInstance(app)
        val clockCount = manager.getAppWidgetIds(ComponentName(app, ClockWidgetProvider::class.java)).size
        val searchCount = manager.getAppWidgetIds(ComponentName(app, SearchWidgetProvider::class.java)).size
        val alarmManager = app.getSystemService(AlarmManager::class.java)
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager == null ||
            alarmManager.canScheduleExactAlarms()
        _state.update {
            it.copy(
                clockCount = clockCount,
                searchCount = searchCount,
                pinSupported = manager.isRequestPinAppWidgetSupported,
                exactAlarmAllowed = exactAllowed,
            )
        }
    }

    fun selectKind(kind: WidgetKind) = _state.update { it.copy(customizeKind = kind) }

    fun setDarkPreview(dark: Boolean) = _state.update { it.copy(darkPreview = dark) }

    fun editClock(transform: (ClockSettings) -> ClockSettings) =
        _state.update { it.copy(draftClock = transform(it.draftClock)) }

    fun editSearch(transform: (SearchSettings) -> SearchSettings) =
        _state.update { it.copy(draftSearch = transform(it.draftSearch)) }

    /** Reset the draft of the widget type being edited back to factory defaults. */
    fun resetDraft(kind: WidgetKind) = _state.update {
        when (kind) {
            WidgetKind.CLOCK -> it.copy(draftClock = ClockSettings())
            WidgetKind.SEARCH -> it.copy(draftSearch = SearchSettings())
        }
    }

    /** Save both drafts and redraw every placed widget. Returns the number of widgets updated. */
    fun applyDrafts(): Int {
        val current = _state.value
        SettingsStore.saveClock(app, current.draftClock)
        SettingsStore.saveSearch(app, current.draftSearch)
        _state.update { it.copy(savedClock = it.draftClock, savedSearch = it.draftSearch) }
        return refreshAllWidgets()
    }

    /** Redraw every placed widget (and re-arm the clock alarm). Returns the widget count. */
    fun refreshAllWidgets(): Int {
        ClockWidgetProvider.updateAll(app)
        SearchWidgetProvider.updateAll(app)
        refreshSystemState()
        return _state.value.clockCount + _state.value.searchCount
    }

    /**
     * Ask the launcher to pin a widget. Returns false when the launcher does not support it
     * (common on MIUI), so the caller can show manual steps instead.
     */
    fun requestPin(kind: WidgetKind): Boolean {
        val manager = AppWidgetManager.getInstance(app)
        if (!manager.isRequestPinAppWidgetSupported) return false
        val provider = when (kind) {
            WidgetKind.CLOCK -> ComponentName(app, ClockWidgetProvider::class.java)
            WidgetKind.SEARCH -> ComponentName(app, SearchWidgetProvider::class.java)
        }
        return try {
            manager.requestPinAppWidget(provider, null, null)
        } catch (e: IllegalStateException) {
            false
        }
    }
}
