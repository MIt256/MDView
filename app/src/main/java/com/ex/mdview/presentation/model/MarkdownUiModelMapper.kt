package com.ex.mdview.presentation.model

import com.ex.mdview.domain.model.MarkdownElement

fun MarkdownElement.toUiModel(): MarkdownUiModel = when (this) {
    is MarkdownElement.Heading -> MarkdownUiModel.Heading(text, level)
    is MarkdownElement.Paragraph -> MarkdownUiModel.Paragraph(text)
    is MarkdownElement.Image -> MarkdownUiModel.Image(url, altText)
    is MarkdownElement.Table -> MarkdownUiModel.Table(headers, rows)
    is MarkdownElement.EmptyLine -> MarkdownUiModel.EmptyLine
}

fun List<MarkdownElement>.toUiModels(): List<MarkdownUiModel> = map { it.toUiModel() } 