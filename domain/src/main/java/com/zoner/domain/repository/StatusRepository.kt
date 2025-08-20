package com.zoner.domain.repository

import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.UserStatus
import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    suspend fun saveStatus(status: UserStatus)

    suspend fun getStatusGroups(): Flow<List<StatusGroup>>

    suspend fun getPendingStatuses(): List<UserStatus>

    suspend fun getStatusById(id: String): UserStatus?

    suspend fun updateStatus(status: UserStatus)

    suspend fun deleteStatus(id: String)

    suspend fun cleanExpiredStatuses() : Int

    suspend fun markStatusAsViewed(statusId: String)

    // Testing
    suspend fun getStatusCount() : Int

    suspend fun getExpiredStatusCount(expiryTime: Long) : Int

}