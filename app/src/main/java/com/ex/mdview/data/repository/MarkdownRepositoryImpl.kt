package com.ex.mdview.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

/**
 * Реализация интерфейса [MarkdownRepository].
 * Отвечает за фактическое взаимодействие с источниками данных:
 * - Чтение файлов из локальной файловой системы Android
 * - Загрузка данных по URL
 * - Сохранение данных в локальный файл
 *
 * @param context Контекст приложения
 */
class MarkdownRepositoryImpl(
    private val context: Context
) : MarkdownRepository {

    /**
     * Загружает содержимое Markdown-документа по заданному URL.
     *
     * @param url URL-адрес Markdown-документа.
     * @return Строка с содержимым Markdown.
     * @throws IOException В случае проблем с сетью, чтением потока или некорректным HTTP-ответом.
     */
    override suspend fun getMarkdownContentFromUrl(url: String): String =
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    return@withContext reader.readText()
                }
            } catch (e: Exception) {
                throw IOException("Ошибка загрузки с URL: ${e.message}", e)
            }
        }

    /**
     * Загружает содержимое Markdown-документа из локального файла по заданному URI.
     *
     * @param uri URI локального файла.
     * @return Строка с содержимым Markdown.
     * @throws FileNotFoundException Если файл не найден по указанному URI.
     * @throws IOException В случае других проблем при чтении файла.
     */
    override suspend fun getMarkdownContentFromFile(uri: Uri): String =
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    return@withContext reader.readText()
                }
            } ?: throw FileNotFoundException("Файл не найден по URI: $uri")
        }

    /**
     * Сохраняет предоставленное содержимое Markdown в локальный файл.
     *
     * @param content Строка с содержимым Markdown для сохранения.
     * @param documentId Необязательный идентификатор (имя файла) для сохранения.
     * Если null, будет сгенерировано уникальное имя файла.
     * @throws IOException В случае проблем при записи в файл.
     */
    override suspend fun saveMarkdownContent(content: String, documentId: String?) =
        withContext(Dispatchers.IO) {
            val fileName = documentId ?: "new_markdown_document_${System.currentTimeMillis()}.md"
            val outputDir = context.filesDir
            val file = File(outputDir, fileName)

            file.writeText(content)
        }
}