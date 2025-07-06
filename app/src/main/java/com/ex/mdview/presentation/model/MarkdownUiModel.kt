package com.ex.mdview.presentation.model

sealed class MarkdownUiModel {
    data class Heading(val text: String, val level: Int) : MarkdownUiModel()
    data class Paragraph(val text: String) : MarkdownUiModel()
    data class Image(val url: String, val altText: String) : MarkdownUiModel()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownUiModel()
    object EmptyLine : MarkdownUiModel()
} 