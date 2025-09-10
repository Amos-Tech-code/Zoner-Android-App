package com.zoner.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.zoner.data.local.database.ZonerDatabase
import com.zoner.data.local.database.dao.OtherStatusDao
import com.zoner.data.local.database.dao.StatusInteractionDao
import com.zoner.data.local.database.dao.UserStatusDao
import com.zoner.data.local.database.entities.OtherUserStatusEntity
import com.zoner.data.local.database.entities.StatusInteractionEntity
import com.zoner.data.local.database.entities.UserStatusEntity
import com.zoner.data.local.database.toEntity
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.data.utils.MediaUtils
import com.zoner.data.workers.StatusCleanupWorker
import com.zoner.data.workers.StatusSyncWorker
import com.zoner.domain.StatusState
import com.zoner.domain.model.InteractionType
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.MyStatus
import com.zoner.domain.model.OtherUserStatus
import com.zoner.domain.model.SaveUserStatus
import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.StatusInteraction
import com.zoner.domain.model.UserStatusSummary
import com.zoner.domain.model.request.RecordStatusInteraction
import com.zoner.domain.network.NetworkService
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.associate
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class StatusRepositoryImpl (
    private val context: Context,
    private val session: ZonerSession,
    private val database: ZonerDatabase,
    private val networkService: NetworkService,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) : StatusRepository {

    private val userStatusDao: UserStatusDao by lazy { database.userStatusDao() }
    private val otherStatusDao: OtherStatusDao by lazy { database.otherUsersStatusDao() }
    private val interactionDao: StatusInteractionDao by lazy { database.statusInteractionsDao() }
    private val appFilesDir = context.filesDir.canonicalPath

    @OptIn(ExperimentalTime::class)
    override suspend fun saveStatus(status: SaveUserStatus) {
        withContext(dispatchers) {
            try {
                // Calculate duration based on media type
                val duration = if (status.mediaType == MediaType.IMAGE) {
                    5000L // 5 seconds for images
                } else {
                    MediaUtils.getVideoDuration(context, status.mediaUri).coerceAtLeast(1000L) // Minimum 1 second
                }
                // Copy the media file to app's private storage
                val localFile = when (status.mediaUri.scheme) {
                    "content", "file" -> copyMediaToPrivateStorage(status.mediaUri)
                    else -> null // For remote URIs
                }

                val statusToSave = localFile?.let { file ->
                    status.copy(
                        durationMillis = duration,
                        mediaUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        ),
                        localFilePath = file.absolutePath
                    )
                } ?: status

                // Insert Status
                userStatusDao.insertStatus(statusToSave.toEntity())

                // Enqueue worker for upload with small delay to ensure DB commit
                StatusSyncWorker.enqueue(context, 1000) // 1 second delay

                // Trigger cleanup worker
                StatusCleanupWorker.enqueue(context)

            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to save status", e)
                throw e // Re-throw to handle in ViewModel
            }
        }
    }

    override suspend fun retryFailedStatuses() {
        withContext(dispatchers) {
            val failedState = StatusState.Failed("")
            val failedStatuses = userStatusDao.getStatusesByState(failedState.stateName)
            if (failedStatuses.isNotEmpty()) {
                // Reset state to PENDING for retry
                failedStatuses.forEach { status ->
                    userStatusDao.updateStatusState(status.localId, StatusState.Pending.stateName)
                }
                StatusSyncWorker.enqueue(context)
            }
        }
    }

    override suspend fun updateStatus(status: MyStatus) {
        TODO("Not yet implemented")
    }

    // User Status group from local database
    @OptIn(ExperimentalTime::class)
    override suspend fun fetchUserStatusGroupFromLocal(): Flow<List<StatusGroup>> {
        return userStatusDao.getUserStatuses().map { entities ->
            val myStatuses = entities.map { it.toMyStatus() }
               .filterNot { it.isExpired() }

            listOf(StatusGroup(
                authorId = getCurrentUser().id,
                authorName = getCurrentUser().name,
                authorAvatar = getCurrentUser().imgUrl,
                statuses = myStatuses.sortedByDescending { it.createdAt },
                updatedAt = myStatuses.maxOfOrNull { it.createdAt.toEpochMilliseconds() },
                unviewedCount = myStatuses.sumOf { it.views.size }, // Or your specific logic
                isMyStatus = true
            ))
        }.flowOn(dispatchers)
    }

    override suspend fun getUserStatusSummary(): Flow<UserStatusSummary> {
        return combine(
            userStatusDao.getStatusCountFlow(),
            userStatusDao.getLatestStatus(),
            userStatusDao.getStatusCountsByState()
        ) { totalCount, latestEntity, stateCounts ->
            UserStatusSummary(
                totalCount = totalCount,
                latestStatus = latestEntity?.toMyStatus(),
                countsByState = stateCounts.associate { it.state to it.count }
            )
        }.flowOn(dispatchers)
    }

    // For other users' statuses from Server
    @OptIn(ExperimentalTime::class)
    override suspend fun fetchOtherUsersStatusFromServer(): Flow<List<StatusGroup>> {
        return withContext(dispatchers) {
            try {
                // Fetch from server
                val statusGroupsFromServer = networkService.getOtherUsersStatuses()

                // Save to local database
                statusGroupsFromServer.forEach { group ->
                    group.statuses.forEach { status ->
                        val entity = OtherUserStatusEntity(
                            id = status.id, //status.serverId
                            userId = group.authorId,
                            userName = group.authorName,
                            userAvatar = group.authorAvatar,
                            mediaUri = status.mediaUri.toString(),
                            caption = status.caption,
                            mediaType = status.mediaType.name,
                            createdAt = status.createdAt.toEpochMilliseconds(),
                            localPath = null,
                            isViewed = false,
                            isDownloaded = false,
                            blurHash = "",//status.blurHash,
                            durationMillis = 0L,//status.durationMillis,
                            expiresAt = status.expiresAt?.toEpochMilliseconds()
                                ?: (status.createdAt + 24.hours).toEpochMilliseconds(),
                            lastUpdated = null,
                            deleted = false,
                        )
                        otherStatusDao.insert(entity)
                    }
                }

                fetchOtherUsersStatusFromLocal()
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to fetch other users' statuses", e)
                fetchOtherUsersStatusFromLocal() // Return cached data if available
            }
        }
    }

    // For other users' statuses from local database
    @OptIn(ExperimentalTime::class)
    override suspend fun fetchOtherUsersStatusFromLocal(): Flow<List<StatusGroup>> {
        return otherStatusDao.getAll().map { entities ->
            // Group by user ID and create StatusGroups
            entities.groupBy { it.userId }.map { (userId, userEntities) ->
                val firstEntity = userEntities.firstOrNull()
                val statuses = userEntities.map { it.toOtherUserStatus() }
                    .filterNot { it.isExpired() }
                    .sortedByDescending { it.createdAt }

                StatusGroup(
                    authorId = userId,
                    authorName = firstEntity?.userName,
                    authorAvatar = firstEntity?.userAvatar,
                    statuses = statuses,
                    updatedAt = statuses.maxOfOrNull { it.createdAt.toEpochMilliseconds() },
                    unviewedCount = statuses.count { !it.isViewed },
                    isMyStatus = false
                )
            }.sortedByDescending { it.updatedAt ?: 0L }
        }.flowOn(dispatchers)

    }

    override suspend fun downloadStatusMedia(statusId: String) {
        withContext(dispatchers) {
            try {
                val status = otherStatusDao.getById(statusId) ?: return@withContext

                if (!status.isDownloaded) {
                    // Download media from server
                    val mediaData = networkService.downloadStatusMedia(status.id ?: statusId)
                   // val localPath = saveDownloadedMedia(mediaData, statusId, status.mediaType)

                    // Update database
                    otherStatusDao.updateDownloadStatus(statusId, true, "localPath")
                    otherStatusDao.markAsViewed(statusId)

                    // Record view interaction
                    recordInteraction(
                        RecordStatusInteraction(
                            statusId = statusId,
                            userId = getCurrentUser().id,
                            type = InteractionType.VIEW
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to download status media", e)
            }
        }
    }

    // Status interactions
    override suspend fun recordInteraction(interaction: RecordStatusInteraction ) {
        withContext(dispatchers) {
            try {
                val interaction = StatusInteractionEntity(
                    statusId = interaction.statusId,
                    actorUserId = interaction.userId,
                    interactionType = interaction.type.name,
                    timestamp = System.currentTimeMillis(),
                    replyText = interaction.replyText,
                    replyMediaUri = interaction.replyMediaUri?.toString(),
                    isSynced = false,
                    ownerUserId = ""
                )

                interactionDao.insert(interaction)

                // Sync to server (for non-view interactions)
                if (interaction.interactionType != InteractionType.VIEW.name) {
                    syncInteractionToServer(interaction)
                }
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to record interaction", e)
            }
        }
    }

    private suspend fun syncInteractionToServer(interaction: StatusInteractionEntity) {
        try {
            networkService.recordInteraction(
                statusId = interaction.statusId,
                userId = interaction.actorUserId,
                type = InteractionType.valueOf(interaction.interactionType),
                replyText = interaction.replyText,
                replyMediaUri = interaction.replyMediaUri?.let { Uri.parse(it) }
            )

            interactionDao.markAsSynced(interaction.localId)
        } catch (e: Exception) {
            Log.e("StatusRepository", "Failed to sync interaction", e)
        }
    }

    override suspend fun deleteStatus(id: String) {
        try {
            userStatusDao.getStatusById(id)?.let { status ->
                status.localPath?.let { path ->
                    deleteFileIfSafe(path)
                }
                userStatusDao.deleteStatus(id.toLong())
            }
        } catch (e: Exception) {
            Log.e("StatusRepository", "Local Saved status delete failed", e)
            throw e
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun cleanExpiredStatuses(): Int {
        return withContext(dispatchers) {
            try {
                val expiryTime = Clock.System.now().minus(24.hours).toEpochMilliseconds()
                val countBefore = userStatusDao.getStatusCount()
                userStatusDao.getExpiredStatusesWithPaths(expiryTime).forEachIndexed { index, status ->
                    if (index % 10 == 0) delay(50) // Small pause every 10 files
                    status.localPath?.let { deleteFileIfSafe(it) }
                }
                userStatusDao.deleteExpiredStatuses(expiryTime)
                val countAfter = userStatusDao.getStatusCount()
                countBefore - countAfter // Return number of deleted items
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to clean expired statuses", e)
                0
            }
        }
    }

    override suspend fun getStatusCount(): Int {
        return withContext(dispatchers) {
            try {
                val count = userStatusDao.getStatusCount()
                count
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to count", e)
                0
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getExpiredStatusCount(): Int {
        return withContext(dispatchers) {
            try {
                val count = userStatusDao.getExpiredStatusCount(Clock.System.now().toEpochMilliseconds())
                count
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to count expired", e)
                0
            }
        }
    }

    private fun copyMediaToPrivateStorage(uri: Uri): File? {
        return try {
            val fileDir = File(context.filesDir, "status_media").apply { mkdirs() }
            val extension = getFileExtension(uri)
            val file = File(fileDir, "${System.currentTimeMillis()}$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("StatusRepository", "Failed to copy media file", e)
            null
        }
    }

    private fun getFileExtension(uri: Uri): String {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?.let { ".$it" } ?: uri.toString().substringAfterLast('.').takeIf { it.length <= 5 }
                ?.let { ".$it" } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun deleteFileIfSafe(path: String) {
        try {
            val file = File(path)
            if (file.exists() && file.canonicalPath.startsWith(appFilesDir)) {
                if (!file.delete()) {
                    Log.w("FileCleanup", "Failed to delete: ${file.name}")
                }
            }
        } catch (e: SecurityException) {
            Log.w("FileCleanup", "Security exception: ${File(path).name}")
        }
    }

    // Entity conversion extensions
    @OptIn(ExperimentalTime::class)
    private suspend fun UserStatusEntity.toMyStatus(): MyStatus {
        return MyStatus(
            id = serverId ?: localId.toString(),
            mediaUri = mediaUri.toUri(),
            caption = caption,
            mediaType = enumValueOf(mediaType),
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            state = when (state) {
                "PENDING" -> StatusState.Pending
                "UPLOADING" -> StatusState.Uploading
                "FAILED" -> StatusState.Failed(errorMessage ?: "Unknown error")
                "UPLOADED" -> StatusState.Uploaded
                else -> StatusState.Pending
            },
            durationMillis = durationMillis,
            localFilePath = localPath,
            expiresAt = Instant.fromEpochMilliseconds(expiresAt ?: (createdAt + 24.hours.inWholeMilliseconds)),
            likes = fetchInteractions(serverId.toString(), InteractionType.LIKE),
            views = fetchInteractions(serverId.toString(), InteractionType.VIEW),
            replies = fetchInteractions(serverId.toString(), InteractionType.REPLY)
        )
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun OtherUserStatusEntity.toOtherUserStatus(): OtherUserStatus {
        return OtherUserStatus(
            id = id,
            mediaUri = mediaUri.toUri(),
            caption = caption,
            mediaType = enumValueOf(mediaType),
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            isViewed = isViewed,
            isDownloaded = isDownloaded,
            blurHash = blurHash,
            durationMillis = durationMillis,
            localFilePath = localPath,
            expiresAt = Instant.fromEpochMilliseconds(expiresAt),
            likes = fetchInteractions(id, InteractionType.LIKE),
            views = fetchInteractions(id, InteractionType.VIEW),
            replies = fetchInteractions(id, InteractionType.REPLY),
        )
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun fetchInteractions(statusId: String, type: InteractionType): List<StatusInteraction> {
        return withContext(dispatchers) {
            interactionDao.getInteractions(statusId, type.name).map { entity ->
                StatusInteraction(
                    id = entity.localId,
                    userId = entity.actorUserId,
                    userName = "Zoner User",//getUserName(entity.userId), // You'll need to implement user lookup
                    userAvatar = "https://picsum.photos/300/300?random=10",//getUserAvatar(entity.userId),
                    type = InteractionType.valueOf(entity.interactionType),
                    timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
                    replyText = entity.replyText,
                    replyMediaUri = entity.replyMediaUri?.toUri()
                )
            }
        }
    }

    private suspend fun getCurrentUser(): LocalUser {
        val user = session.getUser().first()
        return LocalUser(
            id = user?.id.orEmpty(),
            imgUrl = user?.imgUrl,
            name = user?.name
        )
    }

}