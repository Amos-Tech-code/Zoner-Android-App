package com.zoner.data.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log

object MediaUtils {

    /**
     * Gets the duration of a video file in milliseconds
     * @param context Android context
     * @param mediaUri URI of the video file
     * @return Duration in milliseconds, or 0 if unable to retrieve
     */
    fun getVideoDuration(context: Context, mediaUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, mediaUri)
            val durationStr = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e("MediaUtils", "Error getting video duration for $mediaUri", e)
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e("MediaUtils", "Error releasing MediaMetadataRetriever", e)
            }
        }
    }

    /**
     * Gets the duration of a video file from file path
     * @param filePath Path to the video file
     * @return Duration in milliseconds, or 0 if unable to retrieve
     */
    fun getVideoDuration(filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e("MediaUtils", "Error getting video duration for $filePath", e)
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e("MediaUtils", "Error releasing MediaMetadataRetriever", e)
            }
        }
    }
}