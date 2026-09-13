package com.example.expense_tracker.ui.chat.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

/**
 * Render teks Markdown untuk bubble chat respons AI Assistant.
 *
 * Menggunakan library native Compose (com.mikepenz:multiplatform-markdown-renderer-m3)
 * sehingga rendering seluruh elemen markdown (heading, bold, italic, bullet list, blockquote, link)
 * sepenuhnya berupa Compose composables asli (bukan WebView/TextView) yang performan dan
 * konsisten dengan layout engine Compose.
 *
 * Styling warna dan tipografi di-override menggunakan token tema aplikasi (MaterialTheme)
 * agar harmonis dengan design system (Dynamic Color / Dark / Light theme).
 */
@Composable
internal fun AssistantMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Override warna markdown agar mengikuti token MaterialTheme aktif
    val colors = markdownColor(
        text = colorScheme.onSurface,
        codeText = colorScheme.onSurfaceVariant,
        inlineCodeText = colorScheme.primary,
        linkText = colorScheme.primary,
        codeBackground = colorScheme.surfaceContainerHighest,
        inlineCodeBackground = colorScheme.surfaceContainerHigh,
        dividerColor = colorScheme.outlineVariant
    )

    // Override tipografi markdown agar konsisten dengan skala font aplikasi
    val markdownTypography = markdownTypography(
        h1 = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        h2 = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        h3 = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        h4 = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        h5 = typography.titleSmall,
        h6 = typography.titleSmall,
        text = typography.bodyLarge,
        code = typography.bodyMedium,
        quote = typography.bodyMedium.copy(
            fontStyle = FontStyle.Italic,
            color = colorScheme.onSurfaceVariant
        ),
        paragraph = typography.bodyLarge,
        ordered = typography.bodyLarge,
        bullet = typography.bodyLarge,
        list = typography.bodyLarge
    )

    Markdown(
        content = markdown,
        colors = colors,
        typography = markdownTypography,
        modifier = modifier.fillMaxWidth()
    )
}
