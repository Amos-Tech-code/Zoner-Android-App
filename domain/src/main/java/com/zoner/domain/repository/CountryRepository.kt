package com.zoner.domain.repository

import com.zoner.domain.model.CountryModel

interface CountryRepository {

    suspend fun getAllCountries(): List<CountryModel>

}