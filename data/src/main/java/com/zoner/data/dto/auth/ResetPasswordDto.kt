package com.zoner.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordDto(
    val email: String,
)

@Serializable
data class ResetPasswordDto(
    val email: String,
    val newPassword: String,
    val otp: String
)

