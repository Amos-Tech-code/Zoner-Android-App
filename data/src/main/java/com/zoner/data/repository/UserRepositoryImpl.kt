package com.zoner.data.repository

import com.zoner.data.local.database.ZonerDatabase
import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.repository.UserRepository

class UserRepositoryImpl(
    private val session: ZonerSession,
    private val database: ZonerDatabase
) : UserRepository {
    override suspend fun logOut() {
        session.clearSession()
        database.clearAllTables()
    }
}