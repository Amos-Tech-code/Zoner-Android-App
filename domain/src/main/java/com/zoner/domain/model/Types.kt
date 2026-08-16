package com.zoner.domain.model


enum class RegistrationStage {
    EMAIL_SUBMITTED,     // After signup, before verification
    EMAIL_VERIFIED,      // After verification, before profile
    PROFILE_COMPLETED,   // Username/profile set

    BUSINESS_ADDED,       // Business profile added
}

enum class UserRole {
    USER,               // Default (can only view posts)
    BUSINESS           // Can post business content (verified or unverified)
}


enum class DevicePlatform {
    ANDROID,
    IOS,
    WEB
}

enum class MediaType {
    IMAGE, VIDEO
}

enum class PostType {
    POST, STATUS
}

enum class SyncStatus { PENDING, SYNCED, FAILED }

enum class Audience(val displayName: String) {
    PUBLIC("Public"),
    PEOPLE_NEAR_ME("People Near me"),
    CUSTOM("Custom")
}

enum class InteractionType { VIEW, LIKE, REPLY }