package com.ex.mdview.domain.usecase

import com.ex.mdview.domain.model.MarkdownElement
import com.ex.mdview.domain.util.getGroupValue

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
                elements.add(MarkdownElement.EmptyLine)
                return@forEachIndexed
            }
            val matchedHeading =
                headingPrefixes.firstOrNull { (prefix, _) -> line.startsWith(prefix) }
            if (matchedHeading != null) {
                val (prefix, level) = matchedHeading
                inTable = handleElement(
                    elements,
                    inTable,
                    currentTableHeaders,
                    currentTableRows
                ) {
                    MarkdownElement.Heading(line.substring(prefix.length).trim(), level)
                }
                currentTableHeaders.clear()
                currentTableRows.clear()
            } else when {
                line.matches(IMAGE_REGEX) -> {
                    inTable = handleElement(
                        elements,
                        inTable,
                        currentTableHeaders,
                        currentTableRows
                    ) {
                        val matchResult = IMAGE_REGEX.find(line)
                        val altText = matchResult.getGroupValue(1)
                        val imageUrl = matchResult.getGroupValue(2)
                        MarkdownElement.Image(imageUrl.trim(), altText.trim())
                    }
                    currentTableHeaders.clear()
                    currentTableRows.clear()
                }

                line.trim().startsWith("|") && line.contains("|") -> {
                    val nextLineIsDivider =
                        lines.size > index + 1 && lines[index + 1].matches(TABLE_DIVIDER_REGEX)

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
                        if (line.matches(TABLE_DIVIDER_REGEX)) {
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
    ): Boolean {
        if (inTable) {
            elements.add(
                MarkdownElement.Table(
                    currentTableHeaders.toList(),
                    currentTableRows.toList()
                )
            )
            return false
        }
        elements.add(createElement())
        return inTable
    }

    companion object {
        private val headingPrefixes = listOf(
            "###### " to 6,
            "##### " to 5,
            "#### " to 4,
            "### " to 3,
            "## " to 2,
            "# " to 1
        )

        private val IMAGE_REGEX = Regex("!\\[(.*)\\]\\((.*)\\)")
        private val TABLE_DIVIDER_REGEX = Regex("\\|([\\-: ]+\\|)+[\\-: ]*")
    }
}