package com.zoner.android.ui.feature.post_details

sealed class PostDetailsEvent {
    data class ShowErrorMessage(val message: String) : PostDetailsEvent()
}