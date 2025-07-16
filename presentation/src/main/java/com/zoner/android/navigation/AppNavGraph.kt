package com.zoner.android.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.ui.feature.add_post.AddPostScreen
import com.zoner.android.ui.feature.country_selection.CountrySelectionScreen
import com.zoner.android.ui.feature.notifications.NotificationScreen
import com.zoner.android.ui.feature.onboarding.OnboardingScreen
import com.zoner.android.ui.feature.account.otp_verify.OtpVerificationScreen
import com.zoner.android.ui.feature.profile.ProfileScreen
import com.zoner.android.ui.feature.search.SearchScreen
import com.zoner.android.ui.feature.account.sign_in.SignInScreen
import com.zoner.android.ui.feature.account.sign_up.SignUpScreen
import com.zoner.android.ui.feature.add_business_profile.CreateBusinessProfileScreen
import com.zoner.android.ui.feature.home.HomeScreen

@Composable
fun AppNavGraph(
    startDestination: NavRoutes,
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = MainAppRoute,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {

        composable<OnboardingRoute> {
            OnboardingScreen(navController, windowSizeClass)
        }

        composable<SignInRoute> {
            SignInScreen(navController, windowSizeClass)
        }

        composable<SignUpRoute> {
            SignUpScreen(navController, windowSizeClass)
        }

        composable<CountrySelectRoute> {
            CountrySelectionScreen(navController, windowSizeClass)
        }

        composable<OTPVerificationRoute> {
            OtpVerificationScreen(navController, windowSizeClass)
        }

        // Main navigation graph
        // Main app container with persistent navigation
        // ✅ Only ONE main route
        composable<MainAppRoute> {
            MainScreensNavigation(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }

        composable<CreateBusinessRoute> {
            CreateBusinessProfileScreen(navController, windowSizeClass)
        }

    }
}