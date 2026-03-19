package org.privatespice.htmlrenderer.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import org.privatespice.htmlrenderer.node.DefaultSupportedTags
import org.privatespice.htmlrenderer.node.HtmlDocument
import org.privatespice.htmlrenderer.node.HtmlInlineTag
import org.privatespice.htmlrenderer.node.HtmlNodeMapper
import org.privatespice.htmlrenderer.node.HtmlRawElementNode
import org.privatespice.htmlrenderer.node.HtmlRawLineBreakNode
import org.privatespice.htmlrenderer.node.HtmlRawNode
import org.privatespice.htmlrenderer.node.HtmlRawTextNode
import org.privatespice.htmlrenderer.node.HtmlTag
import org.privatespice.htmlrenderer.node.htmlTagFromName
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser as KsoupEngineParser

internal class KsoupHtmlParser {

    fun parse(
        html: String,
        supportedTags: Set<HtmlTag> = DefaultSupportedTags,
    ): HtmlDocument {
        val stack = ArrayDeque<NodeFrame>()
        stack.add(NodeFrame(null, emptyMap()))

        val handler = KsoupHtmlHandler
            .Builder()
            .onOpenTag { name, attributes, _ ->
                val tag = htmlTagFromName(name)
                if (tag == HtmlInlineTag.BR && supportedTags.contains(tag)) {
                    if (stack.isNotEmpty()) {
                        stack.last().children.add(HtmlRawLineBreakNode)
                    }
                    return@onOpenTag
                }
                if (tag != null && supportedTags.contains(tag)) {
                    stack.addLast(NodeFrame(tag, filterAttrs(tag, attributes)))
                } else {
                    stack.addLast(NodeFrame(null, emptyMap()))
                }
            }
            .onCloseTag { name, _ ->
                if (name.equals(HtmlInlineTag.BR.tagName, ignoreCase = true)) return@onCloseTag
                if (stack.isEmpty()) return@onCloseTag

                val frame = stack.removeLast()
                if (stack.isEmpty()) return@onCloseTag

                val parent = stack.last()
                val tag = frame.tag

                if (tag != null && supportedTags.contains(tag)) {
                    parent.children.add(
                        HtmlRawElementNode(
                            tag = tag,
                            attrs = frame.attrs,
                            children = frame.children.toList(),
                        ),
                    )
                } else {
                    parent.children.addAll(frame.children)
                }
            }
            .onText { text ->
                if (text.isNotEmpty()) {
                    if (stack.isNotEmpty()) {
                        stack.last().children.add(HtmlRawTextNode(text))
                    }
                }
            }
            .build()

        val parser = KsoupEngineParser(handler)
        parser.write(html)
        parser.end()

        val root = if (stack.isEmpty()) emptyList() else stack.first().children.toList()
        return HtmlNodeMapper.mapDocument(normalizeNodes(root))
    }

    private fun filterAttrs(tag: HtmlTag, attrs: Map<String, String>): Map<String, String> {
        val dataAttrs = attrs.filterKeys { it.startsWith("data-", ignoreCase = true) }

        return when (tag) {
            HtmlInlineTag.SPAN -> {
                val style = attrs.getIgnoreCase("style").orEmpty()
                val declarations = parseStyleDeclarations(style)
                val textDecoration = declarations["text-decoration"].orEmpty()
                val underline = "underline" in textDecoration
                val lineThrough = "line-through" in textDecoration || "line through" in textDecoration

                buildMap {
                    putAll(dataAttrs)
                    if (underline) put("data-underline", "true")
                    if (lineThrough) put("data-line-through", "true")
                }
            }

            HtmlInlineTag.A -> {
                buildMap {
                    putAll(dataAttrs)
                    attrs.getIgnoreCase("href")?.let { put("href", it) }
                }
            }

            else -> {
                val style = attrs.getIgnoreCase("style").orEmpty()
                val align = parseTextAlign(style)

                if (align != null) {
                    dataAttrs + mapOf("data-text-align" to align)
                } else {
                    dataAttrs
                }
            }
        }
    }

    private fun parseTextAlign(style: String): String? {
        if (!style.contains("text-align", ignoreCase = true)) return null
        return textAlignRegex.find(style)?.groupValues?.get(1)?.lowercase()
    }

    private fun parseStyleDeclarations(style: String): Map<String, String> {
        return style
            .split(";")
            .mapNotNull { declaration ->
                val parts = declaration.split(":", limit = 2)
                if (parts.size == 2) {
                    parts[0].trim().lowercase() to parts[1].trim().lowercase()
                } else {
                    null
                }
            }
            .toMap()
    }

    private fun Map<String, String>.getIgnoreCase(key: String): String? =
        entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value

    private fun normalizeNodes(nodes: List<HtmlRawNode>): List<HtmlRawNode> {
        if (nodes.isEmpty()) return nodes

        val normalized = nodes.map { node ->
            when (node) {
                is HtmlRawElementNode -> node.copy(children = normalizeNodes(node.children))
                is HtmlRawTextNode -> node
                HtmlRawLineBreakNode -> HtmlRawLineBreakNode
            }
        }

        return mergeAdjacentTextNodes(normalized)
    }

    private fun mergeAdjacentTextNodes(nodes: List<HtmlRawNode>): List<HtmlRawNode> {
        if (nodes.isEmpty()) return nodes

        val result = mutableListOf<HtmlRawNode>()
        val buffer = StringBuilder()

        fun flushBuffer() {
            if (buffer.isNotEmpty()) {
                result.add(HtmlRawTextNode(buffer.toString()))
                buffer.clear()
            }
        }

        for (node in nodes) {
            when (node) {
                is HtmlRawTextNode -> buffer.append(node.text)
                else -> {
                    flushBuffer()
                    result.add(node)
                }
            }
        }

        flushBuffer()
        return result
    }

    private val textAlignRegex = Regex(
        "text-align\\s*:\\s*(left|right|center|justify)",
        RegexOption.IGNORE_CASE,
    )

    private data class NodeFrame(
        val tag: HtmlTag?,
        val attrs: Map<String, String>,
        val children: MutableList<HtmlRawNode> = mutableListOf(),
    )
}
