package com.zoner.data.utils

import org.json.JSONObject

//Helper function to extract error message from error body
fun

        extractErrorMessage(errorBody: String?, statusCode: Int): String {
    if (errorBody.isNullOrBlank()) return getDefaultErrorMessage(statusCode)

    return try {
        if (errorBody.trim().startsWith("{")) { // Process only JSON responses
            val jsonObject = JSONObject(errorBody)
            jsonObject.optString("message", getDefaultErrorMessage(statusCode))
        } else {
            getDefaultErrorMessage(statusCode) // Ignore HTML responses
        }
    } catch (e: Exception) {
        getDefaultErrorMessage(statusCode)
    }
}

fun getDefaultErrorMessage(statusCode: Int): String {
    return when (statusCode) {
        400 -> "Bad Request: The server could not understand the request."
        401 -> "Unauthorized: Please login again."
        403 -> "Forbidden: You do not have access to this resource."
        404 -> "Not Found: The requested resource was not found."
        405 -> "Method Not Allowed: Invalid request method."
        408 -> "Request Timeout: The server took too long to respond."
        409 -> "Conflict: The request conflicts with existing data."
        410 -> "Gone: This resource is no longer available."
        413 -> "Payload Too Large: Request size is too big."
        415 -> "Unsupported Media Type: The request format is not supported."
        422 -> "Unprocessable Entity: Invalid input or validation failed."
        429 -> "Too Many Requests: You are sending requests too fast. Please slow down."
        500 -> "Internal Server Error: Something went wrong on our end."
        502 -> "Bad Gateway: Server received an invalid response."
        503 -> "Service Unavailable: The server is temporarily down. Try again later."
        504 -> "Gateway Timeout: The server did not respond in time."
        else -> "An unexpected error occurred. Please try again later."
    }
}
