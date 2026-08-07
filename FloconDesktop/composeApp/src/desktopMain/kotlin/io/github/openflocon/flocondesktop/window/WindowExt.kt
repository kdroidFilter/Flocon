package io.github.openflocon.flocondesktop.window

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import dev.nucleusframework.window.tao.TaoScreenGeometry
import kotlin.math.min

fun WindowStateData?.windowPosition() = this?.let { WindowPosition(x.dp, y.dp) } ?: WindowPosition.PlatformDefault

fun WindowStateData?.size(): DpSize {
    val maxWidth = primaryScreenLogicalWidth() ?: Int.MAX_VALUE
    val maxHeight = primaryScreenLogicalHeight() ?: Int.MAX_VALUE

    val width = this?.width?.dp ?: min(DEFAULT_WINDOW_WIDTH, maxWidth).dp
    val height = this?.height?.dp ?: min(DEFAULT_WINDOW_HEIGHT, maxHeight).dp

    return DpSize(
        width = width,
        height = height,
    )
}

/**
 * Primary monitor work-area size in logical pixels via Tao (no AWT Toolkit).
 */
private fun primaryScreenLogicalWidth(): Int? {
    val workArea = TaoScreenGeometry.primaryMonitorWorkAreaPx() ?: return null
    val scale = TaoScreenGeometry.primaryMonitorScaleFactor().coerceAtLeast(0.01f)
    // workArea: [x, y, width, height] in physical pixels
    return (workArea[2] / scale).toInt().coerceAtLeast(1)
}

private fun primaryScreenLogicalHeight(): Int? {
    val workArea = TaoScreenGeometry.primaryMonitorWorkAreaPx() ?: return null
    val scale = TaoScreenGeometry.primaryMonitorScaleFactor().coerceAtLeast(0.01f)
    return (workArea[3] / scale).toInt().coerceAtLeast(1)
}
