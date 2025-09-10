package com.zoner.domain.model

import android.net.Uri
import com.zoner.domain.StatusState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Generic StatusGroup that can contain both MyStatus and OtherUserStatus
@OptIn(ExperimentalTime::class)
data class StatusGroup(
    val authorId: String,
    val authorName: String?,
    val authorAvatar: String?,
    val statuses: List<BaseStatus>, //To handle both types
    val updatedAt: Long? = null,
    val unviewedCount: Int = 0,
    val isMyStatus: Boolean = false // Flag to distinguish user's own statuses
)


// Base sealed class for all status types
@OptIn(ExperimentalTime::class)
sealed class BaseStatus {
    abstract val id: String
    abstract val mediaUri: Uri
    abstract val caption: String?
    abstract val mediaType: MediaType
    abstract val createdAt: Instant
    abstract val durationMillis: Long
    abstract val localFilePath: String?
    abstract val expiresAt: Instant?
    abstract val likes: List<StatusInteraction>
    abstract val views: List<StatusInteraction>
    abstract val replies: List<StatusInteraction>

    // Common helper methods
    fun isExpired(): Boolean {
        return expiresAt?.let { Clock.System.now() > it } ?: false
    }

}

// Your own statuses
@OptIn(ExperimentalTime::class)
data class MyStatus(
    override val id: String,
    override val mediaUri: Uri,
    override val caption: String?,
    override val mediaType: MediaType,
    override val createdAt: Instant,
    val state: StatusState,
    override val durationMillis: Long,
    override val localFilePath: String?,
    override val expiresAt: Instant,
    override val likes: List<StatusInteraction>,
    override val views: List<StatusInteraction>,
    override val replies: List<StatusInteraction>
) : BaseStatus()

// Other users' statuses
@OptIn(ExperimentalTime::class)
data class OtherUserStatus(
    override val id: String,
    override val mediaUri: Uri,
    override val caption: String?,
    override val mediaType: MediaType,
    override val createdAt: Instant,
    val isViewed: Boolean,
    val isDownloaded: Boolean,
    val blurHash: String?,
    override val durationMillis: Long,
    override val localFilePath: String?,
    override val expiresAt: Instant,
    override val likes: List<StatusInteraction>,
    override val views: List<StatusInteraction>,
    override val replies: List<StatusInteraction>,
) : BaseStatus()


@OptIn(ExperimentalTime::class)
data class StatusInteraction(
    val id: Long,
    val userId: String,
    val userName: String?,
    val userAvatar: String?,
    val type: InteractionType,
    val timestamp: Instant,
    val replyText: String?,
    val replyMediaUri: Uri?
)


@OptIn(ExperimentalTime::class)
data class SaveUserStatus(
    val id: Long = 0,
    val mediaUri: Uri,
    val caption: String? = null,
    val mediaType: MediaType,
    val createdAt: Instant = Clock.System.now(),
    val state: StatusState = StatusState.Pending,
    val durationMillis: Long = 0L,
    val localFilePath: String? = null,
)

data class UserStatusSummary(
    val totalCount: Int,
    val latestStatus: BaseStatus?,
    val countsByState: Map<String, Int>
)