package com.zoner.data.mapper

import com.zoner.data.dto.request.LoginRequestDto
import com.zoner.domain.model.request.LoginRequest

fun LoginRequest.toDto() : LoginRequestDto {
    return LoginRequestDto(
        email = email,
        password = password
    )
}