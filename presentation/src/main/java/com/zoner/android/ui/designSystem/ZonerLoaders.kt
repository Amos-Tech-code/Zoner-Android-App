package com.zoner.android.ui.designSystem

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    action: String = "Loading...",
    text: String = "Please wait a moment."
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 32.dp)
        ) {
            Text(text = action, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZonerCircularIndicator()
                Spacer(modifier = Modifier.width(10.dp))
                Text(text, fontWeight = FontWeight.Light)
            }
        }
    }
}

@Composable
fun ZonerCircularIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    roundedEnds: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .rotate(angle)
    ) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = if (roundedEnds) StrokeCap.Round else StrokeCap.Butt
        )

        val diameter = size.toPx() - stroke.width
        val topLeftOffset = Offset(
            x = (drawContext.size.width - diameter) / 2f,
            y = (drawContext.size.height - diameter) / 2f
        )

        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            style = stroke,
            topLeft = topLeftOffset,
            size = Size(diameter, diameter)
        )
    }
}


@Composable
fun LoadingComponent(
    modifier: Modifier = Modifier,
    type: LoadingType = LoadingType.Circular,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    size: Dp = 48.dp,
    message: String? = null,
    messageStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    isFullScreen: Boolean = false
) {
    Box(
        modifier = modifier
            .then(if (isFullScreen) Modifier.fillMaxSize() else Modifier)
            .background(if (isFullScreen) backgroundColor.copy(alpha = 0.3f) else Color.Transparent)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (type) {
                LoadingType.Circular -> CircularProgressIndicator(
                    modifier = Modifier.size(size),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 4.dp
                )

                LoadingType.Linear -> LinearLoadingIndicator(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    color = color
                )

                LoadingType.Shimmer -> ShimmerLoadingBox(
                    color = color,
                    modifier = Modifier.size(size)
                )
                LoadingType.Wave -> WaveLoadingIndicator(color, size)
                LoadingType.BouncingBars -> BouncingBarsLoader(color, size)
                LoadingType.RotatingDots -> RotatingDotsLoader(color, size)
            }

            message?.let {
                Text(
                    text = it,
                    style = messageStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
private fun LinearLoadingIndicator(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Animate progress from 0 to 1
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Main progress bar
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            progress = { progress }
        )

        // Subtle glow trail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .drawWithCache {
                    val brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.1f),
                            color.copy(alpha = 0.3f),
                            color.copy(alpha = 0.1f)
                        )
                    )
                    onDrawBehind {
                        drawRect(brush = brush)
                    }
                }
        )
    }
}


@Composable
private fun WaveLoadingIndicator(
    color: Color,
    size: Dp
) {
    val dotCount = 5
    val dotSize = size / 6
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1000,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer { scaleY = scale }
                    .background(
                        color = color.copy(alpha = scale),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun BouncingBarsLoader(
    color: Color,
    size: Dp
) {
    val barCount = 4
    val barWidth = size / 8
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.height(size)
    ) {
        repeat(barCount) { index ->
            // Animate height as a fraction (0-1) then multiply by size
            val heightFraction by infiniteTransition.animateFloat(
                initialValue = 0.25f, // size/4
                targetValue = 1f,     // full size
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 100,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val height = size * heightFraction

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height)
                    .background(
                        color = color.copy(alpha = 0.3f + heightFraction * 0.7f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun RotatingDotsLoader(
    color: Color,
    size: Dp
) {
    val dotCount = 4
    val dotSize = size / 6
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(contentAlignment = Alignment.Center) {
        // Center dot
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(color = color, shape = CircleShape)
        )

        // Rotating dots
        repeat(dotCount) { index ->
            val angle = (index * (360f / dotCount)) + rotation
            val radius = size.value / 2 - dotSize.value

            Box(
                modifier = Modifier
                    .offset {
                        val x = (radius * cos(Math.toRadians(angle.toDouble()))).toFloat()
                        val y = (radius * sin(Math.toRadians(angle.toDouble()))).toFloat()
                        IntOffset(x.roundToInt(), y.roundToInt())
                    }
                    .size(dotSize)
                    .background(
                        color = color.copy(alpha = 0.6f + 0.4f * sin(Math.toRadians(angle.toDouble())).toFloat()),
                        shape = CircleShape
                    )
            )
        }
    }
}


@Composable
fun ShimmerLoadingBox(
    color: Color,
    modifier: Modifier,
) {
    val shimmerColors = listOf(
        color.copy(alpha = 0.3f),
        color.copy(alpha = 0.7f),
        color.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerEffect(translateAnim, shimmerColors)
        )
    }
}


fun Modifier.shimmerEffect(
    translateAnim: Float,
    colors: List<Color>
): Modifier = drawWithCache {
    val gradientWidth = (size.width / 2 * 1.5f)

    onDrawWithContent {
        drawContent()

        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(x = translateAnim - gradientWidth, y = 0f),
                end = Offset(x = translateAnim, y = 0f)
            ),
            blendMode = BlendMode.SrcAtop
        )
    }
}


enum class LoadingType {
    Circular, Linear, Shimmer, Wave, BouncingBars, RotatingDots
}
