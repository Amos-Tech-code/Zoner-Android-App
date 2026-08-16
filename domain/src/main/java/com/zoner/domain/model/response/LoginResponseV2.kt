package com.zoner.domain.model.response

import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.model.UserRole

data class LoginResponseV2(
    val accessToken: String,
    val accessTokenExpiresIn: Int,
    val refreshToken: String,
    val refreshTokenExpiresIn: Int,
    val user: UserV2
)

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
