package com.zoner.android.ui.feature.country_selection

sealed class CountrySelectionState {
    data object Loading : CountrySelectionState()
    data object Success : CountrySelectionState()
    data class Error(val message: String) : CountrySelectionState()
}