package io.github.openflocon.flocondesktop.common.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowState
import dev.nucleusframework.application.LocalNucleusApplicationScope
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.application.NucleusDecoratedWindowScope
import dev.nucleusframework.application.NucleusWindow
import dev.nucleusframework.application.NucleusWindowHost
import dev.nucleusframework.window.material.MaterialDecoratedWindow
import dev.nucleusframework.window.material.MaterialTitleBar
import flocondesktop.composeapp.generated.resources.Res
import flocondesktop.composeapp.generated.resources.app_icon
import io.github.openflocon.library.designsystem.components.escape.LocalEscapeHandlerStack
import org.jetbrains.compose.resources.painterResource

actual fun floconNucleusWindowHost(): NucleusWindowHost = FloconNucleusWindowHost

/**
 * Material chrome for secondary windows opened via HostedWindow /
 * LocalNucleusWindowHost (Nucleus 2.3.2).
 *
 * Same chrome as [FloconWindow]: MaterialDecoratedWindow + MaterialTitleBar + escape stack.
 */
private object FloconNucleusWindowHost : NucleusWindowHost {
    @Composable
    override fun Window(
        onCloseRequest: () -> Unit,
        state: WindowState,
        visible: Boolean,
        title: String,
        icon: Painter?,
        resizable: Boolean,
        enabled: Boolean,
        focusable: Boolean,
        alwaysOnTop: Boolean,
        undecorated: Boolean,
        popupFor: NucleusWindow?,
        nativePopupLayers: Boolean,
        hiddenFromDock: Boolean,
        minimumSize: DpSize?,
        onPreviewKeyEvent: (KeyEvent) -> Boolean,
        onKeyEvent: (KeyEvent) -> Boolean,
        content: @Composable NucleusDecoratedWindowScope.() -> Unit,
    ) {
        val handlers = remember { mutableStateListOf<() -> Boolean>() }
        val defaultIcon = painterResource(Res.drawable.app_icon)
        val applicationScope: NucleusApplicationScope = LocalNucleusApplicationScope.current

        // Explicit type so the NucleusApplicationScope overload is chosen (not AWT ApplicationScope).
        applicationScope.MaterialDecoratedWindow(
            onCloseRequest = onCloseRequest,
            state = state,
            visible = visible,
            title = title,
            icon = icon ?: defaultIcon,
            resizable = resizable,
            enabled = enabled,
            focusable = focusable,
            alwaysOnTop = alwaysOnTop,
            nativePopupLayers = nativePopupLayers,
            hiddenFromDock = hiddenFromDock,
            minimumSize = minimumSize,
            onPreviewKeyEvent = { event: KeyEvent ->
                when {
                    event.key == Key.Escape && event.type == KeyEventType.KeyDown ->
                        handlers.lastOrNull()?.invoke() ?: onPreviewKeyEvent(event)
                    else -> onPreviewKeyEvent(event)
                }
            },
            onKeyEvent = onKeyEvent,
        ) {
            MaterialTitleBar()
            CompositionLocalProvider(LocalEscapeHandlerStack provides handlers) {
                content()
            }
        }
    }
}
