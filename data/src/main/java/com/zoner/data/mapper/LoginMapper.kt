package com.zoner.data.mapper

import com.zoner.data.dto.auth.BusinessProfileDto
import com.zoner.data.dto.auth.LoginRequestDto
import com.zoner.data.dto.auth.LoginResponseDto
import com.zoner.data.dto.auth.UserDto
import com.zoner.domain.model.request.LoginRequest
import com.zoner.domain.model.response.BusinessProfile
import com.zoner.domain.model.response.LoginResponse
import com.zoner.domain.model.response.User

fun LoginRequest.toDto() = LoginRequestDto(
    email = email,
    password = password
)


fun LoginResponseDto.toDomain(): LoginResponse {
    val safeToken = data?.token ?: ""
    val safeUser = data?.user?.toDomain() ?: User(
        id = "",
        name = "",
        username = "",
        email = "",
        profilePicUrl = null,
        registrationStage = "",
        role = "",
        businessProfile = null
    )

    return LoginResponse(
        token = safeToken,
        user = safeUser,
        message = message,
    )
}

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        username = username,
        email = email,
        profilePicUrl = profilePicUrl,
        registrationStage = registrationStage,
        role = role,
        businessProfile = businessProfile?.toDomain()
    )
}

fun BusinessProfileDto.toDomain(): BusinessProfile {
    return BusinessProfile(
        businessEmail = businessEmail,
        businessLogo = businessLogo,
        businessName = businessName,
        isVerified = isVerified
    )
}