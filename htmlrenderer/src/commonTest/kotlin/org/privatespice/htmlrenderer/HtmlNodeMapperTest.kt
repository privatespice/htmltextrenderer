package org.privatespice.htmlrenderer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.privatespice.htmlrenderer.node.HtmlBlockTag
import org.privatespice.htmlrenderer.node.HtmlHeadingNode
import org.privatespice.htmlrenderer.node.HtmlInlineTag
import org.privatespice.htmlrenderer.node.HtmlListNode
import org.privatespice.htmlrenderer.node.HtmlNodeMapper
import org.privatespice.htmlrenderer.node.HtmlParagraphNode
import org.privatespice.htmlrenderer.node.HtmlRawElementNode
import org.privatespice.htmlrenderer.node.HtmlRawTextNode
import org.privatespice.htmlrenderer.node.HtmlStrongNode
import org.privatespice.htmlrenderer.node.HtmlTextAlign
import org.privatespice.htmlrenderer.node.HtmlTextNode

class HtmlNodeMapperTest {

    @Test
    fun mapsHeadingWithTextAlignAndInlineChildren() {
        val raw = listOf(
            HtmlRawElementNode(
                tag = HtmlBlockTag.H2,
                attrs = mapOf("data-text-align" to "right"),
                children = listOf(
                    HtmlRawTextNode("Hello "),
                    HtmlRawElementNode(
                        tag = HtmlInlineTag.STRONG,
                        attrs = emptyMap(),
                        children = listOf(HtmlRawTextNode("world")),
                    ),
                ),
            ),
        )

        val doc = HtmlNodeMapper.mapDocument(raw)
        val heading = doc.children.first() as HtmlHeadingNode

        assertEquals(2, heading.level)
        assertEquals(HtmlTextAlign.Right, heading.textAlign)
        assertIs<HtmlTextNode>(heading.children[0])
        assertIs<HtmlStrongNode>(heading.children[1])
    }

    @Test
    fun preservesNestedListsInsideListItemsAsBlockNodes() {
        val raw = listOf(
            HtmlRawElementNode(
                tag = HtmlBlockTag.UL,
                attrs = emptyMap(),
                children = listOf(
                    HtmlRawElementNode(
                        tag = HtmlBlockTag.LI,
                        attrs = emptyMap(),
                        children = listOf(
                            HtmlRawTextNode("Top"),
                            HtmlRawElementNode(
                                tag = HtmlBlockTag.UL,
                                attrs = emptyMap(),
                                children = listOf(
                                    HtmlRawElementNode(
                                        tag = HtmlBlockTag.LI,
                                        attrs = emptyMap(),
                                        children = listOf(HtmlRawTextNode("Nested")),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val doc = HtmlNodeMapper.mapDocument(raw)
        val outerList = doc.children.first() as HtmlListNode
        val item = outerList.items.first()

        assertIs<HtmlTextNode>(item.children[0])
        val nestedList = item.children[1] as HtmlListNode
        assertEquals(false, nestedList.ordered)
        assertEquals("Nested", ((nestedList.items.first().children.first()) as HtmlTextNode).text)
    }

    @Test
    fun ignoresInlineTagsAtBlockLevel() {
        val raw = listOf(
            HtmlRawElementNode(
                tag = HtmlInlineTag.STRONG,
                attrs = emptyMap(),
                children = listOf(HtmlRawTextNode("x")),
            ),
            HtmlRawElementNode(
                tag = HtmlBlockTag.P,
                attrs = emptyMap(),
                children = listOf(HtmlRawTextNode("y")),
            ),
        )

        val doc = HtmlNodeMapper.mapDocument(raw)

        assertEquals(1, doc.children.size)
        val paragraph = doc.children.first() as HtmlParagraphNode
        assertEquals("y", (paragraph.children.first() as HtmlTextNode).text)
    }

    @Test
    fun mapsTextAlignValuesToEnum() {
        val raw = listOf(
            HtmlRawElementNode(HtmlBlockTag.P, mapOf("data-text-align" to "left"), emptyList()),
            HtmlRawElementNode(HtmlBlockTag.P, mapOf("data-text-align" to "center"), emptyList()),
            HtmlRawElementNode(HtmlBlockTag.P, mapOf("data-text-align" to "justify"), emptyList()),
        )

        val doc = HtmlNodeMapper.mapDocument(raw)

        assertEquals(HtmlTextAlign.Left, (doc.children[0] as HtmlParagraphNode).textAlign)
        assertEquals(HtmlTextAlign.Center, (doc.children[1] as HtmlParagraphNode).textAlign)
        assertEquals(HtmlTextAlign.Justify, (doc.children[2] as HtmlParagraphNode).textAlign)
    }
}
