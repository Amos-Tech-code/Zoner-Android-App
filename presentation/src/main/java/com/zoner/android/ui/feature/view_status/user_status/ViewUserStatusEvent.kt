package com.zoner.android.ui.feature.view_status.user_status

sealed class ViewUserStatusEvent {

    data class ShowErrorMessage(val message: String) : ViewUserStatusEvent()
    object NavigateBack : ViewUserStatusEvent()

    object CreatePost : ViewUserStatusEvent()

}