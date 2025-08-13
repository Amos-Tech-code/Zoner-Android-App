package com.zoner.android.ui.feature.account.sign_in

import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.account.BaseAuthViewModel
import com.zoner.domain.model.CountryModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignInViewModel : BaseAuthViewModel() {

    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state: StateFlow<SignInState> = _state

    private val _event = Channel<SignInEvent>()
    val event = _event.receiveAsFlow()

    private val _isGoogleSignIn = MutableStateFlow(false)
    val isGoogleSignIn = _isGoogleSignIn.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

//    private val _selectedCountry = MutableStateFlow(
//        CountryModel(
//            name = "Kenya",
//            code = "KE",
//            emoji = "🇰🇪",
//            dialCode = "+254"
//        )
//    )
//    val selectedCountry = _selectedCountry.asStateFlow()

    fun onEmailUpdated(value: String) {
        _email.value = value
    }
    fun onPasswordUpdated(value: String) {
        _password.value = value
    }

//    fun onSelectedCountryUpdated(country: CountryModel) {
//        _selectedCountry.value = country
//    }

    fun signIn() {
        viewModelScope.launch {
            _state.value = SignInState.Loading
            delay(1000)
            _state.value = SignInState.Success
            _event.send(SignInEvent.NavigateToHome)
        }
    }

//    fun onCountrySelectionClicked() {
//        viewModelScope.launch {
//            _event.send(SignInEvent.NavigateToCountrySelection)
//        }
//    }

    fun onSignUpClicked() {
        viewModelScope.launch {
            _event.send(SignInEvent.NavigateToSignUp)
        }
    }

    fun onForgotPasswordClicked() {
        viewModelScope.launch {
            _event.send(SignInEvent.NavigateToResetPassword)
        }
    }

    override fun loading() {
        viewModelScope.launch {
            _isGoogleSignIn.value = true
        }
    }

    override fun onGoogleError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Google Sign In Failed"
            _isGoogleSignIn.value = false
            _event.send(SignInEvent.ShowErrorMessage(errorDescription))
        }
    }

    override fun onGoogleLoginSuccess(token: String) {
        viewModelScope.launch {
            TODO()
        }
    }

}