package com.zoner.domain.repository

interface UserRepository {
    suspend fun logOut()
}