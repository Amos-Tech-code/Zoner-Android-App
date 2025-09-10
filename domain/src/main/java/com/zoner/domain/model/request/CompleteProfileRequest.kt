package com.zoner.domain.model.request

import android.net.Uri

data class CompleteProfileRequest(
    val userId: String,
    val username: String,
    val profilePicture: Uri?
)