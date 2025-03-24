package com.example.pitapp.ui.features.scheduling.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.ui.features.scheduling.utils.dayOfWeekToString

@Composable
fun DaysOfWeekSelection(
    selectedDays: MutableMap<Int, Boolean>,
    sessionsState: MutableMap<Int, String>,
    sessionErrorStates: MutableMap<Int, Boolean>
) {
    (1..5).forEach { day ->
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Checkbox(
                    checked = selectedDays[day] ?: false,
                    onCheckedChange = { selectedDays[day] = it },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = dayOfWeekToString(day),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                AnimatedVisibility(visible = selectedDays[day] == true) {
                    Column(
                        modifier = Modifier.animateContentSize(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = sessionsState[day] ?: "",
                            onValueChange = { newValue ->
                                sessionsState[day] = newValue
                                val hour = newValue.toIntOrNull()
                                sessionErrorStates[day] = !(hour != null && hour in 7..19)
                            },
                            isError = sessionErrorStates[day] ?: false,
                            label = { Text(stringResource(R.string.hour)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier
                                .widthIn(max = 120.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (sessionErrorStates[day] == true) {
                            Text(
                                text = stringResource(R.string.invalid_hour_range),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}