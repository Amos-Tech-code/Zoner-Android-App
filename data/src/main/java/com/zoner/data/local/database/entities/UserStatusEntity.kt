package com.zoner.data.local.database.entities

import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zoner.domain.StatusState
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.UserStatus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// UserStatusEntity
@Entity(tableName = "user_status")
data class UserStatusEntity(
    @PrimaryKey
    val id: String,
    val mediaUri: String,
    val caption: String,
    val mediaType: String,
    val createdAt: Long,
    val localPath: String?,
    val state: String,
    val uploadTime: Long?,
    val errorMessage: String?
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun fromDomain(status: UserStatus): UserStatusEntity {
            return UserStatusEntity(
                id = status.id,
                mediaUri = status.mediaUri.toString(),
                caption = status.caption,
                mediaType = status.mediaType.name,
                createdAt = status.createdAt.toEpochMilliseconds(),
                state = when (status.state) {
                    is StatusState.Pending -> "PENDING"
                    is StatusState.Uploading -> "UPLOADING"
                    is StatusState.Failed -> "FAILED"
                    is StatusState.Uploaded -> "UPLOADED"
                },
                uploadTime = status.uploadTime?.toEpochMilliseconds(),
                errorMessage = (status.state as? StatusState.Failed)?.error,
                localPath = status.localFilePath
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    fun toDomain(): UserStatus {
        return UserStatus(
            id = id,
            mediaUri = mediaUri.toUri(),
            caption = caption,
            mediaType = mediaTypeOrDefault(mediaType),
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            state = when (state) {
                "PENDING" -> StatusState.Pending
                "UPLOADING" -> StatusState.Uploading
                "FAILED" -> StatusState.Failed(errorMessage ?: "Unknown error")
                "UPLOADED" -> StatusState.Uploaded
                else -> StatusState.Pending
            },
            uploadTime = uploadTime?.let { Instant.fromEpochMilliseconds(it) },
            localFilePath = localPath
        )
    }
}

private fun mediaTypeOrDefault(value: String): MediaType {
    return enumValues<MediaType>().firstOrNull { it.name == value }
        ?: MediaType.IMAGE // default fallback
}
