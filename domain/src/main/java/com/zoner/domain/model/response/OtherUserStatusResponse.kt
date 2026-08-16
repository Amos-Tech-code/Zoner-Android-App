package com.zoner.domain.model.response

data class OtherUserStatusResponse(
    val statusGroups: List<StatusGroupResponse>,
    val totalPages: Int
)

data class StatusGroupResponse(
    val authorAvatar: String,
    val authorId: String,
    val authorName: String,
    val statuses: List<StatusResponse>,
    val unviewedCount: Int,
    val updatedAt: Long
)