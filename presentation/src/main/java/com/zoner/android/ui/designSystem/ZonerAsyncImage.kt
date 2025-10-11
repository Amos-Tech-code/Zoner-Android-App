package com.zoner.android.ui.designSystem

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale

@Composable
fun ZonerAsyncImage(
    modifier: Modifier = Modifier,
    imageUrl: Any?,
    contentDescription: String? = null,
    // Custom composable for loading state (defaults to shimmer)
    loadingComposable: @Composable (() -> Unit)? = null,
    // Custom composable for error state
    errorComposable: @Composable (() -> Unit)? = null,
    // Shimmer color
    shimmerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    // Image content scale
    contentScale: ContentScale = ContentScale.Crop,
    onLoadingStateChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        onLoadingStateChange(isLoading)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .scale(Scale.FILL)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            onLoading = { isLoading = true },
            onError = {
                isLoading = false
                isError = true
                onLoadingStateChange(false)
            },
            onSuccess = { isLoading = false
                isError = false
                onLoadingStateChange(false)
            },
            modifier = Modifier
                .fillMaxSize()
        )

        // Loading state
        if (isLoading) {
            loadingComposable?.invoke() ?: ShimmerLoadingBox(
                color = shimmerColor,
                modifier = Modifier.matchParentSize()
            )
        }

        // Error state
        if (isError) {
            errorComposable?.invoke() ?: DefaultErrorPlaceholder(
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

// Default error placeholder
@Composable
private fun DefaultErrorPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}


@Composable
fun ZoomableImage(uri: Uri) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += panChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2f
                        offset = Offset.Zero
                    }
                )
            }
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state),
            contentScale = ContentScale.Crop,
        )
    }
}
