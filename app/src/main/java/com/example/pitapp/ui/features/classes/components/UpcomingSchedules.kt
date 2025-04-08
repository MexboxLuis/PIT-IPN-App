package com.example.pitapp.ui.features.classes.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.Schedule
import com.example.pitapp.ui.features.classes.helpers.formatSessionTime
import com.example.pitapp.ui.features.classes.helpers.nextSessionTime
import com.example.pitapp.ui.shared.components.EmptyState
import java.util.Calendar

@Composable
fun UpcomingSchedules(
    fireStoreManager: FireStoreManager,
    tutorEmail: String,
    currentSubject: String = ""
) {

    val context = LocalContext.current
    val upcomingSchedules =
        remember { mutableStateOf<List<Pair<Schedule, FormattedTimeInfo>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tutorEmail, currentSubject) {
        isLoading.value = true
        fireStoreManager.getUpcomingSchedules(tutorEmail) { result ->
            result.onSuccess { schedules ->
                val now = Calendar.getInstance()

                val schedulesWithTimeInfo = schedules
                    .filter { schedule ->
                        currentSubject.isBlank() || schedule.subject != currentSubject
                    }
                    .mapNotNull { schedule ->
                        nextSessionTime(schedule, now)?.let { (nextSession, sessionTime) ->
                            Triple(
                                schedule,
                                sessionTime,
                                formatSessionTime(context, nextSession, sessionTime)
                            )
                        }
                    }
                    .sortedBy { it.second.timeInMillis }
                val sortedFormattedSchedules = schedulesWithTimeInfo.map { triple ->
                    Pair(triple.first, triple.third)
                }

                upcomingSchedules.value = sortedFormattedSchedules
                isLoading.value = false
                error.value = null
            }.onFailure {
                error.value = it.localizedMessage ?: context.getString(R.string.unknown_error)
                isLoading.value = false
            }
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error.value != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error.value!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            upcomingSchedules.value.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        upcomingSchedules.value,
                        key = { _, (schedule, _) -> schedule.hashCode() }) { index, (schedule, timeInfo) ->
                        val isNextClass = index == 0

                        val scale = rememberInfiniteTransition(label = "")
                            .animateFloat(
                                initialValue = 1f,
                                targetValue = if (isNextClass) 1.02f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = ""
                            )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale.value),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = schedule.subject,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (schedule.classroomId.isNotBlank()) {
                                        Spacer(Modifier.width(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .wrapContentWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.MeetingRoom,
                                                contentDescription = null,
                                                tint = LocalContentColor.current.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = schedule.classroomId,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = LocalContentColor.current.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = timeInfo.relative,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.wrapContentWidth()
                                    ) {
                                        Text(
                                            text = timeInfo.absolute,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LocalContentColor.current.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.AccessTime,
                                            contentDescription = null,
                                            tint = LocalContentColor.current.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            currentSubject.isNotBlank() -> {

            }

            else -> {
                EmptyState(
                    icon = Icons.Default.SentimentNeutral,
                    message = stringResource(R.string.schedule_message)
                )
            }
        }
    }
}