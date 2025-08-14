package com.example.pitapp.ui.features.classes.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.pitapp.R

fun notifyDownloadComplete(context: Context, uri: Uri) {
    val channelId = "downloads_channel"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.downloads_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.downloads_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    val fileName = getFileNameFromUri(context, uri)
        ?: context.getString(R.string.default_downloaded_file_name)
    val userFriendlyPath =
        "${Environment.DIRECTORY_DOWNLOADS}/$fileName"

    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri))
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val pi = PendingIntent.getActivity(
        context, 0, openIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.pit_logo)
        .setContentTitle(context.getString(R.string.download_complete_title))
        .setContentText(context.getString(R.string.download_complete_text, fileName))
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.download_complete_big_text, userFriendlyPath))
        )
        .setColor(ContextCompat.getColor(context, R.color.white))
        .setColorized(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pi)
        .addAction(
            R.drawable.pit_logo,
            context.getString(R.string.share_file_action),
            PendingIntent.getActivity(
                context, 1,
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = context.contentResolver.getType(uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    context.getString(R.string.share_file_action)
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    manager.notify(1001, notification.build())
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    if (uri.scheme == "content") {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) {

        } finally {
            cursor?.close()
        }
    }

    if (fileName == null) {
        val path = uri.path
        if (path != null) {
            fileName = path
            val cut = fileName.lastIndexOf('/')
            if (cut != -1) {
                fileName = fileName.substring(cut + 1)
            }
        }
    }
    return fileName
}
