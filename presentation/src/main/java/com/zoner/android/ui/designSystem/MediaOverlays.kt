package com.zoner.android.ui.designSystem

import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zoner.android.util.getLegacyPathFromUri
import java.io.File

@Composable
fun VideoThumbnail(
    modifier: Modifier = Modifier,
    uri: Uri,
    localPath: String? = null,
    context: Context = LocalContext.current,
    showPlayButton: Boolean = true, // Parameter to control overlay visibility
    onVideoPlayClicked: () -> Unit = {} // Default empty lambda for when play button is hidden
) {
    val thumbnail by remember(uri, localPath) {
        derivedStateOf {
            try {
                // Using the direct local file path
                localPath?.let { path ->
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        return@derivedStateOf ThumbnailUtils.createVideoThumbnail(
                            path,
                            MediaStore.Images.Thumbnails.MINI_KIND
                        )?.asImageBitmap()
                    }
                }

                // FALLBACK: Only if local path doesn't work, try URI approaches
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Modern way using ContentResolver
                    context.contentResolver.loadThumbnail(
                        uri,
                        Size(512, 512),
                        null
                    )?.asImageBitmap()
                } else {
                    // Legacy way
                    val filePath = context.getLegacyPathFromUri(uri)
                    if (filePath != null) {
                        ThumbnailUtils.createVideoThumbnail(
                            filePath,
                            MediaStore.Images.Thumbnails.MINI_KIND
                        )?.asImageBitmap()
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        thumbnail?.let {
            Image(
                bitmap = it,
                contentDescription = "Video thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            )
        }

        // Only show play button overlay if requested
        if (showPlayButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .clickable { onVideoPlayClicked() }
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play video",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
