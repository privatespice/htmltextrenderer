package org.privatespice.htmlrenderer.parser

private val whitespaceRegex = Regex("\\s+")

internal fun HtmlDocument.normalizeWhitespace(): HtmlDocument =
    copy(children = children.map { it.normalizeBlockNode() })

private fun HtmlBlockNode.normalizeBlockNode(): HtmlBlockNode = when (this) {
    is HtmlHeadingNode -> copy(children = normalizeInlineSiblings(children, trimEdges = true))
    is HtmlParagraphNode -> copy(children = normalizeInlineSiblings(children, trimEdges = true))
    is HtmlBlockQuoteNode -> copy(children = normalizeInlineSiblings(children, trimEdges = true))
    is HtmlListNode -> copy(items = items.map { it.normalizeListItemNode() })
    is HtmlListItemNode -> normalizeListItemNode()
}

private fun HtmlListItemNode.normalizeListItemNode(): HtmlListItemNode {
    val normalizedChildren = children.mapNotNull { it.normalizeMixedNode() }
    val result = mutableListOf<HtmlNode>()
    var index = 0

    while (index < normalizedChildren.size) {
        val node = normalizedChildren[index]
        if (node is HtmlInlineNode) {
            val start = index
            var end = index
            while (end < normalizedChildren.size && normalizedChildren[end] is HtmlInlineNode) {
                end++
            }
            val inlineRun = normalizedChildren.subList(start, end).map { it as HtmlInlineNode }
            result.addAll(normalizeInlineSiblings(inlineRun, trimEdges = false))
            index = end
        } else {
            result.add(node)
            index++
        }
    }

    trimLeadingMixedWhitespace(result)
    trimTrailingMixedWhitespace(result)

    return copy(children = result)
}

private fun HtmlNode.normalizeMixedNode(): HtmlNode? = when (this) {
    is HtmlInlineNode -> normalizeInlineNode()
    is HtmlBlockNode -> normalizeBlockNode()
}

private fun HtmlInlineNode.normalizeInlineNode(): HtmlInlineNode? = when (this) {
    is HtmlTextNode -> HtmlTextNode(collapseWhitespace(text))
    HtmlLineBreakNode -> HtmlLineBreakNode
    is HtmlStrongNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlEmphasisNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlUnderlineNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlStrikeThroughNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlSubscriptNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlSuperscriptNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlCodeNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlLinkNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
    is HtmlSpanNode -> copy(children = normalizeInlineSiblings(children, trimEdges = false)).ifHasChildren()
}

private fun normalizeInlineSiblings(
    nodes: List<HtmlInlineNode>,
    trimEdges: Boolean,
): List<HtmlInlineNode> {
    if (nodes.isEmpty()) return nodes

    val normalizedNodes = nodes.mapNotNull { it.normalizeInlineNode() }
    if (normalizedNodes.isEmpty()) return emptyList()

    val result = mutableListOf<HtmlInlineNode>()
    var pendingSpace = false

    normalizedNodes.forEachIndexed { index, node ->
        if (node is HtmlTextNode) {
            val collapsed = collapseWhitespace(node.text)
            val isWhitespaceOnly = collapsed.isBlank()

            val hasTextualBefore = hasTextualContentOnLeft(normalizedNodes, index)
            val hasTextualAfter = hasTextualContentOnRight(normalizedNodes, index)

            val nextText = when {
                isWhitespaceOnly -> {
                    if (hasTextualBefore && hasTextualAfter) " " else ""
                }
                else -> {
                    val leading = collapsed.startsWith(" ") && hasTextualBefore
                    val trailing = collapsed.endsWith(" ") && hasTextualAfter
                    val core = collapsed.trim()
                    buildString {
                        if (leading) append(' ')
                        append(core)
                        if (trailing) append(' ')
                    }
                }
            }

            if (nextText.isNotEmpty()) {
                appendTextNode(result, if (pendingSpace) " $nextText" else nextText)
                pendingSpace = false
            }
        } else {
            if (pendingSpace && startsWithTextualContent(node)) {
                appendTextNode(result, " ")
                pendingSpace = false
            }

            result.add(node)

            if (node is HtmlLineBreakNode) {
                pendingSpace = false
            } else if (endsWithSpace(node)) {
                pendingSpace = true
            }
        }

        val lastText = (result.lastOrNull() as? HtmlTextNode)?.text
        if (lastText != null) {
            pendingSpace = lastText.endsWith(" ")
        }
    }

    val merged = mergeAdjacentTextNodes(result)
    if (!trimEdges) return merged

    return trimInlineEdges(merged)
}

private fun appendTextNode(result: MutableList<HtmlInlineNode>, text: String) {
    if (text.isEmpty()) return

    val previous = result.lastOrNull()
    if (previous is HtmlTextNode) {
        val merged = (previous.text + text).replace(Regex(" {2,}"), " ")
        result[result.lastIndex] = HtmlTextNode(merged)
    } else {
        result.add(HtmlTextNode(text.replace(Regex(" {2,}"), " ")))
    }
}

private fun mergeAdjacentTextNodes(nodes: List<HtmlInlineNode>): List<HtmlInlineNode> {
    if (nodes.isEmpty()) return nodes
    val result = mutableListOf<HtmlInlineNode>()

    nodes.forEach { node ->
        if (node is HtmlTextNode) {
            val previous = result.lastOrNull()
            if (previous is HtmlTextNode) {
                result[result.lastIndex] = HtmlTextNode((previous.text + node.text).replace(Regex(" {2,}"), " "))
            } else {
                result.add(node)
            }
        } else {
            result.add(node)
        }
    }

    return result
}

