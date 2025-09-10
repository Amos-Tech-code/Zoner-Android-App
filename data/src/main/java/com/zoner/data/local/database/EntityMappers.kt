package com.zoner.data.local.database

import androidx.core.net.toUri
import com.zoner.data.local.database.entities.UserStatusEntity
import com.zoner.domain.StatusState
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.SaveUserStatus
import kotlin.time.ExperimentalTime


/**
 * Convert UserStatus to UserStatusEntity
 */
@OptIn(ExperimentalTime::class)
fun SaveUserStatus.toEntity() : UserStatusEntity {
    return UserStatusEntity(
        serverId = null,
        mediaUri = mediaUri.toString(),
        caption = caption,
        mediaType = mediaType.name,
        localPath = localFilePath,
        durationMillis = durationMillis,
        createdAt = createdAt.toEpochMilliseconds(),
        expiresAt = null,
        state = state.stateName,
        errorMessage = (state as? StatusState.Failed)?.error,
    )
}

@OptIn(ExperimentalTime::class)
fun UserStatusEntity.toUploadStatusRequest(): SaveUserStatus {
    return SaveUserStatus(
        id = localId,
        mediaUri = mediaUri.toUri(),
        caption = caption?.ifEmpty { null },
        mediaType = MediaType.valueOf(mediaType),
        durationMillis = durationMillis,
    )
}

// Helper for grouped counts
data class StateCount(
    val state: String,
    val count: Int
)

// Response after query for expired status media paths
data class StatusPath(val localId: String, val localPath: String?)

