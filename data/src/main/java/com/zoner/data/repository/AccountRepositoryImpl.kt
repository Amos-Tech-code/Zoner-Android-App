package com.zoner.data.repository

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
import com.zoner.domain.network.NetworkService
import com.zoner.domain.repository.AccountRepository

class AccountRepositoryImpl(private val networkService: NetworkService) : AccountRepository {

    override suspend fun login(loginRequest: LoginRequest): ResultWrapper<LoginResponse> {
        return networkService.login(loginRequest)
    }

    override suspend fun loginV2(loginRequest: LoginRequestV2): ResultWrapper<LoginResponseV2> {
        return networkService.loginV2(loginRequest)
    }

    override suspend fun register(registerRequest: RegisterRequest): ResultWrapper<RegisterResponse> {
        return networkService.register(registerRequest)
    }

    override suspend fun validateUserName(userId: String, username: String): ResultWrapper<UsernameAvailability> {
        return networkService.checkUsernameAvailability(userId, username)
    }

    override suspend fun completeProfile(completeProfileRequest: CompleteProfileRequest): ResultWrapper<LoginResponse> {
        return networkService.completeProfile(completeProfileRequest)
    }

    override suspend fun googleSignIn(token: String): ResultWrapper<LoginResponse> {
        return networkService.googleSignIn(token)
    }

    override suspend fun googleSignUp(token: String): ResultWrapper<RegisterResponse> {
        return networkService.googleSignUp(token)
    }

    override suspend fun forgotPassword(email: String): ResultWrapper<GenericResponse> {
        return networkService.forgotPassword(email)
    }

    override suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest): ResultWrapper<GenericResponse> {
        return networkService.resetPassword(resetPasswordRequest)
    }

    override suspend fun newOTPForPasswordReset(email: String): ResultWrapper<GenericResponse> {
        return networkService.newOTPForPasswordReset(email)
    }

    override suspend fun verify(
        userId: String,
        otp: String
    ): ResultWrapper<RegisterResponse> {
        return networkService.verifyEmail(userId, otp)
    }

    override suspend fun requestNewOTP(userId: String): ResultWrapper<ResendOtpResponse> {
        return networkService.requestNewOTP(userId)
    }

}