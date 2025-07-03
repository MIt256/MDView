package com.ex.mdview.domain.model

sealed class MarkdownElement {
    data class Heading(val text: String, val level: Int) : MarkdownElement()
    data class Paragraph(val text: String) : MarkdownElement()
    data class BoldText(val text: String) : MarkdownElement()
    data class ItalicText(val text: String) : MarkdownElement()
    data class StrikethroughText(val text: String) : MarkdownElement()
    data class Image(val url: String, val altText: String) : MarkdownElement()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownElement()
    // TODO: add elements
}