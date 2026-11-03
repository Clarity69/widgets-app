package com.taro.inkwidgets.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.taro.inkwidgets.R
import com.taro.inkwidgets.ui.theme.Ink
import com.taro.inkwidgets.ui.theme.Muted
import com.taro.inkwidgets.ui.theme.Paper
import kotlinx.coroutines.launch

private enum class Tab(val route: String, val labelRes: Int, val icon: ImageVector) {
    WIDGETS("widgets", R.string.tab_widgets, Icons.Outlined.Widgets),
    CUSTOMIZE("customize", R.string.tab_customize, Icons.Outlined.Tune),
    SETTINGS("settings", R.string.tab_settings, Icons.Outlined.Settings),
}

@Composable
fun InkApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Widget count, pin support and alarm permission can change while we are away.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSystemState()
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    fun navigateTo(tab: Tab) {
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val appliedMessage = stringResource(R.string.customize_applied)
    val appliedNoneMessage = stringResource(R.string.customize_applied_none)
    val refreshedMessage = stringResource(R.string.settings_refreshed)

    Scaffold(
        containerColor = Paper,
        contentColor = Ink,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = Ink, contentColor = Paper)
            }
        },
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val destination = backStackEntry?.destination
            Column {
                HorizontalDivider(thickness = 1.dp, color = Ink)
                NavigationBar(containerColor = Paper, contentColor = Ink, tonalElevation = 0.dp) {
                    Tab.entries.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateTo(tab) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Paper,
                                selectedTextColor = Ink,
                                indicatorColor = Ink,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.WIDGETS.route,
            // E-ink: switch screens instantly, no animation.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(Tab.WIDGETS.route) {
                WidgetScreen(
                    state = state,
                    contentPadding = innerPadding,
                    onPin = viewModel::requestPin,
                    onCustomize = { kind ->
                        viewModel.selectKind(kind)
                        navigateTo(Tab.CUSTOMIZE)
                    },
                    onMessage = ::showMessage,
                )
            }
            composable(Tab.CUSTOMIZE.route) {
                CustomizeScreen(
                    state = state,
                    contentPadding = innerPadding,
                    onSelectKind = viewModel::selectKind,
                    onDarkPreview = viewModel::setDarkPreview,
                    onEditClock = viewModel::editClock,
                    onEditSearch = viewModel::editSearch,
                    onReset = viewModel::resetDraft,
                    onApply = {
                        val count = viewModel.applyDrafts()
                        showMessage(
                            if (count > 0) appliedMessage.format(count) else appliedNoneMessage
                        )
                    },
                )
            }
            composable(Tab.SETTINGS.route) {
                SettingsScreen(
                    state = state,
                    contentPadding = innerPadding,
                    onRefreshWidgets = {
                        val count = viewModel.refreshAllWidgets()
                        showMessage(refreshedMessage.format(count))
                    },
                )
            }
        }
    }
}
