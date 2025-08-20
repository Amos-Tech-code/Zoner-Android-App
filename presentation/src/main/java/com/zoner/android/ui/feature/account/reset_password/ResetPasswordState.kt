package com.zoner.android.ui.feature.account.reset_password

sealed class ResetPasswordState {
    data object Nothing : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Success : ResetPasswordState()
    data object Error : ResetPasswordState()
}

sealed class ResetPasswordScreenState {
    data object ForgotPassword : ResetPasswordScreenState()
    data class ResetPassword(
        val resendCountdown: Int = 0,
        val isResending: Boolean = false
    ) : ResetPasswordScreenState()
}