package com.example.pitapp.ui.features.classes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pitapp.R

@Composable
fun StudentStatusBadge(isRegular: Boolean) {
    Text(
        text = if (isRegular) stringResource(R.string.student_regular) else stringResource(R.string.student_irregular),
        style = MaterialTheme.typography.labelSmall,
        color = if (isRegular) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier
            .background(
                color = if (isRegular) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}