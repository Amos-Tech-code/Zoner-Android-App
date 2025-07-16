package com.zoner.android.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File


// Extension function for Context
fun Context.isVideoUri(uri: Uri): Boolean {
    val mimeType = contentResolver.getType(uri)
    return mimeType?.startsWith("video/") ?: uri.toString().contains("video", ignoreCase = true)
}

@SuppressLint("Range")
fun Context.getLegacyPathFromUri(uri: Uri): String? {
    return when {
        // DocumentProvider (for API 19+)
        DocumentsContract.isDocumentUri(this, uri) -> {
            when {
                // ExternalStorageProvider
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]

                    if ("primary".equals(type, ignoreCase = true)) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        // Handle non-primary volumes
                        "/storage/$type/${split[1]}"
                    }
                }
                // DownloadsProvider
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        return id.substring(4)
                    }

                    try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            id.toLong()
                        )
                        getDataColumn(contentUri, null, null)
                    } catch (e: NumberFormatException) {
                        // Handle URIs with non-long document IDs
                        uri.path?.replaceFirst("^/document/raw:", "")?.replaceFirst("^raw:", "")
                    }
                }
                // MediaProvider
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]

                    val contentUri = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> MediaStore.Files.getContentUri("external")
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    getDataColumn(contentUri, selection, selectionArgs)
                }
                else -> null
            }
        }
        // MediaStore (general)
        "content".equals(uri.scheme, ignoreCase = true) -> {
            if (isGooglePhotosUri(uri)) {
                uri.lastPathSegment // Return Google Photos URI directly
            } else {
                getDataColumn(uri, null, null)
            }
        }
        // File
        "file".equals(uri.scheme, ignoreCase = true) -> uri.path
        else -> null
    }
}

private fun Context.getDataColumn(
    uri: Uri,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    contentResolver.query(
        uri,
        arrayOf(MediaStore.MediaColumns.DATA),
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
        }
    }
    return null
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return uri.authority == "com.android.externalstorage.documents"
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return uri.authority == "com.android.providers.downloads.documents"
}

private fun isMediaDocument(uri: Uri): Boolean {
    return uri.authority == "com.android.providers.media.documents"
}

private fun isGooglePhotosUri(uri: Uri): Boolean {
    return uri.authority == "com.google.android.apps.photos.content"
}

