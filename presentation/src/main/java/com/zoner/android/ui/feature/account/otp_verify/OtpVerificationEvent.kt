package com.zoner.android.ui.feature.account.otp_verify

sealed class OtpVerificationEvent {
    data class ShowErrorMessage(val message: String) : OtpVerificationEvent()

    data object NavigateToHome : OtpVerificationEvent()
}