package com.zoner.android.ui.designSystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.zoner.android.ConnectionBannerState
import com.zoner.android.R
import com.zoner.android.ui.theme.ZonerError
import com.zoner.android.ui.theme.ZonerInfo
import com.zoner.android.ui.theme.ZonerSuccess

@Composable
fun ConnectionBanner(
    modifier: Modifier = Modifier,
    state: ConnectionBannerState
) {
    AnimatedVisibility(
        visible = state != ConnectionBannerState.Hidden,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val message = when (state) {
            ConnectionBannerState.Disconnected -> "No Internet Connection"
            ConnectionBannerState.BackOnline -> "Back Online"
            else -> ""
        }
        val backgroundColor = when (state) {
            ConnectionBannerState.Disconnected -> ZonerInfo
            ConnectionBannerState.BackOnline -> ZonerSuccess
            else -> Color.Transparent
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun SuccessAlertDialog(
    title: String = "Success",
    message: String,
    icon: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = "Great!",
    onConfirmButtonClick: () -> Unit,
    dismissButtonText: String? = null,
    onDismissButtonClick: (() -> Unit)? = null,
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    dismissButtonColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    messageTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true
    )
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = if (icon == null) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = "Success Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else icon,
        title = {
            Column {
                Text(
                    text = title,
                    style = titleTextStyle,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = messageTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmButtonClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = confirmButtonColor
                )
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = if (dismissButtonText != null && onDismissButtonClick != null) {
            {
                TextButton(
                    onClick = {
                        onDismissButtonClick()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = dismissButtonColor
                    )
                ) {
                    Text(text = dismissButtonText)
                }
            }
        } else null,
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        properties = properties
    )
}


@Composable
fun ErrorAlertDialog(
    title: String = "Something went wrong",
    message: String,
    icon: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = "OK",
    onConfirmButtonClick: () -> Unit,
    dismissButtonText: String? = null,
    onDismissButtonClick: (() -> Unit)? = null,
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    dismissButtonColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    messageTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false
    )
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = if (icon == null) {
            {
                Text(
                    text = "\uD83D\uDE15",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        } else icon,
        title = {
            Column{
                Text(
                    text = title,
                    style = titleTextStyle,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message,
                    style = messageTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmButtonClick()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = confirmButtonColor
                )
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = if (dismissButtonText != null && onDismissButtonClick != null) {
            {
                TextButton(
                    onClick = {
                        onDismissButtonClick()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = dismissButtonColor
                    )
                ) {
                    Text(text = dismissButtonText)
                }
            }
        } else null,
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        properties = properties
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountRequiredDialog(
    modifier: Modifier = Modifier,
    showDialog: Boolean = false,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit
) {
    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = true
            )
        ) {
            Column(
                modifier = modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_caution),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Business Account Required",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Want to create a post?\n" +
                            "Switch to a business account to showcase your services, deals, and updates to nearby customers.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ZonerButton(
                    text = "Switch Now",
                    onClick = onConfirmButtonClick,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = onDismiss,
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

