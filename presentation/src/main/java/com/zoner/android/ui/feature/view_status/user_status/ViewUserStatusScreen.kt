package com.zoner.android.ui.feature.view_status.user_status

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.zoner.android.ui.feature.view_status.StatusViewerContent
import com.zoner.android.ui.navigation.AddPostRoute
import com.zoner.android.util.ObserveAsEvents
import com.zoner.android.util.formatShort
import com.zoner.android.util.toRelativeDuration
import com.zoner.android.util.toRelativeTime
import com.zoner.domain.StatusState
import com.zoner.domain.model.BaseStatus
import com.zoner.domain.model.InteractionType
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.MyStatus
import com.zoner.domain.model.StatusInteraction
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun MyStatusScreen(
    navController: NavController,
    viewModel: ViewUserStatusViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val viewState by viewModel.viewingState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { events ->
        when (events) {
            is ViewUserStatusEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, events.message, Toast.LENGTH_SHORT).show()
            }
            is ViewUserStatusEvent.NavigateBack -> {
                when (screenState) {
                    ViewUserStatusScreenState.ListUserStatus -> {
                        navController.popBackStack()
                    }
                    ViewUserStatusScreenState.ViewUserStatus -> {
                        viewModel.closeViewer()
                    }
                }
            }

            ViewUserStatusEvent.CreatePost -> navController.navigate(AddPostRoute)
        }
    }

    when (screenState) {
        ViewUserStatusScreenState.ListUserStatus -> {
            Scaffold(
                topBar = {
                    MyStatusAppBar(
                        totalViews = state.totalViews,
                        totalLikes = state.totalLikes,
                        totalReplies = state.totalReplies,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            )
            { innerPadding ->
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
                            onAddStatus = {
                            /* Navigate to create status */ viewModel::createStatus
                            }
                        )
                        else -> MyStatusList(
                            statuses = state.myStatuses,
                            expandedStatusId = state.expandedStatusId,
                            onStatusClick = { status ->
                                // Start viewing user status from index of the selected status
                                viewModel.startViewingFromStatus(status)
                            },
                            onDeleteStatusClick = {
                                viewModel.deleteStatus(it)
                            },
                            onRetry = { viewModel.retryFailedUploads() },

                            onToggleExpand = { statusId ->
                                viewModel.toggleStatusExpanded(statusId)
                            }
                        )
                    }
                }
            }
        }
        ViewUserStatusScreenState.ViewUserStatus -> {
            // Navigate to status viewer
            Scaffold { innerPadding ->
                StatusViewerContent(
                    state = viewState,
                    onNext = viewModel::moveToNextStatus,
                    onPrevious = viewModel::moveToPreviousStatus,
                    onClose = viewModel::closeViewer,
                    onProgressChanged = viewModel::onProgressChanged,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
    onRetry: () -> Unit,
    onDeleteStatusClick: (String) -> Unit,
    onToggleExpand: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(statuses, key = { it.id }) { status ->
            MyStatusListItem(
                status = status as MyStatus,
                isExpanded = expandedStatusId == status.id,
                onStatusClick = { onStatusClick(status) },
                onToggleExpand = { onToggleExpand(status.id) },
                onRetryUpload = onRetry,
                onDeleteStatusClick = { onDeleteStatusClick(status.id) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun MyStatusListItem(
    status: MyStatus,
    isExpanded: Boolean,
    onStatusClick: () -> Unit,
    onDeleteStatusClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onRetryUpload: () -> Unit // Add retry callback
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDeleteStatusClick()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    // Animation states
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "expand_arrow_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = status.state is StatusState.Uploaded, // Only allow clicks for uploaded statuses
                onClick = onStatusClick
            )
            .padding(16.dp)
    ) {
        // Status header with preview and basic info
        StatusHeader(
            status = status,
            isExpanded = isExpanded,
            rotationState = rotationState,
            onToggleExpand = onToggleExpand,
            onRetryUpload = onRetryUpload
        )

        // Expanded engagement details (only show for uploaded statuses)
        AnimatedVisibility(
            visible = isExpanded && status.state is StatusState.Uploaded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                DeleteButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { showDeleteDialog = true }
                Spacer(modifier = Modifier.height(8.dp))
                EngagementDetails(status = status)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun StatusHeader(
    status: MyStatus,
    isExpanded: Boolean,
    rotationState: Float,
    onToggleExpand: () -> Unit,
    onRetryUpload: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Status media preview with upload state overlay
        Box {
            StatusMediaPreview(status = status)

            // Upload state overlay
            UploadStateOverlay(
                state = status.state,
                modifier = Modifier.matchParentSize()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Status info
        Column(modifier = Modifier.weight(1f)) {
            // First row: Time + Upload Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${status.createdAt.toRelativeTime()} ago",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when (status.state) {
                        is StatusState.Failed -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                UploadStatusIndicator(state = status.state)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Upload state message and actions
            UploadStateContent(
                state = status.state,
                onRetry = onRetryUpload,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Engagement summary for this status (only show for uploaded)
            if (status.state is StatusState.Uploaded) {
                StatusEngagementSummary(
                    views = status.views.size,
                    likes = status.likes.size,
                    replies = status.replies.size
                )
            }

            status.caption.takeUnless { it.isNullOrBlank() }?.let { caption ->
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

        // Expand/collapse button (only show for uploaded statuses)
        if (status.state is StatusState.Uploaded) {
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(24.dp).rotate(rotationState)
            ) {
                Icon(
                    imageVector =Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Composable
private fun UploadStateOverlay(
    state: StatusState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = when (state) {
                    is StatusState.Failed -> Color.Red.copy(alpha = 0.1f)
                    is StatusState.Uploading -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    is StatusState.Pending -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
    ) {
        when (state) {
            is StatusState.Uploading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is StatusState.Failed -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Upload failed",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            is StatusState.Pending -> {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pending upload",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> { /* No overlay for uploaded status */ }
        }
    }
}

@Composable
private fun UploadStatusIndicator(state: StatusState) {
    val (text, color) = when (state) {
        is StatusState.Pending -> "Pending" to MaterialTheme.colorScheme.onSurfaceVariant
        is StatusState.Uploading -> "Uploading" to MaterialTheme.colorScheme.primary
        is StatusState.Uploaded -> "Uploaded" to MaterialTheme.colorScheme.primary
        is StatusState.Failed -> "Failed" to MaterialTheme.colorScheme.error
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun UploadStateContent(
    state: StatusState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is StatusState.Uploading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Uploading your status...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is StatusState.Pending -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pending",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Waiting to upload...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is StatusState.Failed -> {
            Column(modifier = modifier) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upload failed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onRetry,
                    modifier = Modifier.height(32.dp).align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Retry Upload",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        else -> {
            // No content for uploaded status
        }
    }
}


@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete status",
            //tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Delete Status",
            style = MaterialTheme.typography.bodySmall,
            //color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this status? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
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
                    uri = status.mediaUri as Uri,
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