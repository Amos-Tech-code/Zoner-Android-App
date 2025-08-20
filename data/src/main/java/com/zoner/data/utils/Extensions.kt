package com.zoner.data.utils

import android.content.Context
import android.net.Uri
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
    val fileExtension = getFileExtension(context, this)
        ?: return FileConversionResult.Error("Could not determine file type")

    val mimeType = getMimeType(fileExtension)
        ?: return FileConversionResult.Error("Unsupported image format: $fileExtension")

    return try {
        val uniqueFileName = "upload_${UUID.randomUUID()}.$fileExtension"
        val file = File(context.cacheDir, uniqueFileName)

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
        FileConversionResult.Error("File processing failed")
    }
}

fun getFileExtension(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)?.let { mimeType ->
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }
}

fun getMimeType(fileExtension: String): String? {
    return when (fileExtension.lowercase(Locale.ROOT)) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "bmp" -> "image/bmp"
        "webp" -> "image/webp"
        "heic" -> "image/heic" // iOS photos
        else -> null
    }
}
