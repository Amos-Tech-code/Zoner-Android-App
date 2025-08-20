package com.zoner.android.ui.feature.account.sign_up

import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.account.BaseAuthViewModel
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.model.request.RegisterRequest
import com.zoner.domain.repository.AccountRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val repository: AccountRepository,
    private val session: ZonerSession
) : BaseAuthViewModel() {

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
            val validationError = validateInputs(_formState.value.fullName, _formState.value.email, _formState.value.password)
            if (validationError != null) {
                _event.send(SignUpEvent.ShowSnackBar(validationError))
                return@launch
            }
            _state.value = SignUpState.Loading

            try {
                val result = repository.register(RegisterRequest(
                    name = _formState.value.fullName,
                    email = _formState.value.email,
                    password = _formState.value.password
                ))

                when(result) {
                    is ResultWrapper.Failure -> {
                        _state.value = SignUpState.Error
                        _event.send(SignUpEvent.ShowErrorDialog(result.exception.message ?: "An unknown error occurred. Please try again."))
                    }
                    is ResultWrapper.Success -> {
                        val trackRegistrationStage = result.value.data?.currentStage
                        val registrationStage = when {
                            trackRegistrationStage == "EMAIL_SUBMITTED" -> RegistrationStage.EMAIL_SUBMITTED
                            trackRegistrationStage == "EMAIL_VERIFIED" -> RegistrationStage.EMAIL_VERIFIED
                            trackRegistrationStage == "PROFILE_COMPLETED" -> RegistrationStage.PROFILE_COMPLETED
                            else -> null
                        }

                        when (registrationStage) {
                            RegistrationStage.EMAIL_SUBMITTED -> {
                                _state.value = SignUpState.Success
                                session.saveUserSession(
                                    userId = result.value.data?.userId,
                                    stage = RegistrationStage.EMAIL_SUBMITTED
                                )
                                _event.send(SignUpEvent.NavigateToVerification(result.value.data?.userId))
                            }
                            RegistrationStage.EMAIL_VERIFIED -> {
                                _state.value = SignUpState.Success
                                session.saveUserSession(
                                    userId = result.value.data?.userId,
                                    stage = RegistrationStage.EMAIL_VERIFIED
                                )
                                _event.send(SignUpEvent.NavigateToCompleteProfile(result.value.data?.userId))
                            }
                            RegistrationStage.PROFILE_COMPLETED -> {
                                _state.value = SignUpState.Success
                                session.saveUserSession(
                                    userId = result.value.data?.userId,
                                    stage = RegistrationStage.PROFILE_COMPLETED
                                )
                                _event.send(SignUpEvent.ShowErrorDialog(result.value.message))
                            }
                            null -> {
                                _state.value = SignUpState.Success
                                _event.send(SignUpEvent.ShowErrorDialog(result.value.message))
                            }
                        }

                    }
                }
            } catch (e: Exception) {
                _state.value = SignUpState.Error
                _event.send(SignUpEvent.ShowErrorDialog("An unknown error occurred. Please try again."))
            }


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
            _event.send(SignUpEvent.ShowOauthErrorDialog)
        }
    }

    override fun onGoogleLoginSuccess(token: String) {
        viewModelScope.launch {
            _isGoogleSignIn.value = true
            try {
                val result = repository.googleSignUp(token)

                when(result) {
                    is ResultWrapper.Failure -> {
                        _isGoogleSignIn.value = false
                        error = "Google Sign Up Failed"
                        errorDescription = result.exception.message ?: "An unknown error occurred. Please try again."
                        _event.send(SignUpEvent.ShowErrorDialog(result.exception.message ?: "An unknown error occurred. Please try again."))
                    }
                    is ResultWrapper.Success -> {
                        val trackRegistrationStage = result.value.data?.currentStage
                        val registrationStage = when {
                            trackRegistrationStage == "EMAIL_SUBMITTED" -> RegistrationStage.EMAIL_SUBMITTED
                            trackRegistrationStage == "EMAIL_VERIFIED" -> RegistrationStage.EMAIL_VERIFIED
                            trackRegistrationStage == "PROFILE_COMPLETED" -> RegistrationStage.PROFILE_COMPLETED
                            else -> null
                        }
                        when (registrationStage) {
                            RegistrationStage.EMAIL_SUBMITTED -> {
                                _state.value = SignUpState.Success
                                session.saveUserSession(
                                    userId = result.value.data?.userId,
                                    stage = RegistrationStage.EMAIL_SUBMITTED
                                )
                                _event.send(SignUpEvent.NavigateToVerification(result.value.data?.userId))
                            }
                            RegistrationStage.EMAIL_VERIFIED -> {
                                _state.value = SignUpState.Success
                                session.saveUserSession(
                                    userId = result.value.data?.userId,
                                    stage = RegistrationStage.EMAIL_VERIFIED
                                )
                                _event.send(SignUpEvent.NavigateToCompleteProfile(result.value.data?.userId))
                            }
                            RegistrationStage.PROFILE_COMPLETED -> {
                                _state.value = SignUpState.Success
                                session.saveUserSession(
                                    userId = result.value.data?.userId,
                                    stage = RegistrationStage.PROFILE_COMPLETED
                                )
                                _event.send(SignUpEvent.ShowErrorDialog(result.value.message))
                            }
                            null -> {
                                _state.value = SignUpState.Success
                                _event.send(SignUpEvent.ShowErrorDialog(result.value.message))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _isGoogleSignIn.value = false
                error = "Google Sign Up Failed"
                errorDescription = "An unknown error occurred. Please try again."
                _event.send(SignUpEvent.ShowOauthErrorDialog)
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String): String? {
        if (name.isBlank()) {
            return "Name cannot be empty"
        }
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

