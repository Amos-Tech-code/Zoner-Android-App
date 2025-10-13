package com.zoner.android.util

import com.zoner.android.BuildConfig

// Constants
const val GoogleServerClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID
const val MAX_POST_MEDIA = 4
const val MAX_STATUS_MEDIA = 10

// Countdown for requesting new OTP (3 minutes)
const val REQUEST_NEW_OTP_COUNTDOWN: Long = 3 * 60 * 1000L

const val MAX_FILE_SIZE = 20 * 1024 * 1024 // 20MB

const val MAX_VIDEO_DURATION = 60 * 1000L // 60 seconds
