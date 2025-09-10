package com.zoner.domain.model.request

import android.net.Uri
import com.zoner.domain.model.InteractionType

data class RecordStatusInteraction(
    val statusId: String,
    val userId: String,
    val type: InteractionType,
    val replyText: String? = null,
    val replyMediaUri: Uri? = null
)
