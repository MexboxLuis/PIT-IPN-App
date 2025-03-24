package com.example.pitapp.ui.features.scheduling.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.features.scheduling.components.ClassroomDropdown
import com.example.pitapp.ui.features.scheduling.components.DaysOfWeekSelection
import com.example.pitapp.ui.features.scheduling.components.MonthDropdown
import com.example.pitapp.ui.features.scheduling.model.Schedule
import com.example.pitapp.ui.features.scheduling.utils.createSessions
import com.example.pitapp.ui.screens.Classroom
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun EditScheduleScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    scheduleId: String?
) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    val subjectState = remember { mutableStateOf("") }
    val subjectError = remember { mutableStateOf(false) }

    val startYearState = remember { mutableStateOf("") }
    val startYearError = remember { mutableStateOf(false) }

    val endYearState = remember { mutableStateOf("") }
    val endYearError = remember { mutableStateOf(false) }

    val startMonthState = remember { mutableStateOf("") }
    val startMonthError = remember { mutableStateOf(false) }
    val endMonthState = remember { mutableStateOf("") }
    val endMonthError = remember { mutableStateOf(false) }

    val selectedDays = remember { mutableStateMapOf<Int, Boolean>() }
    (1..5).forEach { selectedDays.getOrPut(it) { false } }
    val sessionsState = remember { mutableStateMapOf<Int, String>() }
    val sessionErrorStates = remember { mutableStateMapOf<Int, Boolean>() }

    val tutorEmailState = remember { mutableStateOf("") }
    val classroomState = remember { mutableStateOf<Classroom?>(null) }
    val classroomError = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val classrooms = remember { mutableStateOf<List<Pair<Int, Classroom>>>(emptyList()) }
    val isLoadingClassrooms = remember { mutableStateOf(true) }
    val errorLoadingClassrooms = remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val isLoadingSchedule = remember { mutableStateOf(true) }
    val errorLoadingSchedule = remember { mutableStateOf<String?>(null) }

    val generalMessage = remember { mutableStateOf("") }

    LaunchedEffect(scheduleId) {
        if (scheduleId != null) {
            fireStoreManager.getScheduleById(scheduleId) { result ->
                result.onSuccess { schedule ->
                    subjectState.value = schedule.subject
                    startYearState.value = schedule.startYear.toString()
                    endYearState.value = schedule.endYear.toString()
                    startMonthState.value = schedule.startMonth.toString()
                    endMonthState.value = schedule.endMonth.toString()
                    tutorEmailState.value = schedule.tutorEmail

                    schedule.sessions.forEach { session ->
                        selectedDays[session.dayOfWeek] = true
                        sessionsState[session.dayOfWeek] = session.startTime.toString()
                        sessionErrorStates[session.dayOfWeek] = false
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            fireStoreManager.getClassroomByNumber(schedule.salonId) { classroomResult ->
                                classroomResult.onSuccess { classroomState.value = it }
                                classroomResult.onFailure {
                                    classroomState.value = null
                                    errorLoadingClassrooms.value =
                                        context.getString(R.string.original_classroom_error)
                                }
                            }
                        } catch (e: Exception) {
                            errorLoadingClassrooms.value =
                                context.getString(R.string.original_classroom_error)
                        }
                    }

                    isLoadingSchedule.value = false
                }.onFailure {
                    errorLoadingSchedule.value =
                        context.getString(R.string.load_error, it.localizedMessage ?: "")
                    isLoadingSchedule.value = false
                }
            }
        } else {
            errorLoadingSchedule.value = context.getString(R.string.invalid_schedule_id)
            isLoadingSchedule.value = false
        }
    }

    LaunchedEffect(Unit) {
        fireStoreManager.getClassrooms { result ->
            result.onSuccess { list ->
                classrooms.value = list.map { it.number to it }.sortedBy { it.first }
                isLoadingClassrooms.value = false
            }.onFailure {
                errorLoadingClassrooms.value =
                    it.localizedMessage ?: context.getString(R.string.unknown_error)
                isLoadingClassrooms.value = false
            }
        }
    }
    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(R.string.edit_schedule)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            if (isLoadingSchedule.value) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else if (errorLoadingSchedule.value != null) {
                Text(errorLoadingSchedule.value!!, color = MaterialTheme.colorScheme.error)
            } else {
                ClassroomDropdown(
                    classrooms = classrooms.value,
                    selectedClassroom = classroomState.value,
                    isLoading = isLoadingClassrooms.value,
                    errorMessage = errorLoadingClassrooms.value,
                    onClassroomSelected = {
                        classroomState.value = it
                        classroomError.value = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = subjectState.value,
                    onValueChange = {
                        subjectState.value = it
                        subjectError.value = it.isBlank()
                    },
                    label = { Text(stringResource(R.string.subject)) },
                    leadingIcon = { Icon(Icons.Filled.Book, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = subjectError.value
                )
                if (subjectError.value) {
                    Text(
                        text = stringResource(R.string.subject_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthDropdown(
                        label = stringResource(R.string.start_month),
                        selectedMonth = startMonthState.value,
                        onMonthSelected = {
                            startMonthState.value = it
                            val month = it.toIntOrNull()
                            startMonthError.value = !(month != null && month in 1..12)
                        }
                    )
                    OutlinedTextField(
                        value = startYearState.value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                startYearState.value = newValue
                                val year = newValue.toIntOrNull()
                                startYearError.value = !(year != null && year >= currentYear)
                            }
                        },
                        label = { Text(stringResource(R.string.start_year)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        isError = startYearError.value
                    )
                }
                if (startMonthError.value) {
                    Text(
                        text = stringResource(R.string.invalid_months),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                if (startYearError.value) {
                    Text(
                        text = stringResource(R.string.invalid_year),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthDropdown(
                        label = stringResource(R.string.end_month),
                        selectedMonth = endMonthState.value,
                        onMonthSelected = {
                            endMonthState.value = it
                            val month = it.toIntOrNull()
                            val startMonth = startMonthState.value.toIntOrNull()

                            endMonthError.value =
                                !(month != null && month in 1..12 && (startMonth == null || month >= startMonth))
                        }
                    )
                    OutlinedTextField(
                        value = endYearState.value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                endYearState.value = newValue
                                val startYear = startYearState.value.toIntOrNull()
                                val endYear = newValue.toIntOrNull()
                                endYearError.value =
                                    !(endYear != null && endYear >= currentYear && (startYear == null || endYear >= startYear))
                            }
                        },
                        label = { Text(text = stringResource(R.string.end_year)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        isError = endYearError.value
                    )
                }

                if (endMonthError.value) {
                    Text(
                        text = stringResource(R.string.invalid_end_month),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                if (endYearError.value) {
                    Text(
                        text = stringResource(R.string.invalid_end_year),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.select_days_and_time),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                DaysOfWeekSelection(
                    selectedDays = selectedDays,
                    sessionsState = sessionsState,
                    sessionErrorStates = sessionErrorStates
                )

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {

                        var isValid = true

                        if (classroomState.value == null) {
                            generalMessage.value = context.getString(R.string.classroom_required)
                            isValid = false
                        } else if (subjectState.value.isBlank()) {
                            generalMessage.value = context.getString(R.string.subject_required)
                            isValid = false
                        } else if (!selectedDays.values.any { it }) {
                            generalMessage.value = context.getString(R.string.at_least_one_day)
                            isValid = false
                        }

                        for ((day, isSelected) in selectedDays) {
                            if (isSelected) {
                                val hour = sessionsState[day]?.toIntOrNull()
                                if (hour == null || hour !in 7..19) {
                                    generalMessage.value =
                                        context.getString(R.string.invalid_hour_range)
                                    isValid = false
                                    break
                                }
                            }
                        }

                        if (!isValid) {
                            return@OutlinedButton
                        }

                        val updatedSchedule = Schedule(
                            salonId = classroomState.value!!.number.toString(),
                            tutorEmail = tutorEmailState.value,
                            subject = subjectState.value,
                            approved = false,
                            startYear = startYearState.value.toInt(),
                            startMonth = startMonthState.value.toInt(),
                            endYear = endYearState.value.toInt(),
                            endMonth = endMonthState.value.toInt(),
                            sessions = createSessions(selectedDays, sessionsState)
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            val isOverlap =
                                fireStoreManager.checkForUpdatedOverlap(
                                    updatedSchedule,
                                    scheduleId
                                )
                            if (isOverlap) {
                                generalMessage.value =
                                    context.getString(R.string.schedule_overlap_error)
                                return@launch
                            }

                            val isEmailOverlap =
                                fireStoreManager.checkForEmailOverlap(updatedSchedule, scheduleId)
                            if (isEmailOverlap) {
                                generalMessage.value =
                                    context.getString(R.string.tutor_schedule_overlap_error)
                                return@launch
                            }

                            if (scheduleId != null) {
                                fireStoreManager.updateSchedule(
                                    scheduleId,
                                    updatedSchedule
                                ) { result ->
                                    result.onSuccess {
                                        generalMessage.value =
                                            context.getString(R.string.schedule_updated)
                                        navController.popBackStack()
                                    }
                                    result.onFailure {
                                        generalMessage.value =
                                            context.getString(
                                                R.string.update_error,
                                                it.localizedMessage ?: ""
                                            )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.update_schedule))
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.AlarmOn,
                            contentDescription = null
                        )
                    }

                }

                if (generalMessage.value.isNotEmpty()) {
                    Text(
                        text = generalMessage.value,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}