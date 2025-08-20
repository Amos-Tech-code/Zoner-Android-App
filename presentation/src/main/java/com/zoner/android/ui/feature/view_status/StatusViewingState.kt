package com.zoner.android.ui.feature.view_status

import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.UserStatus



sealed class StatusViewingEvents {
    data object NavigateBack : StatusViewingEvents()

    data class ShowError(val message: String) : StatusViewingEvents()


}// viewer state for a single group (i.e., statuses from one user)
data class StatusViewingState(
    val statusGroup: StatusGroup? = null,
    val statuses: List<UserStatus> = emptyList(),
    val currentIndex: Int = 0,
    val isViewingOwnStatus: Boolean = false,
    val paused: Boolean = false,            // paused by user press/hold
    val progressForCurrent: Float = 0f,      // 0..1
    val isLoading: Boolean = false
)
