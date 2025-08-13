package com.zoner.android.ui.feature.account.reset_password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.R
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.designSystem.ZonerTextField
import com.zoner.android.util.DeviceConfiguration

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    email: String,
    isLoading: Boolean,
    windowSizeClass: WindowSizeClass,
    onEmailChanged: (String) -> Unit,
    onForgotClicked: () -> Unit
) {
    Column(modifier = modifier) {

        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT,
            DeviceConfiguration.MOBILE_LANDSCAPE -> {

                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    ForgotScreenHeader(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    ForgotScreenForm(
                        email = email,
                        isLoading = isLoading,
                        onEmailChange = onEmailChanged,
                        onForgotClicked = onForgotClicked
                    )
                }

            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    modifier = Modifier
                        .padding(top = 48.dp)
                ) {
                    ForgotScreenHeader(
                        modifier = Modifier.weight(1f)
                    )
                    ForgotScreenForm(
                        email = email,
                        isLoading = isLoading,
                        onEmailChange = onEmailChanged,
                        onForgotClicked = onForgotClicked,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

    }

}

@Composable
fun ForgotScreenForm(
    modifier: Modifier = Modifier,
    email: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onForgotClicked: () -> Unit
) {
    Column(modifier = modifier) {
        ZonerSpacer(8.dp)
        ZonerTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            hint = "Email Address",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Email
            )
        )
        ZonerSpacer(16.dp)
        Button(
            onClick = onForgotClicked,
            enabled = !isLoading && email.isNotBlank(),
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
                    text = "Forgot Password",
                )
            }
        }
    }
}

@Composable
private fun ForgotScreenHeader(
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
            text = "Forgot Password",
            style = MaterialTheme.typography.titleLarge
        )
        ZonerSpacer(4.dp)
        Text(
            text = "Enter your email to receive a code in your email.",
            style = MaterialTheme.typography.titleSmall
        )

    }
}