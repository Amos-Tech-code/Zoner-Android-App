package com.zoner.data.repository

import com.zoner.data.local.source.CountryLocalDataSource
import com.zoner.data.mappers.toDomainCountryList
import com.zoner.domain.model.CountryModel
import com.zoner.domain.repository.CountryRepository

class CountryRepositoryImpl(
    private val localDataSource: CountryLocalDataSource
) : CountryRepository {
    override suspend fun getAllCountries(): List<CountryModel> {
        return localDataSource.loadCountries().toDomainCountryList()
    }
}