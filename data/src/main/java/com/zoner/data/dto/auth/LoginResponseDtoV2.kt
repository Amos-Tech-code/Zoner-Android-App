package com.zoner.data.dto.auth

import com.zoner.domain.model.DevicePlatform
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDtoV2(
    val accessToken: String,
    val accessTokenExpiresIn: Int,
    val refreshToken: String,
    val refreshTokenExpiresIn: Int,
    val user: UserV2
)

@Serializable
data class UserV2(
    val displayName: String,
    val email: String,
    val emailVerified: Boolean,
    val id: String,
    val profilePictureUrl: String,
    val registrationStage: RegistrationStage,
    val role: UserRole,
    val username: String
)

@Serializable
data class LoginRequestDtoV2(
    val email: String,
    val password: String,
    val deviceId: String,
    val deviceName: String,
    val platform: DevicePlatform
)