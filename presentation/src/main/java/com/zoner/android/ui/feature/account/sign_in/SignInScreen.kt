package com.zoner.android.ui.feature.account.sign_in

import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.R
import com.zoner.android.ui.designSystem.ErrorAlertDialog
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.designSystem.ZonerTextField
import com.zoner.android.ui.designSystem.ZonerTextLink
import com.zoner.android.ui.feature.account.sign_up.SignInWithGoogle
import com.zoner.android.ui.navigation.MainAppRoute
import com.zoner.android.ui.navigation.ResetPasswordRoute
import com.zoner.android.ui.navigation.SignInRoute
import com.zoner.android.ui.navigation.SignUpRoute
import com.zoner.android.util.DeviceConfiguration
import com.zoner.android.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: SignInViewModel = koinViewModel()
) {

    var showErrorDialog by remember { mutableStateOf(false) }
    var showOauthErrorDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is SignInEvent.ShowErrorDialog -> {
                showErrorDialog = true
                message = event.message
            }

            SignInEvent.NavigateToHome -> {
                navController.navigate(MainAppRoute) {
                    popUpTo(SignInRoute) {
                        inclusive = true
                    }
                }
            }

            SignInEvent.NavigateToSignUp -> {
                navController.navigate(SignUpRoute)
            }

            SignInEvent.NavigateToResetPassword -> {
                navController.navigate(ResetPasswordRoute)
            }

            is SignInEvent.ShowSnackBar -> {
                scope.launch {
                    snackBarHostState.showSnackbar(event.message)
                }
            }

            SignInEvent.ShowOauthErrorDialog -> {
                showOauthErrorDialog = true
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSigningIn by remember { derivedStateOf { state is SignInState.Loading } }
    val isSigningInWithGoogle by viewModel.isGoogleSignIn.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
//    val useThisCountry =
//        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<CountryModel?>(
//            "selected_country",null)?.collectAsStateWithLifecycle()
    val context = LocalContext.current as ComponentActivity

//    LaunchedEffect(useThisCountry?.value) {
//        useThisCountry?.value?.let {
//            viewModel.onSelectedCountryUpdated(it)
//            // Optional: Clear savedStateHandle to avoid re-triggering on recomposition
//            navController.currentBackStackEntry?.savedStateHandle?.remove<CountryModel>("selected_country")
//        }
//    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackBarHostState) }
    )
    { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .consumeWindowInsets(WindowInsets.navigationBars)
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT,
            DeviceConfiguration.MOBILE_LANDSCAPE -> {

                Column(
                    modifier = rootModifier,
                    verticalArrangement = Arrangement.Center
                ) {
                    SignInScreenHeader(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    SignInScreenForm(
                        email = email,
                        onEmailChanged = { viewModel.onEmailUpdated(it) },
                        password = password,
                        onPasswordChanged = { viewModel.onPasswordUpdated(it) },
                        onSignInClicked = viewModel::signIn,
                        onGoogleSignInClicked = { viewModel.onGoogleClicked(context) },
                        onSignUpClicked = viewModel::onSignUpClicked,
                        onForgotPasswordClicked = viewModel::onForgotPasswordClicked,
                        isSigningIn = isSigningIn,
                        isSigningInWithGoogle = isSigningInWithGoogle
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
                    SignInScreenHeader(
                        modifier = Modifier.weight(1f)
                    )
                    SignInScreenForm(
                        email = email,
                        onEmailChanged = { viewModel.onEmailUpdated(it) },
                        password = password,
                        onPasswordChanged = { viewModel.onPasswordUpdated(it) },
                        onSignInClicked = viewModel::signIn,
                        onGoogleSignInClicked = { viewModel.onGoogleClicked(context) },
                        onSignUpClicked = viewModel::onSignUpClicked,
                        onForgotPasswordClicked = viewModel::onForgotPasswordClicked,
                        isSigningIn = isSigningIn,
                        isSigningInWithGoogle = isSigningInWithGoogle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

    }

    if (showErrorDialog) {
        ErrorAlertDialog(
            title = "Sign In Failed",
            message = message ?: "Something went wrong.",
            onDismissRequest = { showErrorDialog = false },
            onConfirmButtonClick = { showErrorDialog = false }
        )
    }
    if (showOauthErrorDialog) {
        ErrorAlertDialog(
            title = viewModel.error,
            message = viewModel.errorDescription,
            onDismissRequest = { showOauthErrorDialog = false },
            onConfirmButtonClick = { showOauthErrorDialog = false }
        )
    }

}


@Composable
private fun SignInScreenHeader(
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
            text = "SIGN IN",
            style = MaterialTheme.typography.titleLarge
        )
        ZonerSpacer(4.dp)
        Text(
            text = "Enter your email and password to login",
            style = MaterialTheme.typography.titleSmall
        )

    }
}


@Composable
private fun SignInScreenForm(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    isSigningIn: Boolean,
    isSigningInWithGoogle: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onGoogleSignInClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
    ) {
        ZonerTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = "Email",
            hint = "Email Address",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )
        )
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = "Password",
            hint = "Enter password",
            leadingIcon = Icons.Default.Lock,
            isInputSecret = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        ZonerSpacer(8.dp)
        ZonerTextLink(
            text = "Forgot Password?",
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp),
            onClick = onForgotPasswordClicked
        )
        ZonerSpacer(24.dp)
        ZonerTextLink(
            text = "Don\'t have an account? Register",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onSignUpClicked
        )
        ZonerSpacer(16.dp)
        Button(
            onClick = {
                onSignInClicked()
                keyboardController?.hide()
            },
            enabled = !isSigningIn && !isSigningInWithGoogle,
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
                visible = isSigningIn
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                )
            }
            AnimatedVisibility(
                visible = !isSigningIn
            ) {
                Text(
                    text = "SIGN IN",
                )
            }
        }
        ZonerSpacer(16.dp)
        SignInWithGoogle(
            modifier =  Modifier
                .fillMaxWidth()
                .height(50.dp),
            text = "SIGN IN WITH GOOGLE",
            isSigningUp = isSigningIn,
            isSigningUpWithGoogle = isSigningInWithGoogle,
            onClick = onGoogleSignInClicked
        )
    }
}