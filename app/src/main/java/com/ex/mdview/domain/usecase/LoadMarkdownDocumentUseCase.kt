package com.ex.mdview.domain.usecase

import com.ex.mdview.data.repository.MarkdownRepository
import com.ex.mdview.domain.model.MarkdownSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use Case: Загрузка Markdown-документа.
 * Инкапсулирует бизнес-логику, связанную с получением Markdown-контента
 * из различных источников (локальный файл или URL).
 * @param markdownRepository Реализация интерфейса MarkdownRepository,
 * которая предоставляет методы для доступа к данным.
 */
class LoadMarkdownDocumentUseCase(
    private val markdownRepository: MarkdownRepository
) {
    /**
     * Загружает Markdown-документ из указанного источника.
     * @param source Источник Markdown-документа (локальный файл или URL).
     * @return Flow<Result<String>> с содержимым Markdown или ошибкой.
     */
    operator fun invoke(source: MarkdownSource): Flow<Result<String>> = flow {
        try {
            when (source) {
                is MarkdownSource.LocalFile -> {
                    val content = markdownRepository.getMarkdownContentFromFile(source.uri)
                    emit(Result.success(content))
                }

                is MarkdownSource.Url -> {
                    val content = markdownRepository.getMarkdownContentFromUrl(source.url)
                    emit(Result.success(content))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}