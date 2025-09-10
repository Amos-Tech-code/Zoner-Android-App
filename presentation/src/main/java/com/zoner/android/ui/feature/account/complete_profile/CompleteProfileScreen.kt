package com.zoner.android.ui.feature.account.complete_profile

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.ui.designSystem.ErrorAlertDialog
import com.zoner.android.ui.designSystem.ZonerAsyncImage
import com.zoner.android.ui.navigation.CompleteProfileRoute
import com.zoner.android.ui.navigation.MainAppRoute
import com.zoner.android.ui.navigation.SignInRoute
import com.zoner.android.ui.navigation.SignUpRoute
import com.zoner.android.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    navController: NavController,
    userId: String? = null,
    viewModel: CompleteProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    var showErrorDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = !uiState.isLoading) {
        activity?.moveTaskToBack(true)

    }

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            CompleteProfileEvent.NavigateToHome -> {
                navController.navigate(MainAppRoute) {
                    popUpTo(SignInRoute) {
                        inclusive = true
                    }
                }
            }
            CompleteProfileEvent.NavigateToSignUp -> {
                navController.navigate(SignUpRoute) {
                    popUpTo<CompleteProfileRoute> { inclusive = true }
                }
            }
            is CompleteProfileEvent.ShowErrorDialog -> {
                message = event.message
                showErrorDialog = true
            }
            is CompleteProfileEvent.ShowSnackBar -> {
                scope.launch { snackBarHostState.showSnackbar(event.message) }
            }
        }
    }

    LaunchedEffect(userId) {
        viewModel.initUserId(userId)
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            Toast.makeText(navController.context, message, Toast.LENGTH_SHORT).show()
            viewModel.errorShown()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "Complete Your Profile",
                    style = MaterialTheme.typography.titleLarge,
                )}
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    )
    { innerPadding ->

        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            // Profile Picture Section
            ProfilePicturePicker(
                currentImageUri = uiState.profilePictureUri,
                onImageSelected = { uri ->
                    viewModel.onProfilePictureSelected(uri)
                },
                //modifier = Modifier
            )

            // Username Section
            UsernameFieldWithValidation(
                username = uiState.username,
                isChecking = uiState.isCheckingUsername,
                isAvailable = uiState.isUsernameAvailable,
                suggestions = uiState.usernameSuggestions,
                onUsernameChanged = { username ->
                    viewModel.onUsernameChanged(username)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Complete Button
            Button(
                onClick = {
                    viewModel.completeProfile()
                    keyboardController?.hide() },
                enabled = uiState.isUsernameValid && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Complete Setup")
                }
            }
        }
    }

    if (showErrorDialog) {
        ErrorAlertDialog(
            title = "Failed to complete profile",
            message = message ?: "Something went wrong.",
            onDismissRequest = { showErrorDialog = false },
            onConfirmButtonClick = { showErrorDialog = false }
        )
    }
}

@Composable
fun ProfilePicturePicker(
    currentImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp), // fixed size for consistency
            contentAlignment = Alignment.Center
        ) {
            if (currentImageUri != null) {
                ZonerAsyncImage(
                    imageUrl = currentImageUri,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Add profile picture",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            FloatingActionButton(
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = if (currentImageUri == null) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = "Select profile picture",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Add a profile picture (optional)",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun UsernameFieldWithValidation(
    username: String,
    isChecking: Boolean,
    isAvailable: Boolean?,
    suggestions: List<String>,
    onUsernameChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
            placeholder = { Text("Must be at least 3 characters.")},
            leadingIcon = {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Person, null)
                }
            },
            trailingIcon = {
                when {
                    isChecking -> Unit // Already showing progress as leading icon
                    isAvailable == true -> Icon(
                        Icons.Default.Check,
                        contentDescription = "Available",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    isAvailable == false -> Icon(
                        Icons.Default.Close,
                        contentDescription = "Unavailable",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = isAvailable == false,
            supportingText = {
                when {
                    isChecking -> Text("Checking availability...")
                    isAvailable == true -> Text("Username available!", color = MaterialTheme.colorScheme.primary)
                    isAvailable == false -> Text("Username taken", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Show suggestions if username is taken
        if (isAvailable == false && suggestions.isNotEmpty()) {
            Column {
                Text(
                    text = "Suggested usernames:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { onUsernameChanged(suggestion) },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        }
    }
}
