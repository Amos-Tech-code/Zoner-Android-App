package com.zoner.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ZonerSession(private val dataStore: DataStore<Preferences>) {

    object SessionKeys {

        val tokenKey = stringPreferencesKey("token")
        val tokenTimestampKey = longPreferencesKey("token_timestamp")
        val userIdKey = stringPreferencesKey("user_id")
        val fullNameKey = stringPreferencesKey("first_name")
        val emailKey = stringPreferencesKey("email")
        val phoneNumber = stringPreferencesKey("phone_number")
        val profileImageUrl = stringPreferencesKey("profile_image_url")
        val rememberMeKey = booleanPreferencesKey("remember_me")

        val all = listOf(
            tokenKey, userIdKey, fullNameKey, phoneNumber,
            emailKey, tokenTimestampKey, profileImageUrl, rememberMeKey
        )

    }

    private val darkThemeKey = booleanPreferencesKey("dark_theme_enabled")
    private val onboardingCompleted = booleanPreferencesKey("onboarding_completed")

    fun onboardingCompletedFlow(): Flow<Boolean> {
        return dataStore.data.map { prefs -> prefs[onboardingCompleted] ?: false }
    }

    //Save Onboarding Status
    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[onboardingCompleted] = true }
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
        token: String?,
        userId: String?,
        firstName: String?,
        email: String?,
        phoneNumber: String?,
        imgUrl: String?,
        rememberMe: Boolean = true
    ) {
        dataStore.edit { prefs ->
            token?.let {
                prefs[SessionKeys.tokenKey] = it
                prefs[SessionKeys.tokenTimestampKey] = System.currentTimeMillis() // Save current time when token is stored
            }
            userId?.let { prefs[SessionKeys.userIdKey] = it }
            firstName?.let { prefs[SessionKeys.fullNameKey] = it }
            email?.let { prefs[SessionKeys.emailKey] = it }
            imgUrl?.let { prefs[SessionKeys.profileImageUrl] = it }
            phoneNumber?.let { prefs[SessionKeys.phoneNumber] = it }
            rememberMe.let { prefs[SessionKeys.rememberMeKey] = it }
        }
    }

    // Exposing flows to observe changes reactively
    val firstNameFlow: Flow<String?> = dataStore.data.map { it[SessionKeys.fullNameKey] }
    val emailFlow: Flow<String?> = dataStore.data.map { it[SessionKeys.emailKey] }
    val phoneNumber: Flow<String?> = dataStore.data.map { it[SessionKeys.phoneNumber] }
    val profileImgUrl : Flow<String?> = dataStore.data.map { it[SessionKeys.profileImageUrl] }
    val isDarkThemeEnabledFlow: Flow<Boolean> = dataStore.data.map { it[darkThemeKey] ?: false }

    suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            val rememberMe = preferences[SessionKeys.rememberMeKey]
            val token = preferences[SessionKeys.tokenKey]
            val timestamp = preferences[SessionKeys.tokenTimestampKey] ?: 0L
            val currentTime = System.currentTimeMillis()

            // Return token only if it exists AND is not expired
            if (rememberMe == true && token != null && (currentTime - timestamp) < (6 * 24 * 60 * 60 * 1000)) {
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

    // Clear all session data
    suspend fun clearSession() {
        dataStore.edit { prefs ->
            SessionKeys.all.forEach { prefs.remove(it) }
            // darkThemeKey is preserved
        }
    }
}