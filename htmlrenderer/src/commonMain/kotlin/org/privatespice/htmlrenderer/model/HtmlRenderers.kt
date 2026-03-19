package org.privatespice.htmlrenderer.model

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import org.privatespice.htmlrenderer.HtmlAnnotatedText
import org.privatespice.htmlrenderer.RenderList
import org.privatespice.htmlrenderer.node.HtmlBlockQuoteNode
import org.privatespice.htmlrenderer.node.HtmlHeadingNode
import org.privatespice.htmlrenderer.node.HtmlInlineNode
import org.privatespice.htmlrenderer.node.HtmlListNode
import org.privatespice.htmlrenderer.node.HtmlParagraphNode
import org.privatespice.htmlrenderer.node.HtmlTextAlign
import org.privatespice.htmlrenderer.style.HtmlSpacing
import org.privatespice.htmlrenderer.style.HtmlTypography

data class HtmlRenderers(
    // Block renderers
    val headingRenderer: @Composable (HtmlHeadingNode, HtmlRenderContext) -> Unit,
    val paragraphRenderer: @Composable (HtmlParagraphNode, HtmlRenderContext) -> Unit,
    val blockQuoteRenderer: @Composable (HtmlBlockQuoteNode, HtmlRenderContext) -> Unit,
    val listRenderer: @Composable (HtmlListNode, HtmlRenderContext) -> Unit,

    // Inline SpanStyles — used by the AnnotatedString builder inside HtmlTextRenderer
    val strongStyle: SpanStyle,
    val emphasisStyle: SpanStyle,
    val underlineStyle: SpanStyle,
    val strikeThroughStyle: SpanStyle,
    val subscriptStyle: SpanStyle,
    val superscriptStyle: SpanStyle,
    val codeStyle: SpanStyle,
) {
    companion object {
        fun defaults(
            typography: HtmlTypography,
            spacing: HtmlSpacing,
        ): HtmlRenderers {
            fun HtmlTextAlign.toComposeTextAlign(): TextAlign = when (this) {
                HtmlTextAlign.Left -> TextAlign.Left
                HtmlTextAlign.Right -> TextAlign.Right
                HtmlTextAlign.Center -> TextAlign.Center
                HtmlTextAlign.Justify -> TextAlign.Justify
            }

            // Shared text-block renderer used by headings, paragraphs and blockquotes
            val renderTextBlock: @Composable (List<HtmlInlineNode>, HtmlTextAlign?, HtmlRenderContext, TextStyle, Modifier) -> Unit =
                { children, textAlign, context, style, modifier ->
                    val resolvedStyle = if (textAlign != null) {
                        style.copy(textAlign = textAlign.toComposeTextAlign())
                    } else {
                        style
                    }
                    HtmlAnnotatedText(
                        modifier = modifier,
                        text = context.buildInlineText(children),
                        onLinkClicked = context.onLinkClicked,
                        style = resolvedStyle,
                    )
                }

            return HtmlRenderers(
                headingRenderer = { node, context ->
                    val style = when (node.level) {
                        1 -> typography.h1
                        2 -> typography.h2
                        3 -> typography.h3
                        4 -> typography.h4
                        else -> typography.paragraph
                    }
                    renderTextBlock(node.children, node.textAlign, context, style, Modifier)
                },

                paragraphRenderer = { node, context ->
                    renderTextBlock(node.children, node.textAlign, context, typography.paragraph, Modifier)
                },

                blockQuoteRenderer = { node, context ->
                    renderTextBlock(
                        node.children,
                        node.textAlign,
                        context,
                        typography.paragraph.copy(fontStyle = FontStyle.Italic),
                        Modifier.padding(start = 12.dp),
                    )
                },

                listRenderer = { node, context ->
                    RenderList(
                        node = node,
                        context = context,
                        typography = typography,
                        listItemSpacing = spacing.listItem,
                    )
                },

                strongStyle = SpanStyle(fontWeight = FontWeight.Bold),
                emphasisStyle = SpanStyle(fontStyle = FontStyle.Italic),
                underlineStyle = SpanStyle(textDecoration = TextDecoration.Underline),
                strikeThroughStyle = SpanStyle(textDecoration = TextDecoration.LineThrough),
                subscriptStyle = SpanStyle(
                    baselineShift = BaselineShift.Subscript,
                    fontSize = 0.8.em,
                ),
                superscriptStyle = SpanStyle(
                    baselineShift = BaselineShift.Superscript,
                    fontSize = 0.8.em,
                ),
                codeStyle = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color(0xFFEDEDED),
                    fontSize = 0.9.em,
                ),
            )
        }
    }
}
