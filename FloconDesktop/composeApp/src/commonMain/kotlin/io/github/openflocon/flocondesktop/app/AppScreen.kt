package io.github.openflocon.flocondesktop.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.SinglePaneSceneStrategy
import dev.nucleusframework.application.LocalNucleusWindowHost
import io.github.openflocon.flocondesktop.app.ui.settings.settingsRoutes
import io.github.openflocon.flocondesktop.app.ui.view.leftpannel.LeftPanelView
import io.github.openflocon.flocondesktop.app.version.VersionCheckerView
import io.github.openflocon.flocondesktop.common.ui.feedback.FeedbackDisplayerView
import io.github.openflocon.flocondesktop.common.ui.window.floconNucleusWindowHost
import io.github.openflocon.flocondesktop.features.adbcommander.adbCommanderRoutes
import io.github.openflocon.flocondesktop.features.analytics.analyticsRoutes
import io.github.openflocon.flocondesktop.features.crashreporter.crashReporterRoutes
import io.github.openflocon.flocondesktop.features.dashboard.dashboardRoutes
import io.github.openflocon.flocondesktop.features.database.databaseRoutes
import io.github.openflocon.flocondesktop.features.deeplinks.deeplinkRoutes
import io.github.openflocon.flocondesktop.features.files.filesRoutes
import io.github.openflocon.flocondesktop.features.images.imageRoutes
import io.github.openflocon.flocondesktop.features.network.networkRoutes
import io.github.openflocon.flocondesktop.features.sharedpreferences.sharedPreferencesRoutes
import io.github.openflocon.flocondesktop.features.table.tableRoutes
import io.github.openflocon.library.designsystem.FloconTheme
import io.github.openflocon.navigation.FloconNavigation
import io.github.openflocon.navigation.MainFloconNavigationState
import io.github.openflocon.navigation.scene.BigDialogSceneStrategy
import io.github.openflocon.navigation.scene.DialogSceneStrategy
import io.github.openflocon.navigation.scene.PanelSceneStrategy
import io.github.openflocon.navigation.scene.WindowSceneStrategy

@Composable
internal fun AppScreen(
    uiState: AppUiState,
    navigationState: MainFloconNavigationState,
    onAction: (AppAction) -> Unit,
) {
    // Nucleus 2.3.2: override default DecoratedWindow host with Flocon Material chrome
    // (title bar + escape stack). WindowScene uses HostedWindow → this host.
    CompositionLocalProvider(LocalNucleusWindowHost provides floconNucleusWindowHost()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Content(
                uiState = uiState,
                navigationState = navigationState,
                onAction = onAction,
            )
            FeedbackDisplayerView()
            VersionCheckerView()
        }
    }
}

@Composable
private fun Content(
    uiState: AppUiState,
    navigationState: MainFloconNavigationState,
    onAction: (AppAction) -> Unit,
) {
    val sceneStrategies = remember {
        listOf(
            PanelSceneStrategy(),
            WindowSceneStrategy(),
            DialogSceneStrategy(),
            BigDialogSceneStrategy(),
            SinglePaneSceneStrategy(),
        )
    }

    FloconNavigation(
        navigationState = navigationState,
        sceneStrategies = sceneStrategies,
        sceneDecoratorStrategies = listOf(
            MenuSceneStrategy(
                menuContent = {
                    LeftPanelView(
                        current = uiState.contentState.current,
                        state = uiState.menuState,
                        expanded = it,
                        onClickItem = { menu -> onAction(AppAction.SelectMenu(menu.screen)) },
                    )
                },
            )
        ),
        modifier = Modifier
            .fillMaxSize()
            .background(FloconTheme.colorPalette.surface),
    ) {
        analyticsRoutes()
        dashboardRoutes()
        databaseRoutes()
        deeplinkRoutes()
        adbCommanderRoutes()
        filesRoutes()
        imageRoutes()
        networkRoutes()
        sharedPreferencesRoutes()
        tableRoutes()
        settingsRoutes()
        crashReporterRoutes()
    }
}
