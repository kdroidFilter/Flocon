package io.github.openflocon.flocondesktop.features.dashboard.view.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nucleusframework.webview.web.WebView
import dev.nucleusframework.webview.web.rememberWebViewStateWithHTMLData
import io.github.openflocon.flocondesktop.features.dashboard.model.DashboardContainerViewState
import io.github.openflocon.library.designsystem.FloconTheme

/**
 * Renders dashboard HTML via Nucleus ComposeNativeWebView (Tao NativeView).
 * Replaces SwingPanel + JEditorPane, which is unsupported on the Tao backend.
 */
@Composable
internal fun DashboardHtmlView(
    modifier: Modifier = Modifier,
    rowItem: DashboardContainerViewState.RowItem.Html,
) {
    val webViewState = rememberWebViewStateWithHTMLData(
        data = rowItem.value,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (rowItem.label.isNotEmpty()) {
            Text(
                text = rowItem.label,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                color = FloconTheme.colorPalette.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Thin,
                ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(FloconTheme.colorPalette.secondary)
                .padding(8.dp),
        ) {
            WebView(
                state = webViewState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(584.dp),
            )
        }
    }
}
