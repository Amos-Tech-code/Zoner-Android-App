package com.zoner.data.remote

import android.content.Context
import com.zoner.data.utils.extractErrorMessage
import com.zoner.domain.ResultWrapper
import com.zoner.domain.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private val appContext: Context
) : NetworkService {

    // Helper function to handle network requests and common errors
    private suspend fun <T> executeRequest(
        networkCall: suspend () -> T
    ): ResultWrapper<T> {

        return try {
            val result = withContext(Dispatchers.IO) { networkCall() }
            ResultWrapper.Success(result)
        } catch (e: retrofit2.HttpException) {  // Handle API errors
            val statusCode = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            val extractedMessage = extractErrorMessage(errorBody, statusCode)

            val errorMessage = when (statusCode) {
                400 -> "Bad Request: $extractedMessage"
                401 -> "Unauthorized: $extractedMessage"
                403 -> "Forbidden: $extractedMessage"
                404 -> "Not Found: $extractedMessage"
                405 -> "Method Not Allowed: $extractedMessage"
                408 -> "Request Timeout: $extractedMessage"
                409 -> "Conflict: $extractedMessage"
                410 -> "Gone: $extractedMessage"
                413 -> "Payload Too Large: $extractedMessage"
                415 -> "Unsupported Media Type: $extractedMessage"
                422 -> "Unprocessable Entity: $extractedMessage"
                429 -> "Too Many Requests: $extractedMessage"
                500 -> "Internal Server Error: $extractedMessage"
                502 -> "Bad Gateway: $extractedMessage"
                503 -> "Service Unavailable: $extractedMessage"
                504 -> "Gateway Timeout: $extractedMessage"
                else -> "Error $statusCode: $extractedMessage"
            }

            ResultWrapper.Failure(IOException(errorMessage))
        } catch (e: Exception) {
            if (e is CancellationException) throw e // Ensure coroutine cancellations are not swallowed

            return when (e) {
                is UnknownHostException -> {
                    ResultWrapper.Failure(
                        IOException("You're offline. Please check your internet connection and try again.")
                    )
                }
                is SocketTimeoutException -> {
                    ResultWrapper.Failure(
                        IOException("The server is taking too long to respond. Please try again later.")
                    )
                }
                else -> {
                    ResultWrapper.Failure(
                        IOException(e.localizedMessage ?: "Something went wrong. Please try again later.")
                    )
                }
            }
        }

    }

}