package com.zoner.android.mediaplaybackmanager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import androidx.media3.exoplayer.ExoPlayer

object MediaPlaybackManager {
    private var currentlyPlaying: ExoPlayer? = null
    private lateinit var appContext: Context
    private var audioManager: AudioManager? = null
    private var wasOtherAppPlaying = false

    fun initialize(context: Context) {
        appContext = context.applicationContext
        audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun registerNewPlayer(player: ExoPlayer) {
        wasOtherAppPlaying = audioManager?.isMusicActive ?: false
        if (requestAudioFocus()) {
            currentlyPlaying?.pause()
            currentlyPlaying = player
        }
    }

    fun unregisterPlayer(player: ExoPlayer) {
        if (currentlyPlaying == player) {
            currentlyPlaying = null
            abandonAudioFocus()
            attemptResumeOtherApp()
        }
    }

    private fun attemptResumeOtherApp() {
        if (!wasOtherAppPlaying) return

        // Method 1: Try to send media button event (requires RECEIVE_MEDIA_BUTTONS permission)
        try {
            val eventTime = SystemClock.uptimeMillis()
            val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0)
            val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0)

            audioManager?.dispatchMediaKeyEvent(downEvent)
            audioManager?.dispatchMediaKeyEvent(upEvent)
        } catch (e: SecurityException) {
            // Fallback to method 2 if we don't have permission
            adjustVolumeToTriggerResume()
        }
    }

    private fun adjustVolumeToTriggerResume() {
        // Method 2: Adjust volume slightly to trigger audio focus change
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0

        if (currentVolume < maxVolume) {
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume + 1,
                AudioManager.FLAG_SHOW_UI
            )
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume,
                AudioManager.FLAG_SHOW_UI
            )
        } else {
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume - 1,
                AudioManager.FLAG_SHOW_UI
            )
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume,
                AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> currentlyPlaying?.pause()
                        AudioManager.AUDIOFOCUS_GAIN -> currentlyPlaying?.play()
                    }
                }
                .build()
            audioManager?.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> currentlyPlaying?.pause()
                        AudioManager.AUDIOFOCUS_GAIN -> currentlyPlaying?.play()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.abandonAudioFocusRequest(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build()
            )
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }
}