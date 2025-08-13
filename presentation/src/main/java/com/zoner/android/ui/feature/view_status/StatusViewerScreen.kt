package com.zoner.android.ui.feature.view_status

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.ui.designSystem.LoadingComponent
import com.zoner.android.ui.designSystem.LoadingType
import com.zoner.android.ui.designSystem.StatusVideoPlayer
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.util.ObserveAsEvents
import com.zoner.domain.model.MediaType
import org.koin.androidx.compose.koinViewModel

@Composable
fun StatusViewerScreen(
    navController: NavController,
    viewModel: StatusViewerViewModel = koinViewModel(),
) {
    val state by viewModel.viewingState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            StatusViewingEvents.NavigateBack -> navController.navigateUp()
        }
    }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (state.isLoading) {
                LoadingComponent(
                    type = LoadingType.Circular,
                    isFullScreen = true
                )
            } else {
                val current = state.statuses.getOrNull(state.currentIndex)
                val animatable = remember { Animatable(0f) }
                var isPressed by remember { mutableStateOf(false) }

                // Handle animation for both image and video
                LaunchedEffect(current, state.paused, isPressed) {
                    if (current == null) return@LaunchedEffect

                    animatable.stop()

                    if (!state.paused && !isPressed) {
                        val remainingTime = when (current.mediaType) {
                            MediaType.IMAGE -> (current.durationMillis ?: 5000L) * (1f - state.progressForCurrent)
                            MediaType.VIDEO -> 0L // Video handles its own progress
                        }

                        if (current.mediaType == MediaType.IMAGE) {
                            animatable.snapTo(state.progressForCurrent)
                            animatable.animateTo(
                                1f,
                                animationSpec = tween(
                                    durationMillis = remainingTime.toInt(),
                                    easing = LinearEasing
                                )
                            ) {
                                viewModel.onProgressChanged(value)
                                if (value >= 1f) {
                                    viewModel.onImageAnimationFinished()
                                }
                            }
                        }
                    }
                }

                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    if (current != null) {
                        when (current.mediaType) {
                            MediaType.IMAGE -> {
                                ZonerAsyncImage(
                                    imageUrl = current.mediaUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            MediaType.VIDEO -> {
                                StatusVideoPlayer(
                                    uri = current.mediaUri,
                                    paused = state.paused || isPressed,
                                    onProgress = viewModel::onVideoProgress,
                                    onEnded = viewModel::onVideoEnded
                                )
                            }
                        }
                    }

                    StatusProgressBars(
                        total = state.statuses.size,
                        currentIndex = state.currentIndex,
                        progress = state.progressForCurrent,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        viewModel.togglePause(true)
                                        tryAwaitRelease()
                                        isPressed = false
                                        viewModel.togglePause(false)
                                    },
                                    onTap = { offset ->
                                        val screenWidth = size.width
                                        when {
                                            // Left 1/3 - previous status
                                            offset.x < screenWidth / 3 -> {
                                                viewModel.moveToPreviousStatus()
                                            }
                                            // Right 1/3 - next status
                                            offset.x > screenWidth * 2f / 3 -> {
                                                viewModel.moveToNextStatus()
                                            }
                                            // Middle 1/3
                                        }
                                    }
                                )
                            }
                    )

                    IconButton(
                        onClick = { viewModel.closeViewer() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
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
                animationSpec = tween(durationMillis = 100, easing = LinearEasing),
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