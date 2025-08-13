package com.zoner.android.ui.feature.account.reset_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {

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
        _state.value = ResetPasswordState.Loading
        viewModelScope.launch {
            delay(1000)
            _screenState.value = ResetPasswordScreenState.ResetPassword
        }
        _state.value = ResetPasswordState.Nothing
    }

    fun navigateToForgotPassword() {
        viewModelScope.launch {
            _screenState.value = ResetPasswordScreenState.ForgotPassword
        }
    }

    fun resendOtp() {

    }

    fun resetPassword() {
        _state.value = ResetPasswordState.Loading
        viewModelScope.launch {
            delay(1000)
            _event.send(ResetPasswordEvent.NavigateToSignIn)
        }
        _state.value = ResetPasswordState.Nothing
    }

}