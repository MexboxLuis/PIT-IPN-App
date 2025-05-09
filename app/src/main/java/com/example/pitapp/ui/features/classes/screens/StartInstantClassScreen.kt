package com.example.pitapp.ui.features.classes.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.NonWorkingDay
import com.example.pitapp.model.Period
import com.example.pitapp.model.SavedClass
import com.example.pitapp.model.Schedule
import com.example.pitapp.ui.features.classes.components.InfoClassRowWithIcon
import com.example.pitapp.ui.features.classes.components.SectionClassTitle
import com.example.pitapp.ui.features.classes.components.UpcomingSchedules
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.ui.shared.formatting.formatTitleCase
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StartInstantClassScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val tutorEmail = authManager.getUserEmail() ?: ""
    val context = LocalContext.current
    val currentSchedules = remember { mutableStateOf<List<Schedule>>(emptyList()) }
    val isSchedulesLoading = remember { mutableStateOf(false) }
    val schedulesError = remember { mutableStateOf<String?>(null) }
    var isCalendarDataReady by remember { mutableStateOf(false) }

    val topic = remember { mutableStateOf("") }
    val classMessage = remember { mutableStateOf("") }
    val canStartClass = remember { mutableStateOf(false) }
    val currentSubject = remember { mutableStateOf("") }
    val currentClassroomId = remember { mutableStateOf("") }
    val classCreated = remember { mutableStateOf(false) }
    val remainingTime = remember { mutableStateOf("") }

    var currentMonth = remember { mutableStateOf(LocalDate.now()) }
    var nonWorkingDays = remember { mutableStateOf<List<NonWorkingDay>>(emptyList()) }
    var periods = remember { mutableStateOf<List<Period>>(emptyList()) }

    LaunchedEffect(Unit) {
        isSchedulesLoading.value = true
        try {
            launch {
                fireStoreManager.getCurrentSchedules(tutorEmail) { result ->
                    result.onSuccess { schedules ->
                        currentSchedules.value = schedules
                        isSchedulesLoading.value = false
                        schedulesError.value = null
                    }.onFailure {
                        schedulesError.value =
                            it.localizedMessage ?: context.getString(R.string.unknown_error)
                        isSchedulesLoading.value = false
                    }
                }
            }
        val currentYear = currentMonth.value.year.toString()
        val nextYear = (currentMonth.value.year + 1).toString()

        val nonWorkingDaysCurrent = fireStoreManager.getNonWorkingDays(currentYear)
        val nonWorkingDaysNext = fireStoreManager.getNonWorkingDays(nextYear)
        nonWorkingDays.value = nonWorkingDaysCurrent + nonWorkingDaysNext

        val periodsCurrent = fireStoreManager.getPeriods(currentYear)
        val periodsNext = fireStoreManager.getPeriods(nextYear)
        periods.value = periodsCurrent + periodsNext
            } catch (e: Exception) {
                schedulesError.value =
                    e.localizedMessage ?: context.getString(R.string.unknown_error)
                isSchedulesLoading.value = false

        } finally {
            if( isSchedulesLoading.value) isSchedulesLoading.value = false
            if (!isCalendarDataReady) isCalendarDataReady = true
        }
    }

    LaunchedEffect(currentSchedules.value, classCreated.value) {
        if (classCreated.value) return@LaunchedEffect

        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val adaptedDayOfWeek = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1

        var targetSessionStartTime: Calendar? = null
        var targetSessionEndTime: Calendar? = null
        var targetSchedule: Schedule? = null

        var bestDelta = Long.MAX_VALUE

        for (schedule in currentSchedules.value) {
            if (currentYear !in schedule.startYear..schedule.endYear ||
                !fireStoreManager.isMonthWithinRange(currentYear, currentMonth, schedule)
            ) {
                continue
            }
            for (session in schedule.sessions) {
                if (session.dayOfWeek == adaptedDayOfWeek) {
                    val sessionStartTime = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, session.startTime)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val sessionEndTime = (sessionStartTime.clone() as Calendar).apply {
                        add(Calendar.MINUTE, 60)
                    }
                    if (now.timeInMillis < sessionEndTime.timeInMillis) {
                        val delta = if (now.timeInMillis < sessionStartTime.timeInMillis)
                            sessionStartTime.timeInMillis - now.timeInMillis
                        else
                            0L

                        if (delta < bestDelta) {
                            bestDelta = delta
                            targetSessionStartTime = sessionStartTime
                            targetSessionEndTime = sessionEndTime
                            targetSchedule = schedule
                        }
                    }
                }
            }
        }

        if (targetSessionStartTime == null || targetSchedule == null) {
            canStartClass.value = false
            return@LaunchedEffect
        }

        currentSubject.value = targetSchedule.subject
        currentClassroomId.value = targetSchedule.classroomId

        if (now.timeInMillis < targetSessionStartTime.timeInMillis) {
            while (true) {
                val currentTime = Calendar.getInstance()
                val diffMillis = targetSessionStartTime.timeInMillis - currentTime.timeInMillis
                if (diffMillis <= 0) break
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60
                remainingTime.value =
                    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

                canStartClass.value = false
                delay(1000)
            }
        }

        while (true) {
            val currentTime = Calendar.getInstance()
            val diffMillis = targetSessionEndTime!!.timeInMillis - currentTime.timeInMillis
            if (diffMillis <= 0) {
                navController.navigate("startInstantClassScreen") {
                    popUpTo("startInstantClassScreen") { inclusive = true }
                    launchSingleTop = true
                }

                break
            }
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60
            remainingTime.value = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            canStartClass.value = true
            delay(1000)
        }
    }
    val today = LocalDate.now()

    fun Timestamp.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this.seconds * 1000)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    val isTodayNonWorking = nonWorkingDays.value.any { it.date.toLocalDate() == today }
    val isTodayInPeriod = periods.value.any { period ->
        val start = period.startDate.toLocalDate()
        val end = period.endDate.toLocalDate()
        !today.isBefore(start) && !today.isAfter(end)
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(R.string.create_class)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSchedulesLoading.value || !isCalendarDataReady) {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator()
                Spacer(Modifier.weight(1f))
            }
            else if (schedulesError.value != null) {
                Spacer(Modifier.weight(1f))
                Text(schedulesError.value!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.weight(1f))
            } else if (isTodayNonWorking || isTodayInPeriod) {
                val todayMessage = if (isTodayNonWorking) {
                    stringResource(R.string.today_non_working)
                } else {
                    val currentPeriod = periods.value.find { period ->
                        val start = period.startDate.toLocalDate()
                        val end   = period.endDate.toLocalDate()
                        !today.isBefore(start) && !today.isAfter(end)
                    }

                    val formattedEndDate = currentPeriod
                        ?.endDate
                        ?.toLocalDate()
                        ?.let { endLocalDate ->
                            SimpleDateFormat("dd '${context.getString(R.string.of)}' MMMM", Locale.getDefault())
                                .format(Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        }
                        ?: "—"

                    stringResource(R.string.today_in_period, formattedEndDate)
                }

                val todayIcon = if (isTodayNonWorking) {
                    Icons.Filled.EventBusy
                } else {
                    Icons.Filled.Timelapse
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NoClassInfoMessageCard(icon = todayIcon, message = todayMessage)
                }
            } else if (currentSchedules.value.isEmpty()) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    UpcomingSchedules(
                        fireStoreManager = fireStoreManager,
                        tutorEmail = tutorEmail,
                        nonWorkingDays = nonWorkingDays.value,
                        periods = periods.value
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed = interactionSource.collectIsPressedAsState()

                    val scale = animateFloatAsState(
                        targetValue = if (isPressed.value) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "buttonScaleAnim"
                    )

                    OutlinedButton(
                        onClick = { navController.navigate("generateScheduleScreen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            },
                        interactionSource = interactionSource
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(R.string.create_new_schedule))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.MoreTime,
                                contentDescription = null
                            )
                        }
                    }
                }

            } else {
                if (
                    currentSubject.value.isNotEmpty() ||
                    currentClassroomId.value.isNotEmpty() ||
                    remainingTime.value.isNotEmpty() ||
                    canStartClass.value
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            AnimatedVisibility(visible = currentSubject.value.isNotEmpty()) {
                                InfoClassRowWithIcon(
                                    icon = Icons.Default.School,
                                    text = stringResource(
                                        R.string.class_of_subject,
                                        currentSubject.value
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            AnimatedVisibility(visible = currentClassroomId.value.isNotEmpty()) {
                                InfoClassRowWithIcon(
                                    icon = Icons.Default.MeetingRoom,
                                    text = stringResource(
                                        R.string.classroom_card_title_prefix,
                                        currentClassroomId.value.toInt()
                                    )
                                )
                            }

                            if (currentSubject.value.isNotEmpty() || currentClassroomId.value.isNotEmpty()) {
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            }

                            AnimatedVisibility(visible = remainingTime.value.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val infiniteTransition =
                                        rememberInfiniteTransition(label = "timer_icon_rotation")
                                    val rotation = infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "rotation"
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(rotation.value),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    val countdownText = if (!canStartClass.value) {
                                        stringResource(R.string.next_class_in, remainingTime.value)
                                    } else {
                                        stringResource(R.string.ends_in, remainingTime.value)
                                    }

                                    Text(
                                        text = countdownText,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }

                            if (remainingTime.value.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(Modifier.padding(bottom = 16.dp))
                            }

                            if (canStartClass.value) {
                                AnimatedVisibility(visible = !classCreated.value) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        OutlinedTextField(
                                            value = topic.value,
                                            onValueChange = { topic.value = it },
                                            label = { Text(text = stringResource(R.string.class_topic_label)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Topic,
                                                    contentDescription = null
                                                )
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = ImeAction.Done
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                classCreated.value = true
                                                classMessage.value =
                                                    context.getString(R.string.class_starting)

                                                val savedClass = SavedClass(
                                                    tutorEmail = tutorEmail,
                                                    subject = currentSubject.value,
                                                    classroom = currentClassroomId.value,
                                                    topic = formatTitleCase(topic.value),
                                                    date = Timestamp.now()
                                                )

                                                fireStoreManager.startInstantClass(savedClass) { result ->
                                                    result.onSuccess { classDocumentId ->
                                                        classMessage.value =
                                                            context.getString(R.string.class_started_success)
                                                        navController.navigate("instantClassDetailsScreen/$classDocumentId") {
                                                            popUpTo("startInstantClassScreen") {
                                                                inclusive = true
                                                            }
                                                            launchSingleTop = true
                                                        }
                                                    }.onFailure {
                                                        classMessage.value =
                                                            "${it.localizedMessage}"
                                                        classCreated.value = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = topic.value.trim().isNotEmpty()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircleOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(ButtonDefaults.IconSize)
                                            )
                                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                            Text(text = stringResource(R.string.create_class))
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = classCreated.value) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 24.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 4.dp,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = classMessage.value,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (currentSchedules.value.isNotEmpty() && !classCreated.value) {
                    UpcomingSchedules(
                        fireStoreManager = fireStoreManager,
                        tutorEmail = tutorEmail,
                        currentSubject = currentSubject.value,
                        nonWorkingDays = nonWorkingDays.value,
                        periods = periods.value
                    )
                }
            }
        }
    }
}
@Composable
fun NoClassInfoMessageCard(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            val infiniteTransition = rememberInfiniteTransition(label = "timer_icon_rotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .rotate(rotation),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}