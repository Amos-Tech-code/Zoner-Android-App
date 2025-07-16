package com.zoner.android.ui.feature.account.otp_verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OtpVerificationViewModel : ViewModel() {

    private var _state = MutableStateFlow(OtpVerificationUIState())
    val state = _state.asStateFlow()

    // Events Channel
    private val _event = Channel<OtpVerificationEvent>()
    val event = _event.receiveAsFlow()

    fun handleDigitFocus(index: Int) {
        _state.value = _state.value.copy(focusedIndex = index)
    }

    // Handle OTP digit changes
    fun onOtpChange(index: Int, value: String) {
        val newOtp = _state.value.otp.toCharArray()
        if (index < newOtp.size) {
            if (value.isEmpty()) {
                newOtp[index] = ' '
            } else {
                newOtp[index] = value[0]
            }
            _state.value = _state.value.copy(otp = String(newOtp).trim())
        } else if (value.isNotEmpty() && index < 4) {
            _state.value = _state.value.copy(otp = _state.value.otp + value[0])
        }
    }

    fun setOtpFromPaste(pasted: String) {
        if (pasted.length == 4 && pasted.all { it.isDigit() }) {
            _state.value = _state.value.copy(otp = pasted)
        }
    }

    // Verify OTP
    fun verifyOtp() {
        if (_state.value.otp.length != 4) {
            viewModelScope.launch {
                _event.send(
                    OtpVerificationEvent.ShowErrorMessage("Please enter a valid 4-digit code")
                )
            }
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            // Simulate network call
            delay(1000)

            // Replace with your actual verification logic
            val isSuccess = verifyOtpWithServer(_state.value.otp)

            if (isSuccess) {
                _state.value = _state.value.copy(isLoading = false)
                _event.send(OtpVerificationEvent.NavigateToHome)
            } else {
                _state.value = _state.value.copy(isLoading = false)
                _event.send(
                    OtpVerificationEvent.ShowErrorMessage("Invalid verification code")
                )
            }
        }
    }

    // Resend OTP
    fun resendOtp() {
        _state.value = _state.value.copy(isResending = true)

        viewModelScope.launch {
            // Simulate network call
            delay(3000)

            // Replace with your actual resend logic
            val isSuccess = resendOtpToPhone()

            if (isSuccess) {
                startResendCountdown()
                _event.send(
                    OtpVerificationEvent.ShowErrorMessage("New code sent successfully")
                )
            } else {
                _event.send(
                    OtpVerificationEvent.ShowErrorMessage("Failed to resend code. Please try again.")
                )
            }
            _state.value = _state.value.copy(isResending = false)
        }
    }

    // Start countdown timer for resend
    private fun startResendCountdown() {
        _state.value = _state.value.copy(resendCountdown = 30)

        viewModelScope.launch {
            while (_state.value.resendCountdown > 0) {
                delay(1000)
                _state.value = _state.value.copy(resendCountdown = _state.value.resendCountdown - 1)
            }
        }
    }

    // Dummy functions - replace with your actual API calls
    private suspend fun verifyOtpWithServer(otp: String): Boolean {
        // Replace with your actual verification API call
        return otp == "1234" // For testing, accepts "1234" as valid OTP
    }

    private suspend fun resendOtpToPhone(): Boolean {
        // Replace with your actual resend OTP API call
        return true
    }
}

// Data class representing the UI state
data class OtpVerificationUIState(
    val otp: String = "",
    val focusedIndex: Int = 0,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendCountdown: Int = 0,
    val showNumpad: Boolean = true // Show number pad by default
)
