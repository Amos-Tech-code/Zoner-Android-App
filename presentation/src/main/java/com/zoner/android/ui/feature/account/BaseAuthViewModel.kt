package com.zoner.android.ui.feature.account

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoner.android.Oauth.GoogleAuthUiProvider
import com.zoner.domain.repository.AccountRepository
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel : ViewModel() {

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
                onGoogleLoginSuccess(response.token)
            } catch (e: Throwable) {
                onGoogleError(e.message.toString())
            }
        }
    }


}