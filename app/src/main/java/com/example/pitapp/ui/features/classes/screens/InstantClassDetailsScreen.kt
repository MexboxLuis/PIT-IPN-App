package com.example.pitapp.ui.features.classes.screens

import android.util.Patterns
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.SavedClass
import com.example.pitapp.model.SavedStudent
import com.example.pitapp.ui.features.classes.components.AddStudentForm
import com.example.pitapp.ui.features.classes.components.InfoClassRow
import com.example.pitapp.ui.features.classes.components.NoStudentsAddedState
import com.example.pitapp.ui.features.classes.components.QRScanner
import com.example.pitapp.ui.features.classes.components.SectionClassTitle
import com.example.pitapp.ui.features.classes.components.StudentRow
import com.example.pitapp.ui.features.classes.components.WaitingForStudentsState
import com.example.pitapp.ui.features.classes.model.ScannedData
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.ui.shared.components.EmptyState
import com.example.pitapp.ui.shared.screens.LoadingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun InstantClassDetailsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    classDocumentId: String
) {
    val context = LocalContext.current
    val classDetails = remember { mutableStateOf<SavedClass?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val timeLeftToAddStudents = remember { mutableStateOf("") }
    val timeLeftForClass = remember { mutableStateOf("") }
    val showAddStudents = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val studentEmail = remember { mutableStateOf("") }
    val studentIsRegular = remember { mutableStateOf(true) }
    val studentMessage = remember { mutableStateOf("") }

    val studentList = remember { mutableStateListOf<SavedStudent>() }
    val isFetchingStudentData = remember { mutableStateOf(false) }
    val isScanningProcessing = remember { mutableStateOf(false) }
    val qrDataScanned = remember { mutableStateOf(false) }
    val scannedData = remember { mutableStateOf<ScannedData?>(null) }

    val progressFraction = remember { mutableFloatStateOf(0f) }
    val addStudentProgressFraction = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(classDocumentId) {
        fireStoreManager.getInstantClassDetails(classDocumentId) { result ->
            result.onSuccess { savedClass ->
                classDetails.value = savedClass
                isLoading.value = false
                error.value = null
                fireStoreManager.getStudentsNow(classDocumentId) { result ->
                    result.onSuccess { students ->
                        studentList.clear()
                        studentList.addAll(students)
                    }
                }
            }.onFailure {
                error.value = it.localizedMessage ?: context.getString(R.string.unknown_error)
                isLoading.value = false
            }

        }
    }


    LaunchedEffect(classDetails.value) {
        if (classDetails.value == null) return@LaunchedEffect

        val classActualStartTime = classDetails.value!!.date.toDate()
        val classActualStartCal = Calendar.getInstance().apply { time = classActualStartTime }

        val conceptualClassStartCal = (classActualStartCal.clone() as Calendar).apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val conceptualClassStartMillis = conceptualClassStartCal.timeInMillis

        val conceptualClassEndCal = (conceptualClassStartCal.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }
        val conceptualClassEndMillis = conceptualClassEndCal.timeInMillis

        val totalDurationMillis = 60 * 60 * 1000L

        val addStudentEndCal = (conceptualClassEndCal.clone() as Calendar).apply {
            add(Calendar.MINUTE, -15)
        }
        val addStudentEndMillis = addStudentEndCal.timeInMillis
        val addStudentWindowDurationMillis = 45 * 60 * 1000L

        while (true) {
            val now = Calendar.getInstance()
            val nowMillis = now.timeInMillis

            val classDiffMillis = conceptualClassEndMillis - nowMillis

            if (classDiffMillis <= 0) {
                navController.navigate("homeScreen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
                break
            }

            val classMinutes = TimeUnit.MILLISECONDS.toMinutes(classDiffMillis).coerceAtLeast(0)
            val classSeconds =
                (TimeUnit.MILLISECONDS.toSeconds(classDiffMillis) % 60).coerceAtLeast(0)

            timeLeftForClass.value =
                String.format(Locale.getDefault(), "%02d:%02d", classMinutes, classSeconds)

            val elapsedMillis =
                nowMillis - conceptualClassStartMillis

            progressFraction.floatValue =
                (elapsedMillis.toFloat() / totalDurationMillis).coerceIn(0f, 1f)

            val addStudentDiffMillis = addStudentEndMillis - nowMillis
            showAddStudents.value =
                addStudentDiffMillis <= 0

            if (!showAddStudents.value) {
                val addStudentMinutes =
                    TimeUnit.MILLISECONDS.toMinutes(addStudentDiffMillis).coerceAtLeast(0)
                val addStudentSeconds =
                    (TimeUnit.MILLISECONDS.toSeconds(addStudentDiffMillis) % 60).coerceAtLeast(0)
                timeLeftToAddStudents.value = String.format(
                    Locale.getDefault(), "%02d:%02d",
                    addStudentMinutes,
                    addStudentSeconds
                )
                addStudentProgressFraction.floatValue =
                    (elapsedMillis.toFloat() / addStudentWindowDurationMillis).coerceIn(0f, 1f)

            } else {
                timeLeftToAddStudents.value = ""
            }

            delay(1000)
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(R.string.class_details_title)

    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading.value) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingScreen()
                    }
                }
            } else if (error.value != null) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error.value!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            } else if (classDetails.value != null) {

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                text = classDetails.value!!.topic,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))

                            InfoClassRow(
                                icon = Icons.Default.School,
                                label = stringResource(R.string.subject),
                                value = classDetails.value!!.subject
                            )
                            InfoClassRow(
                                icon = Icons.Default.MeetingRoom,
                                label = stringResource(R.string.classroom),
                                value = classDetails.value!!.classroom
                            )

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.time_left_class_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Box(
                                        modifier = Modifier.size(150.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { progressFraction.floatValue },
                                            modifier = Modifier.matchParentSize(),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 10.dp,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            strokeCap = StrokeCap.Round
                                        )
                                        Text(
                                            text = timeLeftForClass.value,
                                            style = MaterialTheme.typography.displaySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when {
                                            timeLeftToAddStudents.value.isNotEmpty() -> stringResource(
                                                R.string.add_students
                                            )

                                            isScanningProcessing.value || isFetchingStudentData.value -> stringResource(
                                                R.string.searching_data
                                            )

                                            !qrDataScanned.value -> stringResource(R.string.scan_qr)
                                            else -> stringResource(R.string.refresh)
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (timeLeftToAddStudents.value.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier.size(150.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { addStudentProgressFraction.floatValue },
                                                modifier = Modifier.matchParentSize(),
                                                color = MaterialTheme.colorScheme.secondary,
                                                strokeWidth = 10.dp,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                strokeCap = StrokeCap.Round
                                            )
                                            Text(
                                                text = timeLeftToAddStudents.value,
                                                style = MaterialTheme.typography.displaySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    } else {
                                        if (qrDataScanned.value == false) {
                                            if (isScanningProcessing.value || isFetchingStudentData.value) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator()
                                                }
                                            } else {
                                                QRScanner(
                                                    context = context,
                                                    coroutineScope = coroutineScope,
                                                    onScanStart = {
                                                        isScanningProcessing.value = true
                                                        studentMessage.value = ""
                                                        qrDataScanned.value = false
                                                        scannedData.value = null
                                                    },
                                                    onScanComplete = { data ->
                                                        scannedData.value = data
                                                        qrDataScanned.value = true
                                                        isFetchingStudentData.value =
                                                            true
                                                        fireStoreManager.getSavedStudent(studentId = data.studentId) { result ->
                                                            result.onSuccess { student ->
                                                                if (student != null) {
                                                                    studentEmail.value =
                                                                        student.email
                                                                    studentIsRegular.value =
                                                                        student.regular
                                                                    studentMessage.value =
                                                                        context.getString(
                                                                            R.string.welcome_back,
                                                                            student.name
                                                                        )
                                                                } else {
                                                                    studentEmail.value = ""
                                                                    studentIsRegular.value = true
                                                                    studentMessage.value =
                                                                        context.getString(R.string.welcome_new_student)
                                                                }
                                                                isFetchingStudentData.value = false
                                                                isScanningProcessing.value = false
                                                            }
                                                            result.onFailure { e ->
                                                                studentMessage.value =
                                                                    context.getString(
                                                                        R.string.error_fetching_student,
                                                                        "${e.localizedMessage}"
                                                                    )
                                                                isFetchingStudentData.value = false
                                                                isScanningProcessing.value = false
                                                                qrDataScanned.value = false
                                                                scannedData.value = null

                                                            }
                                                        }
                                                    },
                                                    onError = { errorMsg ->
                                                        studentMessage.value = errorMsg
                                                        isScanningProcessing.value = false
                                                        isFetchingStudentData.value = false
                                                        qrDataScanned.value = false
                                                        scannedData.value = null
                                                    }
                                                )
                                            }

                                        } else {
                                            IconButton(
                                                modifier = Modifier.size(150.dp),
                                                onClick = {
                                                    isScanningProcessing.value = false
                                                    isFetchingStudentData.value = false
                                                    qrDataScanned.value = false
                                                    scannedData.value = null
                                                    studentMessage.value = ""
                                                    studentEmail.value = ""
                                                    studentIsRegular.value = true
                                                }) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary

                                                )
                                            }

                                        }

                                    }
                                }


                            }
                        }
                    }
                }

                item {
                    if (studentMessage.value.isNotEmpty() && !isScanningProcessing.value && !isFetchingStudentData.value) {
                        Text(
                            text = studentMessage.value,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }


                if (showAddStudents.value && qrDataScanned.value) {
                    scannedData.let { data ->
                        item {
                            AddStudentForm(
                                name = data.value?.name ?: "",
                                studentId = data.value?.studentId ?: "",
                                academicProgram = data.value?.academicProgram ?: "",
                                email = studentEmail.value,
                                isRegular = studentIsRegular.value,
                                onEmailChange = { studentEmail.value = it },
                                onIsRegularChange = { studentIsRegular.value = it },
                                onAddStudent = {

                                    val scannedName = data.value?.name ?: ""
                                    val scannedStudentId = data.value?.studentId ?: ""
                                    val scannedProgram = data.value?.academicProgram ?: ""

                                    if (!Patterns.EMAIL_ADDRESS.matcher(studentEmail.value)
                                            .matches()
                                    ) {
                                        studentMessage.value =
                                            context.getString(R.string.invalid_email)
                                        return@AddStudentForm
                                    }

                                    if (scannedName.isBlank() || scannedStudentId.isBlank() || scannedProgram.isBlank() || studentEmail.value.isBlank()) {
                                        studentMessage.value =
                                            context.getString(R.string.complete_remaining_data)
                                        return@AddStudentForm
                                    }

                                    if (studentList.any { it.studentId == scannedStudentId }) {
                                        studentMessage.value =
                                            context.getString(R.string.duplicate_student_id)
                                        return@AddStudentForm
                                    }

                                    val newStudent = SavedStudent(
                                        name = scannedName,
                                        studentId = scannedStudentId,
                                        academicProgram = scannedProgram,
                                        email = studentEmail.value,
                                        regular = studentIsRegular.value
                                    )
                                    coroutineScope.launch {
                                        try {
                                            fireStoreManager.addStudent(
                                                classDocumentId,
                                                newStudent
                                            ) { result ->
                                                if (result.isSuccess) {
                                                    studentMessage.value = context.getString(
                                                        R.string.student_added_success,
                                                        newStudent.name
                                                    )
                                                    studentEmail.value = ""
                                                    studentIsRegular.value = true
                                                    scannedData.value = null
                                                    qrDataScanned.value = false
                                                    studentMessage.value = ""
                                                } else {
                                                    studentMessage.value = context.getString(
                                                        R.string.error_adding_student,
                                                        "${result.exceptionOrNull()?.localizedMessage}"
                                                    )
                                                }
                                            }
                                        } catch (e: Exception) {
                                            studentMessage.value = "${e.localizedMessage}"
                                        }
                                    }
                                }
                            )
                        }
                    }
                } else if (studentList.isNotEmpty()) {
                    item {
                        SectionClassTitle(
                            text = stringResource(R.string.added_students_title),
                            icon = Icons.Default.Group
                        )
                    }
                    items(items = studentList, key = { it.studentId }) { student ->
                        StudentRow(student = student)
                    }
                } else if (timeLeftToAddStudents.value.isEmpty()) {
                    item {
                        NoStudentsAddedState(
                            message = stringResource(R.string.no_students_added_message)
                        )
                    }
                } else {
                    item {
                        WaitingForStudentsState(
                            baseMessageResId = R.string.waiting_to_add_students_message
                        )
                    }
                }

            } else {
                item {
                    EmptyState(
                        icon = Icons.Default.Details,
                        message = stringResource(R.string.class_details_not_found)
                    )
                }
            }
        }
    }
}