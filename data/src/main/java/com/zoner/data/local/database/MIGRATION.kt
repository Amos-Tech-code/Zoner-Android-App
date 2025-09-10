package com.zoner.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Create a migration object when you need to change schema
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Execute SQL for migration
        // Add non-nullable with default value

    }
}