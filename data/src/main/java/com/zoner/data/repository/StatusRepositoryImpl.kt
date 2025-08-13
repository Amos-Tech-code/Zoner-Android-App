package com.zoner.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.zoner.data.local.database.ZonerDatabase
import com.zoner.data.local.database.dao.StatusItemsDao
import com.zoner.data.local.database.entities.UserStatusEntity
import com.zoner.domain.model.UserStatus
import com.zoner.domain.model.isExpired
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class StatusRepositoryImpl (
    private val database: ZonerDatabase,
    private val context: Context,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) : StatusRepository {

    private val dao: StatusItemsDao by lazy { database.statusItemsDao() }
    private val appFilesDir = context.filesDir.canonicalPath

    @OptIn(ExperimentalTime::class)
    override suspend fun saveStatus(status: UserStatus) {
        withContext(dispatchers) {
            try {
                // Copy the media file to app's private storage
                val localFile = when (status.mediaUri.scheme) {
                    "content", "file" -> copyMediaToPrivateStorage(status.mediaUri)
                    else -> null // For remote URIs
                }

                val statusToSave = localFile?.let { file ->
                    status.copy(
                        mediaUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        ),
                        localFilePath = file.absolutePath
                    )
                } ?: status

                dao.insertStatus(UserStatusEntity.fromDomain(statusToSave))
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to save status", e)
                throw e // Re-throw to handle in ViewModel
            }
        }
    }

    override suspend fun getUserStatuses(): Flow<List<UserStatus>> {
        return withContext(dispatchers) {
            dao.getUserStatuses().map { entities ->
                entities.map { it.toDomain() }
                    .filterNot { it.isExpired() }
            }
        }
    }

    override suspend fun getPendingStatuses(): List<UserStatus> {
        return withContext(dispatchers) {
            dao.getPendingStatuses().map { it.toDomain() }
        }
    }

    override suspend fun getStatusById(id: String): UserStatus? {
        return withContext(dispatchers) {
            dao.getStatusById(id)?.toDomain()
        }
    }

    override suspend fun updateStatus(status: UserStatus) {
        withContext(dispatchers) {
            dao.updateStatus(UserStatusEntity.fromDomain(status))
        }
    }

    override suspend fun deleteStatus(id: String) {
        try {
            dao.getStatusById(id)?.let { status ->
                status.localPath?.let { path ->
                    deleteFileIfSafe(path)
                }
                dao.deleteStatus(id)
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
                val countBefore = dao.getStatusCount()
                dao.getExpiredStatusesWithPaths(expiryTime).forEachIndexed { index, status ->
                    if (index % 10 == 0) delay(50) // Small pause every 10 files
                    status.localPath?.let { deleteFileIfSafe(it) }
                }
                dao.deleteExpiredStatuses(expiryTime)
                val countAfter = dao.getStatusCount()
                countBefore - countAfter // Return number of deleted items
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to clean expired statuses", e)
                0
            }
        }
    }

    override suspend fun markStatusAsViewed(statusId: String) {
        // TODO("Not yet implemented")
    }

    override suspend fun getStatusCount(): Int {
        return withContext(dispatchers) {
            try {
                val count = dao.getStatusCount()
                count
            } catch (e: Exception) {
                Log.e("StatusRepository", "Failed to count", e)
                0
            }
        }
    }

    override suspend fun getExpiredStatusCount(expiryTime: Long): Int {
        return withContext(dispatchers) {
            try {
                val count = dao.getExpiredStatusCount(expiryTime)
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
}