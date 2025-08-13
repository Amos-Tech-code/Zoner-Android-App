package com.zoner.domain.model.request

data class ResetPasswordRequest(
    val email: String,
    val password: String,
    val otp: String
)
