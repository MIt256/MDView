package com.ex.mdview.domain.model

import android.net.Uri

sealed class MarkdownSource {
    data class LocalFile(val uri: Uri) : MarkdownSource()
    data class Url(val url: String) : MarkdownSource()
}