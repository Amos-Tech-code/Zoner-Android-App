package com.zoner.domain

// Api Result Wrapper
sealed class ResultWrapper<out T> {

    data class Success<out T> (val value: T) : ResultWrapper<T>()

    data class Failure(val exception: Exception) : ResultWrapper<Nothing>()

}

// Upload Status Wrapper
sealed class StatusState {
    data object Pending : StatusState()
    data object Uploading : StatusState()
    data class Failed(val error: String) : StatusState()
    data object Uploaded : StatusState()
}