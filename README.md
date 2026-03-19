## HtmlRenderer (Kotlin Multiplatform)

`HtmlRenderer` is a lightweight Kotlin Multiplatform library for rendering a safe subset of HTML in Compose UI (Android + iOS).

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
    HtmlRenderer(
        html = html,
        onLinkClicked = { url ->
            // Handle URL click
            println("Clicked: $url")
        },
    )
}
```

You can also customize typography, spacing, and per-node renderers (see `HtmlRendererExample.kt`).

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

## Build

From project root:

```bash
./gradlew :htmlrenderer:check
```
