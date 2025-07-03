package com.ex.mdview.domain.usecase

import com.ex.mdview.domain.model.MarkdownElement

/**
 * Use Case: Рендеринг Markdown-текста в структуру для нативного отображения.
 */
class RenderMarkdownUseCase {
    /**
     * Основной метод выполнения Use Case.
     *
     * @param markdownText Сырой Markdown-текст в виде строки.
     * @return List<MarkdownElement> Список объектов MarkdownElement, представляющих
     * разобранную структуру Markdown. Эти элементы
     * могут быть затем использованы UI-слоем для
     * нативного рендеринга
     */
    operator fun invoke(markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()
        //todo : add parsing
        return elements
    }
}