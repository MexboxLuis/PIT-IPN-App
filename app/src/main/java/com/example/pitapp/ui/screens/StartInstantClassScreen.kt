package com.example.pitapp.ui.screens


import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.features.classes.model.SavedClass
import com.example.pitapp.ui.features.classes.model.SavedStudent
import com.example.pitapp.ui.features.scheduling.model.Schedule
import com.example.pitapp.ui.features.scheduling.model.Session
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.example.pitapp.R


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

    val topic = remember { mutableStateOf("") }
    val classMessage = remember { mutableStateOf("") }
    val canStartClass = remember { mutableStateOf(false) }
    val currentSubject = remember { mutableStateOf("") }
    val currentSalonId = remember { mutableStateOf("") }
    val classCreated = remember { mutableStateOf(false) }
    val remainingTime = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isSchedulesLoading.value = true
        fireStoreManager.getCurrentSchedules(tutorEmail) { result ->
            result.onSuccess { schedules ->
                currentSchedules.value = schedules
                isSchedulesLoading.value = false
                schedulesError.value = null
            }.onFailure {
                schedulesError.value = it.localizedMessage ?: context.getString(R.string.unknown_error)
                isSchedulesLoading.value = false
            }
            Log.d("Schedules", "A ver: ${result.getOrNull()}")
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
        currentSalonId.value = targetSchedule.salonId

        if (now.timeInMillis < targetSessionStartTime.timeInMillis) {
            while (true) {
                val currentTime = Calendar.getInstance()
                val diffMillis = targetSessionStartTime.timeInMillis - currentTime.timeInMillis
                if (diffMillis <= 0) break
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60
                remainingTime.value = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

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

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Iniciar Clase"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSchedulesLoading.value) {
                CircularProgressIndicator()
            } else if (schedulesError.value != null) {
                Text(schedulesError.value!!, color = MaterialTheme.colorScheme.error)
            } else if (currentSchedules.value.isEmpty()) {
                Text("No hay clases programadas para este momento.")
                Spacer(Modifier.height(16.dp))
                UpcomingSchedules(fireStoreManager, tutorEmail)
            } else {
                if (currentSubject.value.isNotEmpty()) {
                    Text(
                        "Clase de ${currentSubject.value}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                if (currentSalonId.value.isNotEmpty()) {
                    Text("Salón: ${currentSalonId.value}")
                    Spacer(Modifier.height(16.dp))
                }

                if (remainingTime.value.isNotEmpty()) {
                    if (!canStartClass.value) {
                        Text(
                            "La clase inicia en: ${remainingTime.value}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    } else {
                        Text(
                            "Tiempo para iniciar la clase: ${remainingTime.value}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (canStartClass.value && !classCreated.value) {
                    OutlinedTextField(
                        value = topic.value,
                        onValueChange = { topic.value = it },
                        label = { Text("Tema de la clase") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (topic.value.isBlank()) {
                                classMessage.value = "Ingresa el tema de la clase."
                                return@Button
                            }
                            classCreated.value = true

                            val savedClass = SavedClass(
                                tutorEmail = tutorEmail,
                                subject = currentSubject.value,
                                classroom = currentSalonId.value,
                                topic = topic.value,
                                date = Timestamp.now()
                            )

                            fireStoreManager.startInstantClass(savedClass) { result ->
                                result.onSuccess { classDocumentId ->
                                    classMessage.value = "Clase iniciada."
                                    navController.navigate("instantClassDetailsScreen/$classDocumentId") {
                                        popUpTo("startInstantClassScreen") { inclusive = true }
                                    }
                                }.onFailure {
                                    classMessage.value = "Error al iniciar: ${it.localizedMessage}"
                                    classCreated.value = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Iniciar Clase")
                    }
                }

                if (classMessage.value.isNotEmpty()) {
                    Text(classMessage.value)
                    Spacer(Modifier.height(16.dp))
                }
                if (currentSchedules.value.isNotEmpty() && !classCreated.value) {
                    UpcomingSchedules(fireStoreManager, tutorEmail, currentSubject.value)
                }
            }
        }
    }
}


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

    val studentName = remember { mutableStateOf("") }
    val studentID = remember { mutableStateOf("") }
    val studentProgram = remember { mutableStateOf("") }
    val studentEmail = remember { mutableStateOf("") }
    val studentIsRegular = remember { mutableStateOf(true) }
    val studentMessage = remember { mutableStateOf("") }

    val studentList = remember { mutableStateListOf<SavedStudent>() }

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

        val classStartTime = classDetails.value!!.date.toDate()
        val classStartCal = Calendar.getInstance().apply { time = classStartTime }

        val classEndCal = (classStartCal.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val addStudentEndCal = (classEndCal.clone() as Calendar).apply {
            add(Calendar.MINUTE, -15)
        }

        while (true) {
            val now = Calendar.getInstance()

            // Tiempo restante para la clase
            val classDiffMillis = classEndCal.timeInMillis - now.timeInMillis
            val classMinutes = TimeUnit.MILLISECONDS.toMinutes(classDiffMillis)
            val classSeconds = TimeUnit.MILLISECONDS.toSeconds(classDiffMillis) % 60

            if (classDiffMillis <= 0) {
                navController.navigate("homeScreen") {
                    popUpTo(navController.currentDestination?.id ?: 0) { inclusive = true }
                }
                break
            }

            // Tiempo restante para agregar estudiantes (hasta que falten 14 minutos para el final de la clase)
            val addStudentDiffMillis = addStudentEndCal.timeInMillis - now.timeInMillis
            // Se muestra el formulario cuando el tiempo se agota (<= 0)
            val showAddStudentForm = addStudentDiffMillis <= 0
            showAddStudents.value = showAddStudentForm

            if (!showAddStudentForm) {
                // Mientras quede tiempo para agregar estudiantes, se muestra el contador
                val addStudentMinutes = TimeUnit.MILLISECONDS.toMinutes(addStudentDiffMillis)
                val addStudentSeconds = TimeUnit.MILLISECONDS.toSeconds(addStudentDiffMillis) % 60
                timeLeftToAddStudents.value = "Tiempo para agregar estudiantes: ${
                    String.format(Locale.getDefault(), "%02d:%02d", addStudentMinutes, addStudentSeconds)
                }"
            } else {
                timeLeftToAddStudents.value = ""
            }

            timeLeftForClass.value = "Tiempo restante de clase: ${
                String.format(Locale.getDefault(), "%02d:%02d", classMinutes, classSeconds)
            }"

            delay(1000)
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Detalles de la Clase"

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading.value) {
                CircularProgressIndicator()
            } else if (error.value != null) {
                Text("Error: ${error.value}", color = MaterialTheme.colorScheme.error)
            } else if (classDetails.value != null) {
                Text(
                    "Clase: ${classDetails.value!!.topic}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text("Materia: ${classDetails.value!!.subject}")
                Text("Salón: ${classDetails.value!!.classroom}")
                Spacer(modifier = Modifier.height(16.dp))

                Text(timeLeftForClass.value, style = MaterialTheme.typography.bodyLarge)

                if (showAddStudents.value) {
                    // Show the form, but NO countdown
                    AddStudentForm(
                        name = studentName.value,
                        studentId = studentID.value,
                        academicProgram = studentProgram.value,
                        email = studentEmail.value,
                        isRegular = studentIsRegular.value,
                        onNameChange = { studentName.value = it },
                        onStudentIdChange = { studentID.value = it },
                        onAcademicProgramChange = { studentProgram.value = it },
                        onEmailChange = { studentEmail.value = it },
                        onIsRegularChange = { studentIsRegular.value = it },
                        onAddStudent = {
                            if (studentName.value.isBlank() || studentID.value.isBlank() || studentProgram.value.isBlank() || studentEmail.value.isBlank()) {
                                studentMessage.value = "Completa todos los campos."
                                return@AddStudentForm
                            }
                            val newStudent = SavedStudent(
                                name = studentName.value,
                                studentId = studentID.value,
                                academicProgram = studentProgram.value,
                                email = studentEmail.value,
                                isRegular = studentIsRegular.value
                            )
                            coroutineScope.launch {
                                try {
                                    fireStoreManager.addStudent(
                                        classDocumentId,
                                        newStudent
                                    ) { result ->
                                        if (result.isSuccess) {
                                            studentMessage.value =
                                                "Estudiante ${newStudent.name} agregado."
                                            studentName.value = ""
                                            studentID.value = ""
                                            studentProgram.value = ""
                                            studentEmail.value = ""
                                            studentIsRegular.value = true
                                        } else {
                                            studentMessage.value =
                                                "Error al agregar estudiante: ${result.exceptionOrNull()?.localizedMessage}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    studentMessage.value = "Error: ${e.localizedMessage}"
                                }
                            }
                        }
                    )
                } else {
                    if (timeLeftToAddStudents.value.isNotEmpty()) {
                        Text(
                            timeLeftToAddStudents.value,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (studentList.isNotEmpty()) {
                    Text("Estudiantes agregados:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(studentList) { student ->
                            Text(
                                text = "Nombre: ${student.name}  |  ID: ${student.studentId}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (timeLeftToAddStudents.value.isEmpty()) {
                    Text("No hay estudiantes agregados aún.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text("No se encontraron detalles de la clase.")
            }
        }
    }
}

@Composable
fun UpcomingSchedules(
    fireStoreManager: FireStoreManager,
    tutorEmail: String,
    currentSubject: String = ""
) {
    val upcomingSchedules = remember { mutableStateOf<List<Pair<Schedule, String>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tutorEmail, currentSubject) {
        isLoading.value = true
        fireStoreManager.getUpcomingSchedules(tutorEmail) { result ->
            result.onSuccess { schedules ->
                val now = Calendar.getInstance()
                val schedulesWithFormattedTime = schedules
                    .filter { it.subject != currentSubject }
                    .mapNotNull { schedule ->
                        nextSessionTime(schedule, now)?.let { (nextSession, sessionTime) ->
                            Pair(schedule, formatSessionTime(nextSession, sessionTime))
                        }
                    }
                upcomingSchedules.value = schedulesWithFormattedTime
                isLoading.value = false
                error.value = null
            }.onFailure {
                error.value = it.localizedMessage ?: "Error desconocido"
                isLoading.value = false
            }
        }
    }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Próximas clases:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(upcomingSchedules.value) { (schedule, formattedTime) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "${schedule.subject} (${schedule.salonId})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formattedTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay próximas clases programadas.")
            }
        }
    }
}



private fun nextSessionTime(schedule: Schedule, now: Calendar): Pair<Session, Calendar>? {
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
    val adaptedCurrentDayOfWeek =
        if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1
    val currentHour = now.get(Calendar.HOUR_OF_DAY)

    if (currentYear !in schedule.startYear..schedule.endYear ||
        !schedule.isWithinDateRange(currentYear, currentMonth)
    ) return null


    var nextSession: Session? = null
    var nextSessionTime: Calendar? = null

    for (session in schedule.sessions) {
        val adaptedSessionDayOfWeek = if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1
        val sessionTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1) // Calendar.MONTH is 0-indexed
            set(Calendar.DAY_OF_WEEK, adaptedSessionDayOfWeek)
            set(Calendar.HOUR_OF_DAY, session.startTime)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }


        // Consider the session if it's in the future *or* if it's today and the time is now or later.
        if (adaptedSessionDayOfWeek == adaptedCurrentDayOfWeek && currentHour <= session.startTime) {
            // Today, and the session is upcoming or starting now
            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        } else if (sessionTime.after(now)) { // Check future sessions.
            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        }
    }

    // If no session found in the current week, find the earliest session in the schedule.
    if (nextSession == null) {
        for (session in schedule.sessions) {
            val adaptedSessionDayOfWeek = if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1
            val sessionTime = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth - 1)
                set(Calendar.DAY_OF_WEEK, adaptedSessionDayOfWeek)
                set(Calendar.HOUR_OF_DAY, session.startTime)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.WEEK_OF_YEAR, 1) // Add a week to consider sessions next week.
            }

            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        }
    }

    return if (nextSession != null && nextSessionTime != null) {
        Pair(nextSession, nextSessionTime)
    } else {
        null
    }
}

//Added this function
fun Schedule.isWithinDateRange(currentYear: Int, currentMonth: Int): Boolean {
    return when {
        startYear == endYear -> currentMonth in startMonth..endMonth
        currentYear == startYear -> currentMonth >= startMonth
        currentYear == endYear -> currentMonth <= endMonth
        else -> currentYear in (startYear + 1) until endYear // Between start and end years
    }
}


private fun formatSessionTime(session: Session, sessionTime: Calendar): String {
    val now = Calendar.getInstance()
    val adaptedSessionDayOfWeek = if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1
    val dayOfWeekString = when (adaptedSessionDayOfWeek) {
        Calendar.MONDAY -> "Lunes"
        Calendar.TUESDAY -> "Martes"
        Calendar.WEDNESDAY -> "Miércoles"
        Calendar.THURSDAY -> "Jueves"
        Calendar.FRIDAY -> "Viernes"
        Calendar.SATURDAY -> "Sábado"
        Calendar.SUNDAY -> "Domingo"
        else -> ""
    }
    val timeString = "${session.startTime}:00"

    val diffInMillis = sessionTime.timeInMillis - now.timeInMillis
    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    val relativeTimeString = when {
        diffInDays > 7 -> "el $dayOfWeekString a las $timeString"
        diffInDays > 0 -> "en $diffInDays días, $dayOfWeekString a las $timeString"
        diffInHours > 0 -> "en $diffInHours horas"
        diffInMinutes > 0 -> "en $diffInMinutes minutos"
        else -> "próximamente"
    }

    return "$relativeTimeString ($dayOfWeekString, $timeString)"
}


@Composable
fun AddStudentForm(
    name: String,
    studentId: String,
    academicProgram: String,
    email: String,
    isRegular: Boolean,
    onNameChange: (String) -> Unit,
    onStudentIdChange: (String) -> Unit,
    onAcademicProgramChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onIsRegularChange: (Boolean) -> Unit,
    onAddStudent: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = studentId,
            onValueChange = onStudentIdChange,
            label = { Text("ID de Estudiante") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = academicProgram,
            onValueChange = onAcademicProgramChange,
            label = { Text("Programa Académico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = isRegular,
                onCheckedChange = onIsRegularChange
            )
            Text("Regular")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAddStudent, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar Estudiante")
        }
    }
}