package com.zoner.android.ui.feature.country_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.domain.model.CountryModel
import com.zoner.domain.repository.CountryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CountrySelectionViewModel(
    private val countryRepository: CountryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CountrySelectionState>(CountrySelectionState.Loading)
    val state: StateFlow<CountrySelectionState> = _state

    private val _event = MutableSharedFlow<CountrySelectionEvent>()
    val event = _event.asSharedFlow()

    private val allCountries = MutableStateFlow<List<CountryModel>>(emptyList()) // all fetched countries

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val countries: StateFlow<List<CountryModel>> = combine(allCountries, _searchQuery) { countries, query ->
        if (query.isBlank()) countries
        else countries.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.dialCode.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    init {
        fetchData()
    }

    private fun fetchData() {
        _state.value = CountrySelectionState.Loading
        viewModelScope.launch {
            val result = countryRepository.getAllCountries()
            if (result.isNotEmpty()) {
                allCountries.value = result
                _state.value = CountrySelectionState.Success
            } else {
                _state.value = CountrySelectionState.Error("Unable to load countries")
                _event.emit(CountrySelectionEvent.ShowErrorMessage("Failed to load country data."))
            }
        }
    }

    fun selectCountry(country: CountryModel) {
        viewModelScope.launch {
            _event.emit(CountrySelectionEvent.OnCountrySelected(country))
        }
    }

}