package com.zoner.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// UserStatusEntity (for owner statuses)
@Entity(tableName = "user_status")
data class UserStatusEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: String?, // Server-assigned ID

    val mediaUri: String,
    val caption: String?,
    val mediaType: String,
    val localPath: String?,
    val blurHash: String? = null, // For blurred preview in case of multi-devices
    val durationMillis: Long = 0,
    val state: String,
    val errorMessage: String?,

    // --- Sync Fields ---
    val createdAt: Long,                // When created locally or from server
    val lastUpdated: Long? = null,       // Last modification timestamp
    val expiresAt: Long? = null,        // Expiry for status lifecycle
    val deleted: Boolean = false,       // Soft delete flag
    val deletedAt: Long? = null,        // Timestamp of deletion if deleted
    val version: Int = 0,               // Incremented per update
    val isSynced: Boolean = false
)


// OtherUserStatusEntity (for statuses from other users)
@Entity(tableName = "other_user_status")
data class OtherUserStatusEntity(
    @PrimaryKey
    val id: String, // Server assigned ID
    val userId: String,
    val userName: String?,
    val userAvatar: String?,

    val mediaUri: String,
    val caption: String?,
    val mediaType: String,
    val localPath: String?,
    val blurHash: String?, // For blurred preview
    val durationMillis: Long = 0,

    val isViewed: Boolean = false,
    val isDownloaded: Boolean = false,

    // --- Sync Fields ---
    val createdAt: Long,
    val lastUpdated: Long?,
    val expiresAt: Long,
    val deleted: Boolean = false,
    val deletedAt: Long? = null,
    val version: Int = 0
)


// StatusInteractionEntity (for replies, likes, views)
@Entity(
    tableName = "status_interactions",
    indices = [
        Index(value = ["statusId", "interactionType"]),
        Index(value = ["statusId", "actorUserId", "interactionType"], unique = true)
    ]
)
data class StatusInteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: String? = null,            // Server-assigned ID

    // --- Core Relations ---
    val statusId: String,                    // ID of the status being interacted with
    val ownerUserId: String,                 // Who owns that status (the target)
    val actorUserId: String,                 // Who performed the interaction

    // --- Cached Actor Info (for offline display) ---
    val actorUserName: String? = null,
    val actorUserAvatar: String? = null,

    // --- Interaction Info ---
    val interactionType: String,             // "VIEW", "LIKE", "REPLY"
    val replyText: String? = null,
    val replyMediaUri: String? = null,

    // --- Sync Fields ---
    val timestamp: Long,                     // Local creation time
    val lastUpdated: Long? = null,           // Last modified
    val deleted: Boolean = false,
    val deletedAt: Long? = null,
    val version: Int = 0,
    val isSynced: Boolean?
)
