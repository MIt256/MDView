package com.ex.mdview.domain.usecase

import android.net.Uri
import com.ex.mdview.domain.repository.MarkdownRepository
import com.ex.mdview.domain.model.MarkdownSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use Case: Сохранение Markdown-документа.
 * Инкапсулирует бизнес-логику, связанную с персистентным сохранением
 * измененного Markdown-контента.
 *
 * @param markdownRepository Реализация интерфейса MarkdownRepository,
 * которая предоставляет методы для доступа к данным.
 */
class SaveMarkdownDocumentUseCase(
    private val markdownRepository: MarkdownRepository,
) {
    /**
     * Сохраняет [content] Markdown.
     * @param content Строка Markdown для сохранения.
     * @param targetSource Необязательный [MarkdownSource], указывающий куда сохранить.
     * Если [MarkdownSource.LocalFile], то перезаписывается по этому URI.
     * Если null или [MarkdownSource.Url], сохраняется как новый файл.
     * @return [Flow] с [Result] операции сохранения.
     */
    operator fun invoke(content: String, uri: Uri): Flow<Result<Unit>> = flow {
        try {
            markdownRepository.saveMarkdownContentToFile(content, uri)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}