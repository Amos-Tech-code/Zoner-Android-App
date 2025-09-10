package com.zoner.data.mappers

import com.zoner.data.dto.auth.CheckUserNameResponseDto
import com.zoner.data.dto.auth.RegisterDataDto
import com.zoner.data.dto.auth.RegisterRequestDto
import com.zoner.data.dto.auth.RegisterResponseDto
import com.zoner.data.dto.auth.ResendOtpResponseDto
import com.zoner.data.dto.auth.ResetPasswordDto
import com.zoner.data.dto.business.CreateBusinessProfileDto
import com.zoner.domain.model.request.CreateBusinessProfile
import com.zoner.domain.model.request.RegisterRequest
import com.zoner.domain.model.request.ResetPasswordRequest
import com.zoner.domain.model.response.RegisterData
import com.zoner.domain.model.response.RegisterResponse
import com.zoner.domain.model.response.ResendOtpResponse
import com.zoner.domain.model.response.UsernameAvailability

fun RegisterRequest.toDto(): RegisterRequestDto {
    return RegisterRequestDto(
        name = name,
        email = email,
        password = password,
        role = role,
    )
}

fun RegisterResponseDto.toDomain(): RegisterResponse {
    return RegisterResponse(
        data = data?.toDomain(),
        message = message,
    )
}

fun RegisterDataDto.toDomain(): RegisterData {
    return RegisterData(
        currentStage = currentStage,
        isExistingUser = isExistingUser,
        nextAction = nextAction,
        userId = userId
    )
}


fun CheckUserNameResponseDto.toDomain(): UsernameAvailability {
    return UsernameAvailability(
        isAvailable = data?.available ?: false,
        suggestions = data?.suggestions.orEmpty()
    )
}

fun ResendOtpResponseDto.toDomain(): ResendOtpResponse {
    return ResendOtpResponse(
        message = message,
        userId = data?.userId,
        currentStage = data?.currentStage
    )
}

fun ResetPasswordRequest.toDto() : ResetPasswordDto {
    return ResetPasswordDto(
        email = email,
        newPassword = password,
        otp = otp
    )
}


fun CreateBusinessProfile.toDto(): CreateBusinessProfileDto {
    return CreateBusinessProfileDto(
        businessName = businessName,
        category = category,
        location = location,
        country = country,
        phoneNumber = phoneNumber,
        description = description,
        isTermsAccepted = isTermsAccepted
    )
}