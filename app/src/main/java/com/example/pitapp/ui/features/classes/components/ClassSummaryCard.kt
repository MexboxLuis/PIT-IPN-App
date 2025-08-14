package com.example.pitapp.ui.features.classes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.model.SavedClass
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ClassSummaryCard(
    userEmail: String,
    savedClass: SavedClass,
    enabled: Boolean,
    onClick: () -> Unit
) {

    val formattedDateTime = remember(savedClass.date) {
        try {
            val sdf = SimpleDateFormat("HH:mm 'hr \t' dd/MMM/yyyy", Locale.getDefault())
            sdf.format(savedClass.date.toDate())
        } catch (_: Exception) {
            savedClass.date.toDate().toString()
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = savedClass.subject,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            if (savedClass.tutorEmail != userEmail)
                DetailSummaryCardRow(
                    icon = Icons.Default.Email,
                    label = stringResource(id = R.string.tutor),
                    value = savedClass.tutorEmail
                )
            DetailSummaryCardRow(
                icon = Icons.Default.CalendarToday,
                label = stringResource(id = R.string.date),
                value = formattedDateTime
            )
            DetailSummaryCardRow(
                icon = Icons.Default.LocationOn,
                label = stringResource(id = R.string.classroom),
                value = savedClass.classroom
            )
            DetailSummaryCardRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = stringResource(id = R.string.topic),
                value = savedClass.topic
            )
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = stringResource(id = R.string.download_attendance_list),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Icon(imageVector = Icons.Default.Downloading, contentDescription = null)
            }
        }
    }
}
