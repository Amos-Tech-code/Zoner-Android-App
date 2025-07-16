package com.zoner.android.ui.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.domain.usecase.OnboardingUseCases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingUseCases: OnboardingUseCases
) : ViewModel() {

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            onboardingUseCases.setOnboardingCompleted()
        }
    }

    fun getOnboardingState(): Flow<Boolean> =
        onboardingUseCases.getOnboardingCompleted()

}


data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes
    val imageRes: Int
)