package com.zoner.android.ui.feature.account.otp_verify

sealed class OtpVerificationEvent {
    data class ShowErrorDialog(val message: String) : OtpVerificationEvent()

    data class ShowSnackBar(val message: String) : OtpVerificationEvent()

    data class NavigateToCompleteProfile(val userId: String? = null) : OtpVerificationEvent()

    data object NavigateToSignUp : OtpVerificationEvent()
}