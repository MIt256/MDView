package com.ex.mdview.presentation.viewmodel

sealed class LoadStatus {
    object Idle : LoadStatus()
    object Loading : LoadStatus()
    object Success : LoadStatus()
    data class Error(val message: String) : LoadStatus()
}