package com.ex.mdview.domain.model

import android.net.Uri

/**
 * Определяет возможные источники для загрузки Markdown-документа.
 */
sealed class MarkdownSource {
    /**
     * Представляет Markdown-документ, расположенный в локальной файловой системе устройства.
     * @param uri URI файла, указывающий на его местоположение.
     */
    data class LocalFile(val uri: Uri) : MarkdownSource()
    /**
     * Представляет Markdown-документ, доступный по URL в сети Интернет.
     * @param url Строка, содержащая полный URL-адрес документа.
     */
    data class Url(val url: String) : MarkdownSource()
}