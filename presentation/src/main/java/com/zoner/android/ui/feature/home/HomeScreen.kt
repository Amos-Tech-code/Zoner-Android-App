package com.zoner.android.ui.feature.home

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.NotStarted
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.R
import com.zoner.android.ui.designSystem.ErrorScreen
import com.zoner.android.ui.designSystem.LoadingComponent
import com.zoner.android.ui.designSystem.LoadingType
import com.zoner.android.ui.designSystem.PostItem
import com.zoner.android.ui.designSystem.PostOptionsBottomSheet
import com.zoner.android.ui.designSystem.VideoThumbnail
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.ui.feature.profile.Post
import com.zoner.android.ui.navigation.PostDetailsRoute
import com.zoner.android.ui.navigation.StatusViewRoute
import com.zoner.android.ui.navigation.UserStatusRoute
import com.zoner.android.ui.theme.ZonerInfo
import com.zoner.android.util.ObserveAsEvents
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.OtherUserStatus
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToAddPost: () -> Unit = {},
    onNavigateToAddStatus: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by remember { derivedStateOf { state is HomeState.Loading } }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HomeScreenTopBar(
                scrollBehavior = scrollBehavior,
                myStatusUiState = (state as? HomeState.Success)?.userStatusSummary ?: MyStatusUiState(),
                otherStatus = (state as? HomeState.Success)?.otherStatusSummary ?: emptyList(),
                isBusinessAccount = (state as? HomeState.Success)?.isBusinessAccount ?: false,
                user = viewModel.loggedInUser,
                isLoading = isLoading,
                onAddPostClick = onNavigateToAddPost,
                onAddStatusClick = onNavigateToAddStatus,
                onStatusClicked = { navController.navigate(StatusViewRoute) },
                onMyStatusClick = { navController.navigate(UserStatusRoute) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
    { innerPadding ->

        ObserveAsEvents(viewModel.event) { event ->
            when (event) {
                is HomeEvent.ShowErrorMessage -> {
                    Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(WindowInsets.navigationBars)
            .nestedScroll(scrollBehavior.nestedScrollConnection)

        when (state) {
            is HomeState.Error -> {
                ErrorScreen(
                    message = (state as HomeState.Error).message,
                    onRetry = { viewModel.retry() },
                    modifier = rootModifier
                )
            }

            HomeState.Loading -> {
                LoadingComponent(
                    isFullScreen = true,
                    size = 70.dp
                )
            }

            is HomeState.Success -> {
                PostsList(
                    posts = (state as HomeState.Success).posts,
                    onPostClick = { navController.navigate(PostDetailsRoute) },
                    onMoreItemsClick = { post ->
                        selectedPost = post
                        showBottomSheet = true
                    },
                    modifier = rootModifier
                )
            }
        }

        //Post Options bottom sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    selectedPost = null
                },
                sheetState = sheetState,
                sheetMaxWidth = 400.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                selectedPost?.let { post ->
                    PostOptionsBottomSheet(
                        post = post,
                        onDismiss = {
                            showBottomSheet = false
                            selectedPost = null
                        },
                        onReportClick = { /* Handle report */ },
                        onShareClick = { /* Handle share */ },
                        onBookmarkClick = { /* Handle bookmark */ },
                        onNotInterestedClick = {},
                        onFollowClick = {},
                        onMuteClick = {},
                        onBlockClick = {},
                    )
                }
            }
        }

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    myStatusUiState: MyStatusUiState,
    otherStatus: List<OtherUserStatusUiState>,
    isBusinessAccount: Boolean,
    isLoading: Boolean,
    user: LocalUser?,
    onAddPostClick: () -> Unit,
    onStatusClicked: () -> Unit,
    onMyStatusClick: () -> Unit,
    onAddStatusClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                HomeScreenHeader(onAddPostClick, user, modifier = Modifier.fillMaxWidth())
                if (isLoading) {
                    LoadingComponent(
                        type = LoadingType.Wave,
                    )
                } else {
                    StatusItems(
                        otherStatusUiState = otherStatus,
                        myStatusUiState = myStatusUiState,
                        isBusinessAccount = isBusinessAccount,
                        onAddStatusClick = onAddStatusClick,
                        onStatusClicked = onStatusClicked,
                        onMyStatusClick = onMyStatusClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}


@Composable
private fun HomeScreenHeader(
    onAddPostClick: () -> Unit,
    user: LocalUser?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 40.dp, max = 50.dp)
            .padding(end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = ZonerInfo, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Use this if you have a profile picture
            user?.imgUrl?.let {
                ZonerAsyncImage(
                    imageUrl = user.imgUrl,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().align(Alignment.Center)
                ) } ?: run {
                user?.name?.first()?.uppercase()?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.White,
                        minLines = 1,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(100.dp)
        )
        Button(
            onClick = onAddPostClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 28.dp)
        ) {
            Text("POST")
        }
    }
}


@Composable
private fun StatusItems(
    myStatusUiState: MyStatusUiState,
    otherStatusUiState: List<OtherUserStatusUiState>,
    isBusinessAccount: Boolean,
    onAddStatusClick: () -> Unit,
    onStatusClicked: () -> Unit,
    onMyStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(top = 4.dp)
    ) {
        if (isBusinessAccount && myStatusUiState.latestStatus == null) {
            item {
                AddStatusItem(
                    onClick = onAddStatusClick
                )
            }
        }
        if (myStatusUiState.latestStatus != null) {
            item {
                UserStatusItem(
                    state = myStatusUiState,
                    onClick = { onMyStatusClick() }
                )
            }
        }
        if (otherStatusUiState.isNotEmpty()) {
            items(otherStatusUiState, key = { it.authorId }) { status ->
                StatusItem(
                    state = status,
                    onClick = { onStatusClicked() }
                )
            }
        }
    }
}


@Composable
private fun UserStatusItem(
    modifier: Modifier = Modifier,
    state: MyStatusUiState,
    onClick: () -> Unit
) {
    // Determine border color based on state - UPLOADING has highest priority
    val borderColor = when {
        state.uploading > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        state.failed > 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    // Handle progress animation for uploading state
    val progress = remember { Animatable(0f) }

    LaunchedEffect(state.uploading) {
        if (state.uploading > 0) {
            // Animate progress when uploading
            progress.animateTo(
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            // Reset progress when not uploading
            progress.snapTo(0f)
        }
    }

    val baseCircleColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .width(70.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background border with segments
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2

                // Draw base circle (subtle background)
                drawCircle(
                    color = baseCircleColor,
                    radius = radius,
                    style = Stroke(strokeWidth)
                )

                // Draw status segments
                if (state.statusCount > 1) {
                    val segmentAngle = 360f / min(state.statusCount, 8)
                    val sweepAngle = segmentAngle * 0.85f // Gap between segments

                    repeat(min(state.statusCount, 8)) { index ->
                        val startAngle = index * segmentAngle - 90f
                        drawArc(
                            color = borderColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(strokeWidth, strokeWidth),
                            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
                            style = Stroke(strokeWidth)
                        )
                    }
                } else {
                    // Single status - full circle
                    drawCircle(
                        color = borderColor,
                        radius = radius,
                        style = Stroke(strokeWidth)
                    )
                }

                // Draw progress indicator for uploading
                if (state.uploading > 0) {
                    drawArc(
                        color = borderColor.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = 360f * progress.value,
                        useCenter = false,
                        topLeft = Offset(strokeWidth, strokeWidth),
                        size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
                        style = Stroke(strokeWidth)
                    )
                }
            }

            // Status content container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                // Show indicators - UPLOADING has highest priority
                when {
                    state.uploading > 0 -> {
                        // Uploading state (highest priority)
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Badge showing count of uploading statuses
                        if (state.uploading > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (state.uploading > 9) "9+" else state.uploading.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    state.failed > 0 -> {
                        // Error state (second priority)
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = "Failed to upload",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )

                        // Badge showing count of failed statuses
                        if (state.failed > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            ) {
                                Text(
                                    text = if (state.failed > 9) "9+" else state.failed.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    else -> {
                        // Normal state - show content
                        when(state.latestStatus?.mediaType) {
                            MediaType.IMAGE -> {
                                ZonerAsyncImage(
                                    imageUrl = state.latestStatus.mediaUri,
                                    contentDescription = "Status preview",
                                    modifier = Modifier.clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            MediaType.VIDEO -> {
                                VideoThumbnail(
                                    uri = state.latestStatus.mediaUri as Uri,
                                    localPath = state.latestStatus.localFilePath,
                                    showPlayButton = false,
                                    onVideoPlayClicked = onClick,
                                    modifier = Modifier.clip(CircleShape)
                                )
                            }

                            null -> {
                                // No media - show placeholder
                                Icon(
                                    Icons.Outlined.NotStarted,
                                    contentDescription = "Empty status",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Badge showing total status count if > 1
                        if (state.statusCount > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(y = (-4).dp, x = (-4).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (state.statusCount > 9) "9+" else state.statusCount.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Label
        Text(
            text = "My Status",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = when {
                state.uploading > 0 -> MaterialTheme.colorScheme.primary
                state.failed > 0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}


@Composable
private fun StatusItem(
    modifier: Modifier = Modifier,
    state: OtherUserStatusUiState,
    onClick: () -> Unit
) {
    // Determine border colors based on viewed state
    val unviewedColor = MaterialTheme.colorScheme.primary
    val viewedColor = MaterialTheme.colorScheme.background
    val baseCircleColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .width(70.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background border with segments
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2

                // Draw base circle (subtle background)
                drawCircle(
                    color = baseCircleColor,
                    radius = radius,
                    style = Stroke(strokeWidth)
                )

                // Draw status segments
                if (state.statusCount > 1) {
                    val segmentAngle = 360f / min(state.statusCount, 8)
                    val sweepAngle = segmentAngle * 0.85f // Gap between segments

                    repeat(min(state.statusCount, 8)) { index ->
                        val startAngle = index * segmentAngle - 90f
                        val segmentColor = if (index < state.viewedCount) viewedColor else unviewedColor

                        drawArc(
                            color = segmentColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(strokeWidth, strokeWidth),
                            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
                            style = Stroke(strokeWidth)
                        )
                    }
                } else {
                    // Single status - use appropriate color based on viewed state
                    val borderColor = if (state.viewedCount > 0) viewedColor else unviewedColor
                    drawCircle(
                        color = borderColor,
                        radius = radius,
                        style = Stroke(strokeWidth)
                    )
                }
            }

            // Staus Image container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                ZonerAsyncImage(
                    imageUrl = state.latestStatus?.blurHash,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Badge showing total status count if > 1
                if (state.statusCount > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y = (-4).dp, x = (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.viewedCount < state.statusCount)
                                    unviewedColor
                                else
                                    viewedColor
                            )
                    ) {
                        Text(
                            text = if (state.statusCount > 9) "9+" else state.statusCount.toString(),
                            color = if (state.viewedCount < state.statusCount)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        // Label
        Text(
            text = state.authorName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (state.viewedCount < state.statusCount)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun AddStatusItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Box {
        Box(
            modifier = modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                )
                .clickable { onClick() },
        ) {
            Image(
                painter = painterResource(R.drawable.ic_circle_logo),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center)
            )

        }

        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = "Add Status",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.BottomEnd),
        )
    }
}


@Composable
fun PostsList(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onMoreItemsClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var trackHeightPx by remember { mutableIntStateOf(0) }

    val scrollProgress by remember {
        derivedStateOf {
            val firstVisibleItem = listState.firstVisibleItemIndex
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems > 1)
                (firstVisibleItem.toFloat() / (totalItems - 1)).coerceIn(0f, 1f)
            else 0f
        }
    }

    // Animate the thumb position
    val animatedProgress by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ScrollProgressAnimation"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(posts, key = { item -> item.id }) { post ->
                PostItem(
                    post = post,
                    onPostClick = { onPostClick(post) },
                    onMoreItemsClick = { onMoreItemsClick(post) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // Custom scrollbar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .align(Alignment.CenterEnd)
                .padding(vertical = 12.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    trackHeightPx = layoutCoordinates.size.height
                }
        ) {
            // Scroll thumb
            val thumbHeightPx = with(LocalDensity.current) { 40.dp.toPx() }
            val offsetY = ((trackHeightPx - thumbHeightPx) * animatedProgress).toInt()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .offset { IntOffset(x = 0, y = offsetY) }
                    .background(Color.Gray, RoundedCornerShape(3.dp))
            )
        }
    }
}
