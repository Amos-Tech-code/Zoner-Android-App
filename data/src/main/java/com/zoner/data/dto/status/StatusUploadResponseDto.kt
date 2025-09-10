package com.zoner.data.dto.status

import kotlinx.serialization.Serializable

@Serializable
data class StatusUploadResponseDto(
    val id: String,                 // serverId
    val mediaUrl: String,           // final hosted media URL
    val caption: String?,
    val mediaType: String,
    val blurHash: String? = null,

    val createdAt: Long,            // server timestamp
    val lastUpdated: Long,          // server-side last update
    val expiresAt: Long,            // usually createdAt + 24h

    val version: Int = 0            // optimistic concurrency
)
