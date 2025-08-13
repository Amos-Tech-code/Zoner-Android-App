package com.zoner.data.utils

import androidx.room.TypeConverter
import com.zoner.domain.model.Audience
import com.zoner.domain.model.PostType

// Type Converters
class Converters {
    @TypeConverter
    fun fromPostType(value: PostType): String = value.name

    @TypeConverter
    fun toPostType(value: String): PostType = PostType.valueOf(value)

    @TypeConverter
    fun fromAudience(value: Audience): String = value.name

    @TypeConverter
    fun toAudience(value: String): Audience = Audience.valueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(separator = "|")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("|")
}