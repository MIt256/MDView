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

    //Отслеживание статуса действий.
    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationStatus: StateFlow<OperationStatus> = _operationStatus.asStateFlow()

    //Отслеживание источника текущего документа. Null, если это новый документ.
    private val _currentDocumentSource = MutableStateFlow<MarkdownSource?>(null)
    val currentDocumentSource: StateFlow<MarkdownSource?> = _currentDocumentSource.asStateFlow()

    /**
     * Устанавливает новое содержимое документа в ViewModel.
     * @param content Новая строка содержимого Markdown.
     */
    fun setContent(content: String) {
        _documentContent.value = content
        renderAndDisplayMarkdown(content)
    }

    /**
     * Инициирует загрузку Markdown-документа из локального файла.
     * @param uri URI локального файла.
     */
    fun loadLocalFile(uri: Uri) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus.Loading
            loadMarkdownDocumentUseCase(MarkdownSource.LocalFile(uri))
                .collectLatest { result ->
                    result
                        .onSuccess { content ->
                            _documentContent.value = content
                            renderAndDisplayMarkdown(content)
                            _operationStatus.value = OperationStatus.Success
                            _currentDocumentSource.value = MarkdownSource.LocalFile(uri)
                        }
                        .onFailure { throwable ->
                            _operationStatus.value = OperationStatus.Error(
                                throwable.localizedMessage
                                    ?: "Неизвестная ошибка при загрузке файла."
                            )
                            _currentDocumentSource.value = null
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
            _operationStatus.value = OperationStatus.Loading
            loadMarkdownDocumentUseCase(MarkdownSource.Url(url))
                .collectLatest { result ->
                    result
                        .onSuccess { content ->
                            _documentContent.value = content
                            renderAndDisplayMarkdown(content)
                            _operationStatus.value = OperationStatus.Success
                            _currentDocumentSource.value = null
                        }
                        .onFailure { throwable ->
                            _operationStatus.value = OperationStatus.Error(
                                throwable.localizedMessage ?: "Неизвестная ошибка при загрузке URL."
                            )
                            _currentDocumentSource.value = null
                        }
                }
        }
    }

    /**
     * Инициирует сохранения Markdown-документа.
     * Сохраняет в тот локалный файл из которого брали содержимое или новый файл, в случае
     * если из сети
     */
    fun saveCurrentDocument() {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus.Loading
            val sourceToSaveTo: MarkdownSource? =
                when (val currentSource = _currentDocumentSource.value) {
                    is MarkdownSource.LocalFile -> currentSource
                    is MarkdownSource.Url -> null
                    null -> null
                }

            try {
                saveMarkdownDocumentUseCase(_documentContent.value, sourceToSaveTo)
                    .collectLatest { result ->
                        result
                            .onSuccess {
                                _operationStatus.value = OperationStatus.Success
                            }
                            .onFailure { throwable ->
                                _operationStatus.value = OperationStatus.Error(
                                    throwable.localizedMessage ?: "Неизвестная ошибка сохранения"
                                )
                            }
                    }
            } catch (e: Exception) {
                _operationStatus.value =
                    OperationStatus.Error(
                        e.localizedMessage ?: "Ошибка при инициализации сохранения"
                    )
            }
        }
    }

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