package com.zoner.data.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zoner.domain.repository.StatusRepository
import org.koin.java.KoinJavaComponent.getKoin
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class StatusCleanupWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val statusRepository: StatusRepository? = null
) : CoroutineWorker(context, workerParams) {

    private val TAG = "StatusCleanupWorker"

    // ✅ fallback constructor (for reflection case)
    constructor(context: Context, workerParams: WorkerParameters) :
            this(context, workerParams, null)

    // ✅ always resolve repository (DI first, fallback to lazy Koin lookup)
    private val repository: StatusRepository by lazy {
        statusRepository ?: getKoin().get()
    }

    override suspend fun doWork(): Result {
        return try {
            val expiredCount = repository.getExpiredStatusCount()

            if (expiredCount == 0) {
                Log.d(TAG, "No expired statuses found - skipping cleanup")
                return Result.success() // Success with no work needed
            }

            Log.d(TAG, "Found $expiredCount expired statuses - proceeding with cleanup")

            val deletedCount = repository.cleanExpiredStatuses()

            Log.d(TAG, "Successfully cleaned up $deletedCount expired statuses")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup failed", e)
            Result.retry()
        }
    }

    companion object {

        private const val UNIQUE_WORK_NAME = "status_cleanup"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true) // Don't run when battery is low
                .build()

            val request = PeriodicWorkRequestBuilder<StatusCleanupWorker>(
                24, TimeUnit.HOURS,
                3, TimeUnit.HOURS    // Flex interval
            )
                .setConstraints(constraints)
                .addTag(UNIQUE_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

    }
}