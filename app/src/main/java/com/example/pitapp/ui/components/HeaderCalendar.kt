package com.example.pitapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HeaderCalendar(
    title: String
) {
    Text(
        text = title,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium
    )
}