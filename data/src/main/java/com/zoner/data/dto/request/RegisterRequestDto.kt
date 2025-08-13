package com.zoner.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val phone_number: String,
    val role: String,
    val username: String
)