package com.zoner.android.ui.feature.add_post

sealed class AddPostEvent {
    data class ShowErrorMessage(val message: String) : AddPostEvent()
}