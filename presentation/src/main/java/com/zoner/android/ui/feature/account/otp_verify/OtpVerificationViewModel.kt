package com.zoner.android.ui.feature.account.otp_verify

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.util.REQUEST_NEW_OTP_COUNTDOWN
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.repository.AccountRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OtpVerificationViewModel(
    private val repository: AccountRepository,
    private val session: ZonerSession,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_FINISH_TIME = "otp_finish_time"
    }
    private var _state = MutableStateFlow(OtpVerificationUIState())
    val state = _state.asStateFlow()

    // Events Channel
    private val _event = Channel<OtpVerificationEvent>()
    val event = _event.receiveAsFlow()

    private var userId: String? = null

    init {
        startResendCountdown()
    }
    fun initUserId(navUserId: String?) {
        viewModelScope.launch {
            userId = navUserId ?: session.getUserId()

            if (userId == null) {
                _event.send(OtpVerificationEvent.NavigateToSignUp)
            }
        }
    }
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
                    OtpVerificationEvent.ShowSnackBar("Please enter a valid 4-digit code")
                )
            }
            return
        }
        val safeUserId = userId
        if (safeUserId == null) {
            viewModelScope.launch { _event.send(OtpVerificationEvent.NavigateToSignUp) }
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val result = repository.verify(safeUserId, _state.value.otp)
                when (result) {
                    is ResultWrapper.Failure -> {
                        _state.value = _state.value.copy(isLoading = false)
                        _event.send(
                            OtpVerificationEvent.ShowErrorDialog(result.exception.message ?: "An unknown error occurred. Please try again.")
                        )
                    }
                    is ResultWrapper.Success -> {
                        val registrationStage = result.value.data?.currentStage
                        session.saveUserSession(
                            userId = result.value.data?.userId,
                            stage = when {
                                registrationStage == "EMAIL_SUBMITTED" -> RegistrationStage.EMAIL_SUBMITTED
                                registrationStage == "EMAIL_VERIFIED" -> RegistrationStage.EMAIL_VERIFIED
                                registrationStage == "PROFILE_COMPLETED" -> RegistrationStage.PROFILE_COMPLETED
                                else -> null
                            }
                        )
                        _state.value = _state.value.copy(isLoading = false)
                        _event.send(OtpVerificationEvent.NavigateToCompleteProfile(result.value.data?.userId))
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _event.send(
                    OtpVerificationEvent.ShowErrorDialog("An unknown error occurred. Please try again")
                )
            }
        }
    }

    // Resend OTP
    fun resendOtp() {
        val safeUserId = userId
        if (safeUserId == null) {
            viewModelScope.launch { _event.send(OtpVerificationEvent.NavigateToSignUp) }
            return
        }

        _state.value = _state.value.copy(isResending = true)
        viewModelScope.launch {
            try {
                val result = repository.requestNewOTP(safeUserId)
                when (result) {
                    is ResultWrapper.Failure -> {
                        _state.value = _state.value.copy(isResending = false)
                        _event.send(OtpVerificationEvent.ShowSnackBar(result.exception.message ?: "Failed to resend code. Please try again."))
                    }
                    is ResultWrapper.Success -> {
                        _state.value = _state.value.copy(isResending = false)
                        // Always reset finish time on successful resend
                        val finishTime = System.currentTimeMillis() + REQUEST_NEW_OTP_COUNTDOWN
                        savedStateHandle[KEY_FINISH_TIME] = finishTime
                        startResendCountdown()
                        _event.send(OtpVerificationEvent.ShowSnackBar(result.value.message))
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isResending = false)
                _event.send(OtpVerificationEvent.ShowSnackBar("Failed to resend code. Please try again."))
            }
        }
    }

    // Start countdown timer for resend
    private fun startResendCountdown() {
        val now = System.currentTimeMillis()

        // Check if finishTime already saved
        val finishTime = savedStateHandle.get<Long>(KEY_FINISH_TIME) ?: (now + REQUEST_NEW_OTP_COUNTDOWN)
        savedStateHandle[KEY_FINISH_TIME] = finishTime

        viewModelScope.launch {
            while (true) {
                val remaining = (finishTime - System.currentTimeMillis()).coerceAtLeast(0)

                _state.value = _state.value.copy(resendCountdown = remaining)

                if (remaining <= 0) break
                delay(1000)
            }
        }

    }

}

// Data class representing the UI state
data class OtpVerificationUIState(
    val otp: String = "",
    val focusedIndex: Int = 0,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendCountdown: Long = 0,
    val showNumpad: Boolean = true // Show number pad by default
)
