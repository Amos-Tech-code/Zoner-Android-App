package com.zoner.android.util


// Extension function to format numbers (e.g. 1500 -> 1.5K)
fun Int.formatShort(): String {
    return when {
        this >= 1_000_000 -> "%.1fM".format(this / 1_000_000f)
        this >= 1_000 -> "%.1fK".format(this / 1_000f)
        else -> toString()
    }
}



// Extension function for timestamp formatting
fun Long.toRelativeTime(): String {
    val seconds = (System.currentTimeMillis() - this) / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m"
        seconds < 86400 -> "${seconds / 3600}h"
        seconds < 2592000 -> "${seconds / 86400}d"
        else -> "${seconds / 2592000}mo"
    }
}