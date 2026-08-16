package com.zoner.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zoner.data.utils.DeviceUtil
import com.zoner.domain.model.DevicePlatform
import com.zoner.domain.model.LocalUser
import com.zoner.domain.model.RegistrationStage
import com.zoner.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ZonerSession(
    private val dataStore: DataStore<Preferences>,
    private val deviceUtil: DeviceUtil
) {

    object SessionKeys {

        val tokenKey = stringPreferencesKey("token")
        val tokenTimestampKey = longPreferencesKey("token_timestamp")
        val userIdKey = stringPreferencesKey("user_id")
        val fullNameKey = stringPreferencesKey("first_name")
        val emailKey = stringPreferencesKey("email")
        val phoneNumber = stringPreferencesKey("phone_number")
        val profileImageUrl = stringPreferencesKey("profile_image_url")
        val rememberMeKey = booleanPreferencesKey("remember_me")

        val usernameKey = stringPreferencesKey("user_name")
        val userRoleKey = stringPreferencesKey("user_role")
        val registrationStageKey = stringPreferencesKey("registration_stage")
        // Business Profile Keys
        val businessNameKey = stringPreferencesKey("business_name")
        val businessLogoKey = stringPreferencesKey("business_logo")
        val businessVerifiedKey = booleanPreferencesKey("is_verified")

        val all = listOf(
            tokenKey, userIdKey, fullNameKey, phoneNumber, emailKey, tokenTimestampKey,
            profileImageUrl, rememberMeKey, userRoleKey, registrationStageKey, usernameKey,
            // Business Profile Keys
            businessNameKey, businessLogoKey, businessVerifiedKey
        )

    }

    private val darkThemeKey = booleanPreferencesKey("dark_theme_enabled")
    private val onboardingCompleted = booleanPreferencesKey("onboarding_completed")

    //Save Onboarding Status
    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[onboardingCompleted] = true }
    }

    suspend fun onboardingCompleted() : Boolean {
        return dataStore.data.map { prefs -> prefs[onboardingCompleted] ?: false }.first()
    }

    // Save and retrieve dark theme preference
    suspend fun saveDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[darkThemeKey] = enabled
        }

    }

    suspend fun getDarkThemeEnabled(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[darkThemeKey] ?: false
        }.first()
    }

    // Save and retrieve session data
    suspend fun saveUserSession(
        token: String? = null,
        userId: String? = null,
        name: String? = null,
        email: String? = null,
        username: String? = null,
        phoneNumber: String? = null,
        imgUrl: String? = null,
        role: UserRole? = null,
        stage: RegistrationStage? = null,
        businessName: String? = null,
        businessLogo: String? = null,
        isBusinessVerified: Boolean? = null,
        rememberMe: Boolean = true
    ) {
        dataStore.edit { prefs ->
            token?.let {
                prefs[SessionKeys.tokenKey] = it
                prefs[SessionKeys.tokenTimestampKey] = System.currentTimeMillis() // Save current time when token is stored
            }
            userId?.let { prefs[SessionKeys.userIdKey] = it }
            name?.let { prefs[SessionKeys.fullNameKey] = it }
            username?.let { prefs[SessionKeys.usernameKey] = it }
            email?.let { prefs[SessionKeys.emailKey] = it }
            imgUrl?.let { prefs[SessionKeys.profileImageUrl] = it }
            phoneNumber?.let { prefs[SessionKeys.phoneNumber] = it }
            role?.let{prefs[SessionKeys.userRoleKey] = it.name }
            stage?.let {  prefs[SessionKeys.registrationStageKey] = it.name }
            rememberMe.let { prefs[SessionKeys.rememberMeKey] = it }
            businessName?.let { prefs[SessionKeys.businessNameKey] = it }
            businessLogo?.let { prefs[SessionKeys.businessLogoKey] = it }
            isBusinessVerified?.let { prefs[SessionKeys.businessVerifiedKey] = it }
        }
    }

    suspend fun getUserId() : String? {
        return dataStore.data.map { preferences ->
            preferences[SessionKeys.userIdKey]
        }.firstOrNull()
    }

    suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            val rememberMe = preferences[SessionKeys.rememberMeKey]
            val token = preferences[SessionKeys.tokenKey]
            val timestamp = preferences[SessionKeys.tokenTimestampKey] ?: 0L
            val currentTime = System.currentTimeMillis()

            // Return token only if it exists AND is not expired
            if (rememberMe == true && token != null && (currentTime - timestamp) < (9 * 24 * 60 * 60 * 1000)) {
                token
            } else {
                null
            }

        }.firstOrNull()
    }

    //Token for Auth Interceptor
    suspend fun getValidToken() : String? {
        return dataStore.data.map { value: Preferences ->
            val token = value[SessionKeys.tokenKey]
            token
        }.firstOrNull()
    }

    // Get registration stage
    suspend fun getRegistrationStage(): RegistrationStage? {
        return dataStore.data.map { prefs ->
            prefs[SessionKeys.registrationStageKey]?.let { RegistrationStage.valueOf(it) }
        }.firstOrNull()
    }

    // Get User
     fun getUser(): Flow<LocalUser?> {
        return dataStore.data.map { prefs ->
            val userId = prefs[SessionKeys.userIdKey]
            val name = prefs[SessionKeys.fullNameKey]
            val username = prefs[SessionKeys.usernameKey]
            val email = prefs[SessionKeys.emailKey]
            val phoneNumber = prefs[SessionKeys.phoneNumber]
            val imgUrl = prefs[SessionKeys.profileImageUrl]
            val role = prefs[SessionKeys.userRoleKey]?.let { UserRole.valueOf(it) }
            val stage = prefs[SessionKeys.registrationStageKey]?.let { RegistrationStage.valueOf(it) }
            val isBusiness = when (role) {
                UserRole.USER -> false
                UserRole.BUSINESS -> true
                else -> false
            }
            val businessName = prefs[SessionKeys.businessNameKey]
            val businessLogo = prefs[SessionKeys.businessLogoKey]
            val isVerified = prefs[SessionKeys.businessVerifiedKey]

            if (userId != null && name != null && username != null && email != null) {
                LocalUser(
                    id = userId,
                    name = name,
                    username = username,
                    email = email,
                    phoneNumber = phoneNumber,
                    imgUrl = imgUrl,
                    isBusiness = isBusiness,
                    stage = stage,
                    businessName = businessName,
                    businessLogo = businessLogo,
                    isBusinessVerified = isVerified
                )
            } else {
                null
            }
        }

    }


    // Clear all session data
    suspend fun clearSession() {
        dataStore.edit { prefs ->
            SessionKeys.all.forEach { prefs.remove(it) }
            // darkThemeKey is preserved
            // Onboarding is preserved
        }
    }

    fun getDeviceId(): String = deviceUtil.getDeviceId()

    fun getDeviceName(): String = deviceUtil.getDeviceName()

    fun getDevicePlatform(): DevicePlatform = DevicePlatform.ANDROID
}