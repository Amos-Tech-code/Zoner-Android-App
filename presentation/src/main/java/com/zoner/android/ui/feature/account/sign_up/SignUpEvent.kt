package com.zoner.android.ui.feature.account.sign_up

sealed class SignUpEvent {

    data class ShowErrorMessage(val message: String) : SignUpEvent()

    data object NavigateToSignIn : SignUpEvent()

}