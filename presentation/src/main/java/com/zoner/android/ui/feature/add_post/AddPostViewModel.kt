package com.zoner.android.ui.feature.add_post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.util.MAX_POST_MEDIA
import com.zoner.android.util.MAX_STATUS_MEDIA
import com.zoner.android.util.isImage
import com.zoner.android.util.isVideo
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.StatusState
import com.zoner.domain.model.Audience
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.PostType
import com.zoner.domain.model.SaveUserStatus
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AddPostViewModel(
    private val session: ZonerSession,
    private val statusRepository: StatusRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<AddPostState>(AddPostState.Nothing)
    val state: StateFlow<AddPostState> = _state
    private val _event = Channel<AddPostEvent>()
    val event = _event.receiveAsFlow()

    var loggedInUser: LocalUser? = null

    init {
        observeUserFromLocal()
    }
    private fun observeUserFromLocal() {
        viewModelScope.launch {
            session.getUser().collect { user ->
                if (user != null) {
                   loggedInUser = user
                    when(user.isBusiness) {
                        true -> _state.value = AddPostState.Nothing
                        false -> _state.value = AddPostState.AccountRequired
                    }
                } else {
                    _state.value = AddPostState.AccountRequired
                }
            }
        }
    }
    private val _postType = MutableStateFlow(PostType.POST)
    val postType = _postType.asStateFlow()

    private val _postFormState = MutableStateFlow(PostDataForm())
    val postFormState = _postFormState.asStateFlow()

    private val _statusFormState = MutableStateFlow(StatusDataForm())
    val statusFormState = _statusFormState.asStateFlow()

    // Description input
    fun updateDescription(description: String) {
        _postFormState.update { it.copy(description = description) }
    }

    // Category selector
    fun updateCategory(category: String) {
        _postFormState.update { it.copy(category = category) }
    }

    // Price input
    fun updatePrice(price: String) {
        _postFormState.update { it.copy(price = price) }
    }

    // Location selector or input
    fun updateLocation(location: String) {
        _postFormState.update { it.copy(location = location) }
    }

    // Tags input (could be from a chip group or manual input)
    fun updateTags(tags: List<String>) {
        _postFormState.update { it.copy(tags = tags.take(30)) }
    }

    // Allow repost toggle
    fun toggleAllowReposting() {
        _postFormState.update { it.copy(allowReposting = !it.allowReposting) }
    }

    // Comments toggle
    fun toggleCommentsDisabled() {
        _postFormState.update { it.copy(commentsDisabled = !it.commentsDisabled) }
    }

    // Archive toggle
    fun toggleSaveToArchive() {
        _postFormState.update { it.copy(saveToArchive = !it.saveToArchive) }
    }

    // Audience change
    fun updateAudience(audience: Audience) {
        _postFormState.update { it.copy(audience = audience) }
    }

    // Loading state (e.g. during upload)
    fun setPostLoading(isLoading: Boolean) {
        _postFormState.update { it.copy(isLoading = isLoading) }
    }

    fun updatePostType(newType: PostType) {
        val currentType = _postType.value
        if (currentType == newType) return

        // Handle media transfer when switching types
        when {
            currentType == PostType.POST && newType == PostType.STATUS -> {
                // Convert POST uris to STATUS items
                val postUris = _postFormState.value.uris
                val statusItems = postUris.map { Status(media = it) }
                _statusFormState.update { it.copy(data = statusItems) }
                _postFormState.update { it.copy(uris = emptyList()) }
            }

            currentType == PostType.STATUS && newType == PostType.POST -> {
                // Convert STATUS items to POST uris
                val statusItems = _statusFormState.value.data
                val postUris = statusItems
                    .mapNotNull { it.media }
                    .take(MAX_POST_MEDIA)
                _postFormState.update { it.copy(uris = postUris) }
                _statusFormState.update { it.copy(data = emptyList()) }

                // Notify user if some items weren't transferred
                if (statusItems.size > 4) {
                    viewModelScope.launch {
                        _event.send(AddPostEvent.ShowErrorMessage(
                            "Only the first 4 items were kept (post limit)"
                        ))
                    }
                }
            }
        }

        _postType.value = newType
    }

    fun updateUIState(state: AddPostState) {
        viewModelScope.launch {
            _state.value = state
        }
    }

    fun updatePostUris(newUris: List<Uri>, maxLimit: Int = MAX_POST_MEDIA) {
        _postFormState.update {
            val combined = (it.uris + newUris).distinctBy { uri -> uri.toString() }
            val limited = combined.take(maxLimit)
            it.copy(uris = limited)
        }
    }

    fun removePostMedia(index: Int) {
        _postFormState.update {
            val updated = it.uris.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            it.copy(uris = updated)
        }
    }

    fun uploadPost() {

    }

    /**
     * Status Implementation
     */
    // Add a new status entry
    fun addStatusItems(items: List<Status>, maxLimit: Int = MAX_STATUS_MEDIA) {
        _statusFormState.update {
            val combined = (it.data + items)
            val limited = combined.take(maxLimit)
            it.copy(data = limited)
        }
    }

    fun updateStatusCaption(index: Int, caption: String) {
        _statusFormState.update {
            val updatedList = it.data.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = updatedList[index].copy(caption = caption)
            }
            it.copy(data = updatedList)
        }
    }

    // Remove a status item
    fun removeStatusItem(index: Int) {
        _statusFormState.update {
            val updated = it.data.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            it.copy(data = updated)
        }
    }

    fun reorderStatusItems(fromIndex: Int, toIndex: Int) {
        _statusFormState.update {
            val list = it.data.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            it.copy(data = list)
        }
    }

    // Set loading flag (e.g. while uploading a status)
    fun setStatusLoading(isLoading: Boolean) {
        _statusFormState.update { it.copy(isLoading = isLoading) }
    }

