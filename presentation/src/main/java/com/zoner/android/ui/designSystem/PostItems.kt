package com.zoner.android.ui.designSystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoner.android.R
import com.zoner.android.ui.feature.profile.Media
import com.zoner.android.ui.feature.profile.Post
import com.zoner.android.ui.feature.profile.User
import com.zoner.android.ui.theme.PurpleRed
import com.zoner.android.ui.theme.ZonerInfo
import com.zoner.android.util.formatShort
import com.zoner.android.util.toRelativeTime
import com.zoner.domain.model.MediaType

@Composable
fun PostItem(
    modifier: Modifier = Modifier,
    post: Post,
    onPostClick: (String) -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    onLikeClick: (String) -> Unit = {},
    onReplyClick: (String) -> Unit = {},
    onRepostClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {},
    onMoreItemsClick: (String) ->  Unit = {},
    onMediaClick: (Int) -> Unit = {}
) {

    // Calculate max lines for collapsed state
    val maxLinesCollapsed = if (post.media.isNotEmpty()) 6 else 10

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.id) }
            .padding(16.dp)
    ) {
        // Header (Author info + timestamp)
        PostHeader(
            author = post.author,
            timestamp = post.timestamp,
            onAuthorClick = onAuthorClick,
            onMoreItemsClick = { onMoreItemsClick(post.id) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Post content
        ExpandableText(
            text = post.content.repeat(7),
            collapsedMaxLines = maxLinesCollapsed,
            modifier = Modifier.fillMaxWidth()
        )

        // Media content - Updated to use MediaGrid
        if (post.media.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            MediaGrid(
                media = post.media,
                onMediaClick = onMediaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = if (post.media.size == 1) 300.dp else 200.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Post stats and actions remain the same
        PostStats(
            replies = post.replies,
            reposts = post.reposts,
            likes = post.likes,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        PostActions(
            isLiked = post.isLiked,
            isBookmarked = post.isBookmarked,
            isReposted = post.isReposted,
            onLikeClick = { onLikeClick(post.id) },
            onReplyClick = { onReplyClick(post.id) },
            onRepostClick = { onRepostClick(post.id) },
            onBookmarkClick = { onBookmarkClick(post.id) },
            onShareClick = { onShareClick(post.id) }
        )
    }
}


@Composable
private fun PostHeader(
    author: User,
    timestamp: Long,
    onAuthorClick: (String) -> Unit,
    onMoreItemsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ZonerAsyncImage(
            imageUrl = author.avatarUrl,
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onAuthorClick(author.id) }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = author.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onAuthorClick(author.id) }
                )
                if (author.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = ZonerInfo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "@${author.username} · ${timestamp.toRelativeTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        IconButton(
            onClick = onMoreItemsClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}


@Composable
private fun MediaGrid(
    media: List<Media>,
    onMediaClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridShape = RoundedCornerShape(12.dp)

    when (media.size) {
        1 -> SingleMediaItem(
            media = media[0],
            onClick = { onMediaClick(0) },
            modifier = modifier
        )
        2 -> Column(modifier = modifier) {
            Row(modifier = Modifier.weight(1f)) {
                MediaGridItem(
                    media = media[0],
                    onClick = { onMediaClick(0) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        topEnd = CornerSize(1.dp),
                        bottomEnd = CornerSize(0.dp)
                    )
                )
                Spacer(modifier = Modifier.width(1.dp))
                MediaGridItem(
                    media = media[1],
                    onClick = { onMediaClick(1) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        topStart = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp)
                    )
                )
            }
        }
        3 -> Row(modifier = modifier) {
            MediaGridItem(
                media = media[0],
                onClick = { onMediaClick(0) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = gridShape.copy(
                    topEnd = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                )
            )
            Spacer(modifier = Modifier.width(1.dp))
            Column(modifier = Modifier.weight(1f)) {
                MediaGridItem(
                    media = media[1],
                    onClick = { onMediaClick(1) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        topStart = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    )
                )
                Spacer(modifier = Modifier.height(1.dp))
                MediaGridItem(
                    media = media[2],
                    onClick = { onMediaClick(2) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        topStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp),
                    )
                )
            }
        }
        4 -> Column(modifier = modifier) {
            Row(modifier = Modifier.weight(1f)) {
                MediaGridItem(
                    media = media[0],
                    onClick = { onMediaClick(0) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        bottomEnd = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp)
                    )
                )
                Spacer(modifier = Modifier.width(1.dp))
                MediaGridItem(
                    media = media[1],
                    onClick = { onMediaClick(1) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp),
                        topStart = CornerSize(0.dp)
                    )
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Row(modifier = Modifier.weight(1f)) {
                MediaGridItem(
                    media = media[2],
                    onClick = { onMediaClick(2) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        topStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    )
                )
                Spacer(modifier = Modifier.width(1.dp))
                MediaGridItem(
                    media = media[3],
                    onClick = { onMediaClick(3) },
                    modifier = Modifier.weight(1f),
                    shape = gridShape.copy(
                        topStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp)
                    )
                )
            }
        }
    }
}

@Composable
private fun SingleMediaItem(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            //.aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        when (media.type) {
            MediaType.IMAGE -> ZonerAsyncImage(
                imageUrl = media.url,
                contentDescription = "Post image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            MediaType.VIDEO -> VideoThumbnailUrl(
                url = media.url,
                onPlayClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MediaGridItem(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp)
) {
    Box(
        modifier = modifier
            //.aspectRatio(1f)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        when (media.type) {
            MediaType.IMAGE -> ZonerAsyncImage(
                imageUrl = media.url,
                contentDescription = "Post image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            MediaType.VIDEO -> VideoThumbnailUrl(
                url = media.url,
                onPlayClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }

        // Show play icon for videos
        if (media.type == MediaType.VIDEO) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "Play video",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
            )
        }

        // Show remaining count for grids with more than 4 items (though we limit to 4)
//        if (media == Media("", MediaType.IMAGE, 1f) /* && index == 3 && totalItems > 4 */) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "+${media - 4}",
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
    }
}


