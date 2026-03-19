package org.privatespice.htmlrenderer.node

sealed interface HtmlNode

sealed interface HtmlBlockNode : HtmlNode

sealed interface HtmlInlineNode : HtmlNode

data class HtmlDocument(
    val children: List<HtmlBlockNode>,
)

data class HtmlTextNode(
    val text: String,
) : HtmlInlineNode

data object HtmlLineBreakNode : HtmlInlineNode

data class HtmlHeadingNode(
    val level: Int,
    val children: List<HtmlInlineNode>,
    val textAlign: HtmlTextAlign? = null,
) : HtmlBlockNode

data class HtmlParagraphNode(
    val children: List<HtmlInlineNode>,
    val textAlign: HtmlTextAlign? = null,
) : HtmlBlockNode

data class HtmlBlockQuoteNode(
    val children: List<HtmlInlineNode>,
    val textAlign: HtmlTextAlign? = null,
) : HtmlBlockNode

data class HtmlListNode(
    val ordered: Boolean,
    val items: List<HtmlListItemNode>,
) : HtmlBlockNode

data class HtmlListItemNode(
    val children: List<HtmlNode>,
) : HtmlBlockNode

data class HtmlLinkNode(
    val href: String?,
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlStrongNode(
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlEmphasisNode(
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlUnderlineNode(
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlStrikeThroughNode(
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlSubscriptNode(
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlSuperscriptNode(
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlCodeNode(
    val children: List<HtmlInlineNode>,
) : HtmlInlineNode

data class HtmlSpanNode(
    val children: List<HtmlInlineNode>,
    val underline: Boolean = false,
    val lineThrough: Boolean = false,
) : HtmlInlineNode

enum class HtmlTextAlign {
    Left,
    Right,
    Center,
    Justify,
}
