package org.privatespice.htmlrenderer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.privatespice.htmlrenderer.extension.normalizeWhitespace
import org.privatespice.htmlrenderer.model.HtmlRenderContext
import org.privatespice.htmlrenderer.model.HtmlRenderers
import org.privatespice.htmlrenderer.model.HtmlSpacing
import org.privatespice.htmlrenderer.model.HtmlStyleDefaults
import org.privatespice.htmlrenderer.model.HtmlTypography
import org.privatespice.htmlrenderer.node.DefaultSupportedTags
import org.privatespice.htmlrenderer.node.HtmlBlockNode
import org.privatespice.htmlrenderer.node.HtmlBlockQuoteNode
import org.privatespice.htmlrenderer.node.HtmlCodeNode
import org.privatespice.htmlrenderer.node.HtmlEmphasisNode
import org.privatespice.htmlrenderer.node.HtmlHeadingNode
import org.privatespice.htmlrenderer.node.HtmlInlineNode
import org.privatespice.htmlrenderer.node.HtmlLineBreakNode
import org.privatespice.htmlrenderer.node.HtmlLinkNode
import org.privatespice.htmlrenderer.node.HtmlListItemNode
import org.privatespice.htmlrenderer.node.HtmlListNode
import org.privatespice.htmlrenderer.node.HtmlNode
import org.privatespice.htmlrenderer.node.HtmlParagraphNode
import org.privatespice.htmlrenderer.node.HtmlSpanNode
import org.privatespice.htmlrenderer.node.HtmlStrikeThroughNode
import org.privatespice.htmlrenderer.node.HtmlStrongNode
import org.privatespice.htmlrenderer.node.HtmlSubscriptNode
import org.privatespice.htmlrenderer.node.HtmlSuperscriptNode
import org.privatespice.htmlrenderer.node.HtmlTag
import org.privatespice.htmlrenderer.node.HtmlTextNode
import org.privatespice.htmlrenderer.node.HtmlUnderlineNode
import org.privatespice.htmlrenderer.parser.KsoupHtmlParser

@Composable
fun HtmlTextRenderer(
    html: String,
    modifier: Modifier = Modifier,
    renderers: HtmlRenderers? = null,
    typography: HtmlTypography = HtmlStyleDefaults.typography(),
    spacing: HtmlSpacing = HtmlStyleDefaults.spacing(),
    onLinkClicked: ((String) -> Unit)? = null,
    linkStyles: TextLinkStyles? = null,
    supportedTags: Set<HtmlTag> = DefaultSupportedTags,
) {
    val parser = remember { KsoupHtmlParser() }
    val document = remember(html, supportedTags) {
        parser.parse(html, supportedTags).normalizeWhitespace()
    }

    val resolvedRenderers = remember(renderers, typography, spacing) {
        renderers ?: HtmlRenderers.defaults(
            typography = typography,
            spacing = spacing,
        )
    }

    lateinit var context: HtmlRenderContext

    context = HtmlRenderContext(
        renderChildren = { nodes ->
            nodes.forEach { node ->
                RenderBlockNode(
                    node = node,
                    renderers = resolvedRenderers,
                    context = context,
                )
            }
        },
        buildInlineText = { nodes ->
            buildInlineText(nodes, linkStyles, resolvedRenderers)
        },
        onLinkClicked = onLinkClicked,
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.paragraph),
    ) {
        context.renderChildren(document.children)
    }
}

@Composable
internal fun HtmlAnnotatedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onLinkClicked: ((String) -> Unit)? = null,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = text,
        style = style,
        onTextLayout = { layoutResult = it },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(text, onLinkClicked) {
                detectTapGestures { offset: Offset ->
                    layoutResult?.let { layout ->
                        val position = layout.getOffsetForPosition(offset)
                        text.getStringAnnotations(tag = "URL", start = position, end = position)
                            .firstOrNull()?.let { annotation ->
                                onLinkClicked?.invoke(annotation.item)
                            }
                    }
                }
            },
    )
}

@Composable
private fun RenderBlockNode(
    node: HtmlBlockNode,
    renderers: HtmlRenderers,
    context: HtmlRenderContext,
) {
    when (node) {
        is HtmlHeadingNode -> renderers.headingRenderer(node, context)
        is HtmlParagraphNode -> renderers.paragraphRenderer(node, context)
        is HtmlBlockQuoteNode -> renderers.blockQuoteRenderer(node, context)
        is HtmlListNode -> renderers.listRenderer(node, context)
        is HtmlListItemNode -> {
            val (inlineNodes, blockNodes) = splitInlineAndBlock(node.children)
            if (inlineNodes.isNotEmpty()) {
                HtmlAnnotatedText(
                    text = context.buildInlineText(inlineNodes),
                    onLinkClicked = context.onLinkClicked,
                )
            }
            if (blockNodes.isNotEmpty()) {
                context.renderChildren(blockNodes)
            }
        }
    }
}

