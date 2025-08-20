package com.zoner.android.ui.feature.add_business_profile

import com.zoner.domain.model.CountryModel

data class FormState(
    val businessName: String = "",
    val category: String = "",
    val location: String = "",
    val selectedCountry: CountryModel = CountryModel(name = "Kenya", code = "KE", emoji = "🇰🇪", dialCode = "+254"),
    val phoneNumber: String = "",
    val description: String = "",
    val isTermsAccepted: Boolean = false,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false
)