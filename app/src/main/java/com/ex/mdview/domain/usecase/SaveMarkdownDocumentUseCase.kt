package com.ex.mdview.domain.usecase

import com.ex.mdview.data.repository.MarkdownRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SaveMarkdownDocumentUseCase(
    private val markdownRepository: MarkdownRepository
) {
    /**
     * Сохраняет Markdown-документ.
     * @param content Содержимое Markdown для сохранения.
     * @param documentId Идентификатор документа (для обновления существующего), null для нового.
     * @return Flow<Result<Unit>> с успехом или ошибкой.
     */
    operator fun invoke(content: String, documentId: String? = null): Flow<Result<Unit>> = flow {
        try {
            markdownRepository.saveMarkdownContent(content, documentId)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}