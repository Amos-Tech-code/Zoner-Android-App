package com.zoner.domain.network

import android.net.Uri
import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.InteractionType
import com.zoner.domain.model.SaveUserStatus
import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.request.CompleteProfileRequest
import com.zoner.domain.model.request.CreateBusinessProfile
import com.zoner.domain.model.request.LoginRequest
import com.zoner.domain.model.request.RegisterRequest
import com.zoner.domain.model.request.ResetPasswordRequest
import com.zoner.domain.model.response.GenericResponse
import com.zoner.domain.model.response.LoginResponse
import com.zoner.domain.model.response.RegisterResponse
import com.zoner.domain.model.response.ResendOtpResponse
import com.zoner.domain.model.response.StatusUploadResponse
import com.zoner.domain.model.response.UsernameAvailability

// Network Service
interface NetworkService {

    /**
     * User Account Related Services
     */
    suspend fun login(loginRequest: LoginRequest) : ResultWrapper<LoginResponse>

    suspend fun register(registerRequest: RegisterRequest) : ResultWrapper<RegisterResponse>

    suspend fun verifyEmail(userId: String, otp: String) : ResultWrapper<RegisterResponse>

    suspend fun requestNewOTP(userId: String) : ResultWrapper<ResendOtpResponse>
    suspend fun checkUsernameAvailability(userId: String, username: String) : ResultWrapper<UsernameAvailability>

    suspend fun completeProfile(completeProfileRequest: CompleteProfileRequest): ResultWrapper<LoginResponse>

    suspend fun googleSignIn(token: String) : ResultWrapper<LoginResponse>

    suspend fun googleSignUp(token: String) : ResultWrapper<RegisterResponse>

    suspend fun forgotPassword(email: String) : ResultWrapper<GenericResponse>

    suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest) : ResultWrapper<GenericResponse>

    suspend fun newOTPForPasswordReset(email: String) : ResultWrapper<GenericResponse>

    /**
     * Business Profile Related Services
     */
    suspend fun createBusinessProfile(businessProfile: CreateBusinessProfile): ResultWrapper<LoginResponse>


    /**
     * Status Related Services
     */
    suspend fun uploadStatus(status: SaveUserStatus): ResultWrapper<StatusUploadResponse>

    suspend fun getUserStatuses(): List<StatusGroup>

    suspend fun getOtherUsersStatuses(): List<StatusGroup>

    suspend fun downloadStatusMedia(serverId: String): ByteArray

    suspend fun deleteStatus(statusId: String) : ResultWrapper<GenericResponse>

    suspend fun updateStatus(statusId: String, status: SaveUserStatus) : ResultWrapper<GenericResponse>

    suspend fun recordInteraction(
        statusId: String,
        userId: String,
        type: InteractionType,
        replyText: String?,
        replyMediaUri: Uri?
    )
}