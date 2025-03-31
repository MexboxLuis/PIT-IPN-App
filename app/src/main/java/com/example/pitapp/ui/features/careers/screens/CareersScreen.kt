package com.example.pitapp.ui.features.careers.screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.datasource.AuthManager
import java.net.URLDecoder
import java.net.URLEncoder


@Composable
fun CareersScreen(navController: NavHostController, authManager: AuthManager) {
    val careers = listOf(
        CareerItem(R.string.career_biotech, "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=35&nombre=Ingenier%C3%ADa-Biotecnol%C3%B3gica"),
        CareerItem(R.string.career_ai, "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=68&nombre=Ingenier%C3%ADa-en-Inteligencia-Artificial"),
        CareerItem(R.string.career_automotive, "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=23&nombre=Ingenier%C3%ADa-en-Sistemas-Automotrices"),
        CareerItem(R.string.career_transport, "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=28&nombre=Ingenier%C3%ADa-en-Transporte"),
        CareerItem(R.string.career_data_science, "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=69&nombre=Licenciatura-en-Ciencia-de-Datos"),
        CareerItem(R.string.career_industrial, "") //  Industrial, no URL provided
    )

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.careers_title)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            careers.forEach { careerItem ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            if (careerItem.url.isNotEmpty()) {
                                // URL encoding corrected (encode entire URL)
                                val encodedUrl = URLEncoder.encode(careerItem.url, "UTF-8")
                                navController.navigate("careerWebView/$encodedUrl")
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = careerItem.nameResId),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

data class CareerItem(val nameResId: Int, val url: String)

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerWebViewScreen(navController: NavHostController, encodedUrl: String) {
    // URL decoding corrected
    val url = URLDecoder.decode(encodedUrl, "UTF-8")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Career Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        content = { paddingValues ->
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        // IMPORTANT: Add a WebChromeClient to handle JavaScript alerts, etc.
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
                                        .setTitle("SSL Certificate Error")
                                        .setMessage("The website's security certificate is not trusted. Do you want to continue anyway? (Not Recommended)")
                                        .setPositiveButton("Continue (Unsafe)") { _, _ -> handler.proceed() }
                                        .setNegativeButton("Cancel") { _, _ ->
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
                                    .setTitle("Error Loading Page")
                                    .setMessage("An error occurred: $description")
                                    .setPositiveButton("OK") { _, _ -> navController.popBackStack() }
                                    .show()
                            }
                        }
                        loadUrl(url) // Load the decoded URL
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    )
}

/*
https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=35&nombre=Ingenier%C3%ADa-Biotecnol%C3%B3gica
https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=68&nombre=Ingenier%C3%ADa-en-Inteligencia-Artificial
https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=23&nombre=Ingenier%C3%ADa-en-Sistemas-Automotrices
https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=28&nombre=Ingenier%C3%ADa-en-Transporte
https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=69&nombre=Licenciatura-en-Ciencia-de-Datos
 */

