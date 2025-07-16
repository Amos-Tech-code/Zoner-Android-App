package com.zoner.android.navigation

import kotlinx.serialization.Serializable

interface NavRoutes

@Serializable
object OnboardingRoute : NavRoutes

@Serializable
object SignInRoute : NavRoutes

@Serializable
object SignUpRoute : NavRoutes

@Serializable
object CountrySelectRoute : NavRoutes

@Serializable
object OTPVerificationRoute : NavRoutes

@Serializable
object MainAppRoute : NavRoutes

@Serializable
object HomeRoute : NavRoutes

@Serializable
object SearchRoute : NavRoutes

@Serializable
object AddPostRoute : NavRoutes

@Serializable
object NotificationRoute : NavRoutes

@Serializable
object ProfileRoute : NavRoutes

@Serializable
object CreateBusinessRoute : NavRoutes
