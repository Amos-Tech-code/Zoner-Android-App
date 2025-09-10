package com.zoner.android

import android.app.Application
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.Configuration
import com.zoner.android.di.presentationModule
import com.zoner.android.mediaplaybackmanager.MediaPlaybackManager
import com.zoner.data.di.KoinWorkerFactory
import com.zoner.data.di.dataModule
import com.zoner.domain.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

class ZonerApplication : Application(), Configuration.Provider {
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

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

    }

    // Provide WorkManager config
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(getKoin().get<KoinWorkerFactory>())
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

}
