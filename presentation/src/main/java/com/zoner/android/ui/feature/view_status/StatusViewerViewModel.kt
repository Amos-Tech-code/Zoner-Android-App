package com.zoner.android.ui.feature.view_status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.UserStatus
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatusViewerViewModel(
    private val repo: StatusRepository,
) : ViewModel() {

    private val _viewingState = MutableStateFlow(StatusViewingState())
    val viewingState: StateFlow<StatusViewingState> = _viewingState.asStateFlow()

    private val _event = Channel<StatusViewingEvents>()
    val event = _event.receiveAsFlow()

    private var groups: List<StatusGroup> = emptyList()
    private var currentGroupIndex = 0

    init {
        loadStatuses()
    }

    private fun loadStatuses() {
        _viewingState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                repo.getUserStatuses().collect { statuses ->
                    if (statuses.isNotEmpty()) {
                        startViewingGroups(
                            listOf(
                                StatusGroup(
                                    authorId = "0_my_status",
                                    authorName = "My Status",
                                    statuses = statuses
                                )
                            )
                        )
                    } else {
                        closeViewer()
                    }
                }
            } catch (e: Exception) {
                closeViewer()
                _viewingState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startViewingGroups(
        statusGroups: List<StatusGroup>,
        startGroupIndex: Int = 0,
        startStatusIndex: Int = 0
    ) {
        groups = statusGroups
        currentGroupIndex = startGroupIndex.coerceIn(0, groups.lastIndex)
        startViewingStatuses(groups[currentGroupIndex].statuses, startStatusIndex)
    }

    private fun startViewingStatuses(statusGroup: List<UserStatus>, initialIndex: Int = 0) {
        _viewingState.value = StatusViewingState(
            statuses = statusGroup,
            currentIndex = initialIndex,
            isViewingOwnStatus = false,
            paused = false,
            progressForCurrent = 0f,
            isLoading = false
        )
    }

    fun onProgressChanged(progress: Float) {
        _viewingState.update { it.copy(progressForCurrent = progress) }
    }

    fun onImageAnimationFinished() {
        moveToNextStatus()
    }

    fun onVideoProgress(progress: Float) {
        onProgressChanged(progress)
    }

    fun onVideoEnded() {
        moveToNextStatus()
    }

    fun moveToNextStatus() {
        val state = _viewingState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex < state.statuses.size) {
            markStatusViewed(state.currentIndex)
            _viewingState.value = state.copy(
                currentIndex = nextIndex,
                progressForCurrent = 0f
            )
        } else {
            moveToNextGroup()
        }
    }

    fun moveToPreviousStatus() {
        val state = _viewingState.value
        val prevIndex = state.currentIndex - 1
        if (prevIndex >= 0) {
            _viewingState.value = state.copy(
                currentIndex = prevIndex,
                progressForCurrent = 0f
            )
        } else {
            moveToPreviousGroup()
        }
    }

    private fun moveToNextGroup() {
        if (currentGroupIndex < groups.lastIndex) {
            currentGroupIndex++
            startViewingStatuses(groups[currentGroupIndex].statuses, 0)
        } else {
            closeViewer()
        }
    }

    private fun moveToPreviousGroup() {
        if (currentGroupIndex > 0) {
            currentGroupIndex--
            val lastIndex = groups[currentGroupIndex].statuses.lastIndex
            startViewingStatuses(groups[currentGroupIndex].statuses, lastIndex)
        }
    }

    fun togglePause(paused: Boolean) {
        _viewingState.update { it.copy(paused = paused) }
    }

    private fun markStatusViewed(index: Int) {
        val state = _viewingState.value
        val status = state.statuses[index]
        if (!status.isViewed) {
            viewModelScope.launch {
                repo.markStatusAsViewed(status.id)
            }
        }
    }

    fun closeViewer() {
        viewModelScope.launch {
            _event.send(StatusViewingEvents.NavigateBack)
        }
    }
}