package com.zoner.data.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zoner.data.local.database.MIGRATION_1_2
import com.zoner.data.local.database.ZonerDatabase
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.data.local.datastore.dataStoreImpl
import com.zoner.data.local.source.CountryLocalDataSource
import com.zoner.data.utils.DeviceUtil
import com.zoner.data.utils.ZonerFileManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.koin.dsl.module

// Room database callback for prepopulation or migrations
private val roomDatabaseCallback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Perform any initialization on database creation
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Perform any actions when database is opened
    }
}

val localModule = module {

    // Provide Room Database
    single<ZonerDatabase> {
        Room.databaseBuilder(
            get(),
            ZonerDatabase::class.java,
            "zoner_database"
        )
            .addMigrations(MIGRATION_1_2) // Add all migrations
            .fallbackToDestructiveMigration(false) // Never destroy on missing migration
            .fallbackToDestructiveMigrationOnDowngrade(false) // Never destroy on downgrade
            .addCallback(roomDatabaseCallback)
            .setQueryExecutor(Dispatchers.IO.asExecutor())
            .build()
    }

    // WorkManager configuration
    single { KoinWorkerFactory() }

    // Dispatchers
    single<CoroutineDispatcher> { Dispatchers.IO }

    // Data store
    single { dataStoreImpl(get()) }
    // Zoner Session
    single { ZonerSession(get(), get()) }
    // Country local data source
    single { CountryLocalDataSource(get()) }
    // File manager util
    single { ZonerFileManager(get()) }
    // Device util
    single { DeviceUtil(get()) }

    // DAOs
    single { get<ZonerDatabase>().userStatusDao() }
    single { get<ZonerDatabase>().otherUsersStatusDao() }
    single { get<ZonerDatabase>().statusInteractionsDao() }


}