package com.zoner.android.ui.feature.add_business_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.CountryModel
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.model.UserRole
import com.zoner.domain.model.request.CreateBusinessProfile
import com.zoner.domain.repository.BusinessProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateBusinessProfileViewModel(
    private val repository: BusinessProfileRepository,
    private val session: ZonerSession
) : ViewModel() {

    private val _formState = MutableStateFlow(FormState())
    val formState = _formState.asStateFlow()

    private val _event = Channel<CreateBusinessProfileEvent>()
    val event = _event.receiveAsFlow()

    fun onBusinessNameChange(newValue: String) {
        _formState.update { it.copy(businessName = newValue) }
    }

    fun onCategoryChange(newValue: String) {
        _formState.update { it.copy(category = newValue) }
    }

    fun onLocationChange(newValue: String) {
        _formState.update { it.copy(location = newValue) }
    }

    fun onCountrySelected(newCountry: CountryModel) {
        _formState.update { it.copy(selectedCountry = newCountry) }
    }

    fun onPhoneNumberChange(newValue: String) {
        _formState.update { it.copy(phoneNumber = newValue) }
    }

    fun onDescriptionChange(newValue: String) {
        _formState.update { it.copy(description = newValue) }
    }

    fun onTermsAcceptedChange() {
        _formState.update { it.copy(isTermsAccepted = !it.isTermsAccepted) }
    }

    fun onCountrySelectionClicked() {
        viewModelScope.launch {
            _event.send(CreateBusinessProfileEvent.NavigateToCountrySelection)
        }
    }

    fun createBusinessAccount() {
        viewModelScope.launch {
            val errorMessage = validateForm()
            if (errorMessage != null) {
                _event.send(CreateBusinessProfileEvent.ShowSnackBar(errorMessage))
                return@launch
            }
            _formState.update { it.copy(isLoading = true) }

            try {
                val normalizedPhoneNumber = normalizePhoneNumber(
                    _formState.value.selectedCountry.dialCode,
                    _formState.value.phoneNumber
                )
                val result = repository.createBusinessProfile(
                    CreateBusinessProfile(
                        businessName = formState.value.businessName,
                        category = formState.value.category,
                        location = formState.value.location,
                        country = formState.value.selectedCountry.name,
                        phoneNumber = normalizedPhoneNumber,
                        description = formState.value.description,
                        isTermsAccepted = formState.value.isTermsAccepted
                    )
                )
                when (result) {
                    is ResultWrapper.Success -> {
                        _formState.update { it.copy(isLoading = false) }
                        session.saveUserSession(
                            token = result.value.token,
                            userId = result.value.user.id,
                            name = result.value.user.name,
                            email = result.value.user.email,
                            username = result.value.user.username,
                            imgUrl = result.value.user.profilePicUrl,
                            role = UserRole.BUSINESS,
                            stage = RegistrationStage.PROFILE_COMPLETED,
                            businessName = result.value.user.businessProfile?.businessName,
                            businessLogo = result.value.user.businessProfile?.businessLogo,
                            isBusinessVerified = result.value.user.businessProfile?.isVerified
                        )
                        _event.send(CreateBusinessProfileEvent.ShowSuccessDialog)
                    }

                    is ResultWrapper.Failure -> {
                        _formState.update { it.copy(isLoading = false) }
                        _event.send(
                            CreateBusinessProfileEvent.ShowErrorDialog(
                                result.exception.message
                                    ?: "An unknown error occurred. Please try again."
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _formState.update { it.copy(isLoading = false) }
                _event.send(CreateBusinessProfileEvent.ShowErrorDialog("An unknown error occurred. Please try again."))
            }
        }

    }

    private fun validateForm(): String? {
        val state = _formState.value

        return when {
            state.businessName.isBlank() -> "Business name is required"
            state.category.isBlank() -> "Category is required"
            state.location.isBlank() -> "Location is required"
            state.phoneNumber.isBlank() -> "Phone number is required"
            state.phoneNumber.length < 7 -> "Phone number is too short"
            state.description.isBlank() -> "Description is required"
            !state.isTermsAccepted -> "You must accept the terms and conditions"
            else -> null // valid
        }
    }

    private fun normalizePhoneNumber(
        dialCode: String,
        rawNumber: String
    ): String {
        // remove spaces, hyphens, parentheses, etc.
        var cleaned = rawNumber.replace("[^\\d+]".toRegex(), "")

        // if number starts with leading "0", drop it
        if (cleaned.startsWith("0")) {
            cleaned = cleaned.drop(1)
        }

        // ensure number does not already include the dialCode
        return if (cleaned.startsWith(dialCode.removePrefix("+"))) {
            "+$cleaned"
        } else {
            dialCode + cleaned
        }
    }


}