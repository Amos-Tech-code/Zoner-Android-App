package com.zoner.domain.repository

import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.request.CompleteProfileRequest
import com.zoner.domain.model.request.LoginRequest
import com.zoner.domain.model.request.LoginRequestV2
import com.zoner.domain.model.request.RegisterRequest
import com.zoner.domain.model.request.ResetPasswordRequest
import com.zoner.domain.model.response.GenericResponse
import com.zoner.domain.model.response.LoginResponse
import com.zoner.domain.model.response.LoginResponseV2
import com.zoner.domain.model.response.RegisterResponse
import com.zoner.domain.model.response.ResendOtpResponse
import com.zoner.domain.model.response.UsernameAvailability

interface AccountRepository {

    suspend fun login(loginRequest: LoginRequest) : ResultWrapper<LoginResponse>

    suspend fun loginV2(loginRequest: LoginRequestV2) : ResultWrapper<LoginResponseV2>

    suspend fun register(registerRequest: RegisterRequest) : ResultWrapper<RegisterResponse>

    suspend fun verify(userId: String, otp: String) : ResultWrapper<RegisterResponse>

    suspend fun validateUserName(userId: String, username: String): ResultWrapper<UsernameAvailability>
    suspend fun completeProfile(completeProfileRequest: CompleteProfileRequest): ResultWrapper<LoginResponse>

    suspend fun googleSignIn(token: String) : ResultWrapper<LoginResponse>

    suspend fun googleSignUp(token: String) : ResultWrapper<RegisterResponse>

    suspend fun forgotPassword(email: String) : ResultWrapper<GenericResponse>

    suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest) : ResultWrapper<GenericResponse>

    suspend fun newOTPForPasswordReset(email: String) : ResultWrapper<GenericResponse>

    suspend fun requestNewOTP(userId: String) : ResultWrapper<ResendOtpResponse>

}