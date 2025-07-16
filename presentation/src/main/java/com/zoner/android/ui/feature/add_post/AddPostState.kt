package com.zoner.android.ui.feature.add_post

import android.net.Uri

sealed class AddPostState {
    data object Nothing : AddPostState()
    data object AccountRequired : AddPostState()
    data object CaptionPost : AddPostState()
    data object ShowCamera : AddPostState()
    data object PreviewPost : AddPostState()
    data class PlayVideo(val uri: Uri) : AddPostState()
}