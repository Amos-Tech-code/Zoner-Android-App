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
    private val repository: StatusRepository,
) : ViewModel() {

    private val _viewingState = MutableStateFlow(StatusViewingState())
    val viewingState: StateFlow<StatusViewingState> = _viewingState.asStateFlow()

    private val _event = Channel<StatusViewingEvents>()
    val event = _event.receiveAsFlow()

    private var statusGroups: List<StatusGroup> = emptyList()
    private var currentGroupIndex = 0

    init {
        loadStatusGroups()
    }

    private fun loadStatusGroups() {
        _viewingState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                repository.getStatusGroups().collect { groups ->
                    if (groups.isNotEmpty()) {
                        statusGroups = groups
                        startViewingGroup(0)
                    } else {
                        _event.send(StatusViewingEvents.ShowError("Failed to load statuses"))
                    }
                }
            } catch (e: Exception) {
                _event.send(StatusViewingEvents.ShowError("Failed to load statuses"))
            } finally {
                _viewingState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startViewingGroup(groupIndex: Int, statusIndex: Int = 0) {
        currentGroupIndex = groupIndex.coerceIn(0, statusGroups.lastIndex)
        val group = statusGroups[currentGroupIndex]
        _viewingState.update {
            StatusViewingState(
                statusGroup = group,
                statuses = group.statuses,
                currentIndex = statusIndex,
                isViewingOwnStatus = group.authorId == getCurrentUserId()
            )
        }
    }

    fun onProgressChanged(progress: Float) {
        _viewingState.update { it.copy(progressForCurrent = progress) }
    }

    fun moveToNextStatus() {
        val state = _viewingState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex < state.statuses.size) {
            markStatusViewed(state.currentIndex)
            _viewingState.update {
                it.copy(
                    currentIndex = nextIndex,
                    progressForCurrent = 0f
                )
            }
        } else {
            moveToNextGroup()
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
            moveToPreviousGroup()
        }
    }

    private fun moveToNextGroup() {
        if (currentGroupIndex < statusGroups.lastIndex) {
            startViewingGroup(currentGroupIndex + 1)
        } else {
            closeViewer()
        }
    }

    private fun moveToPreviousGroup() {
        if (currentGroupIndex > 0) {
            val lastIndex = statusGroups[currentGroupIndex - 1].statuses.lastIndex
            startViewingGroup(currentGroupIndex - 1, lastIndex)
        }
    }

    fun togglePause(paused: Boolean) {
        _viewingState.update { it.copy(paused = paused) }
    }

    private fun markStatusViewed(index: Int) {
        val status = _viewingState.value.statuses.getOrNull(index) ?: return
        if (!status.isViewed) {
            viewModelScope.launch {
                repository.markStatusAsViewed(status.id)
            }
        }
    }

    fun closeViewer() {
        viewModelScope.launch {
            _event.send(StatusViewingEvents.NavigateBack)
        }
    }

    private fun getCurrentUserId(): String {
        // Implement your current user ID retrieval
        return ""
    }

}