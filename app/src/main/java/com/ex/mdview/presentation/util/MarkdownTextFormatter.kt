package com.ex.mdview.presentation.util

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan

/**
 * Вспомогательный класс для применения встроенного форматирования Markdown (жирный, курсив, зачеркнутый)
 * к строке и возврата CharSequence (SpannableStringBuilder), который может быть отображен в TextView.
 */
class MarkdownTextFormatter {

    /**
     * Применяет встроенное форматирование Markdown к заданной строке.
     * @param text Сырой текст, содержащий Markdown-разметку
     * @return CharSequence с примененными Span-объектами.
     */
    fun formatInlineText(text: String): CharSequence {
        val sb = SpannableStringBuilder(text)

        // Зачеркнутый текст ~~text~~
        val strikethroughRegex = Regex("~~(.*?)~~")
        val strikethroughMatches = strikethroughRegex.findAll(sb.toString()).toList().reversed()
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

        // Жирный текст **text**
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

        // Курсив *text*
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