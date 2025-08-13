package com.zoner.android.ui.feature.account.sign_up

import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.account.BaseAuthViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel : BaseAuthViewModel() {

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Nothing)
    val state: StateFlow<SignUpState> = _state

    private val _event = Channel<SignUpEvent>()
    val event = _event.receiveAsFlow()

    private val _isGoogleSignIn = MutableStateFlow(false)
    val isGoogleSignIn = _isGoogleSignIn.asStateFlow()

    private val _formState = MutableStateFlow(SignUpFormState())
    val formState = _formState.asStateFlow()

    fun onFullNameUpdated(value: String) {
        _formState.update { it.copy(fullName = value) }
    }

    fun onEmailUpdated(value: String) {
        _formState.update { it.copy(email = value) }
    }

    fun onPhoneNumberUpdated(value: String) {
        _formState.update { it.copy(phoneNumber = value) }
    }
    fun onPasswordUpdated(value: String) {
        _formState.update { it.copy(password = value) }
    }
    fun onCPasswordUpdated(value: String) {
        _formState.update { it.copy(confirmPassword = value) }
    }

    fun register() {
        viewModelScope.launch {
            _state.value = SignUpState.Loading
            delay(3000)
            _state.value = SignUpState.Success
            _event.send(SignUpEvent.NavigateToVerification)
        }
    }

    fun onSignInClicked() {
        viewModelScope.launch {
            _event.send(SignUpEvent.NavigateToSignIn)
        }
    }

    data class SignUpFormState(
        val fullName: String = "",
        val email: String = "",
        val phoneNumber: String = "",
        val password: String = "",
        val confirmPassword: String = ""
    )

    override fun loading() {
        _isGoogleSignIn.value = true
    }

    override fun onGoogleError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Google Sign In Failed"
            _isGoogleSignIn.value = false
            _event.send(SignUpEvent.ShowErrorMessage(errorDescription))
        }
    }

    override fun onGoogleLoginSuccess(token: String) {
        viewModelScope.launch {
            TODO()
        }
    }

}

