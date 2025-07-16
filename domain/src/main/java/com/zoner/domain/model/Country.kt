package com.zoner.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class CountryModel(
    val name: String,
    val code: String,
    val emoji: String,
    val dialCode: String
) : Parcelable