@Composable
fun VideoThumbnailUrl(
    url: String,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // You can use Coil, Glide, or another image loading library
        ZonerAsyncImage(
            imageUrl = url,
            contentDescription = "Video thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onPlayClick
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "Play video",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
    }
}

@Composable
private fun PostStats(
    replies: Int,
    reposts: Int,
    likes: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        StatItem(count = replies, label = "Replies")
        Spacer(modifier = Modifier.width(16.dp))
        StatItem(count = reposts, label = "Reposts")
        Spacer(modifier = Modifier.width(16.dp))
        StatItem(count = likes, label = "Likes")
    }
}

@Composable
private fun StatItem(count: Int, label: String) {
    Text(
        text = "${count.formatShort()} $label",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

@Composable
private fun PostActions(
    isLiked: Boolean,
    isBookmarked: Boolean,
    isReposted: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onRepostClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Reply
        IconPainterTextButton(
            icon = R.drawable.ic_chat_bubble,
            text = "Reply",
            onClick = onReplyClick,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Repost
        IconTextButton(
            icon = Icons.Outlined.Repeat,
            text = "Repost",
            onClick = onRepostClick,
            tint = if (isReposted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Like
        IconTextButton(
            icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            text = "Like",
            onClick = onLikeClick,
            tint = if (isLiked) PurpleRed
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Bookmark
        IconTextButton(
            icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            text = "Bookmark",
            onClick = onBookmarkClick,
            tint = if (isBookmarked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Engagement
        IconPainterTextButton(
            icon = R.drawable.ic_engagement,
            text = "24",
            onClick = onShareClick,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun IconTextButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
    }
}

@Composable
private fun IconPainterTextButton(
    icon: Int,
    text: String,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
    }
}


@Composable
fun PostOptionsBottomSheet(
    post: Post,
    onDismiss: () -> Unit,
    onNotInterestedClick: () -> Unit,
    onFollowClick: () -> Unit,
    onMuteClick: () -> Unit,
    onBlockClick: () -> Unit,
    onReportClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Options list
        Column(modifier = Modifier.fillMaxWidth()) {
            PostOptionItem(
                icon = Icons.Default.InsertEmoticon,
                text = "Not interested in this post",
                onClick = {
                    onNotInterestedClick()
                    onDismiss()
                }
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            PostOptionItem(
                icon = Icons.Default.PersonAdd,
                text = "Follow @${post.author.username}",
                onClick = {
                    onFollowClick()
                    onDismiss()
                }
            )

            PostOptionItem(
                icon = Icons.AutoMirrored.Outlined.VolumeMute,
                text = "Mute @${post.author.username}",
                onClick = {
                    onMuteClick()
                    onDismiss()
                }
            )

            PostOptionItem(
                icon = Icons.Default.HideSource,
                text = "Block @${post.author.username}",
                onClick = {
                    onBlockClick()
                    onDismiss()
                }
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            PostOptionItem(
                icon = Icons.Default.Share,
                text = "Share post",
                onClick = {
                    onShareClick()
                    onDismiss()
                }
            )
            PostOptionItem(
                icon = Icons.Outlined.BookmarkBorder,
                text = "Bookmark post",
                onClick = {
                    onBookmarkClick()
                    onDismiss()
                }
            )
            PostOptionItem(
                icon = Icons.Default.Report,
                text = "Report post",
                onClick = {
                    onReportClick()
                    onDismiss()
                }
            )
        }
    }
}


@Composable
fun PostOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
