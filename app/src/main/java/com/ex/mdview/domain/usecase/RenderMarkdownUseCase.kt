package com.ex.mdview.domain.usecase

import com.ex.mdview.domain.model.MarkdownElement

/**
 * Use Case: Рендеринг Markdown-текста в структуру для нативного отображения.
 */
class RenderMarkdownUseCase {
    operator fun invoke(markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()
        val lines = markdownText.lines()
        var inTable = false
        val currentTableRows = mutableListOf<List<String>>()
        val currentTableHeaders = mutableListOf<String>()

        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) {
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
                return@forEachIndexed
            }

            when {
                line.startsWith("###### ") -> handleElement(
                    elements,
                    inTable,
                    currentTableHeaders,
                    currentTableRows
                ) {
                    MarkdownElement.Heading(line.substring(7).trim(), 6)
                }.also { inTable = it.first; currentTableHeaders.clear(); currentTableRows.clear() }

                line.startsWith("##### ") -> handleElement(
                    elements,
                    inTable,
                    currentTableHeaders,
                    currentTableRows
                ) {
                    MarkdownElement.Heading(line.substring(6).trim(), 5)
                }.also { inTable = it.first; currentTableHeaders.clear(); currentTableRows.clear() }

                line.startsWith("#### ") -> handleElement(
                    elements,
                    inTable,
                    currentTableHeaders,
                    currentTableRows
                ) {
                    MarkdownElement.Heading(line.substring(5).trim(), 4)
                }.also { inTable = it.first; currentTableHeaders.clear(); currentTableRows.clear() }

                line.startsWith("### ") -> handleElement(
                    elements,
                    inTable,
                    currentTableHeaders,
                    currentTableRows
                ) {
                    MarkdownElement.Heading(line.substring(4).trim(), 3)
                }.also { inTable = it.first; currentTableHeaders.clear(); currentTableRows.clear() }

                line.startsWith("## ") -> handleElement(
                    elements,
                    inTable,
                    currentTableHeaders,
                    currentTableRows
                ) {
                    MarkdownElement.Heading(line.substring(3).trim(), 2)
                }.also { inTable = it.first; currentTableHeaders.clear(); currentTableRows.clear() }

                line.startsWith("# ") -> handleElement(
                    elements,
                    inTable,
                    currentTableHeaders,
                    currentTableRows
                ) {
                    MarkdownElement.Heading(line.substring(2).trim(), 1)
                }.also { inTable = it.first; currentTableHeaders.clear(); currentTableRows.clear() }

                line.matches(Regex("!\\[(.*)\\]\\((.*)\\)")) -> {
                    handleElement(
                        elements,
                        inTable,
                        currentTableHeaders,
                        currentTableRows
                    ) {
                        val matchResult = Regex("!\\[(.*)\\]\\((.*)\\)").find(line)
                        val altText = matchResult?.groups?.get(1)?.value ?: ""
                        val imageUrl = matchResult?.groups?.get(2)?.value ?: ""
                        MarkdownElement.Image(imageUrl.trim(), altText.trim())
                    }.also {
                        inTable = it.first; currentTableHeaders.clear(); currentTableRows.clear()
                    }
                }

                line.trim().startsWith("|") && line.contains("|") -> {
                    val nextLineIsDivider =
                        lines.size > index + 1 && lines[index + 1].matches(Regex("\\|([\\-: ]+\\|)+[\\-: ]*"))

                    if (!inTable && nextLineIsDivider) {
                        inTable = true
                        currentTableHeaders.clear()
                        currentTableRows.clear()
                        currentTableHeaders.addAll(
                            line.split("|")
                                .drop(1)
                                .dropLast(1)
                                .map { it.trim() }
                        )
                    } else if (inTable) {
                        if (line.matches(Regex("\\|([\\-: ]+\\|)+[\\-: ]*"))) {
                        } else {
                            val rowData = line.split("|")
                                .drop(1)
                                .dropLast(1)
                                .map { it.trim() }

                            val completedRow = if (rowData.size < currentTableHeaders.size) {
                                rowData + List(currentTableHeaders.size - rowData.size) { "" }
                            } else {
                                rowData
                            }

                            currentTableRows.add(completedRow)
                        }
                    } else {
                        elements.add(MarkdownElement.Paragraph(line))
                    }
                }

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
                    elements.add(MarkdownElement.Paragraph(line))
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

    private fun handleElement(
        elements: MutableList<MarkdownElement>,
        inTable: Boolean,
        currentTableHeaders: MutableList<String>,
        currentTableRows: MutableList<List<String>>,
        createElement: () -> MarkdownElement
    ): Pair<Boolean, Unit> {
        if (inTable) {
            elements.add(
                MarkdownElement.Table(
                    currentTableHeaders.toList(),
                    currentTableRows.toList()
                )
            )
            return Pair(false, Unit)
        }
        elements.add(createElement())
        return Pair(inTable, Unit)
    }
}