package com.zoner.android.ui.feature.add_post

import android.net.Uri
import com.zoner.domain.model.Audience

sealed class AddPostState {
    data object Nothing : AddPostState()
    data object AccountRequired : AddPostState()
    data object CaptionPost : AddPostState()
    data object ShowCamera : AddPostState()
    data object PreviewPost : AddPostState()
    data class PlayVideo(val uri: Uri) : AddPostState()
}


data class PostDataForm(
    val uris: List<Uri> = emptyList(),
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val location: String = "Wangige, Kabete",
    val tags: List<String> = emptyList(),
    val allowReposting : Boolean = true,
    val audience: Audience = Audience.PUBLIC,
    val commentsDisabled: Boolean = false,
    val saveToArchive: Boolean = true,
    val isLoading: Boolean = false
)

data class StatusDataForm(
    val data: List<Status> = emptyList(),
    val isLoading: Boolean = false
)
data class Status(
    val media: Uri? = null,
    val caption: String = ""
)