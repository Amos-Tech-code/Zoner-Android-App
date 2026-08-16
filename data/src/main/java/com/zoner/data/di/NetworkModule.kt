package com.zoner.data.di

import com.zoner.data.network.ConnectivityObserverImpl
import com.zoner.data.remote.ApiService
import com.zoner.data.remote.ApiServiceV2
import com.zoner.data.remote.AuthInterceptor
import com.zoner.data.remote.NetworkServiceImpl
import com.zoner.data.remote.TimeoutInterceptor
import com.zoner.domain.network.ConnectivityObserver
import com.zoner.domain.network.NetworkService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://zoner-server.onrender.com/"
const val BASE_URL_V2 = "https://zoner-v2.onrender.com/"

val networkModule = module {

    // Single instance of OkHttpClient
    single {
        val isDebug: Boolean = getProperty("isDebug")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(get<TimeoutInterceptor>()) // Timeout Interceptor
            .addInterceptor(get<AuthInterceptor>()) // Auth Interceptor
            .build()
    }

    // Auth Interceptor
    single{ AuthInterceptor(get()) }

    // Timeout Interceptor
    single { TimeoutInterceptor() }

    // Single instance of NetworkService
    single<NetworkService> {
        NetworkServiceImpl(
            get(named("v1")),
            get(named("v2")),
            get(),
            get()
        )
    }

    // Single instance of Connectivity Observer
    single<ConnectivityObserver> { ConnectivityObserverImpl(get()) }

    // Single instance of ApiService V1
    single<ApiService>(
        qualifier = named("v1")
    ) {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get()) // Use OkHttpClient from Koin
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Single instance of ApiService V2
    single<ApiServiceV2>(
        qualifier = named("v2")
    ) {
        Retrofit.Builder()
            .baseUrl(BASE_URL_V2)
            .client(get()) // Use OkHttpClient from Koin
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceV2::class.java)
    }



}