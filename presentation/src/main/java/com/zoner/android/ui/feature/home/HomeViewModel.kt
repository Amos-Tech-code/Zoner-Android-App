package com.zoner.android.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.ui.feature.profile.Media
import com.zoner.android.ui.feature.profile.Post
import com.zoner.android.ui.feature.profile.User
import com.zoner.android.ui.feature.view_status.StatusViewingState
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.MediaType
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(
   private val repository: StatusRepository,
   private val session: ZonerSession
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState

   // Status viewing state
   private val _viewingState = MutableStateFlow<StatusViewingState?>(null)
   val viewingState: StateFlow<StatusViewingState?> = _viewingState

   private val _event = Channel<HomeEvent>()
   val event = _event.receiveAsFlow()
   var loggedInUser: LocalUser? = null

   init {
      populateStatuses()
      observeUserFromLocal()
      loadStatuses()
   }

   private fun populateStatuses() {
       try {
           viewModelScope.launch {
              //val userStatuses = async{ repository.fetchUserStatusGroupFromServer() }
              val otherUserStatuses = async { repository.fetchOtherUsersStatusFromServer() }
              //userStatuses.await()
              otherUserStatuses.await()
           }
       } catch (e: Exception) {

       }
   }

   private fun observeUserFromLocal() {
      viewModelScope.launch {
         session.getUser().collect { user ->
            user?.let { loggedInUser = it }
         }
      }
   }

   private fun loadStatuses() {
      viewModelScope.launch {
         combine(
            repository.getUserStatusSummary(),
            repository.getOtherUserStatusSummary()
         ) { mySummary, otherSummaries ->
            HomeState.Success(
               isBusinessAccount = loggedInUser?.isBusiness ?: false,
               userStatusSummary = MyStatusUiState(
                  latestStatus = mySummary.latestStatus,
                  statusCount = mySummary.totalCount,
                  failed = mySummary.countsByState["FAILED"] ?: 0,
                  pending = mySummary.countsByState["PENDING"] ?: 0,
                  uploading = mySummary.countsByState["UPLOADING"] ?: 0,
                  uploaded = mySummary.countsByState["UPLOADED"] ?: 0
               ),
               otherStatusSummary = otherSummaries.map {
                  OtherUserStatusUiState(
                     latestStatus = it.latestStatus,
                     statusCount = it.statusCount,
                     viewedCount = it.viewedCount,
                     authorName = it.authorName,
                     authorId = it.authorId
                  )
               },
               posts = getDummyPosts()
            )
         }.catch { e ->
            _uiState.value = HomeState.Error("Failed to retrieve posts. Please try again later.")
         }.collect { state ->
            _uiState.value = state
         }
      }
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

   fun retry() {
      loadStatuses()
   }


}