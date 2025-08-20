package com.zoner.domain.model


enum class RegistrationStage {
    EMAIL_SUBMITTED,     // After signup, before verification
    EMAIL_VERIFIED,      // After verification, before profile
    PROFILE_COMPLETED   // Username/profile set
}

enum class UserRole {
    USER,               // Default (can only view posts)
    BUSINESS           // Can post business content (verified or unverified)
}

enum class MediaType {
    IMAGE, VIDEO
}

enum class PostType {
    POST, STATUS
}

enum class Audience(val displayName: String) {
    PUBLIC("Public"),
    PEOPLE_NEAR_ME("People Near me"),
    CUSTOM("Custom")
}