package com.example.pitapp.ui.features.classes.components

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.datasource.fetchHtml
import com.example.pitapp.ui.features.classes.model.ScannedData
import com.example.pitapp.ui.shared.formatting.formatTitleCase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.IOException

@Composable
fun QRScanner(
    context: Context,
    coroutineScope: CoroutineScope,
    onScanStart: () -> Unit,
    onScanComplete: (ScannedData) -> Unit,
    onError: (String) -> Unit
) {

    val launcher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents == null) {
            onError(context.getString(R.string.qr_cancelled))
        } else {
            val url = result.contents
            onScanStart()
            coroutineScope.launch {
                try {
                    val html = fetchHtml(context = context, url = url)
                    val document = Jsoup.parse(html)

                    @Suppress("SpellCheckingInspection")
                    val name = document.selectFirst("div.nombre")?.text()?.trim() ?: ""

                    @Suppress("SpellCheckingInspection")
                    val studentId = document.selectFirst("div.boleta")?.text()?.trim() ?: ""

                    @Suppress("SpellCheckingInspection")
                    val academicProgram = document.selectFirst("div.carrera")?.text()?.trim() ?: ""

                    if (name.isBlank() || studentId.isBlank() || academicProgram.isBlank()) {
                        onError(context.getString(R.string.qr_extraction_error))
                    } else {
                        val scannedData = ScannedData(
                            formatTitleCase(name),
                            studentId,
                            formatTitleCase(academicProgram)
                        )
                        onScanComplete(scannedData)
                    }
                } catch (e: Exception) {

                    val errorMessage = if (e is IOException) {
                        context.getString(R.string.network_error)
                    } else {
                        e.localizedMessage ?: context.getString(R.string.qr_processing_error)
                    }
                    onError(errorMessage)

                }
            }

        }
    }
    IconButton(
        modifier = Modifier
            .size(150.dp),
        onClick = {
            val options = ScanOptions().apply {
                setPrompt(context.getString(R.string.qr_prompt))
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setBeepEnabled(true)
                setOrientationLocked(true)
            }
            launcher.launch(options)
        }
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}