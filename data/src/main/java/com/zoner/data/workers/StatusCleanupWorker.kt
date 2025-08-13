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
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class StatusCleanupWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val statusRepository: StatusRepository // Inject directly
) : CoroutineWorker(context, workerParams) {

    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        return try {
            val expiryTime = Clock.System.now().minus(24.hours).toEpochMilliseconds()
            val expiredCount = statusRepository.getExpiredStatusCount(expiryTime)

            if (expiredCount == 0) {
                Log.d("StatusCleanupWorker", "No expired statuses found - skipping cleanup")
                return Result.success() // Success with no work needed
            }

            Log.d("StatusCleanupWorker", "Found $expiredCount expired statuses - proceeding with cleanup")

            val deletedCount = statusRepository.cleanExpiredStatuses()

            Log.d("StatusCleanupWorker", "Successfully cleaned up $deletedCount expired statuses")
            Result.success()
        } catch (e: Exception) {
            Log.e("StatusCleanupWorker", "Cleanup failed", e)
            Result.retry()
        }
    }

    companion object {

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true) // Don't run when battery is low
                .build()

            val request = PeriodicWorkRequestBuilder<StatusCleanupWorker>(
                12, TimeUnit.HOURS, // More frequent checks (minimum interval)
                3, TimeUnit.HOURS    // Flex interval
            )
                .setConstraints(constraints)
                .addTag("status_cleanup")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "status_cleanup",
                ExistingPeriodicWorkPolicy.UPDATE, // Update existing work
                request
            )
        }

//        internal fun enqueueTestWorker(context: Context) {
//            Log.d("StatusCleanupWorker", "Enqueuing TEST worker")
//
//            // No constraints for testing
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
//                .build()
//
//            val request = OneTimeWorkRequestBuilder<StatusCleanupWorker>()
//                .setConstraints(constraints)
//                .addTag("status_cleanup_test")
//                .build()
//
//            WorkManager.getInstance(context).enqueue(request)
//
//            // Monitor the worker's progress
//            WorkManager.getInstance(context)
//                .getWorkInfoByIdLiveData(request.id)
//                .observeForever { workInfo ->
//                    Log.d("WorkerTest", "Worker state: ${workInfo?.state}")
//                    if (workInfo?.state == WorkInfo.State.FAILED) {
//                        Log.e("WorkerTest", "Worker failed: ${workInfo.outputData}")
//                    }
//                }
//        }
    }
}