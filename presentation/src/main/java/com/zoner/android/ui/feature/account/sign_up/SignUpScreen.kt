package com.zoner.android.ui.feature.account.sign_up

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import com.zoner.android.ui.navigation.CompleteProfileRoute
import com.zoner.android.ui.navigation.OTPVerificationRoute
import com.zoner.android.util.DeviceConfiguration
import com.zoner.android.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: SignUpViewModel = koinViewModel()
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    var showOauthErrorDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is SignUpEvent.ShowErrorDialog -> {
                showErrorDialog = true
                message = event.message
            }
            is SignUpEvent.NavigateToSignIn -> {
                navController.popBackStack()
            }

            is SignUpEvent.NavigateToVerification -> {
                navController.navigate(OTPVerificationRoute(event.userId))
            }

            is SignUpEvent.NavigateToCompleteProfile -> {
                navController.navigate(CompleteProfileRoute(event.userId))
            }
            is SignUpEvent.ShowSnackBar -> {
                scope.launch {
                    snackBarHostState.showSnackbar(event.message)
                }
            }

            SignUpEvent.ShowOauthErrorDialog -> { showOauthErrorDialog = true }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isSigningUp by remember { derivedStateOf { state is SignUpState.Loading } }
    val isSigningUpWithGoogle by viewModel.isGoogleSignIn.collectAsStateWithLifecycle()
    val context = LocalContext.current as ComponentActivity

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
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
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SignUpScreenHeader(
                        modifier = Modifier.fillMaxWidth()
                    )
                    SignUpScreenForm(
                        fullName = formState.fullName,
                        onNameChanged = { viewModel.onFullNameUpdated(it) },
                        email = formState.email,
                        onEmailChanged = { viewModel.onEmailUpdated(it) },
                        phoneNumber = formState.phoneNumber,
                        onPhoneChanged = { viewModel.onPhoneNumberUpdated(it) },
                        password = formState.password,
                        onPasswordChanged = { viewModel.onPasswordUpdated(it) },
                        confirmPassword = formState.confirmPassword,
                        onCPasswordChanged = { viewModel.onCPasswordUpdated(it) },
                        modifier = Modifier.fillMaxWidth(),
                        onSignInClick = viewModel::onSignInClicked,
                        onSignUpClick = viewModel::register,
                        onGoogleSignClick = { viewModel.onGoogleClicked(context) },
                        isSigningUp = isSigningUp,
                        isSigningUpWithGoogle = isSigningUpWithGoogle
                    )
                }
            }

            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier
                        //.windowInsetsPadding(WindowInsets.displayCutout)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    SignUpScreenHeader(
                        modifier = Modifier.weight(1f)
                    )
                    SignUpScreenForm(
                        fullName = formState.fullName,
                        onNameChanged = { viewModel.onFullNameUpdated(it) },
                        email = formState.email,
                        onEmailChanged = { viewModel.onEmailUpdated(it) },
                        phoneNumber = formState.phoneNumber,
                        onPhoneChanged = { viewModel.onPhoneNumberUpdated(it) },
                        password = formState.password,
                        onPasswordChanged = { viewModel.onPasswordUpdated(it) },
                        confirmPassword = formState.confirmPassword,
                        onCPasswordChanged = { viewModel.onCPasswordUpdated(it) },
                        onSignInClick = viewModel::onSignInClicked,
                        onSignUpClick = viewModel::register,
                        onGoogleSignClick = { viewModel.onGoogleClicked(context) },
                        isSigningUp = isSigningUp,
                        isSigningUpWithGoogle = isSigningUpWithGoogle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Column(
                    modifier = rootModifier
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SignUpScreenHeader(
                        modifier = Modifier
                            .widthIn(max = 540.dp)
                    )
                    SignUpScreenForm(
                        fullName = formState.fullName,
                        onNameChanged = { viewModel.onFullNameUpdated(it) },
                        email = formState.email,
                        onEmailChanged = { viewModel.onEmailUpdated(it) },
                        phoneNumber = formState.phoneNumber,
                        onPhoneChanged = { viewModel.onPhoneNumberUpdated(it) },
                        password = formState.password,
                        onPasswordChanged = { viewModel.onPasswordUpdated(it) },
                        confirmPassword = formState.confirmPassword,
                        onCPasswordChanged = { viewModel.onCPasswordUpdated(it) },
                        modifier = Modifier.fillMaxWidth(),
                        onSignInClick = viewModel::onSignInClicked,
                        onSignUpClick = viewModel::register,
                        onGoogleSignClick = { viewModel.onGoogleClicked(context) },
                        isSigningUp = isSigningUp,
                        isSigningUpWithGoogle = isSigningUpWithGoogle
                    )
                }
            }
        }
    }

    if (showErrorDialog) {
        ErrorAlertDialog(
            title = "Sign Up Failed",
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
private fun SignUpScreenHeader(
    modifier: Modifier = Modifier
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
        Text(text = "SIGN UP", style = MaterialTheme.typography.titleLarge)
        ZonerSpacer(4.dp)
        Text(
            text = "Enter your details to experience the application",
            style = MaterialTheme.typography.labelLarge
        )

    }
}


@Composable
private fun SignUpScreenForm(
    modifier: Modifier = Modifier,
    fullName: String,
    email: String,
    phoneNumber: String,
    password: String,
    confirmPassword: String,
    isSigningUp: Boolean,
    isSigningUpWithGoogle: Boolean,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onCPasswordChanged: (String) -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignClick: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
    ) {
        ZonerTextField(
            value = fullName,
            onValueChange = onNameChanged,
            label = "Full Name",
            hint = "Enter full Name",
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        ZonerSpacer(8.dp)
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
//        ZonerSpacer(8.dp)
//        ZonerTextField(
//            value = phoneNumber,
//            onValueChange = onPhoneChanged,
//            label = "Phone Number",
//            hint = "Phone Number",
//            leadingIcon = Icons.Default.Phone,
//            keyboardOptions = KeyboardOptions(
//                imeAction = ImeAction.Next,
//                keyboardType = KeyboardType.Phone
//            )
//        )
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = "Password",
            hint = "Enter password",
            leadingIcon = Icons.Default.Lock,
            isInputSecret = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = confirmPassword,
            onValueChange = onCPasswordChanged,
            label = "Confirm Password",
            hint = "Enter password",
            leadingIcon = Icons.Default.Lock,
            isInputSecret = true,
            isError = confirmPassword != password,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        ZonerSpacer(16.dp)
        ZonerTextLink(
            text = "Already have an account? Login",
            onClick = onSignInClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        ZonerSpacer(16.dp)

        Button(
            onClick = {
                onSignUpClick()
                keyboardController?.hide()
            },
            enabled = !isSigningUp && !isSigningUpWithGoogle && password == confirmPassword,
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
                visible = isSigningUp
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                )
            }
            AnimatedVisibility(
                visible = !isSigningUp
            ) {
                Text(
                    text = "SIGN UP",
                )
            }
        }

        ZonerSpacer(16.dp)

        SignInWithGoogle(
            modifier =  Modifier
                .fillMaxWidth()
                .height(50.dp),
            text = "SIGN UP WITH GOOGLE",
            onClick = onGoogleSignClick,
            isSigningUp = isSigningUp,
            isSigningUpWithGoogle = isSigningUpWithGoogle
        )
    }
}


@Composable
fun SignInWithGoogle(
    modifier: Modifier = Modifier,
    text: String,
    isSigningUp: Boolean,
    isSigningUpWithGoogle: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        enabled = !isSigningUp && !isSigningUpWithGoogle
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null
            )
            ZonerSpacer(8.dp)
            AnimatedVisibility(
                visible = !isSigningUpWithGoogle
            ) { Text(text = text) }
            AnimatedVisibility(
                visible = isSigningUpWithGoogle
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
