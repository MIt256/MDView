package com.ex.mdview.data.repository

import android.net.Uri

/**
 * Интерфейс репозитория для работы с Markdown-документами.
 */
interface MarkdownRepository {
    /**
     * Получает содержимое Markdown из указанного URL.
     * @param url URL для загрузки Markdown.
     * @return Строка с содержимым Markdown.
     * @throws Exception в случае ошибки (например, сетевой).
     */
    suspend fun getMarkdownContentFromUrl(url: String): String

    /**
     * Получает содержимое Markdown из локального файла.
     * @param uri URI локального файла.
     * @return Строка с содержимым Markdown.
     * @throws Exception в случае ошибки (например, FileNotFoundException).
     */
    suspend fun getMarkdownContentFromFile(uri: Uri): String

    /**
     * Сохраняет содержимое Markdown.
     * @param content Содержимое для сохранения.
     * @param documentId Идентификатор документа для обновления, null для нового.
     * @throws Exception в случае ошибки сохранения.
     */
    suspend fun saveMarkdownContent(content: String, documentId: String? = null)
}