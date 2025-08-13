package com.zoner.data.di

import android.content.Context
import com.zoner.data.network.ConnectivityObserverImpl
import com.zoner.data.remote.ApiService
import com.zoner.data.remote.AuthInterceptor
import com.zoner.data.remote.NetworkServiceImpl
import com.zoner.data.workers.StatusCleanupWorker
import com.zoner.domain.network.ConnectivityObserver
import com.zoner.domain.network.NetworkService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://zooner.onrender.com/api/"

val networkModule = module {

    // Single instance of OkHttpClient
    single {
        val isDebug: Boolean = getProperty("isDebug") ?: false
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(get<AuthInterceptor>()) // Inject AuthInterceptor
            .build()
    }

    // Single instance of ApiService
    single<ApiService> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get()) // Use OkHttpClient from Koin
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Single instance of NetworkService
    single<NetworkService> {
        NetworkServiceImpl(get(), get())
    }
    // Single instance of Connectivity Observer
    single<ConnectivityObserver> { ConnectivityObserverImpl(get()) }

}