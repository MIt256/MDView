package com.ex.mdview.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ex.mdview.domain.model.MarkdownElement
import com.ex.mdview.domain.model.MarkdownSource
import com.ex.mdview.domain.usecase.LoadMarkdownDocumentUseCase
import com.ex.mdview.domain.usecase.RenderMarkdownUseCase
import com.ex.mdview.domain.usecase.SaveMarkdownDocumentUseCase
import com.ex.mdview.presentation.model.MarkdownUiModel
import com.ex.mdview.presentation.model.toUiModels
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val renderMarkdownUseCase: RenderMarkdownUseCase,
) : ViewModel() {

    //Хранение необработанного содержимого Markdown-документа.
    private val _documentContent = MutableStateFlow<String>("")
    val documentContent: StateFlow<String> = _documentContent.asStateFlow()

    //Хранение списка MarkdownUiModel, которые готовы для нативного отображения в UI.
    private val _renderedMarkdownUiModels = MutableStateFlow<List<MarkdownUiModel>>(emptyList())
    val renderedMarkdownUiModels: StateFlow<List<MarkdownUiModel>> =
        _renderedMarkdownUiModels.asStateFlow()

    // StateFlow для статуса операции, который может иметь постоянное состояние
    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationStatus: StateFlow<OperationStatus> = _operationStatus.asStateFlow()

    // SharedFlow для одноразовых сообщений
    private val _oneTimeMessage = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val oneTimeMessage: SharedFlow<String> = _oneTimeMessage.asSharedFlow()

    //Отслеживание источника текущего документа. Null, если это новый документ.
    private val _currentDocumentSource = MutableStateFlow<MarkdownSource?>(null)
    val currentDocumentSource: StateFlow<MarkdownSource?> = _currentDocumentSource.asStateFlow()

    init {
        renderAndDisplayMarkdown(_documentContent.value)
    }

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
                            _oneTimeMessage.emit("Документ успешно загружен!")
                        }
                        .onFailure { throwable ->
                            _operationStatus.value = OperationStatus.Idle
                            val message =
                                throwable.localizedMessage ?: "Неизвестная ошибка загрузки файла."
                            _oneTimeMessage.emit(message)
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
                            _oneTimeMessage.emit("Документ успешно загружен из URL!")
                        }
                        .onFailure { throwable ->
                            _operationStatus.value = OperationStatus.Idle
                            val message =
                                throwable.localizedMessage ?: "Неизвестная ошибка загрузки URL."
                            _oneTimeMessage.emit(message)
                            _currentDocumentSource.value = null
                        }
                }
        }
    }

    /**
     * Функция для выполнения логики сохранения.
     * @param uri URI файла, куда нужно сохранить.
     * @param isNewFile true, если сохраняется как новый файл (для обновления [_currentDocumentSource]).
     */
    private fun executeSaveOperation(uri: Uri, isNewFile: Boolean = false) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus.Loading
            _oneTimeMessage.emit("Сохранение...")
            try {
                saveMarkdownDocumentUseCase(_documentContent.value, uri)
                    .collectLatest { result ->
                        result
                            .onSuccess {
                                _operationStatus.value = OperationStatus.Success
                                if (isNewFile) {
                                    _currentDocumentSource.value = MarkdownSource.LocalFile(uri)
                                }
                                _oneTimeMessage.emit("Документ успешно сохранен!")
                            }
                            .onFailure { throwable ->
                                _operationStatus.value = OperationStatus.Idle
                                val message =
                                    throwable.localizedMessage ?: "Неизвестная ошибка сохранения."
                                _oneTimeMessage.emit(message)
                            }
                    }
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Idle
                _oneTimeMessage.emit(e.localizedMessage ?: "Ошибка при инициализации сохранения.")
            }
        }
    }

    /**
     * Инициирует сохранение Markdown-документа.
     * Сохраняет в тот локальный файл, из которого брали содержимое.
     */
    fun saveCurrentDocument() {
        val currentSource = _currentDocumentSource.value
        if (currentSource is MarkdownSource.LocalFile) {
            executeSaveOperation(currentSource.uri)
        } else {
            viewModelScope.launch {
                _operationStatus.value = OperationStatus.Idle
                _oneTimeMessage.emit("Невозможно сохранить: документ не имеет локального источника для перезаписи. Используйте 'Сохранить как'.")
            }
        }
    }

    /**
     * Инициирует сохранение Markdown-документа в указанный URI.
     * Этот метод используется для сохранения НОВЫХ файлов или сохранения "как".
     * @param uri URI файла для сохранения.
     */
    fun saveCurrentDocument(uri: Uri) {
        executeSaveOperation(uri, isNewFile = true)
    }

    /**
     * Вспомогательная функция для преобразования сырого Markdown-текста в список объектов
     * MarkdownElement.
     * Результат затем обновляет _renderedMarkdownElements, за которым наблюдает UI.
     * @param markdownText Сырой Markdown-текст.
     */
    private fun renderAndDisplayMarkdown(markdownText: String) {
        viewModelScope.launch {
            val elements = renderMarkdownUseCase(markdownText)
            _renderedMarkdownUiModels.value = elements.toUiModels()
        }
    }

    /**
     * Вспомогательная функция для проверки локальный ли файл.
     */
    fun isCurrentDocumentLocal(): Boolean {
        return currentDocumentSource.value is MarkdownSource.LocalFile
    }
}