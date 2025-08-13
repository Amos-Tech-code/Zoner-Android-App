package com.zoner.domain.model.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val phoneNumber: String,
    val username: String,
    val role: String
)