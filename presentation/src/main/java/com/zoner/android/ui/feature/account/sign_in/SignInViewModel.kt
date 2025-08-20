package com.zoner.android.ui.feature.account.sign_in

import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.account.BaseAuthViewModel
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.model.UserRole
import com.zoner.domain.model.request.LoginRequest
import com.zoner.domain.repository.AccountRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val repository: AccountRepository,
    private val session: ZonerSession
) : BaseAuthViewModel() {

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

    fun onEmailUpdated(value: String) {
        _email.value = value
    }
    fun onPasswordUpdated(value: String) {
        _password.value = value
    }

    fun signIn() {

        viewModelScope.launch {
            val validationError = validateInputs(email.value, password.value)
            if (validationError != null) {
                _event.send(SignInEvent.ShowSnackBar(validationError))
                return@launch
            }

            _state.value = SignInState.Loading

            try {
                val result = repository.login(
                    LoginRequest(email.value, password.value)
                )

                when(result) {
                    is ResultWrapper.Failure -> {
                        _state.value = SignInState.Error
                        _event.send(SignInEvent.ShowErrorDialog(result.exception.message ?: "An unknown error occurred. Please try again."))
                    }
                    is ResultWrapper.Success -> {
                        val registrationStage = when {
                            result.value.user.registrationStage == "EMAIL_SUBMITTED" -> RegistrationStage.EMAIL_SUBMITTED
                            result.value.user.registrationStage == "EMAIL_VERIFIED" -> RegistrationStage.EMAIL_VERIFIED
                            result.value.user.registrationStage == "PROFILE_COMPLETED" -> RegistrationStage.PROFILE_COMPLETED
                            else -> null
                        }
                        val userRole = when {
                            result.value.user.role == "USER" -> UserRole.USER
                            result.value.user.role == "BUSINESS" -> UserRole.BUSINESS
                            else -> null
                        }

                        session.saveUserSession(
                            token = result.value.token,
                            userId = result.value.user.id,
                            name = result.value.user.name,
                            username = result.value.user.username,
                            email = result.value.user.email,
                            imgUrl = result.value.user.profilePicUrl,
                            role = userRole,
                            stage = registrationStage

                        )
                        _state.value = SignInState.Success
                        _event.send(SignInEvent.NavigateToHome)
                    }
                }
            } catch (e: Exception) {
                _state.value = SignInState.Error
                _event.send(SignInEvent.ShowErrorDialog("An unknown error occurred. Please try again."))
            }

        }
    }

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
            _event.send(SignInEvent.ShowOauthErrorDialog)
        }
    }

    override fun onGoogleLoginSuccess(token: String) {
        viewModelScope.launch {
            _isGoogleSignIn.value = true

            try {
                val result = repository.googleSignIn(token)
                when(result) {
                    is ResultWrapper.Failure -> {
                        _isGoogleSignIn.value = false
                        errorDescription = result.exception.message ?: "An unknown error occurred. Please try again."
                        error = "Google Sign In Failed"
                        _event.send(SignInEvent.ShowOauthErrorDialog)
                    }
                    is ResultWrapper.Success -> {
                        val registrationStage = when {
                            result.value.user.registrationStage == "EMAIL_SUBMITTED" -> RegistrationStage.EMAIL_SUBMITTED
                            result.value.user.registrationStage == "EMAIL_VERIFIED" -> RegistrationStage.EMAIL_VERIFIED
                            result.value.user.registrationStage == "PROFILE_COMPLETED" -> RegistrationStage.PROFILE_COMPLETED
                            else -> null
                        }
                        val userRole = when {
                            result.value.user.role == "USER" -> UserRole.USER
                            result.value.user.role == "BUSINESS" -> UserRole.BUSINESS
                            else -> null
                        }
                        session.saveUserSession(
                            token = result.value.token,
                            userId = result.value.user.id,
                            name = result.value.user.name,
                            username = result.value.user.username,
                            email = result.value.user.email,
                            imgUrl = result.value.user.profilePicUrl,
                            role = userRole,
                            stage = registrationStage
                        )
                        _isGoogleSignIn.value = false
                        _event.send(SignInEvent.NavigateToHome)
                    }
                }

            } catch (e: Exception) {
                _isGoogleSignIn.value = false
                errorDescription = "An unknown error occurred. Please try again."
                error = "Google Sign In Failed"
                _event.send(SignInEvent.ShowOauthErrorDialog)
            }
        }
    }

    /**
     * Validates email and password inputs.
     * @return error message if invalid, null if valid.
     */
    private fun validateInputs(email: String, password: String): String? {
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
        return null
    }

}