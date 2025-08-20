package com.zoner.android.ui.feature.account.sign_up

import com.zoner.android.ui.feature.account.sign_in.SignInEvent

sealed class SignUpEvent {

    data class ShowErrorDialog(val message: String) : SignUpEvent()

    data class ShowSnackBar(val message: String) : SignUpEvent()

    data object ShowOauthErrorDialog : SignUpEvent()
    data object NavigateToSignIn : SignUpEvent()

    data class NavigateToVerification(val userId: String? = null): SignUpEvent()

    data class NavigateToCompleteProfile(val userId: String? = null): SignUpEvent()


}