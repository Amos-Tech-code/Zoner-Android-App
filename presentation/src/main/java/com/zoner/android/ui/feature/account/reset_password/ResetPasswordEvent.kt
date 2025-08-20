package com.zoner.android.ui.feature.account.reset_password

sealed class ResetPasswordEvent {
    data class ShowErrorDialog(val message: String) : ResetPasswordEvent()

    data class ShowSnackBar(val message: String) : ResetPasswordEvent()
    data object NavigateToSignIn : ResetPasswordEvent()

}