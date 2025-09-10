package com.zoner.domain.model.request

data class CreateBusinessProfile(
    val businessName: String,
    val category: String,
    val phoneNumber: String,
    val description: String,
    val location: String,
    val country: String,
    val isTermsAccepted: Boolean
)
