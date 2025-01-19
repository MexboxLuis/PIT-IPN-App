package com.example.pitapp.data

import androidx.compose.ui.graphics.vector.ImageVector


data class AdminCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

