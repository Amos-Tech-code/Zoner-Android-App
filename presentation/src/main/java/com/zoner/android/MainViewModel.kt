package com.zoner.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.navigation.CompleteProfileRoute
import com.zoner.android.ui.navigation.MainAppRoute
import com.zoner.android.ui.navigation.NavRoutes
import com.zoner.android.ui.navigation.OTPVerificationRoute
import com.zoner.android.ui.navigation.OnboardingRoute
import com.zoner.android.ui.navigation.SignInRoute
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.network.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val onboardingDeferred = async { zonerSession.onboardingCompleted() }
                val tokenDeferred = async { zonerSession.getToken() }
                val stageDeferred = async { zonerSession.getRegistrationStage() }

                val onboardingCompleted = onboardingDeferred.await()
                val token = tokenDeferred.await()
                val stage = stageDeferred.await()

                _startDestination.value = when {
                    !onboardingCompleted -> OnboardingRoute
                    stage == RegistrationStage.EMAIL_SUBMITTED -> OTPVerificationRoute()
                    stage == RegistrationStage.EMAIL_VERIFIED -> CompleteProfileRoute()
                    !token.isNullOrEmpty() && stage == RegistrationStage.PROFILE_COMPLETED -> MainAppRoute
                    else -> SignInRoute
                }

            }
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Failed to determine start destination", e)
            _startDestination.value = SignInRoute
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
