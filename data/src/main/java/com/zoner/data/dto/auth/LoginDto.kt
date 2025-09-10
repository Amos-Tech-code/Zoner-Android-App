package com.zoner.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val `data`: Data?,
    val message: String,
    val success: Boolean
)

@Serializable
data class Data(
    val token: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val businessProfile: BusinessProfileDto?,
    val email: String,
    val id: String,
    val name: String,
    val profilePicUrl: String?,
    val registrationStage: String,
    val role: String,
    val username: String
)

@Serializable
data class BusinessProfileDto(
    val businessEmail: String?,
    val businessLogo: String?,
    val businessName: String,
    val isVerified: Boolean
)

@Serializable
data class OauthRequestDto(
    val provider: String,
    val token: String
)
