package com.example.pitapp.ui.features.careers.screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.ui.shared.components.BackScaffold
import java.net.URLDecoder

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerWebViewScreen(
    navController: NavHostController,
    authManager: AuthManager,
    encodedUrl: String
) {
    val url = URLDecoder.decode(encodedUrl, "UTF-8")

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.career_web_view_title),
        content = {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webChromeClient = object : WebChromeClient() {}

                        webViewClient = object : WebViewClient() {
                            @SuppressLint("WebViewClientOnReceivedSslError")
                            override fun onReceivedSslError(
                                view: WebView,
                                handler: SslErrorHandler,
                                error: SslError
                            ) {
                                if (error.primaryError == SslError.SSL_UNTRUSTED) {
                                    AlertDialog.Builder(context)
                                        .setTitle(context.getString(R.string.ssl_title))
                                        .setMessage(context.getString(R.string.ssl_certificate_message))
                                        .setPositiveButton(context.getString(R.string.continue_unsafe)) { _, _ -> handler.proceed() }
                                        .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
                                            handler.cancel()
                                            navController.popBackStack()
                                        }
                                        .setCancelable(false)
                                        .show()
                                } else {
                                    handler.cancel()
                                    navController.popBackStack()
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                AlertDialog.Builder(context)
                                    .setTitle(context.getString(R.string.error_loading_page_title))
                                    .setMessage(context.getString(R.string.error_loading_page_message, description ?: ""))
                                    .setPositiveButton(context.getString(R.string.ok)) { _, _ -> navController.popBackStack() }
                                    .show()
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    )
}