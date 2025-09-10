package com.zoner.android.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.ui.feature.account.otp_verify.OtpVerificationScreen
import com.zoner.android.ui.feature.account.reset_password.ResetPasswordScreen
import com.zoner.android.ui.feature.account.sign_in.SignInScreen
import com.zoner.android.ui.feature.account.sign_up.SignUpScreen
import com.zoner.android.ui.feature.add_business_profile.CreateBusinessProfileScreen
import com.zoner.android.ui.feature.account.complete_profile.CompleteProfileScreen
import com.zoner.android.ui.feature.country_selection.CountrySelectionScreen
import com.zoner.android.ui.feature.onboarding.OnboardingScreen
import com.zoner.android.ui.feature.post_details.PostDetailsScreen
import com.zoner.android.ui.feature.settings.SettingsScreen
import com.zoner.android.ui.feature.view_status.StatusViewerScreen
import com.zoner.android.ui.feature.view_status.user_status.MyStatusScreen

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
        startDestination = startDestination,
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


        composable<OTPVerificationRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<OTPVerificationRoute>()
            OtpVerificationScreen(navController, windowSizeClass, route.userId)
        }

        composable<CompleteProfileRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CompleteProfileRoute>()
            CompleteProfileScreen(navController, route.userId)
        }

        composable<ResetPasswordRoute> {
            ResetPasswordScreen(navController, windowSizeClass)
        }

        composable<CountrySelectRoute> {
            CountrySelectionScreen(navController, windowSizeClass)
        }

        // Main app container with persistent navigation
        // Only ONE main route
        composable<MainAppRoute> {
            MainScreensNavigation(
                navController = navController,
                windowSizeClass = windowSizeClass
                /** Not fully implemented window size Class**/ /** Not fully implemented **/
            )
        }

        composable<CreateBusinessRoute> {
            CreateBusinessProfileScreen(navController, windowSizeClass)
        }

        composable<SettingsRoute> {
            SettingsScreen(navController)
        }

        composable<PostDetailsRoute> {
            PostDetailsScreen(navController)
        }

        composable<UserStatusRoute> {
            MyStatusScreen(navController)
        }

        composable<StatusViewRoute> {
            StatusViewerScreen(navController)
        }

    }
}