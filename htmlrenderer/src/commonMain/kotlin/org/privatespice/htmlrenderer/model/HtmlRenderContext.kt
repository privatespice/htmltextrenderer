package org.privatespice.htmlrenderer.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import org.privatespice.htmlrenderer.parser.HtmlBlockNode
import org.privatespice.htmlrenderer.parser.HtmlInlineNode

data class HtmlRenderContext(
    val renderChildren: @Composable (List<HtmlBlockNode>) -> Unit,
    val buildInlineText: (List<HtmlInlineNode>) -> AnnotatedString,
    val onLinkClicked: ((String) -> Unit)?,
)
