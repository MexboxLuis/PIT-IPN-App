package com.example.pitapp.ui.features.classes.helpers

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File

@RequiresApi(Build.VERSION_CODES.Q)
fun saveFileToDownloads(
    context: Context,
    sourceFile: File,
    mimeType: String = "text/csv"
): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.name)
        put(MediaStore.Downloads.MIME_TYPE, mimeType)
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    uri?.let { contentUri ->
        resolver.openOutputStream(contentUri)?.use { out ->
            sourceFile.inputStream().use { input -> input.copyTo(out) }
        }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(contentUri, values, null, null)
    }
    notifyDownloadComplete(context, uri!!)
    return uri
}

