package com.ex.mdview.presentation.viewmodel

/**
 * 'Sealed class' для представления различных состояний операции загрузки документа.
 */
sealed class LoadStatus {
    object Idle : LoadStatus()
    object Loading : LoadStatus()
    object Success : LoadStatus()
    data class Error(val message: String) : LoadStatus()
}