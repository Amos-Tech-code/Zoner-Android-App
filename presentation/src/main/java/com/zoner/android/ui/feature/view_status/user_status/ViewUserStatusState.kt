package com.zoner.android.ui.feature.view_status.user_status

import com.zoner.domain.model.BaseStatus


data class ViewUserStatusState(
    val myStatuses: List<BaseStatus> = emptyList(),
    val totalViews: Int = 0,
    val totalLikes: Int = 0,
    val totalReplies: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val expandedStatusId: String? = null // To track which status details are expanded
)

sealed class ViewUserStatusScreenState {

    object ListUserStatus : ViewUserStatusScreenState()

    object ViewUserStatus : ViewUserStatusScreenState()

}