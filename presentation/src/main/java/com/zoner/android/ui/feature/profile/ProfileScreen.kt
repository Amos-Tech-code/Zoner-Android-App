package com.zoner.android.ui.feature.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.ui.navigation.SettingsRoute
import com.zoner.android.ui.designSystem.EmptyState
import com.zoner.android.ui.designSystem.LoadingComponent
import com.zoner.android.ui.designSystem.LoadingType
import com.zoner.android.ui.designSystem.PostItem
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.theme.ZonerInfo
import com.zoner.android.util.ObserveAsEvents
import com.zoner.android.util.formatShort
import com.zoner.android.util.toRelativeTime
import com.zoner.domain.model.LocalUser
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is ProfileEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
            is ProfileEvent.ShowSuccessMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = if (state.isBusinessAccount) {
        listOf("Posts", "Replies", "Likes", "Bookmarks")
    } else listOf("Replies","Likes", "Bookmarks")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })

    Scaffold(
        topBar = {
            ProfileTopBar(
                onSettingsClick = { navController.navigate(SettingsRoute) },
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            LoadingComponent(
                type = LoadingType.Circular,
                isFullScreen = true
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            {
                item {
                    ProfileHeader(
                        user = state.user,
                        isBusinessAccount = state.isBusinessAccount,
                        onEditClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = /*state.user.bio*/ "",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item {
                    ProfileStats(
                        followers = 0/*state.user.followers*/,
                        following = 0/*state.user.following*/,
                        posts = 0 /*state.user.posts*/,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                stickyHeader {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        divider = {}
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = { Text(title) }
                            )
                        }
                    }
                }

                item {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                    ) { page ->
                        when (tabTitles[page]) {
                            "Posts" -> PostList(posts = state.posts)
                            "Replies" -> RepliesList(replies = state.replies)
                            "Likes" -> LikesList(posts = state.likedPosts)
                            "Bookmarks" -> BookmarksList(posts = state.bookmarkedPosts)
                            else -> Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ProfileHeader(
    user: LocalUser?,
    isBusinessAccount: Boolean,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isBusinessAccount) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ZonerAsyncImage(
                    imageUrl = user?.businessLogo,
                    contentDescription = "Business Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                )
                Box(modifier = Modifier
                    .size(80.dp)
                    .offset(y = 20.dp)
                    .clip(CircleShape)
                    .background(ZonerInfo, shape = CircleShape)
                    .border(
                        width = 4.dp,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.background
                    )
                    .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
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
                                fontSize = 24.sp,
                                color = Color.White,
                                minLines = 1,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            user?.imgUrl?.let {
                ZonerAsyncImage(
                    imageUrl = it,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(color = ZonerInfo, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    user?.name?.first()?.uppercase()?.let {
                        Text(
                            text = it,
                            fontSize = 24.sp,
                            color = Color.White,
                            minLines = 1,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        ZonerSpacer(16.dp)

        Column {
            user?.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            user?.username?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (isBusinessAccount) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified business",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Business Account",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Different action buttons based on account type
        if (isBusinessAccount) {
            BusinessProfileActions()
        } else {
            PersonalProfileActions()
        }
    }
}

@Composable
private fun BusinessProfileActions() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { /* Open business tools */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("Business Tools")
        }
        Button(
            onClick = { /* Edit profile */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("Edit Profile")
        }
    }
}

@Composable
private fun PersonalProfileActions() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { /* Edit profile */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("Edit Profile")
        }
    }
}

@Composable
private fun ProfileStats(
    followers: Int,
    following: Int,
    posts: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        ProfileStatItem(count = posts, label = "Posts")
        Spacer(modifier = Modifier.width(16.dp))
        ProfileStatItem(count = followers, label = "Followers")
        Spacer(modifier = Modifier.width(16.dp))
        ProfileStatItem(count = following, label = "Following")
    }
}


@Composable
private fun ProfileStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.formatShort(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            IconButton(
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "settings"
                )
            }

        }
    )
}


@Composable
private fun RepliesList(
    modifier: Modifier = Modifier,
    replies: List<Reply>
) {

    if (replies.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.ChatBubbleOutline,
            message = "No replies yet",
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            replies.forEach { reply ->
                ReplyItem(
                    reply = reply,
                    onReplyClick = {
                        // navController.navigate("post/${reply.postId}")
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}


@Composable
private fun PostList(
    modifier: Modifier = Modifier,
    posts: List<Post>
) {
    if (posts.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.FavoriteBorder,
            message = "No added posts yet"
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            posts.forEach { post ->
                PostItem(
                    post = post,
                    onPostClick = { postId ->
                        // navController.navigate("post/$postId")
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun LikesList(
    modifier: Modifier = Modifier,
    posts: List<Post>
) {
    if (posts.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.FavoriteBorder,
            message = "No liked posts"
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            posts.forEach { post ->
                PostItem(
                    post = post,
                    onPostClick = { postId ->
                        // navController.navigate("post/$postId")
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun BookmarksList(
    modifier: Modifier = Modifier,
    posts: List<Post>
) {
    if (posts.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.BookmarkBorder,
            message = "No bookmarks yet"
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            posts.forEach { post ->
                PostItem(
                    post = post,
                    onPostClick = { postId ->
                        // navController.navigate("post/$postId")
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun ReplyItem(
    reply: Reply,
    onReplyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onReplyClick(reply.postId) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Reply author avatar
            ZonerAsyncImage(
                imageUrl = reply.author.avatarUrl,
                contentDescription = "Reply author",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Reply content
            Column(modifier = Modifier.weight(1f)) {
                // Reply header (name + username + time)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reply.author.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@${reply.author.username}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "· ${reply.timestamp.toRelativeTime()}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Reply text
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reply.content,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Replying to indicator
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Replying to @originalPoster", // You'll need to add original poster info to Reply data class
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Reply actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            // Like button
            IconButton(
                onClick = { /* Handle like reply */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like reply",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Reply button
            IconButton(
                onClick = { /* Handle reply to reply */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Reply,
                    contentDescription = "Reply",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}