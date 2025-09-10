package com.zoner.android.ui.navigation

import kotlinx.serialization.Serializable

interface NavRoutes

@Serializable
object OnboardingRoute : NavRoutes

@Serializable
object SignInRoute : NavRoutes

@Serializable
object SignUpRoute : NavRoutes

@Serializable
object ResetPasswordRoute : NavRoutes

@Serializable
object CountrySelectRoute : NavRoutes

@Serializable
data class OTPVerificationRoute(val userId: String? = null) : NavRoutes

@Serializable
data class CompleteProfileRoute(val userId: String? = null) : NavRoutes

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

@Serializable
object SettingsRoute : NavRoutes

@Serializable
object PostDetailsRoute : NavRoutes

@Serializable
object StatusViewRoute : NavRoutes

@Serializable
object UserStatusRoute : NavRoutes
