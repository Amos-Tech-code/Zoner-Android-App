package com.zoner.android.ui.feature.account.reset_password

sealed class ResetPasswordState {
    data object Nothing : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

sealed class ResetPasswordScreenState {
    data object ForgotPassword : ResetPasswordScreenState()

    data object ResetPassword : ResetPasswordScreenState()
}