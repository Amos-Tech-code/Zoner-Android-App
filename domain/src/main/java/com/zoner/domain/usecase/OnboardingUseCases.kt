package com.zoner.domain.usecase

import com.zoner.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

data class OnboardingUseCases(
    val setOnboardingCompleted: SetOnboardingCompleted,
    val getOnboardingCompleted: GetOnboardingCompleted
) {
    class SetOnboardingCompleted(private val repository: OnboardingRepository) {
        suspend operator fun invoke() =
            repository.setOnboardingCompleted()
    }

    class GetOnboardingCompleted(private val repository: OnboardingRepository) {
        operator fun invoke(): Flow<Boolean> =
            repository.getOnboardingCompleted()
    }
}