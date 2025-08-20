package com.zoner.data.mapper

import com.zoner.data.dto.GenericResponseDto
import com.zoner.domain.model.response.GenericResponse

fun GenericResponseDto.toDomain() : GenericResponse {
    return GenericResponse(
        message = this.message
    )
}