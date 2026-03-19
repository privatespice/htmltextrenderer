package org.privatespice.htmlrenderer.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.privatespice.htmlrenderer.node.HtmlBlockTag
import org.privatespice.htmlrenderer.node.HtmlInlineTag
import org.privatespice.htmlrenderer.node.HtmlLineBreakNode
import org.privatespice.htmlrenderer.node.HtmlLinkNode
import org.privatespice.htmlrenderer.node.HtmlParagraphNode
import org.privatespice.htmlrenderer.node.HtmlSpanNode
import org.privatespice.htmlrenderer.node.HtmlStrongNode
import org.privatespice.htmlrenderer.node.HtmlTextAlign
import org.privatespice.htmlrenderer.node.HtmlTextNode

class KsoupHtmlParserTest {

    private val parser = KsoupHtmlParser()

    @Test
    fun parsesTextAlignFromStyleCaseInsensitively() {
        val doc = parser.parse("<p STYLE='TEXT-ALIGN: center;'>Hello</p>")
        val paragraph = doc.children.first() as HtmlParagraphNode

        assertEquals(HtmlTextAlign.Center, paragraph.textAlign)
    }

    @Test
    fun parsesSpanTextDecorationFlags() {
        val doc = parser.parse("<p><span style='text-decoration: underline line-through;'>x</span></p>")
        val paragraph = doc.children.first() as HtmlParagraphNode
        val span = paragraph.children.first() as HtmlSpanNode

        assertTrue(span.underline)
        assertTrue(span.lineThrough)
    }

    @Test
    fun keepsHrefOnlyForAnchorTag() {
        val doc = parser.parse("<p><a href='https://example.com' data-id='7'>go</a></p>")
        val paragraph = doc.children.first() as HtmlParagraphNode
        val link = paragraph.children.first() as HtmlLinkNode

        assertEquals("https://example.com", link.href)
    }

    @Test
    fun unwrapsUnsupportedTagsButPreservesTextFlow() {
        val doc = parser.parse("<p>Hello <mark>wide</mark> world</p>")
        val paragraph = doc.children.first() as HtmlParagraphNode

        val text = paragraph.children.filterIsInstance<HtmlTextNode>().joinToString("") { it.text }
        assertEquals("Hello wide world", text)
    }

    @Test
    fun keepsBrAsLineBreakNodeInsideParagraph() {
        val doc = parser.parse("<p>a<br>b</p>")
        val paragraph = doc.children.first() as HtmlParagraphNode

        assertIs<HtmlTextNode>(paragraph.children[0])
        assertIs<HtmlLineBreakNode>(paragraph.children[1])
        assertIs<HtmlTextNode>(paragraph.children[2])
    }

    @Test
    fun normalizesAdjacentRawTextIntoSingleTextNodePerRun() {
        val doc = parser.parse("<p>ab<span>cd</span>ef</p>")
        val paragraph = doc.children.first() as HtmlParagraphNode

        val texts = paragraph.children.filterIsInstance<HtmlTextNode>().map { it.text }
        assertEquals(listOf("ab", "ef"), texts)
    }

    @Test
    fun doesNotSetTextAlignWhenStyleIsMissing() {
        val doc = parser.parse("<p>Hello</p>")
        val paragraph = doc.children.first() as HtmlParagraphNode

        assertNull(paragraph.textAlign)
    }

    @Test
    fun filtersSupportedTagsWhenCustomSetProvided() {
        val doc = parser.parse(
            html = "<p><strong>x</strong> <u>y</u></p>",
            supportedTags = setOf(HtmlBlockTag.P, HtmlInlineTag.STRONG),
        )
        val paragraph = doc.children.first() as HtmlParagraphNode

        assertEquals(2, paragraph.children.size)
        assertIs<HtmlStrongNode>(paragraph.children[0])
        assertIs<HtmlTextNode>(paragraph.children[1])
        assertFalse((paragraph.children[1] as HtmlTextNode).text.isBlank())
    }
}
