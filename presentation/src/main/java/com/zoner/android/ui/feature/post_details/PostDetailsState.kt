package com.zoner.android.ui.feature.post_details

sealed class PostDetailsState {
    data object Nothing : PostDetailsState()
    data object Loading : PostDetailsState()
    data object Success : PostDetailsState()
    data class Error(val message: String) : PostDetailsState()
}