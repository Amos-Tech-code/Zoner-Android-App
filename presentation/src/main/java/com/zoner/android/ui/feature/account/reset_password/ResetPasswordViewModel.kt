package com.zoner.android.ui.feature.account.reset_password

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.util.REQUEST_NEW_OTP_COUNTDOWN
import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.request.ResetPasswordRequest
import com.zoner.domain.repository.AccountRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val repository: AccountRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_FINISH_TIME = "reset_password_finish_time"
    }
    private val _state = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Nothing)
    val state: StateFlow<ResetPasswordState> = _state

    private val _screenState = MutableStateFlow<ResetPasswordScreenState>(ResetPasswordScreenState.ForgotPassword)
    val screenState: StateFlow<ResetPasswordScreenState> = _screenState

    private val _event = Channel<ResetPasswordEvent>()
    val event = _event.receiveAsFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword = _newPassword.asStateFlow()

    private val _cNewPassword = MutableStateFlow("")
    val cNewPassword = _cNewPassword.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp = _otp.asStateFlow()

    fun onEmailUpdated(value: String) {
        _email.value = value
    }
    fun onOtpUpdated(value: String) {
        _otp.value = value
    }
    fun onNewPasswordUpdated(value: String) {
        _newPassword.value = value
    }
    fun onConfirmNewPasswordUpdated(value: String) {
        _cNewPassword.value = value
    }

    fun forgotPassword() {
        viewModelScope.launch {
        val validationError = validateEmail(email.value)
        if (validationError != null) {
            _event.send(ResetPasswordEvent.ShowSnackBar(validationError))
            return@launch
        }
        _state.value = ResetPasswordState.Loading
            try {
                val result = repository.forgotPassword(_email.value)

                when(result) {
                    is ResultWrapper.Success -> {
                        _state.value = ResetPasswordState.Success
                        _screenState.value = ResetPasswordScreenState.ResetPassword(
                            resendCountdown = (REQUEST_NEW_OTP_COUNTDOWN / 1000L).toInt()
                        )
                        startResendCountdown()
                    }
                    is ResultWrapper.Failure -> {
                        _state.value = ResetPasswordState.Error
                        _event.send(ResetPasswordEvent.ShowErrorDialog(result.exception.message ?: "Unknown error"))
                    }

                }
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error
                _event.send(ResetPasswordEvent.ShowErrorDialog(e.message ?: "Unknown error"))
            }
        }
    }

    fun navigateToForgotPassword() {
        viewModelScope.launch {
            _screenState.value = ResetPasswordScreenState.ForgotPassword
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            _state.value = ResetPasswordState.Loading
            try {
                val result = repository.forgotPassword(_email.value)
                when (result) {
                    is ResultWrapper.Success -> {
                        _state.value = ResetPasswordState.Success
                        _event.send(ResetPasswordEvent.ShowSnackBar("OTP resent successfully"))
                        startResendCountdown()
                    }

                    is ResultWrapper.Failure -> {
                        _state.value = ResetPasswordState.Error
                        _event.send(ResetPasswordEvent.ShowErrorDialog(result.exception.message ?: "Unknown error"))
                    }
                }
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error
                _event.send(ResetPasswordEvent.ShowErrorDialog(e.message ?: "Unknown error"))
            }
        }
    }

    private fun startResendCountdown() {
        viewModelScope.launch {
            val finishTime = System.currentTimeMillis() + REQUEST_NEW_OTP_COUNTDOWN
            while (true) {
                val remaining = (finishTime - System.currentTimeMillis()) / 1000
                if (remaining <= 0) {
                    _screenState.update {
                        if (it is ResetPasswordScreenState.ResetPassword) {
                            it.copy(resendCountdown = 0)
                        } else it
                    }
                    break
                } else {
                    _screenState.update {
                        if (it is ResetPasswordScreenState.ResetPassword) {
                            it.copy(resendCountdown = remaining.toInt())
                        } else it
                    }
                }
                delay(1000)
            }
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            val validationError = validateInputs(email.value, newPassword.value, otp.value)
            if (validationError != null) {
                _state.value = ResetPasswordState.Error
                _event.send(ResetPasswordEvent.ShowSnackBar(validationError))
                return@launch
            }
            _state.value = ResetPasswordState.Loading

            try {
                val result = repository.resetPassword(ResetPasswordRequest(
                    email = email.value,
                    password = newPassword.value,
                    otp = otp.value
                ))
                when (result) {
                    is ResultWrapper.Success -> {
                        _state.value = ResetPasswordState.Success
                        _event.send(ResetPasswordEvent.NavigateToSignIn)
                    }
                    is ResultWrapper.Failure -> {
                        _state.value = ResetPasswordState.Error
                        _event.send(ResetPasswordEvent.ShowErrorDialog(result.exception.message ?: "Unknown error"))
                    }
                }
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error
                _event.send(ResetPasswordEvent.ShowErrorDialog(e.message ?: "Unknown error"))
            }
        }
    }

    private fun validateInputs(email: String, password: String, otp: String): String? {
        if (email.isBlank()) {
            return "Email cannot be empty"
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email address"
        }
        if (password.isBlank()) {
            return "Password cannot be empty"
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters long"
        }
        if (otp.isBlank()) {
            return "OTP cannot be empty"
        }
        if (otp.length != 6) {
            return "Invalid OTP"
        }
        return null
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "Email cannot be empty"
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email address"
        }
        return null
    }

}