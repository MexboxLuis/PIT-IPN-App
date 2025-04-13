package com.example.pitapp.ui.features.classes.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.Timelapse
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.NonWorkingDay
import com.example.pitapp.model.Period
import com.example.pitapp.model.isDateInPeriod
import com.example.pitapp.model.isDateNonWorking
import com.example.pitapp.ui.features.classes.helpers.findLastTheoreticalSession
import com.example.pitapp.ui.features.classes.helpers.formatSessionTime
import com.example.pitapp.ui.features.classes.helpers.nextSessionTime
import com.example.pitapp.ui.shared.components.EmptyState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingSchedules(
    fireStoreManager: FireStoreManager,
    tutorEmail: String,
    currentSubject: String = "",
    nonWorkingDays: List<NonWorkingDay>,
    periods: List<Period>
) {

    val context = LocalContext.current
    val upcomingSchedules = remember { mutableStateOf<List<UpcomingClassDisplayInfo>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tutorEmail, currentSubject, nonWorkingDays, periods) {
        isLoading.value = true
        error.value = null

        scope.launch {
            fireStoreManager.getCurrentSchedules(tutorEmail) { result ->
                result.onSuccess { schedules ->
                    val now = Calendar.getInstance()

                    val lastTheoreticalSessionMap = schedules.associateWith { schedule ->
                        findLastTheoreticalSession(schedule)
                    }

                    val upcomingInfos = schedules
                        .filter { it.approved && (currentSubject.isBlank() || it.subject != currentSubject) }
                        .mapNotNull { schedule ->
                            nextSessionTime(schedule, now)?.let { (session, sessionTime) ->

                                val lastTheoreticalPair = lastTheoreticalSessionMap[schedule]

                                val isTheActualLastTheoretical = lastTheoreticalPair != null &&
                                        sessionTime == lastTheoreticalPair.second

                                val sessionDate =
                                    sessionTime.toInstant().atZone(ZoneId.systemDefault())
                                        .toLocalDate()

                                val type = when {
                                    isDateNonWorking(
                                        sessionDate,
                                        nonWorkingDays
                                    ) -> InvalidityType.NON_WORKING

                                    isDateInPeriod(sessionDate, periods) -> InvalidityType.PERIOD
                                    else -> InvalidityType.NONE
                                }

                                val isInvalidDate = type != InvalidityType.NONE

                                val shouldDisplayLastTag =
                                    isTheActualLastTheoretical && !isInvalidDate

                                val formatted = formatSessionTime(context, session, sessionTime)

                                UpcomingClassDisplayInfo(
                                    schedule = schedule,
                                    sessionTime = sessionTime,
                                    formattedTime = formatted,
                                    invalidityType = type,
                                    isTheVeryLastSession = shouldDisplayLastTag
                                )
                            }
                        }
                        .sortedBy { it.sessionTime.timeInMillis }

                    upcomingSchedules.value = upcomingInfos
                }.onFailure {
                    error.value = it.localizedMessage ?: context.getString(R.string.unknown_error)
                    upcomingSchedules.value = emptyList()
                }
            }
            delay(1000)
            isLoading.value = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when {

            isLoading.value -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error.value != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error.value!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            upcomingSchedules.value.isNotEmpty() -> {
                Spacer(modifier = Modifier.height(32.dp))
                SectionClassTitle(
                    text = stringResource(R.string.upcoming_classes_title),
                    icon = Icons.Default.CalendarMonth
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = upcomingSchedules.value,
                        key = { _, item -> item.schedule.hashCode() + item.sessionTime.timeInMillis }
                    ) { index, item ->

                        val isNextClass = index == 0
                        val scale = if (isNextClass) {
                            rememberInfiniteTransition(label = "scale_anim")
                                .animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.02f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale_value"
                                ).value
                        } else {
                            1f
                        }

                        val cardBorder = when (item.invalidityType) {
                            InvalidityType.NON_WORKING -> BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.error
                            )

                            InvalidityType.PERIOD -> BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.onSurface
                            )

                            InvalidityType.NONE -> BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.surface
                            )
                        }

                        val cardContainerColor = when (item.invalidityType) {
                            InvalidityType.NON_WORKING -> MaterialTheme.colorScheme.surfaceVariant
                            InvalidityType.PERIOD -> MaterialTheme.colorScheme.surfaceDim
                            InvalidityType.NONE -> MaterialTheme.colorScheme.surface
                        }


                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .alpha(if (item.invalidityType == InvalidityType.NONE) 1f else 0.5f),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isNextClass) 8.dp else 4.dp),
                            border = cardBorder,
                            colors = CardDefaults.cardColors(
                                containerColor = cardContainerColor
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val leadingIconVector = when (item.invalidityType) {
                                        InvalidityType.NON_WORKING -> Icons.Filled.Block
                                        InvalidityType.PERIOD -> Icons.Filled.SentimentNeutral
                                        InvalidityType.NONE -> Icons.Filled.School
                                    }
                                    val leadingIconTint = when (item.invalidityType) {
                                        InvalidityType.NON_WORKING -> MaterialTheme.colorScheme.error
                                        InvalidityType.PERIOD -> MaterialTheme.colorScheme.tertiary
                                        InvalidityType.NONE -> MaterialTheme.colorScheme.primary
                                    }

                                    Icon(
                                        imageVector = leadingIconVector,
                                        contentDescription = null,
                                        tint = leadingIconTint,
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Text(
                                        text = item.schedule.subject,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (item.schedule.classroomId.isNotBlank()) {
                                        Spacer(Modifier.width(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.wrapContentWidth()
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    R.string.classroom_card_title_prefix,
                                                    item.schedule.classroomId.toInt()
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.MeetingRoom,
                                                contentDescription = null,
                                                tint = LocalContentColor.current.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (item.isTheVeryLastSession) {
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.last_class_indicator),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timelapse,
                                            contentDescription = null,
                                            tint = LocalContentColor.current.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = item.formattedTime.relative,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.wrapContentWidth()
                                    ) {
                                        Text(
                                            text = item.formattedTime.absolute,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LocalContentColor.current.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = LocalContentColor.current.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (item.invalidityType != InvalidityType.NONE) {
                                    val infoTextResId = when (item.invalidityType) {
                                        InvalidityType.NON_WORKING -> stringResource(R.string.info_non_working_day)
                                        InvalidityType.PERIOD -> stringResource(R.string.info_period)
                                        InvalidityType.NONE -> TODO()
                                    }
                                    val infoColor = when (item.invalidityType) {
                                        InvalidityType.NON_WORKING -> MaterialTheme.colorScheme.error.copy(
                                            alpha = 0.8f
                                        )

                                        InvalidityType.PERIOD -> MaterialTheme.colorScheme.tertiary.copy(
                                            alpha = 0.9f
                                        )

                                        else -> LocalContentColor.current
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = infoTextResId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = infoColor,
                                        modifier = Modifier.align(Alignment.End)
                                    )

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
                    icon = Icons.Default.EventBusy,
                    message = stringResource(R.string.schedule_message)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

