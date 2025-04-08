package com.example.pitapp.datasource

import android.content.Context
import com.example.pitapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

suspend fun fetchHtml(context: Context, url: String): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException(
            context.getString(
                R.string.qr_unexpected_code,
                response
            )
        )
        response.body?.string() ?: throw IOException(context.getString(R.string.qr_empty_body))
    }
}
