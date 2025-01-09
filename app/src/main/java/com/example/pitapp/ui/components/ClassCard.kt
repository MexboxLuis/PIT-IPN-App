package com.example.pitapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.ui.model.ClassState
import com.example.pitapp.utils.determineClassState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ClassCard(
    classItem: ClassData,
    studentsList: List<Student>,
    onClick: () -> Unit
) {
    val classState = determineClassState(classItem)
    val opacity = when (classState) {
        ClassState.UPCOMING -> 0.75f
        ClassState.IN_PROGRESS -> 1f
        ClassState.FINISHED -> 0.35f
    }

    val classStateText = when (classState) {
        ClassState.IN_PROGRESS -> stringResource(id = R.string.class_state_in_progress)
        ClassState.UPCOMING -> stringResource(id = R.string.class_state_upcoming)
        ClassState.FINISHED -> stringResource(id = R.string.class_state_finished)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .alpha(opacity)
            .heightIn(min = 154.dp, max = 154.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = classItem.topic,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (classState) {
                            ClassState.IN_PROGRESS -> Icons.Default.Person
                            ClassState.UPCOMING -> Icons.Default.CalendarMonth
                            ClassState.FINISHED -> Icons.Default.CoPresent
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (classState) {
                            ClassState.IN_PROGRESS -> "${studentsList.size}"
                            ClassState.UPCOMING -> run {
                                SimpleDateFormat(
                                    "dd/MMM",
                                    Locale.getDefault()
                                ).format(classItem.startTime.toDate())
                            }
                            ClassState.FINISHED -> "${studentsList.size}"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Class,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = classItem.tutoring,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(0.5f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (classState != ClassState.FINISHED) {
                        val text =
                            if (classState == ClassState.IN_PROGRESS) classItem.classroom else run {
                                SimpleDateFormat(
                                    "HH:mm",
                                    Locale.getDefault()
                                ).format(classItem.startTime.toDate())
                            }
                        val icon =
                            if (classState == ClassState.IN_PROGRESS) Icons.Default.Place else Icons.Default.AccessTime


                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = when (classState) {
                        ClassState.IN_PROGRESS -> Icons.Default.PlayCircle
                        ClassState.UPCOMING -> Icons.Default.Schedule
                        ClassState.FINISHED -> Icons.Default.CheckCircle
                    },
                    contentDescription = classStateText,
                    tint = when (classState) {
                        ClassState.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                        ClassState.UPCOMING -> MaterialTheme.colorScheme.primary
                        ClassState.FINISHED -> MaterialTheme.colorScheme.tertiary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = classStateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
