package com.ex.mdview.domain.usecase

import com.ex.mdview.domain.model.MarkdownElement

class RenderMarkdownUseCase {
    /**
     * @param markdownText Сырой Markdown-текст.
     * @return Список объектов MarkdownElement, которые могут быть отображены нативно.
     */
    operator fun invoke(markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()
        //todo : add parsing
        return elements
    }
}