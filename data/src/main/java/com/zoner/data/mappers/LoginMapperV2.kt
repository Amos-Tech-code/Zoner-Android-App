package com.zoner.data.mappers

import com.zoner.data.dto.auth.LoginRequestDtoV2
import com.zoner.data.dto.auth.LoginResponseDtoV2
import com.zoner.domain.model.request.LoginRequestV2
import com.zoner.domain.model.response.LoginResponseV2
import com.zoner.domain.model.response.UserV2

fun LoginRequestV2.toDto() = LoginRequestDtoV2(
    email = email,
    password = password,
    deviceId = deviceId,
    deviceName = deviceName,
    platform = platform
)

fun LoginResponseDtoV2.toDomain() = LoginResponseV2(
    accessToken = accessToken,
    accessTokenExpiresIn = accessTokenExpiresIn,
    refreshToken = refreshToken,
    refreshTokenExpiresIn = refreshTokenExpiresIn,
    user = user.toDomain()
)

fun com.zoner.data.dto.auth.UserV2.toDomain() = UserV2(
    displayName = displayName,
    email = email,
    emailVerified = emailVerified,
    id = id,
    profilePictureUrl = profilePictureUrl,
    registrationStage = registrationStage,
    role = role,
    username = username
)