private fun trimInlineEdges(nodes: List<HtmlInlineNode>): List<HtmlInlineNode> {
    if (nodes.isEmpty()) return nodes
    val result = nodes.toMutableList()

    val first = result.firstOrNull()
    if (first is HtmlTextNode) {
        val trimmed = first.text.trimStart()
        if (trimmed.isEmpty()) result.removeAt(0) else result[0] = HtmlTextNode(trimmed)
    }

    val last = result.lastOrNull()
    if (last is HtmlTextNode) {
        val trimmed = last.text.trimEnd()
        if (trimmed.isEmpty()) result.removeAt(result.lastIndex) else result[result.lastIndex] = HtmlTextNode(trimmed)
    }

    return result
}

private fun trimLeadingMixedWhitespace(nodes: MutableList<HtmlNode>) {
    while (nodes.firstOrNull() is HtmlTextNode) {
        val first = nodes.first() as HtmlTextNode
        val trimmed = first.text.trimStart()
        if (trimmed.isEmpty()) {
            nodes.removeAt(0)
            continue
        }
        nodes[0] = HtmlTextNode(trimmed)
        break
    }
}

private fun trimTrailingMixedWhitespace(nodes: MutableList<HtmlNode>) {
    while (nodes.lastOrNull() is HtmlTextNode) {
        val lastIndex = nodes.lastIndex
        val last = nodes[lastIndex] as HtmlTextNode
        val trimmed = last.text.trimEnd()
        if (trimmed.isEmpty()) {
            nodes.removeAt(lastIndex)
            continue
        }
        nodes[lastIndex] = HtmlTextNode(trimmed)
        break
    }
}

private fun hasTextualContentOnLeft(nodes: List<HtmlInlineNode>, index: Int): Boolean {
    for (i in index - 1 downTo 0) {
        val node = nodes[i]
        if (hasTextualContent(node)) return true
        if (node is HtmlLineBreakNode) return false
    }
    return false
}

private fun hasTextualContentOnRight(nodes: List<HtmlInlineNode>, index: Int): Boolean {
    for (i in index + 1 until nodes.size) {
        val node = nodes[i]
        if (hasTextualContent(node)) return true
        if (node is HtmlLineBreakNode) return false
    }
    return false
}

private fun hasTextualContent(node: HtmlInlineNode): Boolean = when (node) {
    is HtmlTextNode -> node.text.any { !it.isWhitespace() }
    HtmlLineBreakNode -> false
    is HtmlStrongNode -> node.children.any(::hasTextualContent)
    is HtmlEmphasisNode -> node.children.any(::hasTextualContent)
    is HtmlUnderlineNode -> node.children.any(::hasTextualContent)
    is HtmlStrikeThroughNode -> node.children.any(::hasTextualContent)
    is HtmlSubscriptNode -> node.children.any(::hasTextualContent)
    is HtmlSuperscriptNode -> node.children.any(::hasTextualContent)
    is HtmlCodeNode -> node.children.any(::hasTextualContent)
    is HtmlLinkNode -> node.children.any(::hasTextualContent)
    is HtmlSpanNode -> node.children.any(::hasTextualContent)
}

private fun startsWithTextualContent(node: HtmlInlineNode): Boolean = when (node) {
    is HtmlTextNode -> node.text.trimStart().isNotEmpty()
    HtmlLineBreakNode -> false
    is HtmlStrongNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlEmphasisNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlUnderlineNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlStrikeThroughNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlSubscriptNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlSuperscriptNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlCodeNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlLinkNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
    is HtmlSpanNode -> node.children.firstOrNull()?.let(::startsWithTextualContent) == true
}

private fun endsWithSpace(node: HtmlInlineNode): Boolean = when (node) {
    is HtmlTextNode -> node.text.endsWith(' ')
    HtmlLineBreakNode -> false
    is HtmlStrongNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlEmphasisNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlUnderlineNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlStrikeThroughNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlSubscriptNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlSuperscriptNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlCodeNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlLinkNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
    is HtmlSpanNode -> node.children.lastOrNull()?.let(::endsWithSpace) == true
}

private fun collapseWhitespace(text: String): String = text.replace(whitespaceRegex, " ")

private fun HtmlStrongNode.ifHasChildren(): HtmlStrongNode? = takeIf { children.isNotEmpty() }
private fun HtmlEmphasisNode.ifHasChildren(): HtmlEmphasisNode? = takeIf { children.isNotEmpty() }
private fun HtmlUnderlineNode.ifHasChildren(): HtmlUnderlineNode? = takeIf { children.isNotEmpty() }
private fun HtmlStrikeThroughNode.ifHasChildren(): HtmlStrikeThroughNode? = takeIf { children.isNotEmpty() }
private fun HtmlSubscriptNode.ifHasChildren(): HtmlSubscriptNode? = takeIf { children.isNotEmpty() }
private fun HtmlSuperscriptNode.ifHasChildren(): HtmlSuperscriptNode? = takeIf { children.isNotEmpty() }
private fun HtmlCodeNode.ifHasChildren(): HtmlCodeNode? = takeIf { children.isNotEmpty() }
private fun HtmlLinkNode.ifHasChildren(): HtmlLinkNode? = takeIf { children.isNotEmpty() }
private fun HtmlSpanNode.ifHasChildren(): HtmlSpanNode? = takeIf { children.isNotEmpty() }
