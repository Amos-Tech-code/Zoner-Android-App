package com.zoner.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zoner.data.local.database.StateCount
import com.zoner.data.local.database.StatusPath
import com.zoner.data.local.database.entities.UserStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: UserStatusEntity)

    @Query("SELECT * FROM user_status ORDER BY createdAt ASC")
    fun getUserStatuses(): Flow<List<UserStatusEntity>>

    @Query("SELECT * FROM user_status WHERE isSynced = 0 OR deleted = 1")
    suspend fun getPendingStatuses(): List<UserStatusEntity>

    @Query("UPDATE user_status SET" +
            " serverId=:serverId, state='UPLOADED'," +
            " lastUpdated=:lastUpdated, version=:version," +
            " isSynced=:isSynced, expiresAt=:expiresAt" +
            " WHERE localId=:localId")
    suspend fun updateStatusAfterSync(
        localId: Long,
        serverId: String,
        expiresAt: Long,
        lastUpdated: Long?,
        version: Int,
        isSynced: Boolean
    )

    @Query("UPDATE user_status SET state='FAILED', errorMessage=:errorMessage, isSynced=0 WHERE localId=:localId")
    suspend fun markSyncFailed(localId: Long, errorMessage: String?)

    @Query("SELECT * FROM user_status WHERE state = :state")
    suspend fun getStatusesByState(state: String) : List<UserStatusEntity>

    @Query("UPDATE user_status SET state = :state WHERE localId = :localId")
    suspend fun updateStatusState(localId: Long, state: String)

    @Query("SELECT * FROM user_status WHERE localId = :id")
    suspend fun getStatusById(id: String): UserStatusEntity?

    @Query("SELECT COUNT(*) FROM user_status")
    fun getStatusCount(): Int

    @Update
    suspend fun updateStatus(status: UserStatusEntity)

    @Query("DELETE FROM user_status WHERE localId = :id")
    suspend fun deleteStatus(id: Long)

    @Query("DELETE FROM user_status WHERE state = 'UPLOADED' AND expiresAt < :expiryTime")
    suspend fun deleteExpiredStatuses(expiryTime: Long)

    @Query("SELECT COUNT(*) FROM user_status")
    fun getStatusCountFlow(): Flow<Int>
    // Group by state
    @Query("SELECT state, COUNT(*) as count FROM user_status GROUP BY state")
    fun getStatusCountsByState(): Flow<List<StateCount>>

    // Latest status (for preview in UI)
    @Query("SELECT * FROM user_status ORDER BY createdAt DESC LIMIT 1")
    fun getLatestStatus(): Flow<UserStatusEntity?>

    @Query("SELECT COUNT(*) FROM user_status WHERE state = 'UPLOADED' AND expiresAt < :currentTime")
    suspend fun getExpiredStatusCount(currentTime: Long): Int

    @Query("SELECT localId, localPath FROM user_status WHERE state = 'UPLOADED' AND expiresAt < :currentTime")
    suspend fun getExpiredStatusesWithPaths(currentTime: Long): List<StatusPath>


}