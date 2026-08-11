package io.github.openflocon.flocondesktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.menu.macos.NativeMenuBar
import dev.nucleusframework.window.DecoratedWindowScope
import dev.nucleusframework.window.TitleBarLayoutPolicy
import dev.nucleusframework.window.macOSLargeCornerRadius
import dev.nucleusframework.window.material.MaterialDecoratedWindow
import dev.nucleusframework.window.material.MaterialTitleBar
import dev.nucleusframework.window.material.rememberMaterialTitleBarStyle
import dev.nucleusframework.window.newFullscreenControls
import flocondesktop.composeapp.generated.resources.Res
import flocondesktop.composeapp.generated.resources.app_icon_small
import io.github.openflocon.domain.feedback.FeedbackDisplayer
import io.github.openflocon.domain.feedback.FeedbackDisplayerHandler
import io.github.openflocon.flocondesktop.about.AboutScreen
import io.github.openflocon.flocondesktop.app.AppAction
import io.github.openflocon.flocondesktop.app.AppScreen
import io.github.openflocon.flocondesktop.app.AppViewModel
import io.github.openflocon.flocondesktop.app.ui.view.topbar.MainScreenTopBar
import io.github.openflocon.flocondesktop.window.MIN_WINDOW_HEIGHT
import io.github.openflocon.flocondesktop.window.MIN_WINDOW_WIDTH
import io.github.openflocon.flocondesktop.window.WindowStateData
import io.github.openflocon.flocondesktop.window.WindowStateSaver
import io.github.openflocon.flocondesktop.window.size
import io.github.openflocon.flocondesktop.window.windowPosition
import io.github.openflocon.library.designsystem.components.escape.LocalEscapeHandlerStack
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val ACTIVATE_TRAY_NOTIFICATION = false

private val TITLE_BAR_HEIGHT = 44.dp

fun main() {
    // to force in FR :
    // Locale.setDefault(Locale.FRENCH)

    FileKit.init(appId = "io.github.openflocon.flocondesktop")

    return nucleusApplication {
        var openAbout by remember { mutableStateOf(false) }
        val savedState = remember { WindowStateSaver.load() }
        val windowState = rememberWindowState(
            size = savedState.size(),
            position = savedState.windowPosition(),
        )

        setSingletonImageLoaderFactory { context ->
            ImageLoader
                .Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                }.build()
        }

        val handlers = remember { mutableStateListOf<() -> Boolean>() }

        // Native macOS app menu (no-op on other platforms). Replaces AWT Desktop.APP_ABOUT.
        NativeMenuBar {
            Menu("Flocon") {
                Item("About Flocon") { openAbout = true }
                Separator()
            }
        }

        FloconApp {
            val materialTitleBarStyle = rememberMaterialTitleBarStyle(MaterialTheme.colorScheme)
            val titleBarStyle = remember(materialTitleBarStyle) {
                materialTitleBarStyle.copy(metrics = materialTitleBarStyle.metrics.copy(height = TITLE_BAR_HEIGHT))
            }

            MaterialDecoratedWindow(
                state = windowState,
                onCloseRequest = {
                    val currentSize = windowState.size
                    val currentPosition = windowState.position
                    WindowStateSaver.save(
                        WindowStateData(
                            width = currentSize.width.value.toInt(),
                            height = currentSize.height.value.toInt(),
                            x = currentPosition.x.value.toInt(),
                            y = currentPosition.y.value.toInt(),
                        ),
                    )

                    exitApplication()
                },
                onPreviewKeyEvent = {
                    when (it.key) {
                        Key.Escape if it.type == KeyEventType.KeyDown -> handlers.lastOrNull()?.invoke() ?: false

                        else -> false
                    }
                },
                title = "Flocon",
                icon = painterResource(Res.drawable.app_icon_small),
                minimumSize = DpSize(MIN_WINDOW_WIDTH.dp, MIN_WINDOW_HEIGHT.dp),
                titleBarStyle = titleBarStyle,
            ) {
                CompositionLocalProvider(LocalEscapeHandlerStack provides handlers) {
                    MainWindowContent()
                    if (ACTIVATE_TRAY_NOTIFICATION) {
                        FloconTray()
                    }

                    if (openAbout) {
                        AboutScreen(
                            onCloseRequest = { openAbout = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DecoratedWindowScope.MainWindowContent() {
    val viewModel = koinViewModel<AppViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTitleBar(layoutPolicy = TitleBarLayoutPolicy.FillCenter, modifier = Modifier.newFullscreenControls().macOSLargeCornerRadius()) {
        MainScreenTopBar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            devicesState = uiState.deviceState,
            appsState = uiState.appState,
            recordState = uiState.recordState,
            deleteApp = { viewModel.onAction(AppAction.DeleteApp(it)) },
            deleteDevice = { viewModel.onAction(AppAction.DeleteDevice(it)) },
            onDeviceSelected = { viewModel.onAction(AppAction.SelectDevice(it)) },
            onAppSelected = { viewModel.onAction(AppAction.SelectApp(it)) },
            onRecordClicked = { viewModel.onAction(AppAction.Record) },
            onRestartClicked = { viewModel.onAction(AppAction.Restart) },
            onTakeScreenshotClicked = { viewModel.onAction(AppAction.Screenshoot) },
        )
    }

    AppScreen(
        uiState = uiState,
        navigationState = viewModel.navigationState,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun ApplicationScope.FloconTray() {
    val trayState = rememberTrayState()
    val feedbackDisplayerHandler = koinInject<FeedbackDisplayerHandler>()

    LaunchedEffect(Unit) {
        feedbackDisplayerHandler.notificationsToDisplay
            .collect { notification ->
                trayState.sendNotification(
                    Notification(
                        title = notification.title,
                        message = notification.message,
                        type = when (notification.type) {
                            FeedbackDisplayer.NotificationType.None -> Notification.Type.None
                            FeedbackDisplayer.NotificationType.Info -> Notification.Type.Info
                            FeedbackDisplayer.NotificationType.Warning -> Notification.Type.Warning
                            FeedbackDisplayer.NotificationType.Error -> Notification.Type.Error
                        }
                    )
                )
            }
    }

    Tray(
        state = trayState,
        icon = painterResource(Res.drawable.app_icon_small)
    )
}
