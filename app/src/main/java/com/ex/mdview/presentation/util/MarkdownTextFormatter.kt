package com.ex.mdview.presentation.util

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan

/**
 * Вспомогательный класс для применения встроенного форматирования Markdown (жирный, курсив, зачеркнутый)
 * к строке и возврата CharSequence (SpannableStringBuilder), который может быть отображен в TextView.
 */
class MarkdownTextFormatter {

    /**
     * Применяет встроенное форматирование Markdown к заданной строке.
     * @param text Сырой текст, содержащий Markdown-разметку.
     * @return CharSequence с примененными Span-объектами.
     */
    fun formatInlineText(text: String): CharSequence {
        val spannable = SpannableStringBuilder(text)

        applySpan(spannable, STRIKETHROUGH_REGEX) { StrikethroughSpan() }
        applySpan(spannable, BOLD_REGEX) { StyleSpan(Typeface.BOLD) }
        applySpan(spannable, ITALIC_REGEX) { StyleSpan(Typeface.ITALIC) }

        return spannable
    }

    /**
     * Находит все совпадения по Regex в SpannableStringBuilder, заменяет их
     * содержимым первой группы и применяет указанный Span.
     *
     * @param spannable Объект для модификации.
     * @param regex Регулярное выражение для поиска.
     * @param createSpan Лямбда-функция для создания экземпляра Span'а.
     */
    private fun applySpan(
        spannable: SpannableStringBuilder,
        regex: Regex,
        createSpan: () -> CharacterStyle
    ) {
        val matches = regex.findAll(spannable).toList().reversed()

        matches.forEach { matchResult ->
            val contentGroup = matchResult.groups[1]
            if (contentGroup != null) {
                val content = contentGroup.value
                val range = matchResult.range

                spannable.replace(range.first, range.last + 1, content)

                spannable.setSpan(
                    createSpan(),
                    range.first,
                    range.first + content.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    companion object {
        private val STRIKETHROUGH_REGEX = Regex("~~(.*?)~~")
        private val BOLD_REGEX = Regex("\\*\\*(.*?)\\*\\*")
        private val ITALIC_REGEX = Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)")
    }
}