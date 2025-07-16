package com.zoner.android.ui.feature.account.otp_verify

sealed class OtpVerificationState {
    data object Nothing : OtpVerificationState()
    data object Loading : OtpVerificationState()
    data object Success : OtpVerificationState()
    data class Error(val message: String) : OtpVerificationState()
}