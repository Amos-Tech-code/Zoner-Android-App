package com.zoner.data.remote

import com.zoner.data.dto.health.HealthCheckDto
import com.zoner.data.dto.auth.LoginRequestDto
import com.zoner.data.dto.auth.LoginRequestDtoV2
import com.zoner.data.dto.auth.LoginResponseDto
import com.zoner.data.dto.auth.LoginResponseDtoV2
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiServiceV2 {

    @GET("actuator/health")
    suspend fun checkHealth(): Response<HealthCheckDto>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDtoV2): Response<LoginResponseDtoV2>

}