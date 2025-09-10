package com.zoner.data.di

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.zoner.data.local.database.dao.UserStatusDao
import com.zoner.data.workers.StatusCleanupWorker
import com.zoner.data.workers.StatusSyncWorker
import com.zoner.domain.network.NetworkService
import com.zoner.domain.repository.StatusRepository
import org.koin.java.KoinJavaComponent.getKoin

// di/KoinWorkerFactory
class KoinWorkerFactory() : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d("KoinWorkerFactory", "Creating worker: $workerClassName")
        return try {
            when (workerClassName) {
                StatusCleanupWorker::class.java.name -> {
                    val statusRepository = getKoin().get<StatusRepository>()
                    StatusCleanupWorker(appContext, workerParameters, statusRepository).also {
                        Log.d("KoinWorkerFactory", "Worker created successfully")
                    }
                }
                StatusSyncWorker::class.java.name -> {
                    // Get dependencies from Koin
                    val userStatusDao = getKoin().get<UserStatusDao>()
                    val networkService = getKoin().get<NetworkService>()
                    StatusSyncWorker(appContext, workerParameters, userStatusDao, networkService)
                }

                else -> null
            }
        } catch (e: Exception) {
            Log.e("KoinWorkerFactory", "Error creating worker", e)
            null // Let WorkManager try the default factory
        }
    }
}