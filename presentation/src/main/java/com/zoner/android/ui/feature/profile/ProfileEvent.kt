package com.zoner.android.ui.feature.profile

sealed class ProfileEvent {
    data class ShowErrorMessage(val message: String) : ProfileEvent()

    data class ShowSuccessMessage(val message: String) : ProfileEvent()
}