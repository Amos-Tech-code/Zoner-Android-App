package com.zoner.android.ui.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zoner.android.ui.designSystem.ZonerSpacer
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = koinViewModel()
) {

    Scaffold(
        topBar = {
            SettingsTopAppBar { navController.popBackStack() }
        }
    ) { innerPadding ->
        SettingsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onLogOutClick = { }
        )

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Go back"
                )
            }
        }
    )
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    onLogOutClick: () -> Unit
) {
    LazyColumn(modifier = modifier) {
        item { SectionTitle("ACCOUNT") }
        item { SettingsItem("Edit Profile", Icons.Default.Person, onItemClick = {}) }
        item { SettingsItem("Change Password", Icons.Default.Lock, onItemClick = {}) }
        item { SettingsItem("Delete Account", Icons.Default.Delete, onItemClick = {}) }

        item { SectionTitle("LOCATION & DISCOVERY") }
        item { SettingsItem("Default Location", Icons.Default.LocationOn, onItemClick = {}) }
        item { SettingsItem("Search Radius", Icons.Default.LocationSearching, onItemClick = {}) }

        item { SectionTitle("NOTIFICATIONS") }
        item { SettingsItem("Notifications", Icons.Default.Notifications, onItemClick = {}) }

        item { SectionTitle("PRIVACY & SECURITY") }
        item { SettingsItem("Post Visibility", Icons.Default.Visibility, onItemClick = {}) }

        item { SectionTitle("HELP & SUPPORT") }
        item { SettingsItem("Report a problem", Icons.Default.Report, onItemClick = {}) }
        item { SettingsItem("FAQs", Icons.Default.QuestionAnswer, onItemClick = {}) }

        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                SignOutButton(
                    onClick = onLogOutClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.padding(end = 16.dp))
        Text(title, fontSize = 16.sp)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = "More")
    }
}

@Composable
private fun SignOutButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        ZonerSpacer(8.dp)
        Text("Logout", color = MaterialTheme.colorScheme.error)
    }
}