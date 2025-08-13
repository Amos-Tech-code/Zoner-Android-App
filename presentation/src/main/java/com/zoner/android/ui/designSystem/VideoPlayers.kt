package com.zoner.android.ui.designSystem

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.zoner.android.mediaplaybackmanager.MediaPlaybackManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
    uri: Uri,
    paused: Boolean,
    onProgress: (Float) -> Unit,
    onEnded: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPrepared by remember { mutableStateOf(false) }

    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = !paused
        }
    }

    LaunchedEffect(paused) {
        exoPlayer.playWhenReady = !paused
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) isPrepared = true
                if (state == Player.STATE_ENDED) onEnded()
            }
        }
        exoPlayer.addListener(listener)

        val progressJob = lifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val duration = exoPlayer.duration
                if (duration > 0) {
                    val pos = exoPlayer.currentPosition
                    onProgress((pos.toFloat() / duration).coerceIn(0f, 1f))
                }
                delay(100L)
            }
        }

        onDispose {
            progressJob.cancel()
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }},
            modifier = Modifier.fillMaxSize()
        )

        if (!isPrepared) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}


