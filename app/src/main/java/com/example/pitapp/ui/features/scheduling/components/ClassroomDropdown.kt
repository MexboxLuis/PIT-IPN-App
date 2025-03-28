package com.example.pitapp.ui.features.scheduling.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.ui.features.classrooms.screens.Classroom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomDropdown(
    classrooms: List<Pair<Int, Classroom>>,
    selectedClassroom: Classroom?,
    isLoading: Boolean,
    errorMessage: String?,
    onClassroomSelected: (Classroom) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedClassroom?.let {
                stringResource(
                    R.string.classroom_number_description,
                    it.number,
                    it.description
                )
            }
                ?: stringResource(R.string.no_classroom_selected),
            onValueChange = {},
            label = {
                Text(
                    text = stringResource(R.string.select_classroom),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = { Icon(Icons.Filled.MeetingRoom, contentDescription = null) },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            if (isLoading) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.loading)) },
                    onClick = {}
                )
            } else if (errorMessage != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.error_message, errorMessage)) },
                    onClick = {}
                )
            } else {
                classrooms.forEach { (id, classroom) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MeetingRoom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Text(
                                        text = id.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }


                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    stringResource(
                                        R.string.classroom_number_description,
                                        classroom.number,
                                        classroom.description
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        onClick = {
                            onClassroomSelected(classroom)
                            onExpandedChange(false)
                        },
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                            .background(Color.Transparent),
                    )
                }
            }
        }
    }
}