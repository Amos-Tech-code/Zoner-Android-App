package com.zoner.android.ui.feature.profile

sealed class ProfileEvent {
    data class ShowErrorMessage(val message: String) : ProfileEvent()
}