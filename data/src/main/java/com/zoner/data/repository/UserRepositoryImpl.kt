package com.zoner.data.repository

import com.zoner.data.local.database.ZonerDatabase
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.data.utils.ZonerFileManager
import com.zoner.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val session: ZonerSession,
    private val database: ZonerDatabase,
    private val fileManager: ZonerFileManager
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

    private suspend fun clearDatabase() {
        deleteAllMediaFiles()
        database.runInTransaction {
            database.userStatusDao().deleteAll()
            database.otherUsersStatusDao().deleteAll()
            database.statusInteractionsDao().deleteAll()
        }
    }

    private suspend fun deleteAllMediaFiles() {
        try {
            // Get all user status files
            val userStatusFiles = database.userStatusDao().getAllLocalPaths()
            userStatusFiles.forEach { localPath ->
                localPath?.let { fileManager.deleteFileIfSafe(it) }
            }

            // Get all other user status files
            val otherStatusFiles = database.otherUsersStatusDao().getAllLocalPaths()
            otherStatusFiles.forEach { localPath ->
                localPath?.let { fileManager.deleteFileIfSafe(it) }
            }

            //Log.d("UserRepository", "Cleaned up all media files during logout")
        } catch (e: Exception) {
            //Log.e("UserRepository", "Failed to delete some media files during logout", e)
            // Continue with database cleanup even if file deletion fails
        }
    }

}