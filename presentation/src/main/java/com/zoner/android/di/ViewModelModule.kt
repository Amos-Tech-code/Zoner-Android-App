package com.zoner.android.di

import com.zoner.android.MainViewModel
import com.zoner.android.ui.feature.add_post.AddPostViewModel
import com.zoner.android.ui.feature.country_selection.CountrySelectionViewModel
import com.zoner.android.ui.feature.home.HomeViewModel
import com.zoner.android.ui.feature.notifications.NotificationViewModel
import com.zoner.android.ui.feature.onboarding.OnboardingViewModel
import com.zoner.android.ui.feature.account.otp_verify.OtpVerificationViewModel
import com.zoner.android.ui.feature.profile.ProfileViewModel
import com.zoner.android.ui.feature.search.SearchViewModel
import com.zoner.android.ui.feature.account.sign_in.SignInViewModel
import com.zoner.android.ui.feature.account.sign_up.SignUpViewModel
import com.zoner.android.ui.feature.add_business_profile.CreateBusinessProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule  = module {

    viewModel { MainViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { SignInViewModel() }
    viewModel { SignUpViewModel() }
    viewModel { CountrySelectionViewModel(get()) }
    viewModel { OtpVerificationViewModel() }
    viewModel { HomeViewModel() }
    viewModel { SearchViewModel() }
    viewModel { AddPostViewModel() }
    viewModel { NotificationViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { CreateBusinessProfileViewModel() }
}