//    fun uploadStatus() {
//
//    }
    @OptIn(ExperimentalTime::class)
    fun saveStatusesLocally() {
        viewModelScope.launch {
            try {
                _statusFormState.update { it.copy(isLoading = true) }

                val currentTime = Clock.System.now()
                val savedStatuses = mutableListOf<SaveUserStatus>()

                statusFormState.value.data.forEach { status ->
                    status.media?.let { uri ->
                        try {
                            val mediaType = when {
                                uri.isVideo(context) -> MediaType.VIDEO
                                uri.isImage(context) -> MediaType.IMAGE
                                else -> throw IllegalArgumentException("Unsupported media type")
                            }

                            val userStatus = SaveUserStatus(
                                mediaUri = uri,
                                mediaType = mediaType,
                                caption = status.caption,
                                createdAt = currentTime,
                                state = StatusState.Pending
                            )

                            statusRepository.saveStatus(userStatus)
                            savedStatuses.add(userStatus)
                        } catch (e: Exception) {
                            //Log.e("AddPostViewModel", "Error saving status", e)
                            _event.send(AddPostEvent.ShowErrorMessage("Failed to save one status item"))
                        }
                    }
                }

                if (savedStatuses.isNotEmpty()) {
                    _statusFormState.update { current ->
                        current.copy(data = current.data.filterNot {
                            savedStatuses.any { saved -> saved.mediaUri == it.media }
                        })
                    }
                    _event.send(AddPostEvent.ShowSuccessMessage("Saved ${savedStatuses.size} statuses. They will upload shortly."))
                }
            } catch (e: Exception) {
                _event.send(AddPostEvent.ShowErrorMessage("Failed to save status items"))
            } finally {
                _statusFormState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun playVideo(uri: Uri) {
        _state.value = AddPostState.PlayVideo(uri)
    }

    fun stopVideo() {
        _state.value = AddPostState.Nothing
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
}