package org.privatespice.htmlrenderer

import kotlin.test.Test
import kotlin.test.assertEquals
import org.privatespice.htmlrenderer.node.HtmlCodeNode
import org.privatespice.htmlrenderer.node.HtmlEmphasisNode
import org.privatespice.htmlrenderer.node.HtmlInlineNode
import org.privatespice.htmlrenderer.node.HtmlLineBreakNode
import org.privatespice.htmlrenderer.node.HtmlLinkNode
import org.privatespice.htmlrenderer.node.HtmlListNode
import org.privatespice.htmlrenderer.node.HtmlParagraphNode
import org.privatespice.htmlrenderer.node.HtmlSpanNode
import org.privatespice.htmlrenderer.node.HtmlStrikeThroughNode
import org.privatespice.htmlrenderer.node.HtmlStrongNode
import org.privatespice.htmlrenderer.node.HtmlSubscriptNode
import org.privatespice.htmlrenderer.node.HtmlSuperscriptNode
import org.privatespice.htmlrenderer.node.HtmlTextNode
import org.privatespice.htmlrenderer.node.HtmlUnderlineNode
import org.privatespice.htmlrenderer.parser.KsoupHtmlParser
import org.privatespice.htmlrenderer.parser.normalizeWhitespace

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

    @Test
    fun keepsSpacingAcrossInlineBoundaryWhenNbspIsUsed() {
        val html = "<p><strong>We have a bold text&nbsp;</strong>and a <em>emphasis</em></p>"

        val document = parser.parse(html).normalizeWhitespace()
        val paragraph = document.children.first() as HtmlParagraphNode

        assertEquals("We have a bold text and a emphasis", toPlainText(paragraph.children))
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
