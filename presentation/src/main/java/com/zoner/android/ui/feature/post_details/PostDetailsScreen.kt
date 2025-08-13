package com.zoner.android.ui.feature.post_details

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zoner.android.ui.designSystem.PostItem
import com.zoner.android.ui.feature.profile.Post
import com.zoner.android.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    navController: NavController,
    postId: String = "id1",
    viewModel: PostDetailsViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is PostDetailsEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(postId) {
        viewModel.loadPostAndReplies(postId)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Post") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            }
        },
        bottomBar = {
            // Reply composer
            ReplyComposer(
                onReplySubmit = { content ->
                    viewModel.postReply(content)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(8.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            // Main post
            item {
                PostItem(
                    post = state.post,
                    onPostClick = { /* Already on post */ },
                    onAuthorClick = { },
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            }

            // Show thread context if available
            if (state.post.thread.isNotEmpty()) {
                items(state.post.thread) { threadPost ->
                    PostItem(
                        post = threadPost,
                        onPostClick = { /* Already in thread */ },
                        onAuthorClick = { },
                    )
                    ThreadConnector()
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            // Replies (nested)
            items(state.replies) { reply ->
                NestedReplyItem(
                    reply = reply,
                    depth = 0,
                    onProfileClick = { },
                    onReplyClick = { parentId ->
                        // Handle reply to reply
                    },
                    modifier = Modifier.padding(start = 16.dp * 0) // Increase for deeper nesting
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}



@Composable
fun ThreadConnector() {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            .padding(start = 24.dp) // Align with avatar
    )
}

@Composable
fun NestedReplyItem(
    reply: Post, // Using Post model for replies too
    depth: Int,
    onProfileClick: (String) -> Unit,
    onReplyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxDepth: Int = 5
) {
    val drawLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Thread line
            if (depth > 0) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = drawLineColor,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            // Reply content
            Column(modifier = Modifier.weight(1f)) {
                PostItem(
                    post = reply,
                    onPostClick = { /* Handle click */ },
                    onAuthorClick = onProfileClick,
                    modifier = Modifier.fillMaxWidth()
                )

                // Nested replies
                if (depth < maxDepth && reply.replyCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    reply.thread.forEach { nestedReply ->
                        NestedReplyItem(
                            reply = nestedReply,
                            depth = depth + 1,
                            onProfileClick = onProfileClick,
                            onReplyClick = onReplyClick,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyComposer(
    onReplySubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var replyText by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar
        AsyncImage(
            model = "https://picsum.photos/300/300?random=10",
            contentDescription = "Your avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Text field
        OutlinedTextField(
            value = replyText,
            onValueChange = { replyText = it },
            placeholder = { Text("Post your reply") },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        // Submit button
        IconButton(
            onClick = {
                if (replyText.isNotBlank()) {
                    onReplySubmit(replyText)
                    replyText = ""
                }
            },
            enabled = replyText.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send reply",
                tint = if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
