package com.zoner.domain.model


enum class MediaType {
    IMAGE, VIDEO
}

enum class UserType {
    BUSINESS, PERSONAL , UNKNOWN
}

enum class PostType {
    POST, STATUS
}

enum class Audience(val displayName: String) {
    PUBLIC("Public"),
    PEOPLE_NEAR_ME("People Near me"),
    CUSTOM("Custom")
}