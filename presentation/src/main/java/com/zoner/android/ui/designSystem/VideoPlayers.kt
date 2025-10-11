package com.zoner.android.ui.designSystem

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.MediaDrmCallbackException
import androidx.media3.exoplayer.mediacodec.MediaCodecRenderer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.zoner.android.mediaplaybackmanager.MediaPlaybackManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.FileNotFoundException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(UnstableApi::class)
@Composable
fun FullScreenVideoPlayer(
    uri: Uri,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekForwardIncrementMs(10_000)
            .setSeekBackIncrementMs(10_000)
            .build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
                prepare()
            }
    }

    // Observe lifecycle to pause/resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    MediaPlaybackManager.registerNewPlayer(exoPlayer)
                    exoPlayer.play()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                    MediaPlaybackManager.unregisterPlayer(exoPlayer)
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
            MediaPlaybackManager.unregisterPlayer(exoPlayer)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {
                exoPlayer.pause()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun StatusVideoPlayer(
    videoUrl: String,
    isPaused: Boolean,
    modifier: Modifier = Modifier,
    onProgress: (Float) -> Unit = {},
    onEnded: () -> Unit = {}
) {
    val context = LocalContext.current
    var playerState by remember { mutableStateOf<PlayerState>(PlayerState.Idle) }

    // ExoPlayer instance should be remembered and properly disposed
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUrl))

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d("VideoPlayer", "Playback state changed: $state")
                        playerState = when (state) {
                            Player.STATE_READY -> PlayerState.Ready
                            Player.STATE_BUFFERING -> PlayerState.Buffering
                            Player.STATE_ENDED -> {
                                onEnded()
                                PlayerState.Ended
                            }
                            Player.STATE_IDLE -> {
                                // Only go to Idle if we're not already in error state
                                if (playerState !is PlayerState.Error) {
                                    PlayerState.Idle
                                } else {
                                    playerState // Keep error state
                                }
                            }
                            else -> PlayerState.Idle
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        playerState = PlayerState.Error(getErrorMessage(error))
                        Log.e("VideoPlayer", "Playback error: ${error.message}", error)
                    }
                })

                prepare()
            }
    }

    // Control playback based on pause state
    LaunchedEffect(isPaused) {
        exoPlayer.playWhenReady = !isPaused
    }

    // Progress tracking
    LaunchedEffect(playerState, exoPlayer.isPlaying) {
        if (playerState is PlayerState.Ready && exoPlayer.isPlaying) {
            while (isActive &&
                playerState is PlayerState.Ready &&
                exoPlayer.isPlaying) {
                val duration = exoPlayer.duration
                if (duration > 0) {
                    val progress = exoPlayer.currentPosition.toFloat() / duration
                    onProgress(progress.coerceIn(0f, 1f))
                }
                delay(100L)
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (playerState) {
            is PlayerState.Error -> {
                // FIX: Make error content fill the entire space and be clickable
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    ErrorContent(
                        errorMessage = (playerState as PlayerState.Error).message,
                        onRetry = {
                            playerState = PlayerState.Idle
                            exoPlayer.prepare()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            is PlayerState.Buffering -> {
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            else -> {
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, defaultMessage) = getErrorIconAndMessage(errorMessage)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Error",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage ?: defaultMessage,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = getErrorSubtitle(errorMessage),
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
//        Spacer(modifier = Modifier.height(16.dp))
//        // FIX: Make button more clickable with proper size
//        Button(
//            onClick = onRetry,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.White,
//                contentColor = Color.Black
//            ),
//            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
//        ) {
//            Text("Try Again", fontWeight = FontWeight.Medium)
//        }
    }
}

sealed class PlayerState {
    object Idle : PlayerState()
    object Buffering : PlayerState()
    object Ready : PlayerState()
    object Ended : PlayerState()

    data class Error(val message: String) : PlayerState()
}
@OptIn(UnstableApi::class)
private fun getErrorMessage(error: PlaybackException): String {
    return when {
        // Network errors
        error is HttpDataSource.HttpDataSourceException -> {
            when (error.type) {
                HttpDataSource.HttpDataSourceException.TYPE_OPEN -> {
                    when {
                        error is HttpDataSource.InvalidResponseCodeException -> {
                            when (error.responseCode) {
                                403 -> "Video is not accessible"
                                404 -> "Video not found"
                                500 -> "Server error"
                                else -> "Network error: ${error.responseCode}"
                            }
                        }
                        error.cause is UnknownHostException -> "No internet connection"
                        error.cause is SocketTimeoutException -> "Connection timeout"
                        else -> "Network error"
                    }
                }
                HttpDataSource.HttpDataSourceException.TYPE_READ -> "Error reading video data"
                else -> "Network error"
            }
        }

        // DRM errors
        error is MediaDrmCallbackException -> "DRM protection error"

        // Decoder errors
        error is MediaCodecRenderer.DecoderInitializationException -> "Video format not supported"

        // File/IO errors
        error is DataSourceException -> {
            when (error.cause) {
                is FileNotFoundException -> "Video file not found"
                is SecurityException -> "No permission to access video"
                else -> "File error"
            }
        }

        // Audio/Video renderer errors
        error is ExoPlaybackException -> {
            when (error.type) {
                ExoPlaybackException.TYPE_RENDERER -> "Video playback error"
                ExoPlaybackException.TYPE_UNEXPECTED -> "Unexpected playback error"
                else -> "Playback error"
            }
        }

        // Default case
        else -> "Failed to play video"
    }
}

@Composable
private fun getErrorIconAndMessage(errorMessage: String?): Pair<ImageVector, String> {
    return when {
        errorMessage?.contains("internet", ignoreCase = true) == true ->
            Pair(Icons.Default.WifiOff, "No internet connection")

        errorMessage?.contains("not supported", ignoreCase = true) == true ->
            Pair(Icons.Default.VideoLibrary, "Format not supported")

        errorMessage?.contains("not found", ignoreCase = true) == true ->
            Pair(Icons.Default.BrokenImage, "Video not found")

        errorMessage?.contains("timeout", ignoreCase = true) == true ->
            Pair(Icons.Default.Schedule, "Connection timeout")

        errorMessage?.contains("server", ignoreCase = true) == true ->
            Pair(Icons.Default.CloudOff, "Server error")

        errorMessage?.contains("permission", ignoreCase = true) == true ->
            Pair(Icons.Default.Lock, "Access denied")

        errorMessage?.contains("DRM", ignoreCase = true) == true ->
            Pair(Icons.Default.Security, "DRM protected content")

        else -> Pair(Icons.Default.Error, "Playback error")
    }
}

private fun getErrorSubtitle(errorMessage: String?): String {
    return when {
        errorMessage?.contains("internet", ignoreCase = true) == true ->
            "Please check your connection and try again"

        errorMessage?.contains("not supported", ignoreCase = true) == true ->
            "This video format is not supported on your device"

        errorMessage?.contains("not found", ignoreCase = true) == true ->
            "The video may have been removed or is unavailable"

        errorMessage?.contains("timeout", ignoreCase = true) == true ->
            "The connection took too long to respond"

        errorMessage?.contains("server", ignoreCase = true) == true ->
            "There's an issue with the video server"

        errorMessage?.contains("permission", ignoreCase = true) == true ->
            "You don't have permission to access this video"

        errorMessage?.contains("DRM", ignoreCase = true) == true ->
            "This content has digital rights protection"

        else -> "Unable to retrieve post. Please try again later."
    }
}

