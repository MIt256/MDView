package com.ex.mdview.presentation.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ex.mdview.data.repository.MarkdownRepositoryImpl
import com.ex.mdview.presentation.viewmodel.SharedViewModel
import com.ex.mdview.domain.usecase.LoadMarkdownDocumentUseCase
import com.ex.mdview.domain.usecase.RenderMarkdownUseCase
import com.ex.mdview.domain.usecase.SaveMarkdownDocumentUseCase

/**
 * Пользовательская фабрика для создания экземпляров [SharedViewModel].
 * @param application Экземпляр Application, который необходим для инициализации
 * [MarkdownRepositoryImpl]
 */
class SharedViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    /**
     * Создает новый экземпляр ViewModel указанного класса.
     * @param modelClass Класс ViewModel, который нужно создать.
     * @return Экземпляр ViewModel.
     * @throws IllegalArgumentException Если передан класс, который не является [SharedViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {

            val markdownRepository = MarkdownRepositoryImpl(application.applicationContext)
            val loadMarkdownDocumentUseCase = LoadMarkdownDocumentUseCase(markdownRepository)
            val saveMarkdownDocumentUseCase = SaveMarkdownDocumentUseCase(markdownRepository)
            val renderMarkdownUseCase = RenderMarkdownUseCase()

            return SharedViewModel(
                loadMarkdownDocumentUseCase,
                saveMarkdownDocumentUseCase,
                renderMarkdownUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}