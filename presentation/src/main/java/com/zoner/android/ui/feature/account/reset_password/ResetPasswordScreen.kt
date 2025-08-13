package com.zoner.android.ui.feature.account.reset_password

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.R
import com.zoner.android.ui.navigation.ResetPasswordRoute
import com.zoner.android.ui.navigation.SignInRoute
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.designSystem.ZonerTextField
import com.zoner.android.util.DeviceConfiguration
import com.zoner.android.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: ResetPasswordViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is ResetPasswordEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }

            ResetPasswordEvent.NavigateToSignIn -> {
                navController.navigate(SignInRoute) {
                    popUpTo(ResetPasswordRoute) {
                        inclusive = true
                    }
                }
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val otp by viewModel.otp.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val newPassword by viewModel.newPassword.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.cNewPassword.collectAsStateWithLifecycle()
    val isLoading by remember { derivedStateOf { state is ResetPasswordState.Loading } }

    BackHandler {
        when (screenState) {
            ResetPasswordScreenState.ForgotPassword -> { navController.navigateUp() }
            ResetPasswordScreenState.ResetPassword -> { viewModel.navigateToForgotPassword() }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (screenState) {
                                ResetPasswordScreenState.ForgotPassword -> { navController.navigateUp() }
                                ResetPasswordScreenState.ResetPassword -> { viewModel.navigateToForgotPassword() }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                title = {},
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .consumeWindowInsets(WindowInsets.navigationBars)
        when (screenState) {
            ResetPasswordScreenState.ForgotPassword -> {
                ForgotPasswordScreen(
                    email = email,
                    isLoading = isLoading,
                    windowSizeClass = windowSizeClass,
                    onEmailChanged = { viewModel.onEmailUpdated(it) },
                    onForgotClicked = viewModel::forgotPassword,
                    modifier = rootModifier
                )
            }

            ResetPasswordScreenState.ResetPassword -> {
                val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

                when (deviceConfiguration) {
                    DeviceConfiguration.MOBILE_PORTRAIT,
                    DeviceConfiguration.MOBILE_LANDSCAPE -> {

                        Column(
                            modifier = rootModifier,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ResetPasswordScreenHeader(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            )
                            ResetPasswordForm(
                                email = email,
                                password = newPassword,
                                confirmPassword = confirmPassword,
                                otp = otp,
                                onOtpChange = { viewModel.onOtpUpdated(it) },
                                isLoading = isLoading,
                                onEmailChange = { viewModel.onEmailUpdated(it) },
                                onNewPasswordChange = { viewModel.onNewPasswordUpdated(it) },
                                onCNewPasswordChange = { viewModel.onConfirmNewPasswordUpdated(it) },
                                onResetPassword = viewModel::resetPassword,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    DeviceConfiguration.TABLET_PORTRAIT,
                    DeviceConfiguration.TABLET_LANDSCAPE,
                    DeviceConfiguration.DESKTOP -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(48.dp),
                            modifier = rootModifier
                                .padding(top = 48.dp)
                        ) {
                            ResetPasswordScreenHeader(
                                modifier = Modifier
                                    .weight(1f)
                            )
                            ResetPasswordForm(
                                email = email,
                                password = newPassword,
                                confirmPassword = confirmPassword,
                                otp = otp,
                                onOtpChange = { viewModel.onOtpUpdated(it) },
                                isLoading = isLoading,
                                onEmailChange = { viewModel.onEmailUpdated(it) },
                                onNewPasswordChange = { viewModel.onNewPasswordUpdated(it) },
                                onCNewPasswordChange = { viewModel.onConfirmNewPasswordUpdated(it) },
                                onResetPassword = viewModel::resetPassword,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

}


@Composable
private fun ResetPasswordForm(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    confirmPassword : String,
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onCNewPasswordChange : (String) -> Unit,
    onResetPassword : () -> Unit
) {
    Column(modifier = modifier) {
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            hint = "Email Address",
            enabled = false,
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )
        )
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = otp,
            onValueChange = onOtpChange,
            label = "Otp",
            hint = "Enter code sent to your email",
            leadingIcon = Icons.Default.ConfirmationNumber,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            )
        )
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = password,
            onValueChange = onNewPasswordChange,
            label = "New Password",
            hint = "Enter New Password",
            leadingIcon = Icons.Default.Lock,
            isInputSecret = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            )
        )
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = confirmPassword,
            onValueChange = onCNewPasswordChange,
            label = "Confirm Password",
            hint = "Confirm new password",
            leadingIcon = Icons.Default.Lock,
            isInputSecret = true,
            isError = password != confirmPassword,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        ZonerSpacer(16.dp)
        Button(
            onClick = onResetPassword,
            enabled = !isLoading && password == confirmPassword,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            AnimatedVisibility(
                visible = isLoading
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                )
            }
            AnimatedVisibility(
                visible = !isLoading
            ) {
                Text(
                    text = "Reset Password",
                )
            }
        }
    }
}


@Composable
private fun ResetPasswordScreenHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(70.dp),
            contentScale = ContentScale.Crop
        )
        ZonerSpacer(4.dp)
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.titleLarge
        )
        ZonerSpacer(4.dp)
        Text(
            text = "Enter your new password and the code sent to the email below.",
            style = MaterialTheme.typography.titleSmall
        )

    }
}