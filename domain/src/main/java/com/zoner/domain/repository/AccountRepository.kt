package com.zoner.domain.repository

import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.request.LoginRequest
import com.zoner.domain.model.request.RegisterRequest
import com.zoner.domain.model.request.ResetPasswordRequest
import com.zoner.domain.model.response.GenericResponse

interface AccountRepository {

    suspend fun login(loginRequest: LoginRequest) : ResultWrapper<GenericResponse>

    suspend fun register(registerRequest: RegisterRequest) : ResultWrapper<GenericResponse>

    suspend fun validateUserName(username: String) : ResultWrapper<GenericResponse>

    suspend fun verify(email: String, otp: String) : ResultWrapper<GenericResponse>

    suspend fun googleSignIn(token: String) : ResultWrapper<GenericResponse>

    suspend fun forgotPassword(email: String) : ResultWrapper<GenericResponse>

    suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest) : ResultWrapper<GenericResponse>

    suspend fun resendOtp(email: String) : ResultWrapper<GenericResponse>

}