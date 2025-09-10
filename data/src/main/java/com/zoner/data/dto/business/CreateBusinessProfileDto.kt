package com.zoner.data.dto.business

import kotlinx.serialization.Serializable

@Serializable
data class CreateBusinessProfileDto(
    val businessName: String,
    val category: String,
    val phoneNumber: String,
    val description: String,
    val location: String,
    val country: String,
    val isTermsAccepted: Boolean
)