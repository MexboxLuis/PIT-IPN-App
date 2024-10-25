package com.example.pitapp.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItem(
    val text: String,
    val icon: ImageVector,
    val action: () -> Unit
)