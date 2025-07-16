package com.zoner.data.local.source

import android.content.Context
import com.zoner.domain.model.CountryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream


class CountryLocalDataSource(private val context: Context) {

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun loadCountries(): List<Country> = withContext(Dispatchers.IO) {
        try {
            context.assets.open("countries.json").use { inputStream ->
                Json.decodeFromStream<List<Country>>(inputStream)
            }
        } catch (e: Exception) {
            emptyList() // Log the error if needed
        }
    }
}

@Serializable
data class Country(
    val name: String,
    val code: String,
    val emoji: String,
    val unicode: String,
    @SerialName("image") val imageName: String,
    @SerialName("dial_code") val dialCode: String
)

