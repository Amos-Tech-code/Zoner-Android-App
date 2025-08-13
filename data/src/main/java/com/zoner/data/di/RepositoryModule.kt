package com.zoner.data.di

import com.zoner.data.repository.CountryRepositoryImpl
import com.zoner.data.repository.OnboardingRepositoryImpl
import com.zoner.data.repository.StatusRepositoryImpl
import com.zoner.domain.repository.CountryRepository
import com.zoner.domain.repository.OnboardingRepository
import com.zoner.domain.repository.StatusRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }

    single<CountryRepository> { CountryRepositoryImpl(get()) }

    single<StatusRepository> { StatusRepositoryImpl(get(), get()) }

}