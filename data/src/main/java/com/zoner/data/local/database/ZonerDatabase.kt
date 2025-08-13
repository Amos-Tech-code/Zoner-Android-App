package com.zoner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zoner.data.local.database.dao.StatusItemsDao
import com.zoner.data.local.database.entities.UserStatusEntity

@Database(
    entities = [UserStatusEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ZonerDatabase : RoomDatabase() {

    abstract fun statusItemsDao() : StatusItemsDao

}