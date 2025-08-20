package com.zoner.domain.model

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.zoner.domain.StatusState
import java.io.File
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Group of Status
data class StatusGroup(
    val authorId: String,
    val authorName: String?,
    val authorAvatar: String?,
    val statuses: List<UserStatus>,
    val updatedAt: Long? = null
)


@OptIn(ExperimentalTime::class)
data class UserStatus(
    val id: String,
    val userId: String,  // Add this field
    val userName: String?,  // Add this field
    val userAvatar: String?,  // Add this field
    val mediaUri: Uri,
    val caption: String,
    val mediaType: MediaType,
    val createdAt: Instant,
    val state: StatusState,
    val durationMillis: Long = if (mediaType == MediaType.IMAGE) 5000 else 0L,
    val uploadTime: Instant? = null,
    val localFilePath: String? = null,
    val isViewed: Boolean = false
)

// Extensions to check expired status
@OptIn(ExperimentalTime::class)
fun UserStatus.isExpired(): Boolean {
    return this.uploadTime?.let { uploadTime ->
        val expirationTime = uploadTime + 24.hours
        Clock.System.now() > expirationTime
    } ?: false
}

// Response after query for expired status media paths
data class StatusPath(val id: String, val localPath: String?)
