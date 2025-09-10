package com.zoner.data.mappers

import com.zoner.data.local.source.Country
import com.zoner.domain.model.CountryModel

fun Country.toDomain() : CountryModel {
    return CountryModel(
        name = this.name,
        code = this.code,
        emoji = this.emoji,
        dialCode = this.dialCode
    )
}

fun List<Country>.toDomainCountryList() : List<CountryModel> = map { it.toDomain() }
