package com.zoner.data.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

fun String.toPlainRequestBody(): RequestBody =
    this.toRequestBody("text/plain".toMediaTypeOrNull())

sealed class FileConversionResult {
    data class Success(val part: MultipartBody.Part) : FileConversionResult()
    data class Error(val message: String) : FileConversionResult()
}

/**
 * Converts a content:// Uri into a MultipartBody.Part for uploading
 */

fun Uri.toMultipartBodyPart(
    context: Context,
    partName: String = "file"
): FileConversionResult {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(this)
        ?: return FileConversionResult.Error("Could not determine file type")

    // Get file name for the upload
    val fileName = getFileName(context, this) ?: "upload_${UUID.randomUUID()}"

    return try {
        val file = File(context.cacheDir, fileName)

        contentResolver.openInputStream(this)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: return FileConversionResult.Error("Failed to open file")

        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        FileConversionResult.Success(
            MultipartBody.Part.createFormData(partName, file.name, requestFile)
        )
    } catch (e: Exception) {
        FileConversionResult.Error("File processing failed: ${e.message}")
    }
}

// Helper function to get file name from Uri
fun getFileName(context: Context, uri: Uri): String? {
    return when (uri.scheme) {
        "content" -> {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else {
                    null
                }
            }
        }
        "file" -> uri.lastPathSegment
        else -> null
    }
}
