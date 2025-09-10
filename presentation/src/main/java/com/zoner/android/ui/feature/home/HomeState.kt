package com.zoner.android.ui.feature.home

 import com.zoner.android.ui.feature.profile.Post
 import com.zoner.domain.model.BaseStatus

sealed class HomeState {
    data object Loading : HomeState()
    data class Success(
        val isBusinessAccount: Boolean = false,
        val userStatusSummary: MyStatusUiState,
        val otherStatus: List<Status> = emptyList(),
        val posts: List<Post> = emptyList(),
    ) : HomeState()
    data class Error(val message: String) : HomeState()
}

data class MyStatusUiState(
    val latestStatus: BaseStatus? = null,   // for preview image/video
    val statusCount: Int = 0,            // for drawing segmented ring
    val failed: Int = 0,
    val pending: Int = 0,
    val uploading: Int = 0,
    val uploaded: Int = 0,
)
