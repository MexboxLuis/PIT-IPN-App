package com.example.pitapp.ui.features.scheduling.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.pitapp.ui.features.scheduling.model.Session
import com.example.pitapp.ui.features.scheduling.utils.dayOfWeekToString

@Composable
fun SessionItem(session: Session) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                RoundedCornerShape(12.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = session.dayOfWeek.toString().take(2),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${dayOfWeekToString(session.dayOfWeek).take(3)} - ${session.startTime}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
