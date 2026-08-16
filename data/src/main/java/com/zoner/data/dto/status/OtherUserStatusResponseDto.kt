package com.zoner.data.dto.status

import kotlinx.serialization.Serializable

@Serializable
data class OtherUserStatusResponseDto(
    val `data`: OtherUserStatusDataDto,
    val message: String,
    val success: Boolean
)

@Serializable
data class OtherUserStatusDataDto(
    val statusGroups: List<StatusGroupDto>,
    val totalPages: Int
)

@Serializable
data class StatusGroupDto(
    val authorAvatar: String,
    val authorId: String,
    val authorName: String,
    val statuses: List<StatusResponseDto>,
    val unviewedCount: Int,
    val updatedAt: Long
)