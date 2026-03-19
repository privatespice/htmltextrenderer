package org.privatespice.htmlrenderer.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HtmlNodeTagTest {

    @Test
    fun htmlTagFromNameIsCaseInsensitive() {
        assertEquals(HtmlBlockTag.P, htmlTagFromName("P"))
        assertEquals(HtmlInlineTag.STRONG, htmlTagFromName("StRoNg"))
        assertEquals(HtmlInlineTag.BR, htmlTagFromName("BR"))
    }

    @Test
    fun defaultSupportedTagsContainsAllKnownTags() {
        HtmlBlockTag.entries.forEach { tag ->
            assertTrue(DefaultSupportedTags.contains(tag))
        }
        HtmlInlineTag.entries.forEach { tag ->
            assertTrue(DefaultSupportedTags.contains(tag))
        }
    }

    @Test
    fun allRegisteredTagNamesResolveBackToATag() {
        val names = (HtmlBlockTag.entries + HtmlInlineTag.entries).map { it.tagName }

        names.forEach { tagName ->
            assertNotNull(htmlTagFromName(tagName))
        }
    }
}
