package com.zoner.android.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.UserRole
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val session: ZonerSession
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    private val _event = Channel<ProfileEvent>()
    val event = _event.receiveAsFlow()

    init {
        observeUserFromLocal()
        loadDummyData()
    }

    private fun observeUserFromLocal() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            session.getUser().collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            user = user,
                            isBusinessAccount = user.isBusiness,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadDummyData() {
        _state.value = ProfileState(
            posts = getDummyPosts(),
            replies = getDummyReplies(),
            likedPosts = getDummyLikedPosts(),
            bookmarkedPosts = getDummyBookmarkedPosts()
        )
    }

    fun deletePost(postId: String) {
        _state.value = _state.value.copy(
            posts = _state.value.posts.filter { it.id != postId }
        )
        viewModelScope.launch {
            _event.send(ProfileEvent.ShowSuccessMessage("Post deleted"))
        }
    }

    // Dummy Data Generators
    private fun getDummyUser(): User {
        return User(
            id = "user_123",
            name = if (_state.value.isBusinessAccount) "Zoner Business" else "Alex Johnson",
            username = if (_state.value.isBusinessAccount) "zoner_biz" else "alexj",
            avatarUrl = if (_state.value.isBusinessAccount)
                "https://picsum.photos/300/300?random=10"
            else
                "https://picsum.photos/300/300?random=12",
            bio = if (_state.value.isBusinessAccount)
                "Official Zoner business account. Follow for updates, features, and more."
            else
                "Photographer | Dev | Tech lover",
            followers = 1243,
            following = 567,
            posts = 42,
            isVerified = true
        )
    }

    private fun getDummyPosts(): List<Post> {
        val author = getDummyUser()
        return listOf(
            Post(
                id = "post_1",
                content = "📢 New Feature Alert: Dark mode is now live! #ZonerUpdate",
                imageUrl = "https://picsum.photos/500/500?random=21",
                likes = 230,
                replies = 10,
                timestamp = System.currentTimeMillis() - 2 * 3600000, // 2 hrs ago
                isLiked = true,
                isBookmarked = false,
                author = author,
                media = listOf(
                    Media("https://picsum.photos/400/400?random=25", MediaType.IMAGE),
                    Media("https://picsum.photos/200/200?random=2", MediaType.IMAGE),
                    Media("https://picsum.photos/200/200?random=2", MediaType.IMAGE),
                    Media("https://picsum.photos/400/400?random=25", MediaType.IMAGE)

                ),
                isReposted = false,
                reposts = 2
            ),
            Post(
                id = "post_2",
                content = "💼 We’re hiring! Join the Zoner team and build the future.",
                imageUrl = "https://picsum.photos/400/400?random=27",
                likes = 89,
                replies = 4,
                timestamp = System.currentTimeMillis() - 6 * 3600000,
                isLiked = true,
                isBookmarked = true,
                author = author,
                media = listOf(
                    Media("https://picsum.photos/300/300?random=28", MediaType.IMAGE)
                ),
                isReposted = false,
                reposts = 0
            ),
            Post(
                id = "post_3",
                content = "🎉 Weekend sale: Get 20% off all subscriptions!",
                imageUrl = null,
                likes = 342,
                replies = 15,
                timestamp = System.currentTimeMillis() - 3 * 24 * 3600000,
                isLiked = false,
                isBookmarked = true,
                author = author,
                media = emptyList(),
                isReposted = false,
                reposts = 1
            )
        )
    }

    private fun getDummyReplies(): List<Reply> {
        return listOf(
            Reply(
                id = "reply_1",
                postId = "post_1",
                content = "This feature is 🔥🔥🔥",
                author = User(
                    id = "user_456",
                    name = "Sam Wilson",
                    username = "samw",
                    avatarUrl = "https://picsum.photos/200/200?random=2"
                ),
                timestamp = System.currentTimeMillis() - 3600000,
                originalPosterUsername = "alexj",
                likes = 12,
                isLiked = true,
                isPartOfThread = true
            ),
            Reply(
                id = "reply_2",
                postId = "post_2",
                content = "This item seems to be cool. How can I get it.",
                author = User(
                    id = "user_456",
                    name = "Sam Wilson",
                    username = "samw",
                    avatarUrl = "https://picsum.photos/200/200?random=2"
                ),
                timestamp = System.currentTimeMillis() - 3600000,
                originalPosterUsername = "alexj",
                likes = 12,
                isLiked = true,
                isPartOfThread = true
            )
        )
    }

    private fun getDummyBookmarkedPosts(): List<Post> {
        val author = getDummyUser()
        return listOf(
            Post(
                id = "bookmark_1",
                content = "📚 Mastering Jetpack Compose animations. A must read!",
                imageUrl = null,
                likes = 543,
                replies = 12,
                timestamp = System.currentTimeMillis() - 24 * 3600000,
                isLiked = false,
                isBookmarked = true,
                author = author,
                media = emptyList(),
                isReposted = false,
                reposts = 0
            ),
            Post(
                id = "bookmark_2",
                content = "🚀 Building fast Kotlin APIs with Ktor",
                imageUrl = null,
                likes = 230,
                replies = 8,
                timestamp = System.currentTimeMillis() - 48 * 3600000,
                isLiked = false,
                isBookmarked = true,
                author = author,
                media = emptyList(),
                isReposted = false,
                reposts = 0
            )
        )
    }

    private fun getDummyLikedPosts(): List<Post> {
        if (_state.value.isBusinessAccount) return emptyList()
        val author = User(
            id = "user_789",
            name = "Jordan Smith",
            username = "jordans",
            avatarUrl = "https://picsum.photos/200/200?random=3"
        )

        return listOf(
            Post(
                id = "liked_1",
                content = "🌅 Sunset vibes #NaturePhotography",
                imageUrl = "https://picsum.photos/500/500?random=3",
                likes = 1243,
                replies = 42,
                timestamp = System.currentTimeMillis() - 12 * 3600000,
                isLiked = true,
                isBookmarked = true,
                author = author,
                media = listOf(Media("https://picsum.photos/400/400?random=4", MediaType.IMAGE)),
                isReposted = false,
                reposts = 10
            ),
            Post(
                id = "liked_2",
                content = "🍜 This ramen recipe changed my life!",
                imageUrl = "https://picsum.photos/300/300?random=52",
                likes = 543,
                replies = 200,
                timestamp = System.currentTimeMillis() - 24 * 3600000,
                isLiked = true,
                isBookmarked = true,
                author = author,
                media = emptyList(),
                isReposted = true,
                reposts = 25
            )
        )
    }


}

// Data Classes
data class ProfileState(
    val user: LocalUser? = null,
    val isBusinessAccount: Boolean = true,
    val posts: List<Post> = emptyList(),
    val replies: List<Reply> = emptyList(),
    val likedPosts: List<Post> = emptyList(),
    val bookmarkedPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
)

data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val followers: Int = 0,
    val following: Int = 0,
    val posts: Int = 0,
    val isVerified: Boolean = true
)

data class Post(
    val id: String,
    val likes: Int,
    val timestamp: Long,
    val content: String,
    val imageUrl: String? = null,
    val replies: Int = 25,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val author: User,
    val media: List<Media>, // Max 4 items
    val isReposted: Boolean = false,
    val reposts: Int = 43,
    val replyCount: Int = 25,
    val parentPost: Post? = null, // For replies
    val thread: List<Post> = emptyList()
)

// Updated Reply data class to include more context
data class Reply(
    val id: String,
    val postId: String,
    val content: String,
    val author: User,
    val timestamp: Long,
    val originalPosterUsername: String = "originalUser", // Add this field
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val isPartOfThread: Boolean = false
)

data class Media(
    val url: String,
    val type: MediaType, // IMAGE or VIDEO
    //val aspectRatio: Float = 1f // For proper sizing
)
