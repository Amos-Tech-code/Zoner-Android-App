package com.zoner.data.di

import com.zoner.data.repository.AccountRepositoryImpl
import com.zoner.data.repository.BusinessProfileRepositoryImpl
import com.zoner.data.repository.CountryRepositoryImpl
import com.zoner.data.repository.StatusRepositoryImpl
import com.zoner.data.repository.UserRepositoryImpl
import com.zoner.domain.repository.AccountRepository
import com.zoner.domain.repository.BusinessProfileRepository
import com.zoner.domain.repository.CountryRepository
import com.zoner.domain.repository.StatusRepository
import com.zoner.domain.repository.UserRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    single<CountryRepository> { CountryRepositoryImpl(get()) }

    single<StatusRepository> { StatusRepositoryImpl(get(), get(), get(), get()) }

    single<AccountRepository> { AccountRepositoryImpl(get()) }

    single<BusinessProfileRepository> { BusinessProfileRepositoryImpl(get()) }

}