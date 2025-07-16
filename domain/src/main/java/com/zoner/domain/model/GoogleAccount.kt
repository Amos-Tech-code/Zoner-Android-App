package com.zoner.domain.model

data class GoogleAccount(
    val token: String,
    val profileName: String,
    val profileImageUrl: String?
)