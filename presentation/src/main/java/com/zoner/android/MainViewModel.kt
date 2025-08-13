package com.zoner.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.navigation.HomeRoute
import com.zoner.android.ui.navigation.NavRoutes
import com.zoner.android.ui.navigation.OnboardingRoute
import com.zoner.android.ui.navigation.SignInRoute
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.network.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val zonerSession: ZonerSession,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _startDestination = MutableStateFlow<NavRoutes?>(null)
    val startDestination: StateFlow<NavRoutes?> = _startDestination.asStateFlow()

    private val _bannerState = MutableStateFlow(ConnectionBannerState.Hidden)
    val bannerState: StateFlow<ConnectionBannerState> = _bannerState.asStateFlow()

//    val isConnected = connectivityObserver
//        .isConnected
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000L),
//            false
//        )

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

        // This handles banner state
        viewModelScope.launch {
            var wasConnected = true
            connectivityObserver.isConnected.collect { connected ->
                if (!connected) {
                    _bannerState.value = ConnectionBannerState.Disconnected
                    wasConnected = false
                } else if (!wasConnected && connected) {
                    _bannerState.value = ConnectionBannerState.BackOnline
                    delay(2000L)
                    _bannerState.value = ConnectionBannerState.Hidden
                    wasConnected = true
                } else {
                    _bannerState.value = ConnectionBannerState.Hidden
                }
            }
        }


    }

}


enum class ConnectionBannerState {
    Hidden,
    Disconnected,
    BackOnline
}
