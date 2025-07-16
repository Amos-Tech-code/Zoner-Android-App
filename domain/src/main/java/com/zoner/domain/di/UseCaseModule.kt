package com.zoner.domain.di

import com.zoner.domain.usecase.OnboardingUseCases
import org.koin.dsl.module

val useCaseModule = module {

    factory { OnboardingUseCases(
        setOnboardingCompleted = OnboardingUseCases.SetOnboardingCompleted(get()),
        getOnboardingCompleted = OnboardingUseCases.GetOnboardingCompleted(get())
    ) }

}