package io.github.openflocon.flocondesktop.common.utils

import java.awt.Desktop
import java.net.URI

/**
 * Same approach as Nucleus jewel-demo (Tao backend):
 * `Desktop.getDesktop().browse(...)` — see TitleBarView / MarkdownPreview.
 */
actual fun openInBrowser(uri: URI) {
    Desktop.getDesktop().browse(uri)
}
