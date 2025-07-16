package com.zoner.android

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.navigation.AppNavGraph
import com.zoner.android.navigation.NavRoutes

@Composable
fun App(
    navController: NavHostController,
    startDestination: NavRoutes,
    windowSizeClass: WindowSizeClass
) {
    AppNavGraph(
        navController = navController,
        startDestination = startDestination,
        windowSizeClass = windowSizeClass
    )

}
