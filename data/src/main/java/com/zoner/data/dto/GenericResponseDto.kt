package com.zoner.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenericResponseDto(
    val success: Boolean,
    val message: String
)