package com.zoner.domain.model.response
data class RegisterResponse(
    val data: RegisterData? = null,
    val message: String,
)

data class RegisterData(
    val currentStage: String,
    val isExistingUser: Boolean,
    val nextAction: String,
    val userId: String
)

data class UsernameAvailability(
    val isAvailable: Boolean,
    val suggestions: List<String>
)

data class ResendOtpResponse(
    val message: String,
    val userId: String?,
    val currentStage: String?
)

