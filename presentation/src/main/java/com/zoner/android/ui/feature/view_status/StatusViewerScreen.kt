package com.zoner.android.ui.feature.view_status

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.ui.designSystem.LoadingComponent
import com.zoner.android.ui.designSystem.LoadingType
import com.zoner.android.ui.designSystem.StatusVideoPlayer
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.util.ObserveAsEvents
import com.zoner.android.util.toRelativeTime
import com.zoner.domain.model.BaseStatus
import com.zoner.domain.model.MediaType
import com.zoner.domain.model.StatusGroup
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun StatusViewerScreen(
    navController: NavController,
    viewModel: StatusViewerViewModel = koinViewModel()
) {
    val state by viewModel.viewingState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            StatusViewingEvents.NavigateBack -> navController.navigateUp()
            is StatusViewingEvents.ShowError -> {
                navController.navigateUp()
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold { innerPadding ->
        StatusViewerContent(
            state = state,
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

@Composable
fun StatusViewerContent(
    state: StatusViewingState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStatus = state.statuses.getOrNull(state.currentIndex)
    var isPressed by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.Black)) {
        if (state.isLoading) {
            LoadingComponent(
                type = LoadingType.Circular,
                isFullScreen = true,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (currentStatus != null) {
            StatusMediaContent(
                status = currentStatus,
                isPaused = state.paused || isPressed,
                onProgressChanged = onProgressChanged,
                onMediaEnded = onNext
            )

            StatusNavigationHandler(
                onNext = onNext,
                onPrevious = onPrevious,
                onPress = { isPressed = it },
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(0f) // <-- explicitly send to back
            )

            state.statusGroup?.let {
                StatusViewHeader(
                    statusGroup = it,
                    currentIndex = state.currentIndex,
                    progress = state.progressForCurrent,
                    onClose = onClose,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusMediaContent(
    status: BaseStatus,
    isPaused: Boolean,
    onProgressChanged: (Float) -> Unit,
    onMediaEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (status.mediaType) {
        MediaType.IMAGE -> {
            StatusImageViewer(
                status = status,
                isPaused = isPaused,
                onProgressChanged = onProgressChanged,
                onAnimationFinished = onMediaEnded,
                modifier = modifier
            )
        }
        MediaType.VIDEO -> {
            StatusVideoPlayer(
                videoUrl = status.mediaUri.toString(),
                isPaused = isPaused,
                onProgress = onProgressChanged,
                onEnded = onMediaEnded,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun StatusViewHeader(
    statusGroup: StatusGroup,
    currentIndex: Int,
    progress: Float,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStatus = statusGroup.statuses.getOrNull(currentIndex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(0.4f))
            .padding(horizontal = 8.dp)
            .height(70.dp)
    ) {
        StatusProgressBars(
            total = statusGroup.statuses.size,
            currentIndex = currentIndex,
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZonerAsyncImage(
                    imageUrl = statusGroup.authorAvatar,
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                )

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    statusGroup.authorName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                        )
                    }
                    currentStatus?.let {
                        Text(
                            text = "${it.createdAt.toRelativeTime()} ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusNavigationHandler(
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPress: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress(true)
                        tryAwaitRelease()
                        onPress(false)
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        when {
                            offset.x < screenWidth / 3 -> onPrevious()
                            offset.x > screenWidth * 2f / 3 -> onNext()
                        }
                    }
                )
            }
    )
}

@Composable
private fun StatusProgressBars(
    total: Int,
    currentIndex: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(total) { index ->
            val animatedProgress by animateFloatAsState(
                targetValue = when {
                    index < currentIndex -> 1f
                    index == currentIndex -> progress
                    else -> 0f
                },
                animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing),
                label = "progress_animation"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            )
        }
    }
}


@Composable
private fun StatusImageViewer(
    status: BaseStatus,
    isPaused: Boolean,
    onProgressChanged: (Float) -> Unit,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isImageLoading by remember { mutableStateOf(true) }
    val animatable = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(status, isPaused, isImageLoading) {
        // Don't start animation if image is still loading or paused
        if (isImageLoading || isPaused) {
            animatable.stop()
            return@LaunchedEffect
        }

        // Start animation only when image is loaded and not paused
        val remainingTime = status.durationMillis * (1f - animatable.value)
        animatable.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = remainingTime.toInt(),
                easing = LinearEasing
            )
        ) {
            onProgressChanged(value)
            if (value >= 1f) {
                onAnimationFinished()
            }
        }
    }

    ZonerAsyncImage(
        imageUrl = status.mediaUri,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        onLoadingStateChange = { loading ->
            isImageLoading = loading
            // Reset progress when loading starts
            if (loading) {
                scope.launch{ animatable.snapTo(0f) }
                onProgressChanged(0f)
            }
        }
    )
}
//@Composable
//private fun StatusImageViewer(
//    status: BaseStatus,
//    isPaused: Boolean,
//    onProgressChanged: (Float) -> Unit,
//    onAnimationFinished: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val animatable = remember { Animatable(0f) }
//
//    LaunchedEffect(status, isPaused) {
//        animatable.stop()
//
//        if (!isPaused) {
//            val remainingTime = status.durationMillis * (1f - animatable.value)
//            animatable.animateTo(
//                1f,
//                animationSpec = tween(
//                    durationMillis = remainingTime.toInt(),
//                    easing = LinearEasing
//                )
//            ) {
//                onProgressChanged(value)
//                if (value >= 1f) {
//                    onAnimationFinished()
//                }
//            }
//        }
//    }
//
//    ZonerAsyncImage(
//        imageUrl = status.mediaUri,
//        contentDescription = null,
//        modifier = modifier.fillMaxSize(),
//        contentScale = ContentScale.Crop
//    )
//}