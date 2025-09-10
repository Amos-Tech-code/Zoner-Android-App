package com.zoner.android.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _event = Channel<SettingsEvent>()
    val event = _event.receiveAsFlow()

    fun logOut() {
        viewModelScope.launch {
            val result = runCatching { userRepository.logOut() }
            when {
                result.isSuccess -> {
                    // Handle success
                    _event.send(SettingsEvent.LogOut)
                }
                result.isFailure -> {
                    // Handle error
                    _event.send(SettingsEvent.ShowError("Failed to log out. Please try again."))
                }
            }
        }
    }
}


sealed class SettingsEvent {
    data object LogOut : SettingsEvent()

    data class ShowError(val message: String) : SettingsEvent()

}