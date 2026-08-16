package com.zoner.data.utils

import android.content.Context
import java.io.File

class ZonerFileManager(context: Context) {

    private val safeAppDirs = listOf(
        context.filesDir.canonicalPath,
        context.cacheDir.canonicalPath,
        context.externalCacheDir?.canonicalPath ?: ""
    ).filter { it.isNotEmpty() }

    fun deleteFileIfSafe(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            val canonicalPath = file.canonicalPath

            // Safety check: only delete from app directories
            if (!isPathInAppStorage(canonicalPath)) {
                //Log.w("FileManager", "Attempted to delete file outside app storage: $filePath")
                return false
            }

            // If file doesn't exist, consider it successful
            if (!file.exists()) {
                return true
            }

            val deleted = file.delete()
            if (!deleted) {
                //Log.w("FileManager", "Failed to delete file: $filePath")
            }
            deleted
        } catch (e: SecurityException) {
            //Log.e("FileManager", "Security exception deleting file: $filePath", e)
            false
        } catch (e: Exception) {
            //Log.e("FileManager", "Error deleting file: $filePath", e)
            false
        }
    }

    private fun isPathInAppStorage(canonicalPath: String): Boolean {
        return safeAppDirs.any { canonicalPath.startsWith(it) }
    }

    // Optional: Method to get all files in a directory recursively
//    fun deleteDirectoryContents(directory: File): Boolean {
//        return try {
//            if (directory.exists() && directory.isDirectory) {
//                directory.listFiles()?.forEach { file ->
//                    if (file.isDirectory) {
//                        deleteDirectoryContents(file)
//                    } else {
//                        file.delete()
//                    }
//                }
//                true
//            } else {
//                false
//            }
//        } catch (e: Exception) {
//            //Log.e("FileManager", "Error deleting directory contents: ${directory.absolutePath}", e)
//            false
//        }
//    }
}