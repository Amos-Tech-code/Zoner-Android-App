package com.zoner.android.ui.feature.add_post

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.ui.navigation.CreateBusinessRoute
import com.zoner.android.ui.navigation.MainAppRoute
import com.zoner.android.ui.designSystem.AccountRequiredDialog
import com.zoner.android.ui.designSystem.DropdownMenuButton
import com.zoner.android.ui.designSystem.FullScreenVideoPlayer
import com.zoner.android.ui.designSystem.VideoThumbnail
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.theme.ZonerInfo
import com.zoner.android.util.MAX_POST_MEDIA
import com.zoner.android.util.MAX_STATUS_MEDIA
import com.zoner.android.util.ObserveAsEvents
import com.zoner.android.util.isVideoUri
import com.zoner.domain.model.Audience
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.PostType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddPostScreen(
    navController: NavController,
    postTypeParam: PostType? = null,
    navigateUp: () -> Unit = {},
    viewModel: AddPostViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val postDataForm by viewModel.postFormState.collectAsStateWithLifecycle()
    val statusDataForm by viewModel.statusFormState.collectAsStateWithLifecycle()
    val postType by viewModel.postType.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val maxSelectableMedia = remember(postType) {
        when (postType) {
            PostType.POST -> MAX_POST_MEDIA
            PostType.STATUS -> MAX_STATUS_MEDIA
        }
    }
    // Add this state for camera handling
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(postTypeParam != null) {
        postTypeParam?.let { viewModel.updatePostType(postTypeParam) }
    }

    // Events
    ObserveAsEvents(viewModel.event) {
        when (it) {
            is AddPostEvent.ShowErrorMessage -> {
                scope.launch{ snackBarHostState.showSnackbar(it.message) }
            }

            is AddPostEvent.ShowSuccessMessage -> {
                Toast.makeText(navController.context, it.message, Toast.LENGTH_LONG).show()
                navigateUp()
            }
        }
    }

    // Handle the captured image
    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uri ->
            when (postType) {
                PostType.POST -> viewModel.updatePostUris(listOf(uri), MAX_POST_MEDIA)
                PostType.STATUS -> viewModel.addStatusItems(
                    listOf(Status(media = uri, caption = "")),
                    MAX_STATUS_MEDIA
                )
            }
            capturedImageUri = null
        }
    }

    // Pick Media Items
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            when (postType) {
                PostType.POST -> {
                    viewModel.updatePostUris(listOf(it), maxLimit = MAX_POST_MEDIA)
                }

                PostType.STATUS -> {
                    viewModel.addStatusItems(
                        listOf(Status(media = it, caption = "")),
                        maxLimit = MAX_STATUS_MEDIA
                    )
                }
            }
        }
    }

    val pickMultipleMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxSelectableMedia)
    ) { uris ->
        if (uris.isNotEmpty()) {
            when (postType) {
                PostType.POST -> {
                    viewModel.updatePostUris(uris, maxLimit = MAX_POST_MEDIA)
                }

                PostType.STATUS -> {
                    val items = uris.map { uri -> Status(media = uri, caption = "") }
                    viewModel.addStatusItems(items, maxLimit = MAX_STATUS_MEDIA)
                }
            }
        }
    }

    BackHandler {
        when (state) {
            AddPostState.AccountRequired -> navigateUp()
            AddPostState.CaptionPost -> { viewModel.updateUIState(AddPostState.Nothing) }
            AddPostState.Nothing -> navigateUp()
            is AddPostState.PlayVideo -> { viewModel.updateUIState(AddPostState.Nothing) }
            AddPostState.PreviewPost -> { viewModel.updateUIState(AddPostState.CaptionPost) }
            AddPostState.ShowCamera -> { viewModel.updateUIState(AddPostState.Nothing) }
        }
    }

    when (state) {
        is AddPostState.AccountRequired -> {
            AccountRequiredDialog(
                showDialog = true,
                onDismissRequest = { navigateUp() },
                onConfirmButtonClick = {
                    navController.navigate(CreateBusinessRoute)
                }
            )
        }

        is AddPostState.CaptionPost -> {
            CaptionPost(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                description = postDataForm.description,
                category = postDataForm.category,
                location = postDataForm.location,
                price = postDataForm.price,
                tags = postDataForm.tags,
                onCategoryChange = {viewModel.updateCategory(it)},
                onLocationChange = { },
                onPriceChange = { viewModel.updatePrice(it) },
                onDescriptionChange = { viewModel.updateDescription(it) },
                onTagsChange = { viewModel.updateTags(it) },
                onPreviewClick = { viewModel.updateUIState(AddPostState.PreviewPost) },
                onPostClick = { viewModel.uploadPost() },
                onBackClick = { viewModel.updateUIState( AddPostState.Nothing) }
            )
        }

        is AddPostState.Nothing -> {
            CreatePostScreen(
                user = viewModel.loggedInUser,
                onProceedToCaption = { viewModel.updateUIState(AddPostState.CaptionPost) },
                onShareStatus = { viewModel.saveStatusesLocally() },
                selectedMedia = if (postType == PostType.POST) postDataForm.uris else statusDataForm.data.map { it.media },
                audience = postDataForm.audience,
                allowReposting = postDataForm.allowReposting,
                commentsDisabled = postDataForm.commentsDisabled,
                saveToArchive = postDataForm.saveToArchive,
                postType = postType,
                onAllowRepostingChanged = { viewModel.toggleAllowReposting() },
                onCommentsDisableChanged = { viewModel.toggleCommentsDisabled() },
                onPostTypeChange = { viewModel.updatePostType(it) },
                statusItems = statusDataForm.data,
                onCaptionChange = { index, newCaption ->
                    viewModel.updateStatusCaption(index, newCaption)
                },
                onRemovePostItem = { viewModel.removePostMedia(it) },
                onRemoveStatusItem = { index -> viewModel.removeStatusItem(index) },
                onMoveStatusItem = { from, to -> viewModel.reorderStatusItems(from, to) },
                onAudienceChange = { viewModel.updateAudience(it) },
                onOpenCamera = {
                    if (when(postType) {
                            PostType.POST -> postDataForm.uris.size >= MAX_POST_MEDIA
                            PostType.STATUS -> statusDataForm.data.size >= MAX_STATUS_MEDIA
                        }) {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                "Only ${if (postType == PostType.POST) MAX_POST_MEDIA else MAX_STATUS_MEDIA} items allowed"
                            )
                        }
                    } else {
                        viewModel.updateUIState(AddPostState.ShowCamera)
                    }
                },
                onOpenGallery = {
                    when(postType) {
                        PostType.POST -> {
                            if (postDataForm.uris.size >= MAX_POST_MEDIA) {
                                scope.launch {
                                    snackBarHostState.showSnackbar("Only $MAX_POST_MEDIA media items can be added to a post.")
                                }
                            } else {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                            }
                        }
                        PostType.STATUS -> {
                            if (statusDataForm.data.size >= MAX_STATUS_MEDIA) {
                                scope.launch {
                                    snackBarHostState.showSnackbar("Only $MAX_STATUS_MEDIA items allowed for status.")
                                }
                            } else {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))

                            }
                        }
                    }
                },
                onOpenMultiple = {
                    when(postType) {
                        PostType.POST -> {
                            if (postDataForm.uris.size >= MAX_POST_MEDIA) {
                                scope.launch {
                                    snackBarHostState.showSnackbar("Only $MAX_POST_MEDIA media items can be added to a post.")
                                }
                            } else {
                                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                            }
                        }
                        PostType.STATUS -> {
                            if (statusDataForm.data.size >= MAX_STATUS_MEDIA) {
                                scope.launch {
                                    snackBarHostState.showSnackbar("Only $MAX_STATUS_MEDIA items allowed for status.")
                                }
                            } else {
                                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                            }
                        }
                    }
                },
                onVideoPlayClicked = { uri ->
                    viewModel.playVideo(uri)
                },
                snackBarHostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        AddPostState.PreviewPost -> {
            PostPreview(
                modifier = Modifier.fillMaxSize(),
                mediaUris = postDataForm.uris,
                caption = postDataForm.description,
                category = postDataForm.category,
                location = postDataForm.location,
                price = postDataForm.price,
                tags = postDataForm.tags,
                onBackClick = {
                    viewModel.updateUIState(AddPostState.CaptionPost)
                },
                onPostClick = viewModel::uploadPost,
            )
        }

        AddPostState.ShowCamera -> {
            CameraScreen(
                onPhotoTaken = { uri ->
                    capturedImageUri = uri
                    viewModel.updateUIState(AddPostState.Nothing)
                },
                onBack = { viewModel.updateUIState(AddPostState.Nothing) },
                onError = { error ->
                    scope.launch {
                        snackBarHostState.showSnackbar(error)
                    }
                    viewModel.updateUIState(AddPostState.Nothing)
                }
            )
        }

        is AddPostState.PlayVideo -> {
            val uri = (state as? AddPostState.PlayVideo)?.uri
            uri?.let {
                FullScreenVideoPlayer(
                    uri = it,
                    onBack = { viewModel.stopVideo() }
                )
            } ?: run {
                viewModel.stopVideo()
                Toast.makeText(navController.context, "Error Playing video", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostScreen(
    selectedMedia: List<Uri?>,
    statusItems: List<Status>,
    audience: Audience,
    allowReposting: Boolean,
    commentsDisabled: Boolean,
    saveToArchive: Boolean,
    postType: PostType,
    snackBarHostState: SnackbarHostState,
    onAllowRepostingChanged: () -> Unit,
    onCommentsDisableChanged: () -> Unit,
    onPostTypeChange: (PostType) -> Unit,
    onRemovePostItem: (Int) -> Unit,
    onCaptionChange: (Int, String) -> Unit,
    onRemoveStatusItem: (index: Int) -> Unit,
    onMoveStatusItem: (from: Int, to: Int) -> Unit,
    onAudienceChange: (Audience) -> Unit,
    onShareStatus: () -> Unit,
    onProceedToCaption: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenMultiple: () -> Unit,
    onVideoPlayClicked: (Uri) -> Unit,
    user: LocalUser?,
    modifier: Modifier = Modifier
) {
    var showAdvancedOptions by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            CreatePostTopBar(
                postType = postType,
                scrollBehavior = scrollBehavior,
                user = user
            )
        },
        bottomBar = {
            CreatePostBottomBar(
               postType =  postType,
               onPostTypeChange =  onPostTypeChange,
               onOpenCamera = onOpenCamera,
               onOpenGallery = onOpenGallery,
               onOpenMultiple = onOpenMultiple
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),

            verticalArrangement = Arrangement.Bottom
        ) {
           // Media Preview
            item {
                MediaPreviewSection(
                    postMediaItems = selectedMedia,
                    statusItems = statusItems,
                    postType = postType,
                    onCaptionChange = onCaptionChange,
                    onRemoveStatusItem = onRemoveStatusItem,
                    onMoveStatusItem = onMoveStatusItem,
                    onOpenCamera = onOpenCamera,
                    onOpenGallery = onOpenGallery,
                    onOpenMultiple = onOpenMultiple,
                    onRemovePostItem = onRemovePostItem,
                    onVideoPlayClicked = onVideoPlayClicked,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
            item {
                // Bottom buttons section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                when (postType) {
                    PostType.POST -> {
                        // Audience Selector
                        AudiencePostSelector(
                            audience = audience,
                            onAudienceChange = onAudienceChange,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Advanced Options
                        AdvancedOptionsSection(
                            isExpanded = showAdvancedOptions,
                            onExpandChange = { showAdvancedOptions = it },
                            postType = postType,
                            allowReposting = allowReposting,
                            commentsDisabled = commentsDisabled,
                            onAllowRepostingChanged = onAllowRepostingChanged,
                            onCommentsDisableChanged = onCommentsDisableChanged,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Button(
                            onClick = onProceedToCaption,
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            enabled = selectedMedia.isNotEmpty(),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Next -> Post")
                        }
                    }

                    PostType.STATUS -> {
                        Button(
                            onClick = onShareStatus,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            contentPadding = PaddingValues(horizontal = 32.dp),
                            enabled = statusItems.isNotEmpty()
                        ) {
                            Text("Post Status")
                        }
                    }
                }
                }

            }

        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostTopBar(
    postType: PostType,
    user: LocalUser?,
    scrollBehavior: TopAppBarScrollBehavior
) {
    CenterAlignedTopAppBar(
        title = {
            Text("CREATE ${postType.name}", style = MaterialTheme.typography.titleLarge)
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = ZonerInfo, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Use this if you have a profile picture
                user?.imgUrl?.let {
                    ZonerAsyncImage(
                        imageUrl = it,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    user?.name?.first()?.uppercase()?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = Color.White,
                            minLines = 1,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}


@Composable
private fun CreatePostBottomBar(
    postType: PostType,
    onPostTypeChange: (PostType) -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenMultiple: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PostTypeSelector(
            selectedType = postType,
            onTypeSelected = onPostTypeChange
        )

        ZonerSpacer(16.dp)

        Row {
            IconButton(onClick = onOpenCamera) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
            }
            IconButton(onClick = onOpenGallery) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Gallery")
            }
            IconButton(onClick = onOpenMultiple) {
                Icon(Icons.Default.Collections, contentDescription = "Multiple")
            }
        }
    }
}


@Composable
private fun PostTypeSelector(
    selectedType: PostType,
    onTypeSelected: (PostType) -> Unit
) {
    val options = listOf(PostType.POST, PostType.STATUS)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                RoundedCornerShape(8.dp)
            )
    ) {
        options.forEach { type ->
            Text(
                text = type.name,
                modifier = Modifier
                    .background(
                        if (type == selectedType) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = if (type == selectedType) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (type == selectedType) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}


@Composable
private fun MediaPreviewSection(
    postMediaItems: List<Uri?>,
    statusItems: List<Status>,
    postType: PostType,
    onRemovePostItem: (Int) -> Unit,
    onCaptionChange: (Int, String) -> Unit,
    onRemoveStatusItem: (index: Int) -> Unit,
    onMoveStatusItem: (from: Int, to: Int) -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenMultiple: () -> Unit,
    onVideoPlayClicked: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
    ) {
        when (postType) {
            PostType.POST -> {
                if (postMediaItems.isEmpty()) {
                    EmptyMediaPlaceholder(
                        onOpenCamera = onOpenCamera,
                        onOpenGallery = onOpenGallery,
                        onOpenMultiple = onOpenMultiple,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    )
                } else {
                    if (postMediaItems.size == 1) {
                        postMediaItems.first()?.let { SinglePostMediaView(uri = it, onRemoveItem = onRemovePostItem, context = context, onVideoPlayClicked = onVideoPlayClicked ) }
                    } else {
                        MultiplePostMediaView(
                            mediaUris = postMediaItems,
                            context = context,
                            onRemoveItem = onRemovePostItem,
                            onVideoPlayClicked = onVideoPlayClicked
                        )
                    }
                }
            }
            PostType.STATUS -> {
                if (postMediaItems.isEmpty()) {
                    EmptyMediaPlaceholder(
                        onOpenCamera = onOpenCamera,
                        onOpenGallery = onOpenGallery,
                        onOpenMultiple = onOpenMultiple,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    )
                } else {
                    StatusMediaCarousel(
                        items = statusItems,
                        onCaptionChange = onCaptionChange,
                        onRemoveItem = onRemoveStatusItem,
                        onMoveItem = onMoveStatusItem,
                        onVideoPlayClicked = onVideoPlayClicked
                    )
                }
            }
        }
    }
}


@Composable
private fun EmptyMediaPlaceholder(
    modifier: Modifier = Modifier,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenMultiple: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val brushColors = listOf(ZonerInfo, ZonerInfo.copy(alpha = 0.2f))
        val emptyMediaItems = listOf(
            EmptyMedia(1, Icons.Default.CameraAlt, "Take a photo"),
            EmptyMedia(2, Icons.Default.AddPhotoAlternate, "Add a single item"),
            EmptyMedia(3, Icons.Default.Collections, "Add multiple items"),
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            emptyMediaItems.forEach { item ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(brushColors)
                        )
                        .border(
                            width = 1.dp,
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        .height(150.dp)
                        .width(100.dp)
                        .clickable {
                            when (item.id) {
                                1 -> {
                                    onOpenCamera()
                                }

                                2 -> {
                                    onOpenGallery()
                                }

                                3 -> {
                                    onOpenMultiple()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                )
                {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                imageVector = item.icon,
                                contentDescription = item.text,
                                colorFilter = ColorFilter.tint(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Text(
                            text = item.text,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

    }
}


@Composable
private fun SinglePostMediaView(
    uri: Uri,
    context: Context,
    onRemoveItem: (Int) -> Unit,
    onVideoPlayClicked: (Uri) -> Unit
) {
    val isVideo = remember(uri) { context.isVideoUri(uri) }
    var showControls by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        if (isVideo) {
            VideoThumbnail(
                uri = uri,
                onVideoPlayClicked = { onVideoPlayClicked(uri) }
            )
        } else {
            ZonerAsyncImage(
                imageUrl = uri,
                contentDescription = "Selected media",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            )
        }
        // Remove Button (✖️)
        IconButton(
            onClick = { onRemoveItem(0) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove media",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun MultiplePostMediaView(
    mediaUris: List<Uri?>,
    context: Context,
    onRemoveItem: (Int) -> Unit,
    onVideoPlayClicked: (Uri) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { mediaUris.size })
    var showControls by remember { mutableStateOf(false) }
    val isVideoList = remember(mediaUris) {
        mediaUris.map { uri -> uri?.let { context.isVideoUri(it) } ?: false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            mediaUris[page]?.let { uri ->
                if (isVideoList[page]) {
                    VideoThumbnail(uri = uri, onVideoPlayClicked = { onVideoPlayClicked(uri) })
                } else {
                    ZonerAsyncImage(
                        imageUrl = mediaUris[page],
                        contentDescription = "Media ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Remove Button (✖️)
        IconButton(
            onClick = { onRemoveItem(pagerState.currentPage) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove media",
                tint = Color.White,
            )
        }
        // Page indicators
        if (mediaUris.size > 1) {
            PageIndicators(
                count = mediaUris.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

    }
}

@Composable
private fun PageIndicators(
    count: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    totalWidth: Dp = 200.dp, // Max total width of all indicators
    height: Dp = 8.dp,
    spacing: Dp = 6.dp
) {
    val indicatorWidth = remember(count, spacing) {
        val totalSpacing = spacing * (count - 1)
        val availableWidth = totalWidth - totalSpacing
        (availableWidth / count).coerceAtLeast(8.dp)
    }

    Row(
        modifier = modifier.width(totalWidth),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isActive = index == currentPage

            val animatedWidth by animateDpAsState(
                targetValue = if (isActive) indicatorWidth * 1.6f else indicatorWidth,
                label = "IndicatorWidth"
            )

            val animatedColor by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                label = "IndicatorColor"
            )

            Box(
                modifier = Modifier
                    .height(height)
                    .width(animatedWidth)
                    .clip(RoundedCornerShape(50))
                    .background(animatedColor)
            )
        }
    }
}



@Composable
private fun AudiencePostSelector(
    audience: Audience,
    onAudienceChange: (Audience) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = "Audience",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Audience:",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        DropdownMenuButton(
            selectedOption = audience.displayName,
            options = Audience.values().map { it.displayName },
            onOptionSelected = { index ->
                onAudienceChange(Audience.values()[index])
            }
        )
    }
}


@Composable
private fun AdvancedOptionsSection(
    isExpanded: Boolean,
    postType: PostType,
    allowReposting: Boolean,
    commentsDisabled: Boolean,
    onAllowRepostingChanged: () -> Unit,
    onCommentsDisableChanged: () -> Unit,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandChange(!isExpanded) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Advanced options",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SwitchWithText(
                    text = "Allow Resharing",
                    checked = allowReposting,
                    onCheckedChange = { onAllowRepostingChanged() }
                )

                SwitchWithText(
                    text = "Turn Off Comments",
                    checked = commentsDisabled,
                    onCheckedChange = { onCommentsDisableChanged() }
                )
                if (postType == PostType.STATUS) {
                    SwitchWithText(
                        text = "Add to Story Archive",
                        checked = true,
                        onCheckedChange = { /* Handle change */ }
                    )
                }

            }
        }
    }
}


@Composable
private fun SwitchWithText(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f), // text takes up remaining space
            style = MaterialTheme.typography.bodyMedium
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.9f) // optional: slightly reduce switch size for aesthetics
        )
    }
}


data class EmptyMedia(
    val id: Int,
    val icon: ImageVector,
    val text: String
)