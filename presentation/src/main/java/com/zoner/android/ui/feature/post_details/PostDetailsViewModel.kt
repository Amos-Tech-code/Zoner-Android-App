package com.zoner.android.ui.feature.post_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.profile.Media
import com.zoner.android.ui.feature.profile.Post
import com.zoner.android.ui.feature.profile.User
import com.zoner.domain.model.MediaType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailsViewModel : ViewModel() {

    private val _uistate = MutableStateFlow<PostDetailsState>(PostDetailsState.Loading)
    val uistate: StateFlow<PostDetailsState> = _uistate

    private val _event = Channel<PostDetailsEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state

    fun loadPostAndReplies(postId: String) {
        // Simulate network loading
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500) // Simulate network delay

            _state.update {
                it.copy(
                    post = getDummyPost(postId),
                    replies = getDummyReplies(postId),
                    isLoading = false
                )
            }
        }
    }

    fun postReply(content: String) {
        val newReply = Post(
            id = "reply-${System.currentTimeMillis()}",
            author = dummyUser.copy(), // Current user
            content = content,
            media = emptyList(),
            timestamp = System.currentTimeMillis(),
            likes = 0,
            replyCount = 0,
            isLiked = false,
            isBookmarked = false,
            parentPost = _state.value.post
        )

        _state.update { state ->
            state.copy(
                replies = listOf(newReply) + state.replies,
                post = state.post.copy(replyCount = state.post.replyCount + 1)
            )
        }
    }

    // Dummy data generators
    private fun getDummyPost(postId: String): Post {
        return Post(
            id = postId,
            author = dummyUser,
            content = "This is the main post content. It can be long or short with multiple lines of text.",
            media = listOf(
                Media(
                    url = "https://picsum.photos/400/400?random=25",
                    type = MediaType.IMAGE,
                )
            ),
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2 hours ago
            likes = 42,
            replyCount = 5,
            isLiked = false,
            isBookmarked = false,
            thread = listOf(
                Post(
                    id = "thread-1",
                    author = dummyUser2,
                    content = "This is the first post in the thread",
                    media = emptyList(),
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5, // 5 hours ago
                    likes = 10,
                    replyCount = 2,
                    isLiked = true,
                    isBookmarked = false
                )
            )
        )
    }

    private fun getDummyReplies(postId: String): List<Post> {
        return listOf(
            Post(
                id = "reply-1",
                author = dummyUser2,
                content = "This is a top-level reply to the post",
                media = emptyList(),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60, // 1 hour ago
                likes = 8,
                replyCount = 2,
                isLiked = false,
                isBookmarked = false,
                parentPost = getDummyPost(postId),
                thread = listOf(
                    Post(
                        id = "reply-1-1",
                        author = dummyUser3,
                        content = "This is a reply to the first reply",
                        media = emptyList(),
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 30, // 30 mins ago
                        likes = 3,
                        replyCount = 0,
                        isLiked = false,
                        isBookmarked = false,
                        parentPost = getDummyPost("reply-1")
                    ),
                    Post(
                        id = "reply-1-2",
                        author = dummyUser,
                        content = "Another reply to the first reply",
                        media = emptyList(),
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 15, // 15 mins ago
                        likes = 1,
                        replyCount = 1,
                        isLiked = true,
                        isBookmarked = false,
                        parentPost = getDummyPost("reply-1"),
                        thread = listOf(
                            Post(
                                id = "reply-1-2-1",
                                author = dummyUser2,
                                content = "Nested reply three levels deep",
                                media = emptyList(),
                                timestamp = System.currentTimeMillis() - 1000 * 60 * 5, // 5 mins ago
                                likes = 0,
                                replyCount = 0,
                                isLiked = false,
                                isBookmarked = false,
                                parentPost = getDummyPost("reply-1-2")
                            )
                        )
                    )
                )
            ),
            Post(
                id = "reply-2",
                author = dummyUser3,
                content = "Another top-level reply with an image",
                media = listOf(
                    Media(
                        url = "https://example.com/reply-image.jpg",
                        type = MediaType.IMAGE,
                    )
                ),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45, // 45 mins ago
                likes = 15,
                replyCount = 0,
                isLiked = false,
                isBookmarked = false,
                parentPost = getDummyPost(postId)
            )
        )
    }

    companion object {
        val dummyUser = User(
            id = "user-1",
            name = "John Doe",
            username = "johndoe",
            avatarUrl = "https://picsum.photos/200/200?random=2",
            isVerified = true
        )

        val dummyUser2 = User(
            id = "user-2",
            name = "Jane Smith",
            username = "janesmith",
            avatarUrl = "https://picsum.photos/400/400?random=25",
            isVerified = false
        )

        val dummyUser3 = User(
            id = "user-3",
            name = "Alex Johnson",
            username = "alexj",
            avatarUrl = "https://picsum.photos/200/200?random=2",
            isVerified = true
        )
    }
}

data class PostDetailState(
    val post: Post = Post(
        id = "",
        author = User("", "", "", "", isVerified = false),
        content = "",
        media = emptyList(),
        timestamp = 0,
        likes = 0,
        replyCount = 0,
        isLiked = false,
        isBookmarked = false
    ),
    val replies: List<Post> = emptyList(),
    val isLoading: Boolean = false
)
