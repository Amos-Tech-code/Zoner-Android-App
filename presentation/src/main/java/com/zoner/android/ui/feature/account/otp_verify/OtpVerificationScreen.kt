package com.zoner.android.ui.feature.account.otp_verify

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.R
import com.zoner.android.navigation.HomeRoute
import com.zoner.android.navigation.MainAppRoute
import com.zoner.android.navigation.SignInRoute
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.util.DeviceConfiguration
import com.zoner.android.util.ObserveAsEvents
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun OtpVerificationScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: OtpVerificationViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when(event) {
            OtpVerificationEvent.NavigateToHome -> {
                // Handle navigation
                navController.navigate(MainAppRoute) {
                    popUpTo(SignInRoute) {
                        inclusive = true
                    }
                }
//                navController.navigate("main") {
//                    // Clear the entire back stack
//                    popUpTo(0)
//                }
            }
            is OtpVerificationEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()

            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler(enabled = !state.isLoading) {
        activity?.moveTaskToBack(true)

    }

    LaunchedEffect(state.otp) {
        if(state.otp.length == 4 ) {
            viewModel.verifyOtp()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(WindowInsets.navigationBars)

        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        val horizontalPadding = when (deviceConfiguration) {
            DeviceConfiguration.DESKTOP -> 120.dp
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.TABLET_PORTRAIT -> 64.dp
            else -> 16.dp
        }

        val verticalArrangement = if (deviceConfiguration == DeviceConfiguration.MOBILE_LANDSCAPE) {
            Arrangement.Center
        } else {
            Arrangement.Top
        }

        Box(modifier = rootModifier) {
            if (deviceConfiguration == DeviceConfiguration.MOBILE_LANDSCAPE) {
                // Use Row layout for better use of horizontal space
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f),
                    ) {
                        OtpScreenHeader()
                        ZonerSpacer(16.dp)
                        OtpVerificationForm(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    ZonerSpacer(16.dp)
                    NumberPadKeyboard(
                        onNumberClick = { number ->
                            val nextEmptyIndex = state.otp.length
                            if (nextEmptyIndex < 4) {
                                viewModel.onOtpChange(nextEmptyIndex, number.toString())
                            }
                        },
                        onBackspaceClick = {
                            val lastIndex = state.otp.length - 1
                            if (lastIndex >= 0) {
                                viewModel.onOtpChange(lastIndex, "")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth().weight(1f)
                    )

                }
            } else {
                // Default Column layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = verticalArrangement
                ) {
                    OtpScreenHeader()
                    ZonerSpacer(32.dp)
                    OtpVerificationForm(state = state, viewModel = viewModel)
                }
                // Bottom NumberPad

                NumberPadKeyboard(
                    onNumberClick = { number ->
                        val nextEmptyIndex = state.otp.length
                        if (nextEmptyIndex < 4) {
                            viewModel.onOtpChange(nextEmptyIndex, number.toString())
                        }
                    },
                    onBackspaceClick = {
                        val lastIndex = state.otp.length - 1
                        if (lastIndex >= 0) {
                            viewModel.onOtpChange(lastIndex, "")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }

        }
    }

}

@Composable
private fun OtpDigitField(
    index: Int,
    value: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPaste: ((String) -> Unit)? = null
) {
    val cursorVisible = remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(isFocused) {
        while (isFocused) {
            cursorVisible.value = true
            delay(500)
            cursorVisible.value = false
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    val pastedText = clipboardManager.getText()?.text ?: ""
                    val digits = pastedText.filter { it.isDigit() }
                    if (digits.length == 4) {
                        onPaste?.invoke(digits)
                    } else {
                        Toast.makeText(context, "Please copy a valid 4-digit code", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else if (isFocused && cursorVisible.value) {
            Box(
                Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Text(
                text = "•",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun NumberPadKeyboard(
    onNumberClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(top = 16.dp, bottom = 32.dp, start = 12.dp, end = 12.dp)
            .navigationBarsPadding()
    ) {
        // Key dimensions
        //val keySize = 80.dp
        val keyHeight = 60.dp
        val keyWidth = 120.dp
        val keyPadding = 4.dp

        // Row 1: 1    2    3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumberPadKeyWithLetters(
                "1", " ",
                onClick = { onNumberClick(1) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            NumberPadKeyWithLetters(
                "2", "ABC",
                onClick = { onNumberClick(2) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            NumberPadKeyWithLetters(
                "3", "DEF",
                onClick = { onNumberClick(3) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
        }

        // Row 2: 4    5    6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumberPadKeyWithLetters(
                "4", "GHI",
                onClick = { onNumberClick(4) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            NumberPadKeyWithLetters(
                "5", "JKL",
                onClick = { onNumberClick(5) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            NumberPadKeyWithLetters(
                "6", "MNO",
                onClick = { onNumberClick(6) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
        }

        // Row 3: 7    8    9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumberPadKeyWithLetters(
                "7", "PQRS",
                onClick = { onNumberClick(7) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            NumberPadKeyWithLetters(
                "8", "TUV",
                onClick = { onNumberClick(8) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            NumberPadKeyWithLetters(
                "9", "WXYZ",
                onClick = { onNumberClick(9) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
        }

        // Row 4: *    0    Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumberPadKeyWithLetters(
                "+*#", " ",
                onClick = { /* Handle * button */ },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            NumberPadKeyWithLetters(
                "0", " ",
                onClick = { onNumberClick(0) },
                modifier = Modifier.height(keyHeight).width(keyWidth).padding(keyPadding)
            )
            Box(
                modifier = Modifier
                    .height(keyHeight).width(keyWidth)
                    .padding(keyPadding)
                    .clickable(onClick = onBackspaceClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun NumberPadKeyWithLetters(
    number: String,
    letters: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(10.dp),
                spotColor = MaterialTheme.colorScheme.surface,
                clip = false // so shadow shows outside bounds
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (letters.isNotBlank()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}


@Composable
private fun OtpScreenHeader(
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
        Text(text = "OTP VERIFICATION", style = MaterialTheme.typography.titleLarge)
        ZonerSpacer(4.dp)
        Text(
            text = "Enter OTP sent to the mobile number you entered.",
            style = MaterialTheme.typography.labelLarge
        )

    }
}

@Composable
fun OtpVerificationForm(
    modifier: Modifier = Modifier,
    state: OtpVerificationUIState,
    viewModel: OtpVerificationViewModel
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // OTP Input Fields
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val focusIndex = state.otp.length
            for (i in 0 until 4) {
                OtpDigitField(
                    index = i,
                    value = state.otp.getOrNull(i)?.toString() ?: "",
                    isFocused = focusIndex == i,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = { viewModel.handleDigitFocus(i) },
                    onPaste = { viewModel.setOtpFromPaste(it) }
                )
            }
        }

        ZonerSpacer(16.dp)

        Button(
            onClick = { viewModel.verifyOtp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = state.otp.length == 4 && !state.isLoading && !state.isResending,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(text = "VERIFY CODE", style = MaterialTheme.typography.labelLarge)
            }
        }

        ZonerSpacer(16.dp)

        Button(
            onClick = { viewModel.resendOtp() },
            enabled = !state.isResending && !state.isLoading,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            AnimatedVisibility(visible = state.isResending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            AnimatedVisibility(visible = !state.isResending) {
                Text(
                    text = "RESEND CODE (${state.resendCountdown} secs)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

