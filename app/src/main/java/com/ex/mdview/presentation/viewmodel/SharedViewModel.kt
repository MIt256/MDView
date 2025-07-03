package com.ex.mdview.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ex.mdview.domain.model.MarkdownElement
import com.ex.mdview.domain.model.MarkdownSource
import com.ex.mdview.domain.usecase.LoadMarkdownDocumentUseCase
import com.ex.mdview.domain.usecase.RenderMarkdownUseCase
import com.ex.mdview.domain.usecase.SaveMarkdownDocumentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel для обмена данными и состояниями между фрагментами.
 * Посредник между UI и доменным слоем ([Use Cases]).
 *
 * @param loadMarkdownDocumentUseCase Use Case для загрузки.
 * @param saveMarkdownDocumentUseCase Use Case для сохранения.
 * @param renderMarkdownUseCase Use Case для рендеринга.
 */
class SharedViewModel(
    private val loadMarkdownDocumentUseCase: LoadMarkdownDocumentUseCase,
    private val saveMarkdownDocumentUseCase: SaveMarkdownDocumentUseCase,
    private val renderMarkdownUseCase: RenderMarkdownUseCase
) : ViewModel() {

    //Хранение необработанного содержимого Markdown-документа.
    private val _documentContent = MutableStateFlow<String>("")
    val documentContent: StateFlow<String> = _documentContent.asStateFlow()

    //Хранение списка MarkdownElement, которые готовы для нативного отображения в UI.
    private val _renderedMarkdownElements = MutableStateFlow<List<MarkdownElement>>(emptyList())
    val renderedMarkdownElements: StateFlow<List<MarkdownElement>> =
        _renderedMarkdownElements.asStateFlow()

    //Отслеживание статуса операции загрузки документа.
    private val _loadStatus = MutableStateFlow<LoadStatus>(LoadStatus.Idle)
    val loadStatus: StateFlow<LoadStatus> = _loadStatus.asStateFlow()

//    private val _saveStatus = MutableStateFlow<LoadStatus>(LoadStatus.Idle)
//    val saveStatus: StateFlow<LoadStatus> = _saveStatus.asStateFlow()

    /**
     * Инициирует загрузку Markdown-документа из локального файла.
     * @param uri URI локального файла.
     */
    fun loadLocalFile(uri: Uri) {
        viewModelScope.launch {
            _loadStatus.value = LoadStatus.Loading
            loadMarkdownDocumentUseCase(MarkdownSource.LocalFile(uri))
                .collectLatest { result ->
                    result
                        .onSuccess { content ->
                            _documentContent.value = content
                            renderAndDisplayMarkdown(content)
                            _loadStatus.value = LoadStatus.Success
                        }
                        .onFailure { throwable ->
                            _loadStatus.value = LoadStatus.Error(
                                throwable.localizedMessage
                                    ?: "Неизвестная ошибка при загрузке файла."
                            )
                        }
                }
        }
    }

    /**
     * Инициирует загрузку Markdown-документа по URL из сети.
     * @param url URL Markdown-документа.
     */
    fun loadFromUrl(url: String) {
        viewModelScope.launch {
            _loadStatus.value = LoadStatus.Loading
            loadMarkdownDocumentUseCase(MarkdownSource.Url(url))
                .collectLatest { result ->
                    result
                        .onSuccess { content ->
                            _documentContent.value = content
                            renderAndDisplayMarkdown(content)
                            _loadStatus.value = LoadStatus.Success
                        }
                        .onFailure { throwable ->
                            _loadStatus.value = LoadStatus.Error(
                                throwable.localizedMessage ?: "Неизвестная ошибка при загрузке URL."
                            )
                        }
                }
        }
    }

//    fun saveCurrentDocument(documentId: String? = null) {
//        viewModelScope.launch {
//            _saveStatus.value = LoadStatus.Loading
//            saveMarkdownDocumentUseCase(_documentContent.value, documentId)
//                .collectLatest { result ->
//                    result
//                        .onSuccess {
//                            _saveStatus.value = LoadStatus.Success
//                            // Возможно, здесь можно сбросить статус или показать Toast
//                        }
//                        .onFailure { throwable ->
//                            _saveStatus.value = LoadStatus.Error(
//                                throwable.localizedMessage ?: "Неизвестная ошибка при сохранении."
//                            )
//                        }
//                }
//        }
//    }

    /**
     * Вспомогательная функция для преобразования сырого Markdown-текста в список объектов
     * MarkdownElement.
     * Результат затем обновляет _renderedMarkdownElements, за которым наблюдает UI.
     * @param markdownText Сырой Markdown-текст.
     */
    private fun renderAndDisplayMarkdown(markdownText: String) {
        val elements = renderMarkdownUseCase(markdownText)
        _renderedMarkdownElements.value = elements
    }
}