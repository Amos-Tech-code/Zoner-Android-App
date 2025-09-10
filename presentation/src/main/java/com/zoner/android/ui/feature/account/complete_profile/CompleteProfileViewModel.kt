package com.zoner.android.ui.feature.account.complete_profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.model.UserRole
import com.zoner.domain.model.request.CompleteProfileRequest
import com.zoner.domain.repository.AccountRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompleteProfileViewModel (
    private val repository: AccountRepository,
    private val session: ZonerSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    private val _event = Channel<CompleteProfileEvent>()
    val event = _event.receiveAsFlow()
    private val _username = MutableStateFlow("")
    private var userId: String? = null

    fun initUserId(navUserId: String?) {
        viewModelScope.launch {
            userId = navUserId ?: session.getUserId()

            if (userId == null) {
                _event.send(CompleteProfileEvent.NavigateToSignUp)
            }
        }
    }

    init {
        // Setup username validation pipeline
        _username
            .debounce(300) // Wait 300ms after last keystroke
            .filter { username ->
                // Only proceed if username meets requirements
                username.length >= 3
            }
            .distinctUntilChanged() // Only proceed if username actually changed
            .onEach { username ->
                val safeUserId = userId
                if (safeUserId == null) {
                    // Don’t even try validating until userId is set
                    return@onEach
                }
                _uiState.update { it.copy(isCheckingUsername = true) }
                try {

                    val result = repository.validateUserName(safeUserId, username)

                    when(result) {
                        is ResultWrapper.Failure -> {
                            _uiState.update { it.copy(
                                isCheckingUsername = false,
                                error = "Failed to check username availability"
                            ) }
                        }
                        is ResultWrapper.Success -> {
                            _uiState.update {
                                it.copy(
                                    isCheckingUsername = false,
                                    isUsernameAvailable = result.value.isAvailable,
                                    usernameSuggestions = result.value.suggestions,
                                    isUsernameValid = result.value.isAvailable,
                                    username = username // Update the username in state
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(
                            isCheckingUsername = false,
                            error = "Failed to check username availability"
                        )
                    }
                }
            }
            .launchIn(viewModelScope) // Start in ViewModel's scope
    }
    fun onUsernameChanged(username: String) {
        _username.value = username // Emit to the Flow
        _uiState.update { it.copy(username = username) } // Update UI state immediately
    }

    // Rest of the ViewModel remains the same...
    fun onProfilePictureSelected(uri: Uri) {
        _uiState.update { it.copy(profilePictureUri = uri) }
    }

    fun completeProfile() {
        val safeUserId = userId
        if (safeUserId == null) {
            viewModelScope.launch { _event.send(CompleteProfileEvent.NavigateToSignUp) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = repository.completeProfile(
                    CompleteProfileRequest(
                        userId = safeUserId,
                        username = _uiState.value.username,
                        profilePicture = _uiState.value.profilePictureUri
                    )
                )
                when (result) {
                    is ResultWrapper.Failure -> {
                        _uiState.update {it.copy( isLoading = false )}
                        _event.send(CompleteProfileEvent.ShowErrorDialog(result.exception.message ?: "An unknown error occurred. Please try again."))
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
                        _uiState.update { it.copy(isComplete = true, isLoading = false) }
                        _event.send(CompleteProfileEvent.NavigateToHome)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy( isLoading = false) }
                _event.send(CompleteProfileEvent.ShowErrorDialog("An unknown error occurred. Please try again."))

            }
        }
    }

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }
}


data class CompleteProfileUiState(
    val username: String = "",
    val isCheckingUsername: Boolean = false,
    val isUsernameAvailable: Boolean? = null,
    val usernameSuggestions: List<String> = emptyList(),
    val isUsernameValid: Boolean = false,
    val profilePictureUri: Uri? = null,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)