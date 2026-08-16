package com.zoner.domain.model.request

import com.zoner.domain.model.DevicePlatform

data class LoginRequestV2(
    val email: String,
    val password: String,
    val deviceId: String,
    val deviceName: String,
    val platform: DevicePlatform
)
