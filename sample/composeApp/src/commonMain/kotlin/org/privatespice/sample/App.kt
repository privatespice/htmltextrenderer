package org.privatespice.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.privatespice.htmlrenderer.HtmlRenderer

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HtmlRenderer(
                html = """
                    <h1>Lorem Ipsum Title</h1>
                    <p>
                        Lorem ipsum dolor sit amet, <strong>consectetur adipiscing elit</strong>.
                        <em>Integer nec odio</em>. Praesent libero.
                        Sed cursus ante dapibus diam. <a href="https://example.com">Visit link</a>.
                    </p>

                    <h2>Subheading Example</h2>
                    <p>
                        Sed nisi. Nulla quis sem at nibh elementum imperdiet.
                        <span> Duis sagittis ipsum.</span>
                        <b>Praesent mauris</b>, <i>fusce nec tellus</i> sed augue semper porta.
                    </p>

                    <blockquote>
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit,
                        sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                    </blockquote>

                    <h3>List Example</h3>
                    <ul>
                        <li>Lorem <strong>ipsum</strong></li>
                        <li>Dolor sit <em>amet</em></li>
                        <li>
                            Consectetur adipiscing
                            <ul>
                                <li>Nested <code>code()</code> example</li>
                                <li>Another item<br>with line break</li>
                            </ul>
                        </li>
                    </ul>

                    <h4>Ordered List</h4>
                    <ol>
                        <li>First item</li>
                        <li>Second item with <u>underline</u> and <s>strikethrough</s></li>
                        <li>Third item with H<sub>2</sub>O and x<sup>2</sup></li>
                    </ol>

                    <p>
                        Final paragraph with inline <code>val example = true</code>
                        and some <span>extra span text</span>.
                    </p>
                """.trimIndent().replace(Regex("\\s+"), " ")
            )
        }
    }
}