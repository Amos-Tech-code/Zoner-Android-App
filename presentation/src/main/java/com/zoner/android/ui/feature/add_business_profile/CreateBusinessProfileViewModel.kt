package com.zoner.android.ui.feature.add_business_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.domain.model.CountryModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateBusinessProfileViewModel : ViewModel() {

    private val _state =
        MutableStateFlow<CreateBusinessProfileState>(CreateBusinessProfileState.Nothing)
    val state: StateFlow<CreateBusinessProfileState> = _state

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

    fun createBusinessAccount() {
    }

    fun onCountrySelectionClicked() {
        viewModelScope.launch {
            _event.send(CreateBusinessProfileEvent.NavigateToCountrySelection)
        }
    }

    data class FormState(
        val businessName: String = "",
        val category: String = "",
        val location: String = "",
        val selectedCountry: CountryModel = CountryModel(name = "Kenya", code = "KE", emoji = "🇰🇪", dialCode = "+254"),
        val phoneNumber: String = "",
        val description: String = "",
        val isTermsAccepted: Boolean = false,
    )

}