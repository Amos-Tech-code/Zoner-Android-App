package com.zoner.android.ui.feature.account.sign_in

sealed class SignInEvent {

    data class ShowErrorDialog(val message: String) : SignInEvent()

    data object ShowOauthErrorDialog : SignInEvent()

    data class ShowSnackBar(val message: String) : SignInEvent()

    data object NavigateToSignUp : SignInEvent()

    data object NavigateToResetPassword : SignInEvent()

    data object NavigateToHome : SignInEvent()
}