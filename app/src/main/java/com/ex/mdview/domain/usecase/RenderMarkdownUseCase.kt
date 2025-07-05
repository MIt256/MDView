package com.ex.mdview.domain.usecase

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import com.ex.mdview.domain.model.MarkdownElement

/**
 * Use Case: Рендеринг Markdown-текста в структуру для нативного отображения.
 */
class RenderMarkdownUseCase {
    /**
     * @param markdownText Сырой Markdown-текст в виде строки.
     * @return List<MarkdownElement> Список объектов MarkdownElement, представляющих
     * разобранную структуру Markdown. Эти элементы могут быть затем использованы UI-слоем для
     * нативного рендеринга
     */
    operator fun invoke(markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()

        val lines = markdownText.lines()
        var inTable = false
        val currentTableRows = mutableListOf<List<String>>()
        val currentTableHeaders = mutableListOf<String>()

        lines.forEachIndexed { index, line ->
            when {
                //Заголовки
                line.startsWith("###### ") -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    elements.add(MarkdownElement.Heading(line.substring(7).trim(), 6))
                }

                line.startsWith("##### ") -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    elements.add(MarkdownElement.Heading(line.substring(6).trim(), 5))
                }

                line.startsWith("#### ") -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    elements.add(MarkdownElement.Heading(line.substring(5).trim(), 4))
                }

                line.startsWith("### ") -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    elements.add(MarkdownElement.Heading(line.substring(4).trim(), 3))
                }

                line.startsWith("## ") -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    elements.add(MarkdownElement.Heading(line.substring(3).trim(), 2))
                }

                line.startsWith("# ") -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    elements.add(MarkdownElement.Heading(line.substring(2).trim(), 1))
                }

                //Изображения
                line.matches(Regex("!\\[(.*)\\]\\((.*)\\)")) -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    val altTextRegex = Regex("!\\[(.*)\\]\\((.*)\\)")
                    val matchResult = altTextRegex.find(line)
                    val altText = matchResult?.groups?.get(1)?.value ?: ""
                    val imageUrl = matchResult?.groups?.get(2)?.value ?: ""
                    elements.add(MarkdownElement.Image(imageUrl, altText))
                }

                //Таблицы
                line.trim().startsWith("|") && line.contains("|") -> {
                    if (!inTable && lines.size > index + 1 && lines[index + 1].matches(Regex("\\|[-:\\s]+\\|[-:\\s]+\\|.*"))) {
                        inTable = true
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                        currentTableHeaders.addAll(line.split("|").map { it.trim() }
                            .filter { it.isNotBlank() })
                    } else if (inTable) {
                        if (line.matches(Regex("\\|[-:\\s]+\\|[-:\\s]+\\|.*"))) {
                        } else {
                            currentTableRows.add(line.split("|").map { it.trim() }
                                .filter { it.isNotBlank() })
                        }
                    }
                }
                //Абзац или конец таблицы
                else -> {
                    if (inTable) {
                        elements.add(
                            MarkdownElement.Table(
                                currentTableHeaders.toList(),
                                currentTableRows.toList()
                            )
                        )
                        inTable = false
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                    }
                    if (line.isNotBlank()) {
                        elements.add(MarkdownElement.Paragraph(processInlineFormatting(line)))
                    }
                }
            }
        }

        if (inTable) {
            elements.add(
                MarkdownElement.Table(
                    currentTableHeaders.toList(),
                    currentTableRows.toList()
                )
            )
        }

        return elements
    }

    private fun processInlineFormatting(text: String): CharSequence {
        val sb = SpannableStringBuilder(text)

        //Зачеркнутый текст ~~text~~
        val strikethroughRegex = Regex("~~(.*?)~~")
        val strikethroughMatches = strikethroughRegex.findAll(text).toList().reversed()
        strikethroughMatches.forEach { matchResult ->
            val originalStart = matchResult.range.first
            val originalEnd = matchResult.range.last + 1
            val content = matchResult.groupValues[1]

            sb.replace(originalStart, originalEnd, content)
            sb.setSpan(
                StrikethroughSpan(),
                originalStart,
                originalStart + content.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        //Жирный текст **text**
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val boldMatches = boldRegex.findAll(sb.toString()).toList().reversed()
        boldMatches.forEach { matchResult ->
            val originalStart = matchResult.range.first
            val originalEnd = matchResult.range.last + 1
            val content = matchResult.groupValues[1]

            sb.replace(originalStart, originalEnd, content)
            sb.setSpan(
                StyleSpan(Typeface.BOLD),
                originalStart,
                originalStart + content.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        //Курсив *text*
        val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)")
        val italicMatches = italicRegex.findAll(sb.toString()).toList().reversed()
        italicMatches.forEach { matchResult ->
            val originalStart = matchResult.range.first
            val originalEnd = matchResult.range.last + 1
            val content = matchResult.groupValues[1]

            sb.replace(originalStart, originalEnd, content)
            sb.setSpan(
                StyleSpan(Typeface.ITALIC),
                originalStart,
                originalStart + content.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return sb
    }
}