@Composable
internal fun RenderList(
    node: HtmlListNode,
    context: HtmlRenderContext,
    typography: HtmlTypography,
    listItemSpacing: Dp,
) {
    val items = node.items

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val maxNumberWidth = remember(items.size, typography.listItem, density) {
        with(density) {
            (1..items.size)
                .maxOfOrNull { number ->
                    textMeasurer.measure(
                        text = number.toString(),
                        style = typography.listItem,
                    ).size.width
                }
                ?.toDp()
                ?: 0.dp
        }
    }

    val dotWidth = remember(typography.listItem, density) {
        with(density) {
            textMeasurer.measure(text = ".", style = typography.listItem).size.width.toDp()
        }
    }

    fun measureTextWidth(text: String): Dp = with(density) {
        textMeasurer.measure(
            text = text,
            style = typography.listItem,
        ).size.width.toDp()
    }

    Column(verticalArrangement = Arrangement.spacedBy(listItemSpacing)) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.Top) {
                if (node.ordered) {
                    Row(
                        modifier = Modifier.width(maxNumberWidth + dotWidth),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier.width(maxNumberWidth),
                            contentAlignment = Alignment.TopEnd,
                        ) {
                            val number = (index + 1).toString()
                            val numberWidth = measureTextWidth(text = number)
                            val opticalEndPadding = (maxNumberWidth - numberWidth) / 4
                            Text(
                                text = number,
                                style = typography.listItem,
                                modifier = Modifier.padding(end = opticalEndPadding),
                            )
                        }

                        Box(
                            modifier = Modifier.width(dotWidth),
                            contentAlignment = Alignment.TopStart,
                        ) {
                            Text(
                                text = ".",
                                style = typography.listItem,
                            )
                        }
                    }
                } else {
                    Text(
                        text = "•",
                        style = typography.listItem,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    val (inlineNodes, blockNodes) = splitInlineAndBlock(item.children)

                    if (inlineNodes.isNotEmpty()) {
                        HtmlAnnotatedText(
                            text = context.buildInlineText(inlineNodes),
                            onLinkClicked = context.onLinkClicked,
                            style = typography.listItem,
                        )
                    }

                    if (blockNodes.isNotEmpty()) {
                        context.renderChildren(blockNodes)
                    }
                }
            }
        }
    }
}

private fun buildInlineText(
    nodes: List<HtmlInlineNode>,
    linkStyles: TextLinkStyles?,
    renderers: HtmlRenderers,
): AnnotatedString = buildAnnotatedString {
    appendInlineNodes(
        builder = this,
        nodes = nodes,
        linkStyles = linkStyles,
        renderers = renderers,
    )
}

private fun appendInlineNodes(
    builder: AnnotatedString.Builder,
    nodes: List<HtmlInlineNode>,
    linkStyles: TextLinkStyles?,
    renderers: HtmlRenderers,
) {
    nodes.forEach { node ->
        when (node) {
            is HtmlTextNode -> builder.append(node.text)

            HtmlLineBreakNode -> builder.append("\n")

            is HtmlStrongNode -> {
                builder.pushStyle(renderers.strongStyle)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
            }

            is HtmlEmphasisNode -> {
                builder.pushStyle(renderers.emphasisStyle)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
            }

            is HtmlUnderlineNode -> {
                builder.pushStyle(renderers.underlineStyle)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
            }

            is HtmlStrikeThroughNode -> {
                builder.pushStyle(renderers.strikeThroughStyle)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
            }

            is HtmlSubscriptNode -> {
                builder.pushStyle(renderers.subscriptStyle)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
            }

            is HtmlSuperscriptNode -> {
                builder.pushStyle(renderers.superscriptStyle)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
            }

            is HtmlCodeNode -> {
                builder.pushStyle(renderers.codeStyle)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
            }

            is HtmlLinkNode -> {
                val href = node.href
                val start = builder.length
                val style = linkStyles?.style ?: SpanStyle(
                    textDecoration = TextDecoration.Underline,
                )
                builder.pushStyle(style)
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                builder.pop()
                val end = builder.length
                if (!href.isNullOrBlank() && end > start) {
                    builder.addStringAnnotation(
                        tag = "URL",
                        annotation = href,
                        start = start,
                        end = end,
                    )
                }
            }

            is HtmlSpanNode -> {
                val decoration = when {
                    node.underline && node.lineThrough -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough),
                    )
                    node.underline -> TextDecoration.Underline
                    node.lineThrough -> TextDecoration.LineThrough
                    else -> null
                }
                if (decoration != null) {
                    builder.pushStyle(SpanStyle(textDecoration = decoration))
                }
                appendInlineNodes(builder, node.children, linkStyles, renderers)
                if (decoration != null) {
                    builder.pop()
                }
            }
        }
    }
}

private fun splitInlineAndBlock(children: List<HtmlNode>): Pair<List<HtmlInlineNode>, List<HtmlBlockNode>> {
    val inline = mutableListOf<HtmlInlineNode>()
    val block = mutableListOf<HtmlBlockNode>()

    children.forEach { child ->
        when (child) {
            is HtmlInlineNode -> inline.add(child)
            is HtmlBlockNode -> block.add(child)
        }
    }

    return inline to block
}

