package com.zoner.android.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.ui.feature.add_post.AddPostScreen
import com.zoner.android.ui.feature.home.HomeScreen
import com.zoner.android.ui.feature.notifications.NotificationScreen
import com.zoner.android.ui.feature.profile.ProfileScreen
import com.zoner.android.ui.feature.search.SearchScreen
import com.zoner.android.util.DeviceConfiguration
import com.zoner.domain.model.PostType

@Composable
fun MainScreensNavigation(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    val navItems = listOf(
        NavItems.Home,
        NavItems.Search,
        NavItems.AddPost,
        NavItems.Notifications,
        NavItems.MyProfile
    )

    // ✅ Manage screen switching via index instead of route
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    // Variable for passing post type parameter to AddPostScreen
    var postTypeParam by remember { mutableStateOf<PostType?>(null) }
    // ✅ Back button returns to Home
    BackHandler(enabled = selectedIndex != 0) {
        selectedIndex = 0
    }

    val selectedItem = navItems[selectedIndex]

    NavigationSuiteScaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        navigationSuiteItems = {
            navItems.forEachIndexed { index, navItem ->
                item(
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    icon = {
                        Image(
                            painter = painterResource(navItem.icon),
                            contentDescription = navItem.label,
                        )
                    },
                    label = if (deviceConfiguration != DeviceConfiguration.MOBILE_PORTRAIT) {
                        { Text(navItem.label)}
                    } else null,
                    alwaysShowLabel = false,
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            navigationDrawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            navigationDrawerContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        layoutType = when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> NavigationSuiteType.NavigationBar
            DeviceConfiguration.MOBILE_LANDSCAPE,
            DeviceConfiguration.TABLET_PORTRAIT -> NavigationSuiteType.NavigationRail
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> NavigationSuiteType.NavigationDrawer
        }
    ) {
        // ✅ Show the active screen
        when (selectedItem) {
            is NavItems.Home -> HomeScreen(
                navController,
                onNavigateToAddPost = { selectedIndex = 2},
                onNavigateToAddStatus = {
                    selectedIndex = 2
                    postTypeParam = PostType.STATUS
                }
            )
            is NavItems.Search -> SearchScreen(navController)
            is NavItems.AddPost -> AddPostScreen(
                navController,
                postTypeParam = postTypeParam,
                navigateUp = { selectedIndex = 0 }
            )
            is NavItems.Notifications -> NotificationScreen(navController)
            is NavItems.MyProfile -> ProfileScreen(navController)
        }
    }
}
