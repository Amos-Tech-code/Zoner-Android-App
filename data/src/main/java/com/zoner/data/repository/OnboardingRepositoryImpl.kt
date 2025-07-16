package com.zoner.data.repository

import com.zoner.data.local.datastore.ZonerSession
import com.zoner.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class OnboardingRepositoryImpl(private val session: ZonerSession) : OnboardingRepository {

    override suspend fun setOnboardingCompleted() {
        session.setOnboardingCompleted()
    }

    override fun getOnboardingCompleted(): Flow<Boolean> {
        return session.onboardingCompletedFlow()
    }

}