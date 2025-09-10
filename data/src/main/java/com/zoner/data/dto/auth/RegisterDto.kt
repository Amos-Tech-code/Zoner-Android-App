package com.zoner.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val name: String,
    val password: String,
    val role: String? = null,
)

@Serializable
data class RegisterResponseDto(
    val `data`: RegisterDataDto? = null,
    val message: String,
    val success: Boolean
)

@Serializable
data class RegisterDataDto(
    val currentStage: String,
    val isExistingUser: Boolean,
    val nextAction: String,
    val userId: String
)

@Serializable
data class VerifyEmailRequestDto(
    val userId: String,
    val code: String
)

@Serializable
data class ResendOtpRequestDto(
    val userId: String,
)

@Serializable
data class ResendOtpResponseDto(
    val `data`: ResendOtpDataDto? = null,
    val message: String,
    val success: Boolean
)

@Serializable
data class ResendOtpDataDto(
    val currentStage: String? = null,
    val userId: String? = null
)