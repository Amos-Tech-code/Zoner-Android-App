package com.zoner.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.navigation.HomeRoute
import com.zoner.android.navigation.NavRoutes
import com.zoner.android.navigation.OnboardingRoute
import com.zoner.android.navigation.SignInRoute
import com.zoner.data.local.datastore.ZonerSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    zonerSession: ZonerSession
) : ViewModel() {

    private val _startDestination = MutableStateFlow<NavRoutes?>(null)
    val startDestination: StateFlow<NavRoutes?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val onboardingCompleted = async { zonerSession.onboardingCompletedFlow().first() }
            val token = async { zonerSession.getToken() }

            _startDestination.value = when {
                !onboardingCompleted.await() -> OnboardingRoute
                token.await() != null -> HomeRoute
                else -> SignInRoute
            }
        }
    }

}