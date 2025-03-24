package com.example.pitapp.ui.features.scheduling.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pitapp.R

@Composable
fun StatusBadge(isApproved: Boolean) {
    val statusText =
        if (isApproved) stringResource(R.string.approved) else stringResource(R.string.pending)
    val statusColor =
        if (isApproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusText,
            color = statusColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

