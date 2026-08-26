package com.electoral.personeros.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(context, uri)
            val outputFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(outputFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    name = cursor.getString(columnIndex)
                }
            }
        }
        return name
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (columnIndex != -1) {
                    size = cursor.getLong(columnIndex)
                }
            }
        }
        return size
    }

    fun fileToBase64(file: File): String? {
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun base64ToFile(context: Context, base64Str: String, fileName: String): File? {
        return try {
            val bytes = Base64.decode(base64Str, Base64.NO_WRAP)
            val file = File(context.cacheDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(bytes)
            fos.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    fun isImageFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }

    fun isPdfFile(mimeType: String?): Boolean {
        return mimeType == "application/pdf"
    }

    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, length)
            }
            inputStream.close()
            byteArrayOutputStream.toString()
        } catch (e: Exception) {
            null
        }
    }

    fun createTempFile(context: Context, prefix: String = "temp", suffix: String = ""): File {
        return File.createTempFile(prefix, suffix, context.cacheDir)
    }

    fun clearCache(context: Context) {
        try {
            context.cacheDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
        }
    }

    fun getInputStream(context: Context, uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            null
        }
    }
}
