package com.zoner.data.dto.health

data class HealthCheckDto(
    val groups: List<String>,
    val status: String
)