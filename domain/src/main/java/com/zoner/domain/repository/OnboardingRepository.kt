package com.zoner.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {

    suspend fun setOnboardingCompleted()

    fun getOnboardingCompleted(): Flow<Boolean>

}