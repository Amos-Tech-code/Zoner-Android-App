package com.zoner.android.ui.feature.add_business_profile

sealed class CreateBusinessProfileEvent {
    data class ShowErrorMessage(val message: String) : CreateBusinessProfileEvent()

    data object NavigateToCountrySelection : CreateBusinessProfileEvent()
}