package com.zoner.android.ui.feature.account

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.Oauth.GoogleAuthUiProvider
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel() : ViewModel() {

    var error: String = ""
    var errorDescription = ""

    private val googleAuthUiProvider = GoogleAuthUiProvider()

    abstract fun loading()
    abstract fun onGoogleError(msg: String)
    abstract fun onGoogleLoginSuccess(token: String)

    fun onGoogleClicked(context: ComponentActivity) {
        initiateGoogleLogin(context)
    }

    private fun initiateGoogleLogin(context: ComponentActivity) {
        viewModelScope.launch {
            loading()
            try {
                val response = googleAuthUiProvider.signIn(
                    context,
                    CredentialManager.create(context)
                )
                //Log.d("Google", response.toString())
                fetchGoogleToken(response.token)
            } catch (e: Throwable) {
                onGoogleError(e.message.toString())
            }
        }
    }

    private fun fetchGoogleToken(token: String) {
        onGoogleError("An error Occurred")
//        viewModelScope.launch {
//            val result = socialSignInUseCase.executeGoogle(token)
//
//            when (result) {
//                is ResultWrapper.Success -> {
//                    onSocialLoginSuccess(result.value.message)
//                }
//
//                is ResultWrapper.Failure -> {
//                    onGoogleError(result.exception.message ?: "Unknown error")
//
//                }
//            }
//        }
    }


}