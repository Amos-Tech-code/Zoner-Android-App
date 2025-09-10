package com.zoner.domain.repository

import com.zoner.domain.model.MyStatus
import com.zoner.domain.model.SaveUserStatus
import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.UserStatusSummary
import com.zoner.domain.model.request.RecordStatusInteraction
import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    suspend fun saveStatus(status: SaveUserStatus)

    suspend fun retryFailedStatuses()

    suspend fun updateStatus(status: MyStatus)

    suspend fun fetchUserStatusGroupFromLocal(): Flow<List<StatusGroup>>

    suspend fun getUserStatusSummary(): Flow<UserStatusSummary>

    suspend fun fetchOtherUsersStatusFromServer(): Flow<List<StatusGroup>>

    suspend fun fetchOtherUsersStatusFromLocal(): Flow<List<StatusGroup>>

    suspend fun downloadStatusMedia(statusId: String)

    suspend fun recordInteraction(interaction: RecordStatusInteraction)

    suspend fun deleteStatus(id: String)

    suspend fun cleanExpiredStatuses() : Int

    // Testing
    suspend fun getStatusCount() : Int

    suspend fun getExpiredStatusCount() : Int

}