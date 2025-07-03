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

class MarkdownRepositoryImpl(
    private val context: Context
) : MarkdownRepository {

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

    override suspend fun getMarkdownContentFromFile(uri: Uri): String =
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    return@withContext reader.readText()
                }
            } ?: throw FileNotFoundException("Файл не найден по URI: $uri")
        }

    override suspend fun saveMarkdownContent(content: String, documentId: String?) =
        withContext(Dispatchers.IO) {
            val fileName = documentId ?: "new_markdown_document_${System.currentTimeMillis()}.md"
            val outputDir = context.filesDir
            val file = File(outputDir, fileName)

            file.writeText(content)
        }
}