package com.zoner.data.di

import com.zoner.data.local.datastore.ZonerSession
import com.zoner.data.local.datastore.dataStoreImpl
import com.zoner.data.local.source.CountryLocalDataSource
import org.koin.dsl.module

val localModule = module {

    // Data store
    single { dataStoreImpl(get()) }
    // Zoner Session
    single { ZonerSession(get()) }
    // Country local data source
    single { CountryLocalDataSource(get()) }

}