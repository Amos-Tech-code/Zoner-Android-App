package com.zoner.android.ui.feature.profile

sealed class ProfileState {
    data object Nothing : ProfileState()
    data object Loading : ProfileState()
    data object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}