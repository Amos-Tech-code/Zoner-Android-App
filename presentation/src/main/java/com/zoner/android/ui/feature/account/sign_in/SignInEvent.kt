package com.zoner.android.ui.feature.account.sign_in

sealed class SignInEvent {

    data class ShowErrorMessage(val message: String) : SignInEvent()

    data object NavigateToSignUp : SignInEvent()

    data object NavigateToResetPassword : SignInEvent()

    data object NavigateToHome : SignInEvent()
}