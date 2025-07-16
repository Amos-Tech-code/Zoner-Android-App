package com.zoner.android.ui.feature.search

sealed class SearchEvent {
    data class ShowErrorMessage(val message: String) : SearchEvent()
}