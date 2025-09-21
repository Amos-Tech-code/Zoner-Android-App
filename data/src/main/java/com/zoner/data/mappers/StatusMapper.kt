package com.zoner.data.mappers

import com.zoner.data.dto.status.OtherUserStatusResponseDto
import com.zoner.data.dto.status.StatusGroupDto
import com.zoner.data.dto.status.StatusResponseDto
import com.zoner.data.dto.status.StatusUploadResponseDto
import com.zoner.data.dto.status.UserStatusResponseDto
import com.zoner.domain.model.response.OtherUserStatusResponse
import com.zoner.domain.model.response.StatusGroupResponse
import com.zoner.domain.model.response.StatusResponse
import com.zoner.domain.model.response.StatusUploadResponse

fun StatusUploadResponseDto.toDomain(): StatusUploadResponse {
    val safeData = this.data

    return StatusUploadResponse(
        id = safeData?.id ?: "0",
        mediaUrl = safeData?.mediaUrl ?: "",
        caption = safeData?.caption,
        mediaType = safeData?.mediaType ?: "UNKNOWN",
        blurHash = safeData?.blurHash,
        createdAt = safeData?.createdAt ?: 0L,
        lastUpdated = safeData?.lastUpdated ?: 0L,
        expiresAt = safeData?.expiresAt ?: 0L,
        version = safeData?.version ?: 0
    )
}

fun UserStatusResponseDto.toDomain(): List<StatusResponse> {
    return data.statuses.map { dto ->
        StatusResponse(
            id = dto.id,
            mediaUrl = dto.mediaUrl,
            caption = dto.caption,
            mediaType = dto.mediaType,
            blurHash = dto.blurHash,
            createdAt = dto.createdAt,
            lastUpdated = dto.lastUpdated,
            expiresAt = dto.expiresAt,
            version = dto.version,
            durationMillis = dto.durationMillis,
            likeCount = dto.likeCount,
            replyCount = dto.replyCount,
            viewCount = dto.viewCount
        )
    }
}


fun OtherUserStatusResponseDto.toDomain(): OtherUserStatusResponse {
    return OtherUserStatusResponse(
        statusGroups = data.statusGroups.map { it.toDomain() },
        totalPages = data.totalPages
    )
}

fun StatusGroupDto.toDomain(): StatusGroupResponse {
    return StatusGroupResponse(
        authorAvatar = authorAvatar,
        authorId = authorId,
        authorName = authorName,
        statuses = statuses.map { it.toDomain() },
        unviewedCount = unviewedCount,
        updatedAt = updatedAt
    )
}

fun StatusResponseDto.toDomain(): StatusResponse {
    return StatusResponse(
        id = id,
        mediaUrl = mediaUrl,
        caption = caption,
        mediaType = mediaType,
        blurHash = blurHash,
        createdAt = createdAt,
        lastUpdated = lastUpdated,
        expiresAt = expiresAt,
        version = version,
        durationMillis = durationMillis,
        likeCount = likeCount,
        replyCount = replyCount,
        viewCount = viewCount
    )
}
