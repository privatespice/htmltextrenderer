## HtmlTextRenderer (Compose Multiplatform)

`HtmlTextRenderer` is a lightweight Compose Multiplatform library for rendering a safe subset of HTML in Compose UI (Android + iOS), built with Kotlin Multiplatform.

It parses HTML into typed nodes, normalizes whitespace to browser-like behavior, and renders the result using Compose text and layout primitives.

### What this framework does

- Renders common HTML content in Compose with styled text blocks
- Supports links, headings, paragraphs, lists, and common inline formatting
- Preserves `<br>` as a real line break
- Normalizes whitespace to avoid:
  - extra spaces from indentation/newlines
  - merged words across inline boundaries
  - duplicate spaces

## Quick example

```kotlin
@Composable
fun Article(html: String) {
    HtmlTextRenderer(
        html = html,
        onLinkClicked = { url ->
            // Handle URL click
            println("Clicked: $url")
        },
    )
}
```

You can also customize typography, spacing, and per-node renderers (see `HtmlRendererExample.kt` and `HtmlTextRenderer(...)`).

## Installation (JitPack)

Add JitPack to your repositories:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

Use only the text renderer module artifact:

```kotlin
implementation("com.github.privatespice.htmltextrenderer:htmlrenderer:<tag>")
```

Replace `<tag>` with a Git tag or commit hash.

## Supported tags

### Block tags

- `h1`
- `h2`
- `h3`
- `h4`
- `p`
- `blockquote`
- `ul`
- `ol`
- `li`

### Inline tags

- `span`
- `a`
- `strong`
- `b`
- `i`
- `em`
- `s`
- `u`
- `sub`
- `sup`
- `code`
- `br`

## Notes

- Unsupported tags are ignored/unwrapped so inner text can still be rendered.
- You can limit allowed tags with the `supportedTags` parameter.
- Default supported tags are defined in `HtmlNode.kt` as `DefaultSupportedTags`.

## Credits

- HTML parsing is powered by [`Ksoup`](https://github.com/MohamedRejeb/Ksoup).

## License

```text
Copyright 2026 Mitchell Wit

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

