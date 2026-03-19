package org.privatespice.htmlrenderer.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlWhitespaceNormalizerTest {

    private val parser = KsoupHtmlParser()

    @Test
    fun collapsesWhitespaceAndTrimsBlockEdges() {
        val document = parser.parse("<p>\n   Hello   world   </p>").normalizeWhitespace()
        val paragraph = document.children.first() as HtmlParagraphNode

        assertEquals("Hello world", toPlainText(paragraph.children))
    }

    @Test
    fun keepsInlineWordSeparationWithoutDoubleSpaces() {
        val document = parser.parse("<p>Lorem <strong>ipsum</strong> dolor</p>").normalizeWhitespace()
        val paragraph = document.children.first() as HtmlParagraphNode

        assertEquals("Lorem ipsum dolor", toPlainText(paragraph.children))
    }

    @Test
    fun collapsesDoubleSpacesInsideTextNodes() {
        val document = parser.parse("<p>Hello  world</p>").normalizeWhitespace()
        val paragraph = document.children.first() as HtmlParagraphNode

        assertEquals("Hello world", toPlainText(paragraph.children))
    }

    @Test
    fun keepsBrAsExplicitLineBreak() {
        val document = parser.parse("<p>Hello<br>   world</p>").normalizeWhitespace()
        val paragraph = document.children.first() as HtmlParagraphNode

        assertEquals("Hello\nworld", toPlainText(paragraph.children))
    }

    @Test
    fun trimsWhitespaceAtListItemBoundaries() {
        val document = parser.parse("<ul><li>  Hello <em> world </em>  </li></ul>").normalizeWhitespace()
        val list = document.children.first() as HtmlListNode
        val itemInline = list.items.first().children.filterIsInstance<HtmlInlineNode>()

        assertEquals("Hello world", toPlainText(itemInline))
    }

    private fun toPlainText(nodes: List<HtmlInlineNode>): String = buildString {
        nodes.forEach { node -> appendInline(node) }
    }

    private fun StringBuilder.appendInline(node: HtmlInlineNode) {
        when (node) {
            is HtmlTextNode -> append(node.text)
            HtmlLineBreakNode -> append("\n")
            is HtmlStrongNode -> node.children.forEach { appendInline(it) }
            is HtmlEmphasisNode -> node.children.forEach { appendInline(it) }
            is HtmlUnderlineNode -> node.children.forEach { appendInline(it) }
            is HtmlStrikeThroughNode -> node.children.forEach { appendInline(it) }
            is HtmlSubscriptNode -> node.children.forEach { appendInline(it) }
            is HtmlSuperscriptNode -> node.children.forEach { appendInline(it) }
            is HtmlCodeNode -> node.children.forEach { appendInline(it) }
            is HtmlLinkNode -> node.children.forEach { appendInline(it) }
            is HtmlSpanNode -> node.children.forEach { appendInline(it) }
        }
    }
}
