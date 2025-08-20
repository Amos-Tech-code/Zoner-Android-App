package com.zoner.android.di

import android.app.Application
import com.zoner.android.MainViewModel
import com.zoner.android.ui.feature.add_post.AddPostViewModel
import com.zoner.android.ui.feature.country_selection.CountrySelectionViewModel
import com.zoner.android.ui.feature.home.HomeViewModel
import com.zoner.android.ui.feature.notifications.NotificationViewModel
import com.zoner.android.ui.feature.onboarding.OnboardingViewModel
import com.zoner.android.ui.feature.account.otp_verify.OtpVerificationViewModel
import com.zoner.android.ui.feature.account.reset_password.ResetPasswordViewModel
import com.zoner.android.ui.feature.profile.ProfileViewModel
import com.zoner.android.ui.feature.search.SearchViewModel
import com.zoner.android.ui.feature.account.sign_in.SignInViewModel
import com.zoner.android.ui.feature.account.sign_up.SignUpViewModel
import com.zoner.android.ui.feature.add_business_profile.CreateBusinessProfileViewModel
import com.zoner.android.ui.feature.account.complete_profile.CompleteProfileViewModel
import com.zoner.android.ui.feature.post_details.PostDetailsViewModel
import com.zoner.android.ui.feature.settings.SettingsViewModel
import com.zoner.android.ui.feature.view_status.StatusViewerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule  = module {

    viewModel { MainViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { SignInViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { CompleteProfileViewModel(get(), get()) }
    viewModel { ResetPasswordViewModel(get(), get()) }
    viewModel { CountrySelectionViewModel(get()) }
    viewModel { OtpVerificationViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SearchViewModel() }
    viewModel { AddPostViewModel(get(), get(), get<Application>().applicationContext) }
    viewModel { NotificationViewModel() }
    viewModel { ProfileViewModel(get()) }
    viewModel { CreateBusinessProfileViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { PostDetailsViewModel() }
    viewModel { StatusViewerViewModel(get()) }
}