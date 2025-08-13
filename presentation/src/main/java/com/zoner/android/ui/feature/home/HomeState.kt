package com.zoner.android.ui.feature.home

 import com.zoner.android.ui.feature.profile.Post
 import com.zoner.domain.model.UserStatus


sealed class HomeState {
    data object Loading : HomeState()
    data class Success(
        val isBusinessAccount: Boolean = false,
        val userStatusItems: List<UserStatus>,
        val otherStatus: List<Status> = emptyList(),
        val posts: List<Post> = emptyList(),
    ) : HomeState()
    data class Error(val message: String) : HomeState()
}
