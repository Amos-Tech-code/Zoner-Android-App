package com.zoner.data.mappers

import com.zoner.data.dto.status.StatusUploadResponseDto
import com.zoner.domain.model.response.StatusUploadResponse

fun StatusUploadResponseDto.toDomain(): StatusUploadResponse {
    return StatusUploadResponse(
        id = id,
        mediaUrl = mediaUrl,
        caption = caption,
        mediaType = mediaType,
        blurHash = blurHash,
        createdAt = createdAt,
        lastUpdated = lastUpdated,
        expiresAt = expiresAt
    )
}