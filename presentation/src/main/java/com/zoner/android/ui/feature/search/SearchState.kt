package com.zoner.android.ui.feature.search

sealed class SearchState {
    data object Nothing : SearchState()
    data object Loading : SearchState()
    data object Success : SearchState()
    data class Error(val message: String) : SearchState()
}