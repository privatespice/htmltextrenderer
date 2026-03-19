package org.privatespice.htmlrenderer.parser

sealed interface HtmlTag {
    val tagName: String
}

enum class HtmlBlockTag(override val tagName: String) : HtmlTag {
    H1("h1"),
    H2("h2"),
    H3("h3"),
    H4("h4"),
    P("p"),
    BLOCKQUOTE("blockquote"),
    UL("ul"),
    OL("ol"),
    LI("li"),
}

enum class HtmlInlineTag(override val tagName: String) : HtmlTag {
    SPAN("span"),
    A("a"),
    STRONG("strong"),
    B("b"),
    I("i"),
    EM("em"),
    S("s"),
    U("u"),
    SUB("sub"),
    SUP("sup"),
    CODE("code"),
    BR("br"),
}

private val allHtmlTagsByRaw: Map<String, HtmlTag> =
    (HtmlBlockTag.entries + HtmlInlineTag.entries).associateBy { it.tagName }

internal fun htmlTagFromName(tagName: String): HtmlTag? {
    return allHtmlTagsByRaw[tagName.lowercase()]
}

internal sealed interface HtmlRawNode

internal data class HtmlRawTextNode(val text: String) : HtmlRawNode

internal data class HtmlRawElementNode(
    val tag: HtmlTag,
    val attrs: Map<String, String>,
    val children: List<HtmlRawNode>,
) : HtmlRawNode

internal data object HtmlRawLineBreakNode : HtmlRawNode

internal val DefaultSupportedTags: Set<HtmlTag> =
    buildSet {
        addAll(HtmlBlockTag.entries)
        addAll(HtmlInlineTag.entries)
    }
