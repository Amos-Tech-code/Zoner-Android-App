package com.zoner.data.remote

import android.content.Context
import android.net.Uri
import com.zoner.data.dto.auth.CheckUserNameRequestDto
import com.zoner.data.dto.auth.ForgotPasswordDto
import com.zoner.data.dto.auth.OauthRequestDto
import com.zoner.data.dto.auth.ResendOtpRequestDto
import com.zoner.data.dto.auth.VerifyEmailRequestDto
import com.zoner.data.dto.status.StatusUploadResponseDto
import com.zoner.data.mappers.toDomain
import com.zoner.data.mappers.toDto
import com.zoner.data.utils.FileConversionResult
import com.zoner.data.utils.extractErrorMessage
import com.zoner.data.utils.toMultipartBodyPart
import com.zoner.data.utils.toPlainRequestBody
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
import com.zoner.domain.network.ConnectivityObserver
import com.zoner.domain.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException


/**
 * Network Service Implementation
 * Remote network
 */
class NetworkServiceImpl(
    private val apiService: ApiService,
    private val connectivityObserver: ConnectivityObserver,
    private val appContext: Context
) : NetworkService {

    // Helper function to handle network requests and common errors
    suspend fun <T> safeApiCall(
        networkCall: suspend () -> T
    ): ResultWrapper<T> {
        return try {
            // ✅ Check connectivity before making the API call
            val isConnected = connectivityObserver.isConnected.first()
            if (!isConnected) {
                return ResultWrapper.Failure(
                    IOException("No internet connection. Please check your connection and try again.")
                )
            }

            // ✅ Execute request in IO context
            val result = withContext(Dispatchers.IO) { networkCall() }
            ResultWrapper.Success(result)

        } catch (e: HttpException) {
            val statusCode = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            val extractedMessage = extractErrorMessage(errorBody, statusCode)

            val errorMessage = when (statusCode) {
                400 -> extractedMessage
                401 -> extractedMessage
                403 -> extractedMessage
                404 -> extractedMessage
                405 -> extractedMessage
                408 -> extractedMessage
                409 -> extractedMessage
                410 -> extractedMessage
                413 -> extractedMessage
                415 -> extractedMessage
                422 -> extractedMessage
                429 -> extractedMessage
                500 -> extractedMessage
                502 -> extractedMessage
                503 -> extractedMessage
                504 -> extractedMessage
                else -> "Error $statusCode: $extractedMessage"
            }

            ResultWrapper.Failure(IOException(errorMessage))

        } catch (e: Exception) {
            if (e is CancellationException) throw e // Don’t swallow cancellations

            when (e) {
                is UnknownHostException -> ResultWrapper.Failure(
                    IOException("You're offline. Please check your internet connection and try again.")
                )
                is SocketTimeoutException -> ResultWrapper.Failure(
                    IOException("The server is taking too long to respond. Please try again later.")
                )
                else -> ResultWrapper.Failure(
                    IOException(e.localizedMessage ?: "Something went wrong. Please try again later.")
                )
            }
        }
    }

    val emptyResponseMessage = "Empty response from server. Please try again."

    override suspend fun login(loginRequest: LoginRequest): ResultWrapper<LoginResponse> {
        return safeApiCall {
            val response = apiService.login(loginRequest.toDto())
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun register(registerRequest: RegisterRequest): ResultWrapper<RegisterResponse> {
        return safeApiCall {
            val response = apiService.register(registerRequest.toDto())
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun verifyEmail(
        userId: String,
        otp: String
    ): ResultWrapper<RegisterResponse> {
        return safeApiCall {
            val response = apiService.verifyEmail(VerifyEmailRequestDto(userId, otp))
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun requestNewOTP(userId: String): ResultWrapper<ResendOtpResponse> {
        return safeApiCall {
            val response = apiService.resendOtp(ResendOtpRequestDto(userId))
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun checkUsernameAvailability(
        userId: String,
        username: String
    ): ResultWrapper<UsernameAvailability> {
        return safeApiCall {
            val response = apiService.checkUsernameAvailability(
                CheckUserNameRequestDto(
                    userId,
                    username
                )
            )
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun googleSignIn(token: String): ResultWrapper<LoginResponse> {
        return safeApiCall {
            val response = apiService.oauthLogin(OauthRequestDto(
                token = token, provider = "google"
            ))
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun googleSignUp(token: String): ResultWrapper<RegisterResponse> {
        return safeApiCall {
            val response = apiService.oauthRegister(OauthRequestDto(
                token = token, provider = "google"
            ))
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun forgotPassword(email: String): ResultWrapper<GenericResponse> {
        return safeApiCall {
            val response = apiService.forgotPassword(ForgotPasswordDto(email))
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest): ResultWrapper<GenericResponse> {
        return safeApiCall {
            val response = apiService.resetPassword(resetPasswordRequest.toDto())
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun newOTPForPasswordReset(email: String): ResultWrapper<GenericResponse> {
        return safeApiCall {
            val response = apiService.newOtpForPasswordReset(ForgotPasswordDto(email))
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun completeProfile(completeProfileRequest: CompleteProfileRequest): ResultWrapper<LoginResponse> {
        return safeApiCall {
            val userIdBody = completeProfileRequest.userId.toPlainRequestBody()
            val usernameBody = completeProfileRequest.username.toPlainRequestBody()
            val profilePicPart: MultipartBody.Part? = completeProfileRequest.profilePicture?.let { uri ->
                when (val result = uri.toMultipartBodyPart(appContext, "profilePic")) {
                    is FileConversionResult.Error -> {
                        throw IOException(result.message) // <-- user-friendly handled by safeApiCall
                    }
                    is FileConversionResult.Success -> result.part
                }
            }

            val response = apiService.completeProfile(
                userId = userIdBody,
                username = usernameBody,
                profilePic = profilePicPart
            )
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createBusinessProfile(businessProfile: CreateBusinessProfile): ResultWrapper<LoginResponse> {
        return safeApiCall {
            val response = apiService.createBusinessProfile(businessProfile.toDto())
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun uploadStatus(status: SaveUserStatus): ResultWrapper<StatusUploadResponse> {
        return safeApiCall {
            val mediaTypeBody = status.mediaType.name.toPlainRequestBody()
            val durationBody = status.durationMillis.toString().toPlainRequestBody()
            val captionBody = status.caption?.toPlainRequestBody()

            // Convert local file/uri to Multipart
            val mediaPart: MultipartBody.Part = when (val result = status.mediaUri.toMultipartBodyPart(appContext, "mediaFile")) {
                is FileConversionResult.Success -> result.part
                is FileConversionResult.Error -> throw IOException(result.message)
            }

            val response = apiService.uploadStatus(
                mediaType = mediaTypeBody,
                durationMillis = durationBody,
                caption = captionBody,
                mediaFile = mediaPart
            )

            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }


    override suspend fun getUserStatuses(): List<StatusGroup> {
        TODO("Not yet implemented")
    }

    override suspend fun getOtherUsersStatuses(): List<StatusGroup> {
        TODO("Not yet implemented")
    }

    override suspend fun downloadStatusMedia(serverId: String): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun deleteStatus(statusId: String): ResultWrapper<GenericResponse> {
        return safeApiCall {
            val response = apiService.deleteStatus(statusId)
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException(emptyResponseMessage)
                body.toDomain()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun updateStatus(
        statusId: String,
        status: SaveUserStatus
    ): ResultWrapper<GenericResponse> {
         TODO()
    }

    override suspend fun recordInteraction(
        statusId: String,
        userId: String,
        type: InteractionType,
        replyText: String?,
        replyMediaUri: Uri?
    ) {
        // TODO("Not yet implemented")
    }


}