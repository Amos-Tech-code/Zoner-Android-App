package com.zoner.data.mapper

import com.zoner.data.dto.request.RegisterRequestDto
import com.zoner.domain.model.request.RegisterRequest

fun RegisterRequest.toDto(): RegisterRequestDto {
    return RegisterRequestDto(
        email = email,
        password = password,
        phone_number = phoneNumber,
        role = role,
        username = username
    )
}
