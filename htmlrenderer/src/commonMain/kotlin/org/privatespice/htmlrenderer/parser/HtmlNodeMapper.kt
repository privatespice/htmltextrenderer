package org.privatespice.htmlrenderer.parser

internal object HtmlNodeMapper {

    fun mapDocument(rawNodes: List<HtmlRawNode>): HtmlDocument {
        val blockNodes = rawNodes.mapNotNull { mapToBlockNode(it) }
        return HtmlDocument(blockNodes)
    }

    private fun mapToBlockNode(node: HtmlRawNode): HtmlBlockNode? = when (node) {
        is HtmlRawElementNode -> mapElementToBlockNode(node)
        is HtmlRawTextNode -> null // bare text at block level is dropped
        HtmlRawLineBreakNode -> null // <br> at block level is dropped
    }

    private fun mapElementToBlockNode(node: HtmlRawElementNode): HtmlBlockNode? = when (node.tag) {
        HtmlBlockTag.H1 -> HtmlHeadingNode(
            level = 1,
            children = mapToInlineChildren(node.children),
            textAlign = parseTextAlign(node.attrs),
        )
        HtmlBlockTag.H2 -> HtmlHeadingNode(
            level = 2,
            children = mapToInlineChildren(node.children),
            textAlign = parseTextAlign(node.attrs),
        )
        HtmlBlockTag.H3 -> HtmlHeadingNode(
            level = 3,
            children = mapToInlineChildren(node.children),
            textAlign = parseTextAlign(node.attrs),
        )
        HtmlBlockTag.H4 -> HtmlHeadingNode(
            level = 4,
            children = mapToInlineChildren(node.children),
            textAlign = parseTextAlign(node.attrs),
        )
        HtmlBlockTag.P -> HtmlParagraphNode(
            children = mapToInlineChildren(node.children),
            textAlign = parseTextAlign(node.attrs),
        )
        HtmlBlockTag.BLOCKQUOTE -> HtmlBlockQuoteNode(
            children = mapToInlineChildren(node.children),
            textAlign = parseTextAlign(node.attrs),
        )
        HtmlBlockTag.UL -> HtmlListNode(
            ordered = false,
            items = mapToListItems(node.children),
        )
        HtmlBlockTag.OL -> HtmlListNode(
            ordered = true,
            items = mapToListItems(node.children),
        )
        HtmlBlockTag.LI -> HtmlListItemNode(
            children = mapToMixedChildren(node.children),
        )
        else -> null // inline tags appearing at block level are ignored
    }

    private fun mapToListItems(nodes: List<HtmlRawNode>): List<HtmlListItemNode> =
        nodes.mapNotNull { node ->
            if (node is HtmlRawElementNode && node.tag == HtmlBlockTag.LI) {
                HtmlListItemNode(children = mapToMixedChildren(node.children))
            } else {
                null
            }
        }

    private fun mapToMixedChildren(nodes: List<HtmlRawNode>): List<HtmlNode> =
        nodes.mapNotNull { node ->
            // Prefer inline; fall back to block so nested lists are preserved.
            mapToInlineNode(node) ?: mapToBlockNode(node)
        }

    private fun mapToInlineChildren(nodes: List<HtmlRawNode>): List<HtmlInlineNode> =
        nodes.mapNotNull { mapToInlineNode(it) }

    private fun mapToInlineNode(node: HtmlRawNode): HtmlInlineNode? = when (node) {
        is HtmlRawTextNode -> HtmlTextNode(node.text)
        HtmlRawLineBreakNode -> HtmlLineBreakNode
        is HtmlRawElementNode -> mapElementToInlineNode(node)
    }

    private fun mapElementToInlineNode(node: HtmlRawElementNode): HtmlInlineNode? = when (node.tag) {
        HtmlInlineTag.STRONG,
        HtmlInlineTag.B,
        -> HtmlStrongNode(children = mapToInlineChildren(node.children))

        HtmlInlineTag.EM,
        HtmlInlineTag.I,
        -> HtmlEmphasisNode(children = mapToInlineChildren(node.children))

        HtmlInlineTag.U -> HtmlUnderlineNode(children = mapToInlineChildren(node.children))

        HtmlInlineTag.S -> HtmlStrikeThroughNode(children = mapToInlineChildren(node.children))

        HtmlInlineTag.SUB -> HtmlSubscriptNode(children = mapToInlineChildren(node.children))

        HtmlInlineTag.SUP -> HtmlSuperscriptNode(children = mapToInlineChildren(node.children))

        HtmlInlineTag.CODE -> HtmlCodeNode(children = mapToInlineChildren(node.children))

        HtmlInlineTag.A -> HtmlLinkNode(
            href = node.attrs["href"],
            children = mapToInlineChildren(node.children),
        )

        HtmlInlineTag.SPAN -> HtmlSpanNode(
            children = mapToInlineChildren(node.children),
            underline = node.attrs["data-underline"] == "true",
            lineThrough = node.attrs["data-line-through"] == "true",
        )

        HtmlInlineTag.BR -> HtmlLineBreakNode

        else -> null
    }

    private fun parseTextAlign(attrs: Map<String, String>): HtmlTextAlign? =
        when (attrs["data-text-align"]) {
            "left" -> HtmlTextAlign.Left
            "right" -> HtmlTextAlign.Right
            "center" -> HtmlTextAlign.Center
            "justify" -> HtmlTextAlign.Justify
            else -> null
        }
}
