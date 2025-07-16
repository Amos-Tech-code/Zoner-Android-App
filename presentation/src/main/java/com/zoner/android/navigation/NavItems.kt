package com.zoner.android.navigation

import com.zoner.android.R

sealed class NavItems(
    val route: NavRoutes,
    val icon: Int,
    val label: String
) {
    data object Home : NavItems(HomeRoute, R.drawable.ic_home, "Dashboard")

    data object Search : NavItems(SearchRoute, R.drawable.ic_search, "Search")

    data object AddPost : NavItems(AddPostRoute, R.drawable.ic_addpost, "Add Post")

    data object Notifications : NavItems(
        NotificationRoute,
        R.drawable.ic_notification, "Notifications"
    )

    data object MyProfile : NavItems(ProfileRoute, R.drawable.ic_profile, "Profile")
}