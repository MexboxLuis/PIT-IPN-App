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
import androidx.compose.runtime.derivedStateOf
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
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.Classroom
import com.example.pitapp.model.Schedule
import com.example.pitapp.ui.features.scheduling.components.ClassroomDropdown
import com.example.pitapp.ui.features.scheduling.components.DaysOfWeekSelection
import com.example.pitapp.ui.features.scheduling.components.MonthDropdown
import com.example.pitapp.ui.features.scheduling.helpers.createSessions
import com.example.pitapp.ui.shared.components.BackScaffold
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
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    var startYearState by remember { mutableStateOf(currentYear.toString()) }
    var startYearError by remember { mutableStateOf(false) }
    var startYearErrorText by remember { mutableStateOf("") }

    var endYearState by remember { mutableStateOf(currentYear.toString()) }
    var endYearError by remember { mutableStateOf(false) }
    var endYearErrorText by remember { mutableStateOf("") }

    var startMonthState by remember { mutableStateOf("") }
    var startMonthError by remember { mutableStateOf(false) }
    var startMonthErrorText by remember { mutableStateOf("") }

    var endMonthState by remember { mutableStateOf("") }
    var endMonthError by remember { mutableStateOf(false) }
    var endMonthErrorText by remember { mutableStateOf("") }

    var subjectState by remember { mutableStateOf("") }
    var subjectError by remember { mutableStateOf(false) }
    var subjectErrorText by remember { mutableStateOf("") }


    val selectedDays = remember { mutableStateMapOf<Int, Boolean>() }
    (1..5).forEach { selectedDays.getOrPut(it) { false } }
    val sessionsState = remember { mutableStateMapOf<Int, String>() }
    val sessionErrorStates = remember { mutableStateMapOf<Int, Boolean>() }
    var daysError by remember { mutableStateOf(false) }
    var daysErrorText by remember { mutableStateOf("") }

    val classrooms = remember { mutableStateOf<List<Pair<Int, Classroom>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedClassroom by remember { mutableStateOf<Classroom?>(null) }
    var classroomError by remember { mutableStateOf(false) }
    var classroomErrorText by remember { mutableStateOf("") }

    val context = LocalContext.current

    var overlapMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fireStoreManager.getClassrooms { result ->
            result.onSuccess { list ->
                classrooms.value = list.map { it.number to it }.sortedBy { it.first }
                isLoading.value = false
            }.onFailure {
                errorMessage.value = it.localizedMessage ?: context.getString(R.string.unknown_error)
                isLoading.value = false
            }
        }
    }

    fun validateForm(): Boolean {
        var isValid = true

        if (selectedClassroom == null) {
            classroomError = true
            classroomErrorText = context.getString(R.string.classroom_required)
            isValid = false
        } else {
            classroomError = false
            classroomErrorText = ""
        }

        if (subjectState.isBlank()) {
            subjectError = true
            subjectErrorText = context.getString(R.string.subject_required)
            isValid = false
        } else {
            subjectError = false
            subjectErrorText = ""
        }

        val startMonth = startMonthState.toIntOrNull()
        val startYear = startYearState.toIntOrNull()

        if (startMonth == null) {
            startMonthError = true
            startMonthErrorText = context.getString(R.string.invalid_months)
            isValid = false
        } else if (startYear != null && (
                    startYear < currentYear || (startYear == currentYear && startMonth < currentMonth))
        ) {
            startMonthError = true
            startMonthErrorText = context.getString(R.string.start_month_before_current)
            isValid = false
        } else {
            startMonthError = false
            startMonthErrorText = ""
        }


        val endMonth = endMonthState.toIntOrNull()
        if (endMonth == null) {
            endMonthError = true
            endMonthErrorText = context.getString(R.string.invalid_months)
            isValid = false
        } else {
            endMonthError = false
            endMonthErrorText = ""
        }

        val endYear = endYearState.toIntOrNull()
        if (endYear == null || endYear < currentYear) {
            endYearError = true
            endYearErrorText = context.getString(R.string.invalid_year)
            isValid = false
        } else {
            endYearError = false
            endYearErrorText = ""
        }

        if (startYear != null && endYear != null && startMonth != null && endMonth != null) {
            if (endYear < startYear || (endYear == startYear && endMonth < startMonth)) {
                endMonthError = true
                endMonthErrorText = context.getString(R.string.start_date_before_end_date)
                isValid = false
            }
        }
        if (!selectedDays.values.any { it }) {
            daysError = true
            daysErrorText = context.getString(R.string.at_least_one_day)
            isValid = false
        } else {
            daysError = false
            daysErrorText = ""
        }


        for ((day, isSelected) in selectedDays) {
            if (isSelected) {
                val hour = sessionsState[day]?.toIntOrNull()
                if (hour == null || hour !in 7..19) {
                    isValid = false
                    sessionErrorStates[day] = true
                } else {
                    sessionErrorStates[day] = false
                }
            } else {
                sessionErrorStates[day] = false
            }
        }

        return isValid
    }

    val isFormValid by remember { derivedStateOf { validateForm() } }


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
                selectedClassroom = selectedClassroom,
                isLoading = isLoading.value,
                errorMessage = errorMessage.value,
                onClassroomSelected = {
                    selectedClassroom = it
                    validateForm()
                },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            )
            if (classroomError) {
                Text(classroomErrorText, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = subjectState,
                onValueChange = {
                    subjectState = it
                    validateForm()
                },
                label = { Text(text = stringResource(R.string.subject)) },
                leadingIcon = { Icon(Icons.Filled.Book, contentDescription = null) },
                isError = subjectError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (subjectError) {
                Text(subjectErrorText, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MonthDropdown(
                    label = stringResource(R.string.start_month),
                    selectedMonth = startMonthState,
                    onMonthSelected = {
                        startMonthState = it
                        validateForm()
                    },
                )
                OutlinedTextField(
                    value = startYearState,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            startYearState = it
                            validateForm()
                        }
                    },
                    label = { Text(text = stringResource(R.string.start_year)) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                    isError = startYearError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }
            if (startMonthError) {
                Text(startMonthErrorText, color = MaterialTheme.colorScheme.error)
            }
            if (startYearError) {
                Text(startYearErrorText, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MonthDropdown(
                    label = stringResource(R.string.end_month),
                    selectedMonth = endMonthState,
                    onMonthSelected = {
                        endMonthState = it
                        validateForm()
                    }
                )
                OutlinedTextField(
                    value = endYearState,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            endYearState = it
                            validateForm()
                        }
                    },
                    label = { Text(text = stringResource(R.string.end_year)) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                    isError = endYearError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }
            if (endMonthError) {
                Text(endMonthErrorText, color = MaterialTheme.colorScheme.error)
            }
            if (endYearError) {
                Text(endYearErrorText, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.select_days_and_time),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            DaysOfWeekSelection(
                selectedDays = selectedDays,
                sessionsState = sessionsState,
                sessionErrorStates = sessionErrorStates
            )
            if (daysError) {
                Text(daysErrorText, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            }


            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    if (isFormValid) {
                        val schedule = Schedule(
                            classroomId = selectedClassroom!!.number.toString(),
                            tutorEmail = tutorEmail,
                            subject = subjectState,
                            approved = false,
                            startYear = startYearState.toInt(),
                            startMonth = startMonthState.toInt(),
                            endYear = endYearState.toInt(),
                            endMonth = endMonthState.toInt(),
                            sessions = createSessions(selectedDays, sessionsState)
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            val isOverlapping = fireStoreManager.checkForEmailOverlap(schedule)
                            if (isOverlapping) {
                                overlapMessage = context.getString(R.string.schedule_overlap_error)
                            } else {
                                overlapMessage = ""
                                fireStoreManager.createSchedule(schedule) { result ->
                                    result.onSuccess {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.schedule_created_successfully),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()

                                    }.onFailure {
                                        overlapMessage = context.getString(
                                            R.string.scheduling_error,
                                            it.localizedMessage
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.schedule_button))
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Default.Schedule, contentDescription = null)
                }
            }

            if (overlapMessage.isNotEmpty()) {
                Text(
                    text = overlapMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}