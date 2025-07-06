package com.ex.mdview.domain.repository

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
     * Сохраняет содержимое Markdown (существующий файл).
     * @param content Содержимое для сохранения.
     * @param uri URI локального файла.
     * @throws Exception в случае ошибки сохранения.
     */
    suspend fun saveMarkdownContentToFile(content: String, uri: Uri)
}