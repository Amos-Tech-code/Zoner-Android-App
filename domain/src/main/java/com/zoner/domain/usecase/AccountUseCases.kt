package com.zoner.domain.usecase

import com.zoner.domain.model.request.LoginRequest
import com.zoner.domain.model.request.RegisterRequest
import com.zoner.domain.model.request.ResetPasswordRequest
import com.zoner.domain.repository.AccountRepository

class LoginUseCase(private val repository: AccountRepository) {

    suspend operator fun invoke(loginRequest: LoginRequest) =
        repository.login(loginRequest)
}

class GoogleSignInUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(token: String) =
        repository.googleSignIn(token)
}

class RegisterUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(registerRequest: RegisterRequest) =
        repository.register(registerRequest)
}

class ValidateUserNameUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(userId: String, username: String) =
        repository.validateUserName(userId, username)
}

class VerifyUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(email: String, otp: String) =
        repository.verify(email, otp)
}

class ForgotPasswordUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(email: String) =
        repository.forgotPassword(email)
}

class ResetPasswordUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(request: ResetPasswordRequest) =
        repository.resetPassword(request)
}

class ResendOtpUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(email: String) =
        repository.requestNewOTP(email)
}