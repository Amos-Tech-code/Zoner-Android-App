package com.zoner.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zoner.data.local.database.entities.UserStatusEntity
import com.zoner.domain.model.StatusPath
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusItemsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: UserStatusEntity)

    @Query("SELECT * FROM user_status ORDER BY createdAt ASC")
    fun getUserStatuses(): Flow<List<UserStatusEntity>>

    @Query("SELECT * FROM user_status WHERE state = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingStatuses(): List<UserStatusEntity>

    @Query("SELECT * FROM user_status WHERE id = :id")
    suspend fun getStatusById(id: String): UserStatusEntity?

    @Update
    suspend fun updateStatus(status: UserStatusEntity)

    @Query("DELETE FROM user_status WHERE id = :id")
    suspend fun deleteStatus(id: String)

    @Query("DELETE FROM user_status WHERE state = 'UPLOADED' AND uploadTime < :expiryTime")
    suspend fun deleteExpiredStatuses(expiryTime: Long)

    @Query("SELECT COUNT(*) FROM user_status")
    suspend fun getStatusCount(): Int

    // Testing
    @Query("SELECT COUNT(*) FROM user_status WHERE state = 'UPLOADED' AND uploadTime < :expiryTime")
    suspend fun getExpiredStatusCount(expiryTime: Long): Int

    @Query("SELECT id, localPath FROM user_status WHERE state = 'UPLOADED' AND uploadTime < :expiryTime")
    suspend fun getExpiredStatusesWithPaths(expiryTime: Long): List<StatusPath>

}