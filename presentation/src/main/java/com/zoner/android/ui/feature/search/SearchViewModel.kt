package com.zoner.android.ui.feature.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

class SearchViewModel : ViewModel() {

    private val _state = MutableStateFlow<SearchState>(SearchState.Loading)
    val state: StateFlow<SearchState> = _state

    private val _event = MutableSharedFlow<SearchEvent>()
    val event = _event.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedLocation = MutableStateFlow("Nairobi")
    val selectedLocation: StateFlow<String> = _selectedLocation

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedLocation(query: String) {
        _selectedLocation.value = query
    }

    fun fetchData() {

    }



}