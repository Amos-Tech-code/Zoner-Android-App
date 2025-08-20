package com.zoner.android.ui.feature.add_business_profile

sealed class CreateBusinessProfileEvent {
    data class ShowErrorDialog(val message: String) : CreateBusinessProfileEvent()

    data class ShowSnackBar(val message: String) : CreateBusinessProfileEvent()

    data object ShowSuccessDialog : CreateBusinessProfileEvent()
    data object NavigateToCountrySelection : CreateBusinessProfileEvent()
}