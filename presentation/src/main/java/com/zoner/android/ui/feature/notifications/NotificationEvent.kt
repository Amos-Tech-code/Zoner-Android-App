package com.zoner.android.ui.feature.notifications

sealed class NotificationEvent {
    data class ShowErrorMessage(val message: String) : NotificationEvent()
}