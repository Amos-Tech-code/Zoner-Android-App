package com.zoner.domain

// Api Result Wrapper
sealed class ResultWrapper<out T> {

    data class Success<out T> (val value: T) : ResultWrapper<T>()

    data class Failure(val exception: Exception) : ResultWrapper<Nothing>()

}


sealed class StatusState {
    abstract val stateName: String
    object Pending : StatusState() { override val stateName = "PENDING" }
    object Uploading : StatusState() { override val stateName = "UPLOADING" }

    data class Failed(val error: String) : StatusState() { override val stateName = "FAILED" }
    object Uploaded : StatusState() { override val stateName = "UPLOADED" }
}
