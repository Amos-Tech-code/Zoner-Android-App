package com.zoner.data.di

import com.zoner.data.remote.ApiService
import com.zoner.data.remote.AuthInterceptor
import com.zoner.data.remote.NetworkServiceImpl
import com.zoner.domain.network.NetworkService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = ""

val networkModule = module {

    // Single instance of OkHttpClient
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
                //In Production
                //level = HttpLoggingInterceptor.Level.NONE
            })
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

}