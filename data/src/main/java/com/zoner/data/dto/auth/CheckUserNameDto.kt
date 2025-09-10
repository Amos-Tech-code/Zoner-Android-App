package com.zoner.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class CheckUserNameRequestDto(
    val userId: String,
    val username: String
)

@Serializable
data class CheckUserNameResponseDto(
    val `data`: UsernameDataDto?,
    val message: String,
    val success: Boolean
)

@Serializable
data class UsernameDataDto(
    val available: Boolean,
    val suggestions: List<String>
)