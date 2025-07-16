package com.zoner.android.ui.feature.account.sign_in

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.R
import com.zoner.android.navigation.CountrySelectRoute
import com.zoner.android.navigation.OTPVerificationRoute
import com.zoner.android.navigation.SignUpRoute
import com.zoner.android.ui.designSystem.ZonerSelectorTextField
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.designSystem.ZonerTextField
import com.zoner.android.ui.designSystem.ZonerTextLink
import com.zoner.android.ui.feature.account.sign_up.SignInWithGoogle
import com.zoner.android.util.DeviceConfiguration
import com.zoner.android.util.ObserveAsEvents
import com.zoner.domain.model.CountryModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: SignInViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) {
        when (it) {
            is SignInEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, it.message, Toast.LENGTH_SHORT).show()
            }

            SignInEvent.NavigateToCountrySelection -> {
                navController.navigate(CountrySelectRoute)
            }
            SignInEvent.NavigateToOTPVerification -> {
                navController.navigate(OTPVerificationRoute)
            }

            SignInEvent.NavigateToSignUp -> {
                navController.navigate(SignUpRoute)
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSigningIn by remember { derivedStateOf { state is SignInState.Loading } }
    val isSigningInWithGoogle by viewModel.isGoogleSignIn.collectAsStateWithLifecycle()
    val phoneNumber by viewModel.phoneNumber.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val useThisCountry =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<CountryModel?>(
            "selected_country",null)?.collectAsStateWithLifecycle()
    val context = LocalContext.current as ComponentActivity

    LaunchedEffect(useThisCountry?.value) {
        useThisCountry?.value?.let {
            viewModel.onSelectedCountryUpdated(it)
            // Optional: Clear savedStateHandle to avoid re-triggering on recomposition
            navController.currentBackStackEntry?.savedStateHandle?.remove<CountryModel>("selected_country")
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

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
                        phoneNumber = phoneNumber,
                        onPhoneNumberChanged = { viewModel.onPhoneNumberUpdated(it) },
                        onSignInClicked = viewModel::signIn,
                        onGoogleSignInClicked = { viewModel.onGoogleClicked(context) },
                        onSignUpClicked = viewModel::onSignUpClicked,
                        selectedCountry = selectedCountry,
                        onSelectClicked = viewModel::onCountrySelectionClicked,
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
                        phoneNumber = phoneNumber,
                        onPhoneNumberChanged = { viewModel.onPhoneNumberUpdated(it) },
                        onSignInClicked = viewModel::signIn,
                        onSignUpClicked = viewModel::onSignUpClicked,
                        onGoogleSignInClicked = { viewModel.onGoogleClicked(context) },
                        selectedCountry = selectedCountry,
                        onSelectClicked = viewModel::onCountrySelectionClicked,
                        isSigningIn = isSigningIn,
                        isSigningInWithGoogle = isSigningInWithGoogle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

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
            text = "Enter your phone number to get an OTP number",
            style = MaterialTheme.typography.titleSmall
        )

    }
}


@Composable
private fun SignInScreenForm(
    modifier: Modifier = Modifier,
    selectedCountry: CountryModel,
    phoneNumber: String,
    isSigningIn: Boolean,
    isSigningInWithGoogle: Boolean,
    onPhoneNumberChanged: (String) -> Unit,
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onGoogleSignInClicked: () -> Unit,
    onSelectClicked: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Dial code selector
            ZonerSelectorTextField(
                value = "${selectedCountry.emoji} ${selectedCountry.dialCode}",
                hint = "+254",
                onClick = onSelectClicked,
                modifier = Modifier.weight(1f)
            )
            ZonerSpacer(8.dp)
            // Phone number input
            ZonerTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChanged,
                label = "Phone Number",
                hint = "Enter your phone number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(2f)
            )
        }

        ZonerSpacer(24.dp)

        ZonerTextLink(
            text = "Don\'t have an account? Register",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onSignUpClicked
        )
        ZonerSpacer(16.dp)
        Button(
            onClick = onSignInClicked,
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