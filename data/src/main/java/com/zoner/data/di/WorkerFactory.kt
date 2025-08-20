package com.zoner.data.di

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.zoner.data.workers.StatusCleanupWorker
import com.zoner.domain.repository.StatusRepository

// di/KoinWorkerFactory
class KoinWorkerFactory(
    private val statusRepository: StatusRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d("KoinWorkerFactory", "Creating worker: $workerClassName")
        return try {
            when (workerClassName) {
                StatusCleanupWorker::class.java.name -> {
                    StatusCleanupWorker(appContext, workerParameters, statusRepository).also {
                        Log.d("KoinWorkerFactory", "Worker created successfully")
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("KoinWorkerFactory", "Error creating worker", e)
            null // Let WorkManager try the default factory
        }
    }
}