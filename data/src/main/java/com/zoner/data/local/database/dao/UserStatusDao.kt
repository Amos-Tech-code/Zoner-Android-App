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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStatuses(statuses: List<UserStatusEntity>)

    @Query("SELECT serverId FROM user_status WHERE serverId IN (:serverIds)")
    suspend fun getExistingServerIds(serverIds: List<String>): List<String>

    @Query("SELECT * FROM user_status WHERE deleted = 0 AND (expiresAt IS NULL OR expiresAt > strftime('%s','now') * 1000) ORDER BY createdAt ASC")
    fun getUserStatuses(): Flow<List<UserStatusEntity>>

    @Query("SELECT * FROM user_status WHERE isSynced = 0 OR deleted = 1")
    suspend fun getPendingStatuses(): List<UserStatusEntity>

    @Query("UPDATE user_status SET" +
            " serverId=:serverId, state='UPLOADED', createdAt=:createdAt," +
            " lastUpdated=:lastUpdated, version=:version," +
            " isSynced=:isSynced, expiresAt=:expiresAt" +
            " WHERE localId=:localId")
    suspend fun updateStatusAfterSync(
        localId: Long,
        serverId: String,
        createdAt: Long,
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

    @Update
    suspend fun updateStatus(status: UserStatusEntity)

    @Query("""
        SELECT COUNT(*) FROM user_status 
        WHERE deleted = 0 
        AND (expiresAt IS NULL OR expiresAt > strftime('%s','now') * 1000)
    """)
    fun getStatusCountFlow(): Flow<Int>

    @Query("""
        SELECT state, COUNT(*) as count FROM user_status 
        WHERE deleted = 0 
        AND (expiresAt IS NULL OR expiresAt > strftime('%s','now') * 1000) 
        GROUP BY state
    """)
    fun getStatusCountsByState(): Flow<List<StateCount>>

    @Query("""
        SELECT * FROM user_status 
        WHERE deleted = 0 
        AND (expiresAt IS NULL OR expiresAt > strftime('%s','now') * 1000) 
        ORDER BY createdAt DESC LIMIT 1
    """)
    fun getLatestStatus(): Flow<UserStatusEntity?>

    @Query("SELECT COUNT(*) FROM user_status WHERE state = 'UPLOADED' AND expiresAt < :currentTime")
    suspend fun getExpiredStatusCount(currentTime: Long): Int

    @Query("SELECT localId, localPath FROM user_status WHERE state = 'UPLOADED' AND expiresAt < :currentTime")
    suspend fun getExpiredStatusesWithPaths(currentTime: Long): List<StatusPath>

    @Query("DELETE FROM user_status WHERE localId = :id")
    suspend fun deleteStatus(id: Long)

    @Query("UPDATE user_status SET " +
            "deleted = 1, deletedAt = :currentTime, isSynced = 0  " +
            "WHERE localId = :id"
    )
    suspend fun softDeleteStatus(id: Long, currentTime: Long)

    @Query("DELETE FROM user_status WHERE state = 'UPLOADED' AND expiresAt < :currentTime")
    suspend fun deleteExpiredStatuses(currentTime: Long)

    @Query("SELECT localPath FROM user_status WHERE localPath IS NOT NULL")
    suspend fun getAllLocalPaths(): List<String?>

    @Query("DELETE FROM user_status")
    fun deleteAll()

}