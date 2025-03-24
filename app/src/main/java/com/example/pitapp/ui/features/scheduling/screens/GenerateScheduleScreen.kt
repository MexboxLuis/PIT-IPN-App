package com.example.pitapp.ui.features.scheduling.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
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
fun GenerateScheduleScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val tutorEmail = authManager.getUserEmail() ?: ""

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val startYearState = remember { mutableStateOf(currentYear.toString()) }
    val startYearError = remember { mutableStateOf(false) }

    val endYearState = remember { mutableStateOf(currentYear.toString()) }
    val endYearError = remember { mutableStateOf(false) }

    val startMonthState = remember { mutableStateOf("") }
    val startMonthError = remember { mutableStateOf(false) }

    val endMonthState = remember { mutableStateOf("") }
    val endMonthError = remember { mutableStateOf(false) }

    val subjectState = remember { mutableStateOf("") }
    val subjectError = remember { mutableStateOf(false) }

    val selectedDays = remember { mutableStateMapOf<Int, Boolean>() }
    (1..5).forEach { selectedDays.getOrPut(it) { false } }
    val sessionsState = remember { mutableStateMapOf<Int, String>() }
    val sessionErrorStates = remember { mutableStateMapOf<Int, Boolean>() }

    val message = remember { mutableStateOf("") }

    val classrooms = remember { mutableStateOf<List<Pair<Int, Classroom>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val selectedClassroom = remember { mutableStateOf<Classroom?>(null) }
    val classroomError = remember { mutableStateOf(false) } // Add error for classroom
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        fireStoreManager.getClassrooms { result ->
            result.onSuccess { list ->
                classrooms.value = list.map { it.number to it }.sortedBy { it.first }
                isLoading.value = false
            }.onFailure {
                errorMessage.value =
                    it.localizedMessage ?: context.getString(R.string.unknown_error)
                isLoading.value = false
            }
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(R.string.create_schedule)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            ClassroomDropdown(
                classrooms = classrooms.value,
                selectedClassroom = selectedClassroom.value,
                isLoading = isLoading.value,
                errorMessage = errorMessage.value,
                onClassroomSelected = {
                    selectedClassroom.value = it
                    classroomError.value = false // Reset error on selection
                },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            )
            if (classroomError.value) {
                Text(
                    text = stringResource(R.string.classroom_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = subjectState.value,
                onValueChange = {
                    subjectState.value = it
                    subjectError.value = it.isBlank() // Update error state reactively
                },
                label = { Text(text = stringResource(R.string.subject)) },
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
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
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

            Spacer(Modifier.height(8.dp))


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
                    label = { Text(stringResource(R.string.end_year)) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
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

                    if (selectedClassroom.value == null) {
                        message.value = context.getString(R.string.classroom_required)
                        isValid = false
                    } else if (subjectState.value.isBlank()) {
                        message.value = context.getString(R.string.subject_required)
                        isValid = false
                    } else if (!selectedDays.values.any { it }) {
                        message.value = context.getString(R.string.at_least_one_day)
                        isValid = false
                    }

                    for ((day, isSelected) in selectedDays) {
                        if (isSelected) {
                            val hour = sessionsState[day]?.toIntOrNull()
                            if (hour == null || hour !in 7..19) {
                                message.value = context.getString(R.string.invalid_hour_range)
                                isValid = false
                                break
                            }
                        }
                    }
                    if (!isValid) {
                        return@OutlinedButton
                    }

                    val schedule = Schedule(
                        salonId = selectedClassroom.value!!.number.toString(),
                        tutorEmail = tutorEmail,
                        subject = subjectState.value,
                        approved = false,
                        startYear = startYearState.value.toInt(),
                        startMonth = startMonthState.value.toInt(),
                        endYear = endYearState.value.toInt(),
                        endMonth = endMonthState.value.toInt(),
                        sessions = createSessions(selectedDays, sessionsState)
                    )

                    CoroutineScope(Dispatchers.Main).launch {
                        val isOverlapping = fireStoreManager.checkForEmailOverlap(schedule)
                        if (isOverlapping) {
                            message.value = context.getString(R.string.schedule_overlap_error)

                        } else {
                            fireStoreManager.createSchedule(schedule) { result ->
                                result.onSuccess {
                                    message.value =
                                        context.getString(R.string.schedule_created_successfully)

                                    subjectState.value = ""
                                    startYearState.value = currentYear.toString()
                                    endYearState.value = currentYear.toString()
                                    startMonthState.value = ""
                                    endMonthState.value = ""
                                    selectedClassroom.value = null
                                    selectedDays.keys.forEach { selectedDays[it] = false }
                                    sessionsState.clear()
                                    sessionErrorStates.clear()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.schedule_created_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()

                                }.onFailure {
                                    message.value = context.getString(
                                        R.string.scheduling_error,
                                        it.localizedMessage
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.schedule_button))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            if (message.value.isNotEmpty()) {
                Text(
                    text = message.value,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
