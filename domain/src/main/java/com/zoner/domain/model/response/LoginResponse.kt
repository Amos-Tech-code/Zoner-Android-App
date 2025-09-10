package com.zoner.domain.model.response

data class LoginResponse(
    val token: String,
    val user: User,
    val message: String
)

data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val profilePicUrl: String?,
    val registrationStage: String,
    val role: String,
    val businessProfile: BusinessProfile?
)

data class BusinessProfile(
    val businessEmail: String?,
    val businessLogo: String?,
    val businessName: String,
    val isVerified: Boolean
)
