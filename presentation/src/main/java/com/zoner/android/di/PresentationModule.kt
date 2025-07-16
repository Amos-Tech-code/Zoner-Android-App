package com.zoner.android.di

import org.koin.dsl.module

val presentationModule = module {
    includes(viewModelModule)

}