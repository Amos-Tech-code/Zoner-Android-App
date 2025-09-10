package com.zoner.data.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.BackoffPolicy
import com.zoner.data.local.database.dao.UserStatusDao
import com.zoner.data.local.database.toUploadStatusRequest
import com.zoner.domain.ResultWrapper
import com.zoner.domain.StatusState
import com.zoner.domain.network.NetworkService
import org.koin.java.KoinJavaComponent.getKoin
import java.util.concurrent.TimeUnit

class StatusSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val statusDao: UserStatusDao? = null,
    private val service: NetworkService? = null
) : CoroutineWorker(context, workerParams) {

    // fallback constructor (for reflection case)
    constructor(context: Context, workerParams: WorkerParameters) :
            this(context, workerParams, null, null)

    // always resolve (DI first, fallback to lazy Koin lookup)
    private val userStatusDao: UserStatusDao by lazy { statusDao ?: getKoin().get() }
    private val networkService: NetworkService by lazy { service ?: getKoin().get() }

    override suspend fun doWork(): Result {
        return try {
            // 1. Get unsynced statuses
            val pendingStatuses = userStatusDao.getPendingStatuses()

            if (pendingStatuses.isEmpty()) {
                return Result.success()
            }

            var hasFailures = false
            val successfullyProcessedIds = mutableSetOf<Long>() // Track successful items

            // 2. Sync one by one
            pendingStatuses.forEach { localStatus ->
                try {
                    // Skip if this was already successfully processed in a previous iteration
                    if (successfullyProcessedIds.contains(localStatus.localId)) {
                        return@forEach // skip to next item
                    }
                    // Update state to UPLOADING
                    userStatusDao.updateStatusState(localStatus.localId, StatusState.Uploading.stateName)

                    if (localStatus.deleted) {
                        if (localStatus.serverId != null) {
                            val deleteResult = networkService.deleteStatus(localStatus.serverId)
                            if (deleteResult is ResultWrapper.Success) {
                                userStatusDao.deleteStatus(localStatus.localId)
                                successfullyProcessedIds.add(localStatus.localId) // Mark as done
                            } else {
                                userStatusDao.markSyncFailed(localStatus.localId, "Delete failed")
                                hasFailures = true
                            }
                        } else {
                            // No server ID, just delete locally
                            userStatusDao.deleteStatus(localStatus.localId)
                            successfullyProcessedIds.add(localStatus.localId) // Mark as done
                        }
                    } else {
                        if (localStatus.serverId != null) {
                            val response = networkService.uploadStatus(localStatus.toUploadStatusRequest())
                            when (response) {
                                is ResultWrapper.Failure -> {
                                    userStatusDao.markSyncFailed(
                                        localStatus.localId,
                                        response.exception.message
                                    )
                                    hasFailures = true
                                }

                                is ResultWrapper.Success -> {
                                    // Update local DB on success
                                    userStatusDao.updateStatusAfterSync(
                                        localStatus.localId,
                                        serverId = response.value.id,
                                        expiresAt = response.value.expiresAt,
                                        lastUpdated = response.value.lastUpdated,
                                        version = response.value.version,
                                        isSynced = true,
                                    )
                                    successfullyProcessedIds.add(localStatus.localId) // Mark as done
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Mark as failed (retryable)
                    userStatusDao.markSyncFailed(localStatus.localId, e.message)
                    hasFailures = true
                }
            }

            // 3. Return appropriate result based on whether we have failures
            if (hasFailures) {
                Result.retry() // This will trigger a retry with backoff
            } else {
                Result.success() // All items succeeded
            }

        } catch (e: Exception) {
            Result.retry() // Global failure, retry the entire batch
        }
    }

    companion object {

        const val WORKER_TAG = "status_upload_worker"
        const val UNIQUE_WORK_NAME = "status_upload_work"

        fun enqueue(context: Context, delayMillis: Long = 0) {
            val requestBuilder = OneTimeWorkRequestBuilder<StatusSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, // initial delay in seconds
                    TimeUnit.SECONDS
                )
                .addTag(WORKER_TAG)

            // Add delay if specified (for immediate retries)
            if (delayMillis > 0) {
                requestBuilder.setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            }

            val request = requestBuilder.build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP, // Keep existing work to maintain order
                request
            )
        }

    }
}