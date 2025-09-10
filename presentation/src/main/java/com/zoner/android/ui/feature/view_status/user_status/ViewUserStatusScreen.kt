package com.zoner.android.ui.feature.view_status.user_status

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.ui.designSystem.EmptyMyStatusState
import com.zoner.android.ui.designSystem.ErrorScreen
import com.zoner.android.ui.designSystem.LoadingComponent
import com.zoner.android.ui.designSystem.LoadingType
import com.zoner.android.ui.designSystem.VideoThumbnail
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.util.ObserveAsEvents
import com.zoner.android.util.formatShort
import com.zoner.android.util.toRelativeDuration
import com.zoner.android.util.toRelativeTime
import com.zoner.domain.model.BaseStatus
import com.zoner.domain.model.InteractionType
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.StatusInteraction
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun MyStatusScreen(
    navController: NavController,
    viewModel: ViewUserStatusViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { events ->
        when (events) {
            is ViewUserStatusEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, events.message, Toast.LENGTH_SHORT).show()
            }
            is ViewUserStatusEvent.NavigateBack -> {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            MyStatusAppBar(
                totalViews = state.totalViews,
                totalLikes = state.totalLikes,
                totalReplies = state.totalReplies,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when {
                state.isLoading -> LoadingComponent(
                    type = LoadingType.Circular,
                    isFullScreen = true
                )
                state.error != null -> ErrorScreen(
                    message = state.error ?: "Unknown error",
                    onRetry = { viewModel.fetchMyStatuses() }
                )
                state.myStatuses.isEmpty() -> EmptyMyStatusState(
                    onAddStatus = { /* Navigate to create status */ }
                )
                else -> MyStatusList(
                    statuses = state.myStatuses,
                    expandedStatusId = state.expandedStatusId,
                    onStatusClick = { status ->
                        // Navigate to status viewer
                    },
                    onToggleExpand = { statusId ->
                        viewModel.toggleStatusExpanded(statusId)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyStatusAppBar(
    totalViews: Int,
    totalLikes: Int,
    totalReplies: Int,
    onBackClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = { Text("My Status") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            )
        )

        // Engagement summary
        EngagementSummaryRow(
            views = totalViews,
            likes = totalLikes,
            replies = totalReplies,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun EngagementSummaryRow(
    views: Int,
    likes: Int,
    replies: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        EngagementSummaryItem(
            count = views,
            label = "Total Views",
            icon = Icons.Default.Visibility
        )

        EngagementSummaryItem(
            count = likes,
            label = "Total Likes",
            icon = Icons.Default.Favorite
        )

        EngagementSummaryItem(
            count = replies,
            label = "Total Replies",
            icon = Icons.AutoMirrored.Filled.Chat
        )
    }
}

@Composable
private fun EngagementSummaryItem(
    count: Int,
    label: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.formatShort(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MyStatusList(
    statuses: List<BaseStatus>,
    expandedStatusId: String?,
    onStatusClick: (BaseStatus) -> Unit,
    onToggleExpand: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(statuses, key = { it.id }) { status ->
            MyStatusListItem(
                status = status,
                isExpanded = expandedStatusId == status.id,
                onStatusClick = { onStatusClick(status) },
                onToggleExpand = { onToggleExpand(status.id) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun MyStatusListItem(
    status: BaseStatus,
    isExpanded: Boolean,
    onStatusClick: () -> Unit,
    onToggleExpand: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStatusClick)
            .padding(16.dp)
    ) {
        // Status header with preview and basic info
        StatusHeader(
            status = status,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand
        )

        // Expanded engagement details
        if (isExpanded) {
            Spacer(modifier = Modifier.height(16.dp))
            EngagementDetails(status = status)
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun StatusHeader(
    status: BaseStatus,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Status media preview
        StatusMediaPreview(status = status)

        Spacer(modifier = Modifier.width(16.dp))

        // Status info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${status.createdAt.toRelativeTime()} ago",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Engagement summary for this status
            StatusEngagementSummary(
                views = status.views.size,
                likes = status.likes.size,
                replies = status.replies.size
            )

            status.caption.takeUnless { it.isNullOrBlank()}?.let { caption ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Expand/collapse button
        IconButton(
            onClick = onToggleExpand,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    }
}

@Composable
private fun StatusMediaPreview(status: BaseStatus) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when (status.mediaType) {
            MediaType.IMAGE -> {
                ZonerAsyncImage(
                    imageUrl = status.mediaUri,
                    contentDescription = "Status image",
                    modifier = Modifier.fillMaxSize(),

                    contentScale = ContentScale.Crop
                )
            }
            MediaType.VIDEO -> {
                VideoThumbnail(
                    uri = status.mediaUri,
                    localPath = status.localFilePath,
                    showPlayButton = false,
                    onVideoPlayClicked = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Duration for videos
        if (status.mediaType == MediaType.VIDEO && status.durationMillis > 0) {
            Text(
                text = status.durationMillis.toRelativeDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}


@Composable
private fun EngagementDetails(status: BaseStatus) {
    Column {
        // Views section
        if (status.views.isNotEmpty()) {
            EngagementSection(
                title = "${status.views.size} View${if (status.views.size != 1) "s" else ""}",
                interactions = status.views,
                icon = Icons.Default.Visibility
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Likes section
        if (status.likes.isNotEmpty()) {
            EngagementSection(
                title = "${status.likes.size} Like${if (status.likes.size != 1) "s" else ""}",
                interactions = status.likes,
                icon = Icons.Default.Favorite
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Replies section
        if (status.replies.isNotEmpty()) {
            EngagementSection(
                title = "${status.replies.size} Repl${if (status.replies.size != 1) "ies" else "y"}",
                interactions = status.replies,
                icon = Icons.AutoMirrored.Filled.Chat,
                isReplySection = true
            )
        }
    }
}

@Composable
fun EngagementSection(
    title: String,
    interactions: List<StatusInteraction>,
    icon: ImageVector,
    isReplySection: Boolean = false
) {
    Column {
        // Section header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactions list
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(interactions) { interaction ->
                InteractionItem(
                    interaction = interaction,
                    isReply = isReplySection
                )
                if (interaction != interactions.last()) {
                   HorizontalDivider(modifier = Modifier.padding(start = 24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun InteractionItem(
    interaction: StatusInteraction,
    isReply: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar
        ZonerAsyncImage(
            imageUrl = interaction.userAvatar,
            contentDescription = interaction.userName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = interaction.userName ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (isReply && interaction.replyText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                interaction.replyText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = interaction.timestamp.toRelativeTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        // For replies, show reply media if available
        if (isReply && interaction.replyMediaUri != null) {
            ZonerAsyncImage(
                imageUrl = interaction.replyMediaUri,
                contentDescription = "Reply media",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // For likes, show the reaction type
        if (!isReply) {
            Text(
                text = when (interaction.type) {
                    InteractionType.LIKE -> "Liked"
                    InteractionType.VIEW -> "Viewed"
                    else -> "Interacted"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StatusEngagementSummary(
    views: Int,
    likes: Int,
    replies: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EngagementStat(
            count = views,
            label = if (views == 1) "view" else "views",
            icon = Icons.Default.Visibility
        )

        EngagementStat(
            count = likes,
            label = if (likes == 1) "like" else "likes",
            icon = Icons.Default.Favorite
        )

        EngagementStat(
            count = replies,
            label = if (replies == 1) "reply" else "replies",
            icon = Icons.AutoMirrored.Filled.Chat
        )
    }
}


@Composable
private fun EngagementStat(icon: ImageVector, count: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}