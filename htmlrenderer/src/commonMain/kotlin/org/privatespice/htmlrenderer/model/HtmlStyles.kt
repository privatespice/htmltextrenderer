package org.privatespice.htmlrenderer.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class HtmlTypography(
    val paragraph: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val listItem: TextStyle,
)

data class HtmlSpacing(
    val paragraph: Dp,
    val listItem: Dp,
)

object HtmlStyleDefaults {

    @Composable
    fun typography(): HtmlTypography {
        val typography = MaterialTheme.typography
        return HtmlTypography(
            paragraph = typography.bodyMedium,
            h1 = typography.headlineSmall,
            h2 = typography.titleLarge,
            h3 = typography.titleMedium,
            h4 = typography.titleSmall,
            listItem = typography.bodyMedium,
        )
    }

    fun spacing(): HtmlSpacing = HtmlSpacing(
        paragraph = 8.dp,
        listItem = 4.dp,
    )
}
