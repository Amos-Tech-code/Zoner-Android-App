package com.zoner.android.ui.feature.view_status.user_status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.domain.model.BaseStatus
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewUserStatusViewModel(
    private val repository: StatusRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ViewUserStatusState())
    val state: StateFlow<ViewUserStatusState> = _state

    private val _event = Channel<ViewUserStatusEvent>()
    val event = _event.receiveAsFlow()

    init {
        fetchMyStatuses()
    }

    fun fetchMyStatuses() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                repository.fetchUserStatusGroupFromLocal().collect { statusGroups ->
                    val myStatusGroup = statusGroups.find { it.isMyStatus }
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
}


data class ViewUserStatusState(
    val myStatuses: List<BaseStatus> = emptyList(),
    val totalViews: Int = 0,
    val totalLikes: Int = 0,
    val totalReplies: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val expandedStatusId: String? = null // To track which status details are expanded
)