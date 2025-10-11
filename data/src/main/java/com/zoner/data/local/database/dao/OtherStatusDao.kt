package com.zoner.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zoner.data.local.database.entities.OtherUserStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OtherStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(status: OtherUserStatusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<OtherUserStatusEntity>)

    @Query("""
        SELECT * FROM other_user_status 
        WHERE expiresAt > strftime('%s','now') * 1000
        ORDER BY createdAt DESC
    """)
    fun getAllStatuses(): Flow<List<OtherUserStatusEntity>>

    @Query("SELECT * FROM other_user_status ORDER BY createdAt DESC")
    fun getAll(): Flow<List<OtherUserStatusEntity>>

    @Query("SELECT * FROM other_user_status WHERE id = :id")
    suspend fun getById(id: String): OtherUserStatusEntity?

    @Query("SELECT * FROM other_user_status WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUser(userId: String): List<OtherUserStatusEntity>

    @Query("UPDATE other_user_status SET isDownloaded = :isDownloaded, localPath = :localPath WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, localPath: String?)

    @Query("UPDATE other_user_status SET isViewed = 1 WHERE id = :id")
    suspend fun markAsViewed(id: String)

    @Query("DELETE FROM other_user_status WHERE expiresAt < :currentTime")
    suspend fun deleteExpired(currentTime: Long)

    @Query("DELETE FROM other_user_status")
    fun deleteAll()

}