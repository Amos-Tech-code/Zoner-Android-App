package com.zoner.data.remote

import com.zoner.data.dto.GenericResponseDto
import com.zoner.data.dto.auth.CheckUserNameRequestDto
import com.zoner.data.dto.auth.CheckUserNameResponseDto
import com.zoner.data.dto.auth.ForgotPasswordDto
import com.zoner.data.dto.auth.LoginRequestDto
import com.zoner.data.dto.auth.LoginResponseDto
import com.zoner.data.dto.auth.OauthRequestDto
import com.zoner.data.dto.auth.RegisterRequestDto
import com.zoner.data.dto.auth.RegisterResponseDto
import com.zoner.data.dto.auth.ResendOtpRequestDto
import com.zoner.data.dto.auth.ResendOtpResponseDto
import com.zoner.data.dto.auth.ResetPasswordDto
import com.zoner.data.dto.auth.VerifyEmailRequestDto
import com.zoner.data.dto.business.CreateBusinessProfileDto
import com.zoner.data.dto.status.StatusUploadResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    /**
     * Auth related services
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto) : Response<LoginResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto) : Response<RegisterResponseDto>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto) : Response<RegisterResponseDto>

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequestDto) : Response<ResendOtpResponseDto>

    @POST("auth/check-username")
    suspend fun checkUsernameAvailability(@Body request: CheckUserNameRequestDto) : Response<CheckUserNameResponseDto>

    @Multipart
    @POST("auth/complete-profile")
    suspend fun completeProfile(
        @Part("userId") userId: RequestBody,
        @Part("username") username: RequestBody,
        @Part profilePic: MultipartBody.Part? = null
    ) : Response<LoginResponseDto>

    @POST("auth/oauth/login")
    suspend fun oauthLogin(@Body request: OauthRequestDto) : Response<LoginResponseDto>

    @POST("auth/oauth/register")
    suspend fun oauthRegister(@Body request: OauthRequestDto) : Response<RegisterResponseDto>

    @POST("password/forgot")
    suspend fun forgotPassword(@Body request: ForgotPasswordDto) : Response<GenericResponseDto>

    @POST("password/reset")
    suspend fun resetPassword(@Body resetPasswordDto: ResetPasswordDto) : Response<GenericResponseDto>

    @POST("password/resend-otp")
    suspend fun newOtpForPasswordReset(@Body request: ForgotPasswordDto) : Response<GenericResponseDto>

    /**
     * Business Profile
     */
    @POST("business/create")
    suspend fun createBusinessProfile(@Body request: CreateBusinessProfileDto) : Response<LoginResponseDto>


    /**
     * Status Implementation
     */
    @Multipart
    @POST("status/upload")
    suspend fun uploadStatus(
        @Part("caption") caption: RequestBody?,
        @Part("mediaType") mediaType: RequestBody,
        @Part("durationMillis") durationMillis: RequestBody,
        @Part mediaFile: MultipartBody.Part
    ): Response<StatusUploadResponseDto>

    @Multipart
    @PUT("status/{id}")
    suspend fun updateStatus(
        @Path("id") statusId: String,
        @Part("caption") caption: RequestBody?,
        @Part("mediaType") mediaType: RequestBody,
        @Part("durationMillis") durationMillis: RequestBody,
        @Part mediaFile: MultipartBody.Part? = null
    ) : Response<GenericResponseDto>

    @DELETE("/status/{id}")
    suspend fun deleteStatus(@Path("id") id: String) : Response<GenericResponseDto>

}