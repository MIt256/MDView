package com.ex.mdview.presentation.viewmodel

/**
 * 'Sealed class' для представления различных состояний операции загрузки документа.
 */
sealed class OperationStatus {
    object Idle : OperationStatus()
    object Loading : OperationStatus()
    object Success : OperationStatus()
    data class Error(val message: String) : OperationStatus()
}