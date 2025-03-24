package com.example.pitapp.ui.features.scheduling.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.ui.features.scheduling.model.Session

@Composable
fun SessionsMiniCalendar(sessions: List<Session>) {
    Column {
        Text(
            text = stringResource(R.string.sessions),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        sessions.forEach { session ->
            SessionItem(session = session)
        }
    }
}