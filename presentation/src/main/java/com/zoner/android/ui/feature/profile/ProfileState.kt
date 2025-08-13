package com.zoner.android.ui.feature.profile

sealed class UIProfileState {
    data object Nothing : UIProfileState()
    data object Loading : UIProfileState()
    data object Success : UIProfileState()
    data class Error(val message: String) : UIProfileState()
}