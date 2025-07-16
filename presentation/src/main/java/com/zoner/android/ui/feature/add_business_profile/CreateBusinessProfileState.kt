package com.zoner.android.ui.feature.add_business_profile

sealed class CreateBusinessProfileState {
    data object Nothing : CreateBusinessProfileState()
    data object Loading : CreateBusinessProfileState()
    data object Success : CreateBusinessProfileState()
    data object Error : CreateBusinessProfileState()
}