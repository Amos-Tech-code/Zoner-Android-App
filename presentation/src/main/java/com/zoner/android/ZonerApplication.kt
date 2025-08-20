package com.zoner.android

import android.app.Application
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.zoner.android.di.presentationModule
import com.zoner.android.mediaplaybackmanager.MediaPlaybackManager
import com.zoner.data.di.KoinWorkerFactory
import com.zoner.data.di.dataModule
import com.zoner.data.workers.StatusCleanupWorker
import com.zoner.domain.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

class ZonerApplication : Application(), Configuration.Provider {
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. Cancel any existing work
        //WorkManager.getInstance(this).cancelUniqueWork("status_cleanup")

        MediaPlaybackManager.initialize(this)
        startKoin {
            androidContext(this@ZonerApplication)
            properties(
                mapOf("isDebug" to BuildConfig.DEBUG) // ✅ Pass debug flag
            )
            modules(
                listOf(
                    presentationModule,
                    domainModule,
                    dataModule
                )
            )
        }

        // Enqueue the worker after Koin is initialized
        StatusCleanupWorker.enqueue(this)

        // 4. Monitor
        monitorWorkStatus()

    }

    // Provide WorkManager config
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(getKoin().get<KoinWorkerFactory>())
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

    private fun monitorWorkStatus() {
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData("status_cleanup")
            .observeForever { workInfos ->
                workInfos?.forEach { workInfo ->
                    when (workInfo.state) {
                        WorkInfo.State.ENQUEUED ->
                            Log.d("WorkerStatus", "Worker enqueued")
                        WorkInfo.State.RUNNING ->
                            Log.d("WorkerStatus", "Worker running")
                        WorkInfo.State.SUCCEEDED ->
                            Log.d("WorkerStatus", "Worker succeeded")
                        WorkInfo.State.FAILED -> {
                            Log.e("WorkerStatus", "Worker failed. Output: ${workInfo.outputData}")
                            // Check for exceptions in Logcat around this time
                        }
                        WorkInfo.State.BLOCKED ->
                            Log.d("WorkerStatus", "Worker blocked")
                        WorkInfo.State.CANCELLED ->
                            Log.d("WorkerStatus", "Worker cancelled")
                    }
                }
            }
    }

}
