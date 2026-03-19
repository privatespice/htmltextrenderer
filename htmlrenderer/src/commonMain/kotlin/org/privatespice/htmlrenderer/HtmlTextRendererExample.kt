package org.privatespice.htmlrenderer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.privatespice.htmlrenderer.model.HtmlRenderers
import org.privatespice.htmlrenderer.model.HtmlStyleDefaults

@Composable
fun HtmlTextRendererDefaultExample(html: String) {
    val typography = HtmlStyleDefaults.typography().copy(
        paragraph = MaterialTheme.typography.bodySmall,
    )
    val spacing = HtmlStyleDefaults.spacing().copy(
        paragraph = 12.dp,
        listItem = 8.dp,
    )

    HtmlTextRenderer(
        html = html,
        typography = typography,
        spacing = spacing,
    )
}

@Composable
fun HtmlTextRendererCustomExample(html: String) {
    val typography = HtmlStyleDefaults.typography()
    val spacing = HtmlStyleDefaults.spacing()
    val defaults = HtmlRenderers.defaults(
        typography = typography,
        spacing = spacing,
    )

    HtmlTextRenderer(
        html = html,
        typography = typography,
        spacing = spacing,
        renderers = defaults.copy(
            headingRenderer = { node, ctx ->
                if (node.level == 2) {
                    // Custom H2 with a different text style
                    HtmlAnnotatedText(
                        text = ctx.buildInlineText(node.children),
                        style = MaterialTheme.typography.titleMedium,
                        onLinkClicked = ctx.onLinkClicked,
                    )
                } else {
                    // All other heading levels fall back to the default renderer
                    defaults.headingRenderer(node, ctx)
                }
            },
            paragraphRenderer = { node, ctx ->
                // Custom paragraph renderer with a different spacing
                Text(
                    text = ctx.buildInlineText(node.children),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        ),
    )
}
