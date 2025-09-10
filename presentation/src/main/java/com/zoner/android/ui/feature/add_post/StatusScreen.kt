package com.zoner.android.ui.feature.add_post

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoner.android.ui.designSystem.VideoThumbnail
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.util.isVideoUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun StatusMediaCarousel(
    modifier: Modifier = Modifier,
    items: List<AddPostViewModel.Status>,
    onCaptionChange: (index: Int, caption: String) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    onMoveItem: (from: Int, to: Int) -> Unit,
    onVideoPlayClicked: (Uri) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scrollScope = rememberCoroutineScope()
    val context = LocalContext.current

    LazyRow(
        state = lazyListState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
    ) {
        itemsIndexed(
            items,
            key = { index, status -> "${status.media.toString()}-$index" }
        ) { index, status ->
            var isDragging by remember { mutableStateOf(false) }
            var dragOffset by remember { mutableStateOf(0f) }
            val size by animateDpAsState(
                targetValue = if (isDragging) 240.dp else 220.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val elevation by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 0.dp
            )
            val alpha by animateFloatAsState(
                targetValue = if (isDragging) 0.6f else 1f,
                animationSpec = tween(durationMillis = 100)
            )

            // Auto-scroll when dragging near edges
            if (isDragging) {
                LaunchedEffect(isDragging) {
                    while (isDragging) {
                        with(density) {
                            val viewportWidth = lazyListState.layoutInfo.viewportEndOffset - lazyListState.layoutInfo.viewportStartOffset
                            val centerX = dragOffset + size.toPx() / 2

                            // Calculate scroll speed based on how close to edge
                            val scrollSpeed = when {
                                centerX > viewportWidth * 0.9f -> 50f
                                centerX > viewportWidth * 0.8f -> 30f
                                centerX < viewportWidth * 0.1f -> -50f
                                centerX < viewportWidth * 0.2f -> -30f
                                else -> 0f
                            }

                            if (scrollSpeed != 0f) {
                                scrollScope.launch {
                                    lazyListState.animateScrollBy(scrollSpeed)
                                }
                            }
                        }
                        delay(16) // ~60fps
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(size)
                    .aspectRatio(9f / 16f)
                    .shadow(elevation, RoundedCornerShape(16.dp))
                    .graphicsLayer {
                        translationX = if (isDragging) dragOffset else 0f
                        this.alpha = if (isDragging) 1f else alpha
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDragging = true
                                // Vibrate for better feedback
                                val vibrator =
                                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(
                                        VibrationEffect.createOneShot(
                                            15,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )
                                } else {
                                    vibrator.vibrate(15)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.x

                                // Calculate threshold based on item size
                                val threshold = size.toPx() * 0.3f

                                // Check if we should swap positions
                                when {
                                    dragOffset < -threshold && index > 0 -> {
                                        onMoveItem(index, index - 1)
                                        dragOffset = 0f
                                    }

                                    dragOffset > threshold && index < items.lastIndex -> {
                                        onMoveItem(index, index + 1)
                                        dragOffset = 0f
                                    }
                                }
                            },
                            onDragEnd = {
                                // Animate to final position
                                dragOffset = 0f
                                isDragging = false
                            },
                            onDragCancel = {
                                dragOffset = 0f
                                isDragging = false
                            }
                        )
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .animateItem()
            ) {
                if (status.media?.let { context.isVideoUri(it) } == true) {
                    VideoThumbnail(uri = status.media, onVideoPlayClicked = { onVideoPlayClicked(status.media) })
                } else {
                    ZonerAsyncImage(
                        imageUrl = status.media,
                        contentDescription = "Story Media $index",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Remove button
                IconButton(
                    onClick = { onRemoveItem(index) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                    )
                }

                // Caption input
                OutlinedTextField(
                    value = status.caption,
                    onValueChange = { onCaptionChange(index, it) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    placeholder = {
                        Text("Write a caption...", color = Color.White.copy(alpha = 0.6f))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedContainerColor = Color.Black.copy(0.6f),
                        focusedContainerColor = Color.Black.copy(0.6f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    maxLines = 2
                )

                // Drag Handle (☰)
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(28.dp)
                )
            }
        }
    }
}