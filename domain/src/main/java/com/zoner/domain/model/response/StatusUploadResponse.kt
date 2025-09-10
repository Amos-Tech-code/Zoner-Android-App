package com.zoner.domain.model.response

data class StatusUploadResponse(
    val id: String,
    val mediaUrl: String,
    val caption: String?,
    val mediaType: String,
    val blurHash: String? = null,
    val createdAt: Long,
    val lastUpdated: Long,
    val expiresAt: Long,
    val version: Int = 0
)

