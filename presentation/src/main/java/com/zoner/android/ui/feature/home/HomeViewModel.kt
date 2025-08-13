package com.zoner.android.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.profile.Media
import com.zoner.android.ui.feature.profile.Post
import com.zoner.android.ui.feature.profile.User
import com.zoner.android.ui.feature.view_status.StatusViewingState
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.UserStatus
import com.zoner.domain.usecase.StatusItemsUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(
   private val statusUseCase: StatusItemsUseCases
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState

   // Status viewing state
   private val _viewingState = MutableStateFlow<StatusViewingState?>(null)
   val viewingState: StateFlow<StatusViewingState?> = _viewingState

   private val _event = Channel<HomeEvent>()
   val event = _event.receiveAsFlow()

    init {
       loadStatuses()
    }

   private fun loadStatuses() {
      _uiState.value = HomeState.Loading
      viewModelScope.launch {
         try {
             val result = statusUseCase.getUserStatus.invoke()
            result.collect { statuses ->
               _uiState.value = HomeState.Success(
                  isBusinessAccount = true,
                  userStatusItems = statuses,
                  otherStatus = getDummyStatus(),
                  posts = getDummyPosts()
               )
            }
         } catch (e: Exception) {
            //Log.d("HomeViewModel", e.message.toString())
            _uiState.value = HomeState.Error("Failed to retrieve posts. Please try again later.")
         }
      }
   }

   fun startViewingStatuses(statusGroup: List<UserStatus>, initialIndex: Int = 0) {
      _viewingState.value = StatusViewingState(
         statuses = statusGroup,
         currentIndex = initialIndex,
         isViewingOwnStatus = /*statusGroup.firstOrNull()?.author?.isCurrentUser ?:*/ true
      )
   }

   fun moveToNextStatus() {
      _viewingState.value?.let { current ->
         if (current.currentIndex < current.statuses.size - 1) {
            _viewingState.value = current.copy(
               currentIndex = current.currentIndex + 1,
            )
         } else {
            // Reached end, close viewer
            _viewingState.value = null
         }
      }
   }

   fun moveToPreviousStatus() {
      _viewingState.value?.let { current ->
         if (current.currentIndex > 0) {
            _viewingState.value = current.copy(
               currentIndex = current.currentIndex - 1
            )
         }
      }
   }

   fun closeStatusViewer() {
      _viewingState.value = null
   }

    // Dummy Data Generators
    private fun getDummyUser(): User {
       return User(
          id = "user_123",
          name = "Zoner Business",
          username = "zoner_biz",
          avatarUrl = "https://picsum.photos/300/300?random=10",
          bio = "Official Zoner business account. Follow for updates, features, and more.",
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
          ),
          Post(
             id = "post_4",
             content = "📚 Mastering Jetpack Compose animations. A must read!",
             imageUrl = null,
             likes = 543,
             replies = 12,
             timestamp = System.currentTimeMillis() - 24 * 3600000,
             isLiked = false,
             isBookmarked = false,
             author = author,
             media = emptyList(),
             isReposted = false,
             reposts = 0
          ),
          Post(
             id = "post_5",
             content = "🍜 This ramen recipe changed my life!",
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
                Media("https://picsum.photos/300/300?random=28", MediaType.IMAGE)
             ),
             isReposted = false,
             reposts = 2
          ),
          Post(
             id = "post_6",
             content = "🍜 This ramen recipe changed my life!",
             imageUrl = "https://picsum.photos/500/500?random=21",
             likes = 2000,
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
             reposts = 7
          ),
          Post(
             id = "post_7",
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

    private fun getDummyStatus(): List<Status> {
       val author = getDummyUser()
       return listOf(
          Status(
             id = "status_1",
             content = "📢 New Feature Alert: Dark mode is now live! #ZonerUpdate",
             likes = 230,
             replies = 10,
             timestamp = System.currentTimeMillis() - 2 * 3600000, // 2 hrs ago
             isLiked = true,
             author = User(name = "Magunas"),
             media = listOf(
                Media("https://picsum.photos/400/400?random=25", MediaType.IMAGE),
                Media("https://picsum.photos/200/200?random=2", MediaType.IMAGE),

             ),
             isViewed = true,
          ),
          Status(
             id = "status_2",
             content = "💼 We’re hiring! Join the Zoner team and build the future.",
             likes = 89,
             replies = 4,
             timestamp = System.currentTimeMillis() - 6 * 3600000,
             isLiked = true,
             author = User(name = "QuickMart"),
             media = listOf(
                Media("https://picsum.photos/300/300?random=28", MediaType.IMAGE)
             ),
             isViewed = false,
          ),
          Status(
             id = "status_3",
             content = "🎉 Weekend sale: Get 20% off all subscriptions!",
             likes = 342,
             replies = 15,
             timestamp = System.currentTimeMillis() - 3 * 24 * 3600000,
             isLiked = false,
             author = User(name = "Naivas"),
             media = listOf(
                Media("https://picsum.photos/200/200?random=2", MediaType.IMAGE),
             ),
             isViewed = false,
          ),
          Status(
             id = "status_4",
             content = "📚 Mastering Jetpack Compose animations. A must read!",
             likes = 543,
             replies = 12,
             timestamp = System.currentTimeMillis() - 24 * 3600000,
             isLiked = false,
             author = User(name = "Magunas"),
             media = listOf(
                Media("https://picsum.photos/300/300?random=28", MediaType.IMAGE)
             ),
             isViewed = false,
          ),
          Status(
             id = "status_5",
             content = "🍜 This ramen recipe changed my life!",
             likes = 230,
             replies = 10,
             timestamp = System.currentTimeMillis() - 2 * 3600000, // 2 hrs ago
             isLiked = true,
             author = User(name = "Citadel hardware and metals"),
             media = listOf(
                Media("https://picsum.photos/400/400?random=25", MediaType.IMAGE),
                Media("https://picsum.photos/200/200?random=2", MediaType.IMAGE),
                Media("https://picsum.photos/200/200?random=2", MediaType.IMAGE),
             ),
             isViewed = false,
          ),
          Status(
             id = "status_6",
             content = "🚀 Building fast Kotlin APIs with Ktor",
             likes = 230,
             replies = 8,
             timestamp = System.currentTimeMillis() - 48 * 3600000,
             isLiked = false,
             author = User(name = "Maestro"),
             media = listOf(
                Media("https://picsum.photos/200/200?random=2", MediaType.IMAGE),
                Media("https://picsum.photos/400/400?random=25", MediaType.IMAGE)
             ),
             isViewed = false,
          )
       )
    }

   fun retry() {
      loadStatuses()
   }


}

data class Status(
   val id: String,
   val likes: Int,
   val timestamp: Long,
   val content: String,
   val replies: Int,
   val isLiked: Boolean,
   val author: User,
   val media: List<Media>, // Max 4 items
   val isViewed: Boolean
)