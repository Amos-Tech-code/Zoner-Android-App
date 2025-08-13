package com.zoner.android.ui.feature.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {

    private val _state = MutableStateFlow<NotificationState>(NotificationState.Loading)
    val state: StateFlow<NotificationState> = _state

    private val _event = MutableSharedFlow<NotificationEvent>()
    val event = _event.asSharedFlow()

    private val _notificationEnabled = MutableStateFlow(true)
    val notificationEnabled = _notificationEnabled.asStateFlow()

    fun notificationEnableUpdate() {
        _notificationEnabled.value = !_notificationEnabled.value
    }

    fun fetchData() {

    }

}