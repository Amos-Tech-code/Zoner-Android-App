package com.zoner.android.ui.feature.home

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
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
import com.zoner.android.ui.theme.ZonerInfo
import com.zoner.android.util.ObserveAsEvents
import com.zoner.domain.StatusState
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.UserStatus
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
                userStatus = (state as? HomeState.Success)?.userStatusItems ?: emptyList(),
                otherStatus = (state as? HomeState.Success)?.otherStatus ?: emptyList(),
                isBusinessAccount = (state as? HomeState.Success)?.isBusinessAccount ?: false,
                user = viewModel.loggedInUser,
                isLoading = isLoading,
                onAddPostClick = onNavigateToAddPost,
                onAddStatusClick = onNavigateToAddStatus,
                onStatusClicked = {},
                onMyStatusClick = { navController.navigate(StatusViewRoute) },
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
    otherStatus: List<Status>,
    userStatus: List<UserStatus>,
    isBusinessAccount: Boolean,
    isLoading: Boolean,
    user: LocalUser?,
    onAddPostClick: () -> Unit,
    onStatusClicked: (Status) -> Unit,
    onMyStatusClick: (List<UserStatus>) -> Unit,
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
                        otherStatus = otherStatus,
                        userStatus = userStatus,
                        isBusinessAccount = isBusinessAccount,
                        onAddStatusClick = onAddStatusClick,
                        onStatusClicked = onStatusClicked,
                        onMyStatusClick = onMyStatusClick,
                        modifier = Modifier.fillMaxSize(),
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
    userStatus: List<UserStatus>,
    otherStatus: List<Status>,
    isBusinessAccount: Boolean,
    onAddStatusClick: () -> Unit,
    onStatusClicked: (Status) -> Unit,
    onMyStatusClick: (List<UserStatus>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isBusinessAccount) {
            item {
                AddStatusItem(
                    onClick = onAddStatusClick
                )
            }
        }
        if (userStatus.isNotEmpty()) {
            item {
                UStatusItem(
                    status = userStatus.last(),
                    statusCount = userStatus.size,
                    onClick = { onMyStatusClick(userStatus) }
                )
            }
        }
        items(otherStatus, key = { it.id}) { status ->
            StatusItem(
                status = status,
                onClick = { onStatusClicked(status) }
            )
        }
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
private fun UStatusItem(
    modifier: Modifier = Modifier,
    status: UserStatus,
    statusCount: Int = 1,
    onClick: () -> Unit
) {
    val borderColor = when {
        status.state is StatusState.Failed -> MaterialTheme.colorScheme.error
        status.isViewed -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier.size(70.dp),
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

                if (statusCount > 1) {
                    val segmentAngle = 360f / min(statusCount, 8) // Max 8 segments for visibility
                    repeat(min(statusCount, 8)) { index ->
                        val startAngle = index * segmentAngle - 90f
                        drawArc(
                            color = borderColor,
                            startAngle = startAngle,
                            sweepAngle = segmentAngle * 0.9f, // Small gap between segments
                            useCenter = false,
                            topLeft = Offset(strokeWidth, strokeWidth),
                            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
                            style = Stroke(strokeWidth)
                        )
                    }
                } else {
                    drawCircle(
                        color = borderColor,
                        radius = radius,
                        style = Stroke(strokeWidth)
                    )
                }
            }

            // Status content
            Box(
                modifier = Modifier
                    .size(52.dp) // Slightly smaller than border
                    .clip(CircleShape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                // Show error/uploading indicators
                when (status.state) {
                    is StatusState.Failed -> Icon(
                        Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )

                    StatusState.Uploading ->
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                    else -> {
                        when(status.mediaType) {
                            MediaType.IMAGE -> {
                                ZonerAsyncImage(
                                    imageUrl = status.mediaUri,
                                    contentDescription = "Status preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            MediaType.VIDEO -> {
                                VideoThumbnail(
                                    uri = status.mediaUri,
                                    onVideoPlayClicked = onClick,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "My Status",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusItem(
     modifier: Modifier = Modifier,
     status: Status,
     onClick: () -> Unit
) {
    val backgroundColor = if (status.isViewed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .size(70.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(width = 2.dp, color = backgroundColor, shape = CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            ZonerAsyncImage(
                imageUrl = status.media.firstOrNull()?.url,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Text(
            text = status.author.name,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}