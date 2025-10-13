package com.zoner.android.ui.feature.notifications

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.R
import com.zoner.android.ui.designSystem.ZonerSpacer
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.util.Date
import java.util.concurrent.TimeUnit

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = koinViewModel()
) {

    LaunchedEffect(true) {
        viewModel.event.collectLatest {
            when (it) {
                is NotificationEvent.ShowErrorMessage -> {
                    Toast.makeText(navController.context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isNotificationEnabled by viewModel.notificationEnabled.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            NotificationTopBar(
                notificationsEnabled = isNotificationEnabled,
                onNotificationEnableChange = { viewModel.notificationEnableUpdate() },
            )
        }
    ) { innerPadding ->
        NotificationsContent(
            onNotificationClick = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTopBar(
    notificationsEnabled: Boolean,
    onNotificationEnableChange: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            IconButton(
                onClick = onNotificationEnableChange
            ) {
                if (notificationsEnabled) {
                    Icon(
                        imageVector = Icons.Default.ToggleOn,
                        contentDescription = "Disable push notification",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ToggleOff,
                        contentDescription = "Enable push notification",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            ZonerSpacer(4.dp)
            Text(
                text = "Push Notifications",
                style = MaterialTheme.typography.bodySmall
            )
        }
    )
}




@Composable
private fun NotificationsContent(
    modifier: Modifier = Modifier,
    onNotificationClick: (Notification) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(
            notifications, key = { item -> item.id }
        ) { notification ->
            NotificationItem(
                notification = notification,
                onNotificationClick = onNotificationClick
            )
        }
    }
}


@Composable
private fun NotificationItem(
    modifier: Modifier = Modifier,
    notification: Notification,
    onNotificationClick: (Notification) -> Unit
) {
    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
            .clickable { onNotificationClick(notification) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_circle_logo),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        ZonerSpacer(8.dp)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = notification.message,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = notification.getFormattedTime(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

val currentTime = System.currentTimeMillis()
val notifications = listOf(
    Notification(
        id = 1,
        title = "New Message",
        message = "You have received a new message from John",
        timestamp = Date(currentTime - 1000 * 60 * 30) // 30 minutes ago
    ),
    Notification(
        id = 2,
        title = "System Update",
        message = "A new system update is available",
        timestamp = Date(currentTime - 1000 * 60 * 60) // 1 hour ago
    ),
    Notification(
        id = 3,
        title = "Reminder",
        message = "Don't forget your meeting at 2 PM",
        timestamp = Date(currentTime - 1000 * 60 * 60 * 3) // 3 hours ago
    ),
    Notification(
        id = 4,
        title = "Friend Request",
        message = "Sarah sent you a friend request",
        timestamp = Date(currentTime - 1000 * 60 * 60 * 5) // 5 hours ago
    ),
    Notification(
        id = 5,
        title = "Payment Received",
        message = "You've received \$250 from Alex",
        isRead = true,
        timestamp = Date(currentTime - 1000 * 60 * 60 * 24) // 1 day ago
    )
)

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean = false
) {
    // Function to format time as "X hr ago", "X min ago", etc.
    fun getFormattedTime(): String {
        val now = Date()
        val diffInMillis = now.time - timestamp.time

        return when {
            diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                "$minutes min ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                "$hours hr ago"
            }
            else -> {
                val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                "$days days ago"
            }
        }
    }
}