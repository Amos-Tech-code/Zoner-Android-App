package com.zoner.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val error: String,
    val message: String,
    val path: String,
    val status: Int,
    val timestamp: String
)