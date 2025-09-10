package com.zoner.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zoner.data.local.database.entities.StatusInteractionEntity

@Dao
interface StatusInteractionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interaction: StatusInteractionEntity)

    @Query("SELECT * FROM status_interactions WHERE statusId = :statusId AND interactionType = :type")
    suspend fun getInteractions(statusId: String, type: String): List<StatusInteractionEntity>

    @Query("UPDATE status_interactions SET isSynced = 1 WHERE statusId = :id")
    suspend fun markAsSynced(id: Long)
}