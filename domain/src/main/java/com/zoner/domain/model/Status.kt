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
    val statuses: List<UserStatus>
)


@OptIn(ExperimentalTime::class)
data class UserStatus(
    val id: String,
    val mediaUri: Uri,
    val caption: String,
    val mediaType: MediaType,
    val createdAt: Instant,
    val state: StatusState,
    val durationMillis: Long? = null,
    val uploadTime: Instant? = null,
    val localFilePath: String? = null,
    val isViewed: Boolean = false
) {
    fun getDisplayUri(context: Context): Uri {
        return if (state == StatusState.Uploaded && mediaUri.scheme == "http") {
            mediaUri // Remote URI
        } else {
            // For local content URIs, ensure we have permissions
            localFilePath?.let { path ->
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    File(path)
                )
            } ?: mediaUri
        }
    }
}

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
