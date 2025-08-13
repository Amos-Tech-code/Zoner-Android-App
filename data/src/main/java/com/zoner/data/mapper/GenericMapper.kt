package com.zoner.data.mapper

import com.zoner.data.dto.response.GenericResponseDto
import com.zoner.domain.model.response.GenericResponse

fun GenericResponseDto.toDomain() : GenericResponse {
    return GenericResponse(
        success = this.success,
        message = this.message
    )
}