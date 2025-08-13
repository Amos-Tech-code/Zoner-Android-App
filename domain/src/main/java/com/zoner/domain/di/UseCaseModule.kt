package com.zoner.domain.di

import com.zoner.domain.usecase.ForgotPasswordUseCase
import com.zoner.domain.usecase.GoogleSignInUseCase
import com.zoner.domain.usecase.LoginUseCase
import com.zoner.domain.usecase.OnboardingUseCases
import com.zoner.domain.usecase.RegisterUseCase
import com.zoner.domain.usecase.ResendOtpUseCase
import com.zoner.domain.usecase.ResetPasswordUseCase
import com.zoner.domain.usecase.StatusItemsUseCases
import com.zoner.domain.usecase.ValidateUserNameUseCase
import com.zoner.domain.usecase.VerifyUseCase
import org.koin.dsl.module

val useCaseModule = module {

    factory { OnboardingUseCases(
        setOnboardingCompleted = OnboardingUseCases.SetOnboardingCompleted(get()),
        getOnboardingCompleted = OnboardingUseCases.GetOnboardingCompleted(get())
    ) }
    factory { LoginUseCase(get())}
    factory { RegisterUseCase(get()) }
    factory { GoogleSignInUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { VerifyUseCase(get()) }
    factory { ResendOtpUseCase(get()) }
    factory { ValidateUserNameUseCase(get()) }
    factory { StatusItemsUseCases(
        getUserStatus = StatusItemsUseCases.GetUserStatus(get()),
        saveUserStatus = StatusItemsUseCases.SaveUserStatus(get())
    ) }

}