package com.ex.mdview.domain.model

/**
 * Модель, которая является результатом парсинга сырого Markdown-текста.
 * UI-слой будет использовать эти объекты для нативного отображения.
 */
sealed class MarkdownElement {
    data class Heading(val text: String, val level: Int) : MarkdownElement()
    data class Paragraph(val text: String) : MarkdownElement()
    data class Image(val url: String, val altText: String) : MarkdownElement()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownElement()
    object EmptyLine : MarkdownElement()
}