package com.zoner.android.ui.feature.country_selection

import com.zoner.domain.model.CountryModel

sealed class CountrySelectionEvent {
    data class ShowErrorMessage(val message: String) : CountrySelectionEvent()

    data class OnCountrySelected(val country: CountryModel) : CountrySelectionEvent()
}