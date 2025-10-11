package com.zoner.data.repository

import android.util.Log
import com.zoner.data.local.database.ZonerDatabase
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val session: ZonerSession,
    private val database: ZonerDatabase
) : UserRepository {

    override suspend fun logOut() {
        try {
            withContext(Dispatchers.IO) {
                clearDatabase()
            }
            session.clearSession()
        } catch (e: Exception) {
            // Handle the exception if needed
            //Log.e("UserRepository", "Failed to log out", e)
            throw e
        }
    }

    private fun clearDatabase() {
        database.runInTransaction {
            database.userStatusDao().deleteAll()
            database.otherUsersStatusDao().deleteAll()
            database.statusInteractionsDao().deleteAll()
        }
    }
}