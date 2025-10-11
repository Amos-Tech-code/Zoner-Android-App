package com.zoner.android.ui.feature.view_status.user_status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.view_status.StatusViewingState
import com.zoner.domain.model.BaseStatus
import com.zoner.domain.model.StatusGroup
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewUserStatusViewModel(
    private val repository: StatusRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ViewUserStatusState())
    val state: StateFlow<ViewUserStatusState> = _state

    private val _screenState = MutableStateFlow<ViewUserStatusScreenState>(ViewUserStatusScreenState.ListUserStatus)
    val screenState: StateFlow<ViewUserStatusScreenState> = _screenState

    private val _viewingState = MutableStateFlow(StatusViewingState())
    val viewingState: StateFlow<StatusViewingState> = _viewingState.asStateFlow()

    private val _event = Channel<ViewUserStatusEvent>()
    val event = _event.receiveAsFlow()


    private var statusGroups: List<StatusGroup> = emptyList()
    private var currentGroupIndex = 0

    init {
        fetchMyStatuses()
    }

    fun fetchMyStatuses() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                repository.fetchUserStatusGroupFromLocal().collect { userStatusGroups ->
                    val myStatusGroup = userStatusGroups.find { it.isMyStatus }
                    val myStatuses = myStatusGroup?.statuses ?: emptyList()

                    _state.update {
                        it.copy(
                            myStatuses = myStatuses,
                            totalViews = myStatuses.sumOf { it.views.size },
                            totalLikes = myStatuses.sumOf { it.likes.size },
                            totalReplies = myStatuses.sumOf { it.replies.size },
                            isLoading = false
                        )
                    }
                    statusGroups = userStatusGroups
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleStatusExpanded(statusId: String) {
        _state.update { currentState ->
            if (currentState.expandedStatusId == statusId) {
                currentState.copy(expandedStatusId = null)
            } else {
                currentState.copy(expandedStatusId = statusId)
            }
        }
    }

    fun retryFailedUploads() {
        viewModelScope.launch {
            try {
                repository.retryFailedStatuses()
            } catch (e: Exception) {
                _event.send(
                    ViewUserStatusEvent.ShowErrorMessage(
                        e.message ?: "Something went wrong."
                    )
                )
            }
        }
    }
    fun deleteStatus(statusId: String) {
        viewModelScope.launch {
            try {
                repository.deleteStatus(statusId)
                //fetchMyStatuses()
            } catch (e: Exception) {
                _event.send(
                    ViewUserStatusEvent.ShowErrorMessage(
                        e.message ?: "Something went wrong."
                    )
                )
            }
        }
    }

    /**
     * View User Status Implementation
     *
     */
    fun startViewingFromStatus(clickedStatus: BaseStatus) {
        viewModelScope.launch {
            try {
                // Find the index of the clicked status in the user's status group
                val myStatusGroup = statusGroups.find { it.isMyStatus }
                val statusIndex = myStatusGroup?.statuses?.indexOfFirst { it.id == clickedStatus.id } ?: 0

                // Start viewing from this status
                startViewingFromMyStatusGroup(statusIndex)

                // Update screen state to show the viewer
                _screenState.value = ViewUserStatusScreenState.ViewUserStatus
            } catch (e: Exception) {
                _event.send(
                    ViewUserStatusEvent.ShowErrorMessage(
                        e.message ?: "Failed to start viewing status"
                    )
                )
            }
        }
    }

    private fun startViewingFromMyStatusGroup(startIndex: Int = 0) {
        val myStatusGroup = statusGroups.find { it.isMyStatus }
        myStatusGroup?.let { group ->
            currentGroupIndex = statusGroups.indexOfFirst { it.isMyStatus }
            _viewingState.update {
                StatusViewingState(
                    statusGroup = group,
                    statuses = group.statuses,
                    currentIndex = startIndex.coerceIn(0, group.statuses.lastIndex),
                    isViewingOwnStatus = true,
                    progressForCurrent = 0f
                )
            }
        }
    }

    fun onProgressChanged(progress: Float) {
        _viewingState.update { it.copy(progressForCurrent = progress) }
    }

    fun moveToNextStatus() {
        val state = _viewingState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex < state.statuses.size) {

            _viewingState.update {
                it.copy(
                    currentIndex = nextIndex,
                    progressForCurrent = 0f
                )
            }
        } else {
            closeViewer()
        }
    }

    fun moveToPreviousStatus() {
        val state = _viewingState.value
        val prevIndex = state.currentIndex - 1

        if (prevIndex >= 0) {
            _viewingState.update {
                it.copy(
                    currentIndex = prevIndex,
                    progressForCurrent = 0f
                )
            }
        } else {
            closeViewer()
        }
    }

    fun togglePause(paused: Boolean) {
        _viewingState.update { it.copy(paused = paused) }
    }

    fun closeViewer() {
        _screenState.value = ViewUserStatusScreenState.ListUserStatus
    }

    fun createStatus() {
        _event.trySend(ViewUserStatusEvent.CreatePost)
    }

}