package com.zoner.domain.model

data class LocalUser(
    val id: String,
    val name: String? = null,
    val username: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val imgUrl: String? = null,
    val isBusiness: Boolean = false,
    val stage: RegistrationStage? = null,
    val businessName: String? = null,
    val businessLogo: String? = null,
    val isBusinessVerified: Boolean? = null
)
