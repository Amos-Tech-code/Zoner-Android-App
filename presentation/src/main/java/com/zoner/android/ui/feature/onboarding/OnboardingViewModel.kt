package com.zoner.android.ui.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.data.local.datastore.ZonerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val session: ZonerSession
) : ViewModel() {

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            session.setOnboardingCompleted()
        }
    }

}


data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes
    val imageRes: Int
)