package com.zoner.android

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.zoner.android.di.presentationModule
import com.zoner.android.mediaplaybackmanager.MediaPlaybackManager
import com.zoner.data.di.dataModule
import com.zoner.domain.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZonerApplication : Application() {
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        MediaPlaybackManager.initialize(this)
        startKoin {
            androidContext(this@ZonerApplication)
            modules(
                listOf(
                    presentationModule,
                    domainModule,
                    dataModule
                )
            )
        }
    }

}