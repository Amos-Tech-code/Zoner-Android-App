package com.zoner.android.ui.feature.notifications

sealed class NotificationState {
    data object Nothing : NotificationState()
    data object Loading : NotificationState()
    data object Success : NotificationState()
    data class Error(val message: String) : NotificationState()
}