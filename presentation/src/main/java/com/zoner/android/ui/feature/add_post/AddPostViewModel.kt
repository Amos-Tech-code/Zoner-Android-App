package com.zoner.android.ui.feature.add_post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.util.MAX_POST_MEDIA
import com.zoner.android.util.MAX_STATUS_MEDIA
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPostViewModel : ViewModel() {

    private val _state = MutableStateFlow<AddPostState>(AddPostState.Nothing)
    val state: StateFlow<AddPostState> = _state

    private val _event = Channel<AddPostEvent>()
    val event = _event.receiveAsFlow()

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
        _postFormState.update { it.copy(tags = tags) }
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

    fun uploadStatus() {

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


enum class PostType {
    POST, STATUS
}

enum class Audience(val displayName: String) {
    PUBLIC("Public"),
    PEOPLE_NEAR_ME("People Near me"),
    CUSTOM("Custom")
}