package com.zoner.android.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.data.local.datastore.ZonerSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val session: ZonerSession
) : ViewModel() {

    private val _event = Channel<SettingsEvent>()
    val event = _event.receiveAsFlow()

    fun logOut() {
        viewModelScope.launch {
            session.clearSession()
            _event.send(SettingsEvent.LogOut)
        }
    }
}


sealed class SettingsEvent {
    data object LogOut : SettingsEvent()

}