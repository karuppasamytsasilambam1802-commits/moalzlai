package com.example.myapplication.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

data class FileAttachment(
    val id: String,
    val fileName: String,
    val fileType: String, // "IMAGE", "DOCUMENT"
    val fileUrl: String,
    val fileSize: Long,
    val uploadedAt: Long = System.currentTimeMillis()
)

class StorageManager(private val context: Context) {

    fun uploadFile(uri: Uri, fileName: String): FileAttachment {
        val fileType = if (fileName.lowercase().endsWith(".jpg") || fileName.lowercase().endsWith(".png") || fileName.lowercase().endsWith(".jpeg")) {
            "IMAGE"
        } else {
            "DOCUMENT"
        }

        val destDir = context.getExternalFilesDir(null) ?: context.cacheDir
        val destFile = File(destDir, "upload_${System.currentTimeMillis()}_$fileName")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return FileAttachment(
            id = "att_${System.currentTimeMillis()}",
            fileName = fileName,
            fileType = fileType,
            fileUrl = destFile.absolutePath,
            fileSize = destFile.length()
        )
    }
}
