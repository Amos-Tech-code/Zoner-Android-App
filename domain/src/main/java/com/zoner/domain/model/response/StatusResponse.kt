package com.zoner.domain.model.response

data class StatusResponse(
    val id: String,                 // serverId
    val mediaUrl: String,           // final hosted media URL
    val caption: String?,
    val mediaType: String,
    val blurHash: String,

    val createdAt: Long,            // server timestamp
    val lastUpdated: Long,          // server-side last update
    val expiresAt: Long,            // usually createdAt + 24h

    val version: Int = 0,            // optimistic concurrency
    val durationMillis: Int,
    val likeCount: Int,
    val replyCount: Int,
    val viewCount: Int
)