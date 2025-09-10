package com.zoner.android.ui.feature.account.complete_profile

sealed class CompleteProfileEvent {

    data object NavigateToHome : CompleteProfileEvent()

    data object NavigateToSignUp : CompleteProfileEvent()

    data class ShowErrorDialog(val message: String) : CompleteProfileEvent()

    data class ShowSnackBar(val message: String) : CompleteProfileEvent()


}