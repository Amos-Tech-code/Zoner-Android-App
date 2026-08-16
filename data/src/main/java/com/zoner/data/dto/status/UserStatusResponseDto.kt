package com.zoner.data.dto.status

import kotlinx.serialization.Serializable

@Serializable
data class UserStatusResponseDto(
    val `data`: UserStatusDto,
    val message: String,
    val success: Boolean
)

@Serializable
data class UserStatusDto(
    val statuses: List<StatusResponseDto>
)
