package com.zoner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zoner.data.local.database.dao.OtherStatusDao
import com.zoner.data.local.database.dao.StatusInteractionDao
import com.zoner.data.local.database.dao.UserStatusDao
import com.zoner.data.local.database.entities.OtherUserStatusEntity
import com.zoner.data.local.database.entities.StatusInteractionEntity
import com.zoner.data.local.database.entities.UserStatusEntity

@Database(
    entities = [
        UserStatusEntity::class, OtherUserStatusEntity::class, StatusInteractionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZonerDatabase : RoomDatabase() {

    abstract fun userStatusDao() : UserStatusDao

    abstract fun otherUsersStatusDao() : OtherStatusDao

    abstract fun statusInteractionsDao() : StatusInteractionDao

}