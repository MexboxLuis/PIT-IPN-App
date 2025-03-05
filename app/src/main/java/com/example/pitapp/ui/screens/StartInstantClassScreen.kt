package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit


data class SavedStudent(
    val name: String = "",
    val studentId: String = "",
    val academicProgram: String = "",
    val email: String = "",
    val isRegular: Boolean = true,
    val signature: String = ""
)

data class SavedClass(
    val scheduleId: String = "",
    val tutorEmail: String = "",
    val subject: String = "",
    val classroom: String = "",
    val topic: String = "",
    val date: Timestamp = Timestamp.now(),
    val students: List<SavedStudent> = listOf()
)


@Composable
fun StartInstantClassScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val tutorEmail = authManager.getUserEmail() ?: ""

    val currentSchedules = remember { mutableStateOf<List<Schedule>>(emptyList()) }
    val isSchedulesLoading = remember { mutableStateOf(false) }
    val schedulesError = remember { mutableStateOf<String?>(null) }

    val topic = remember { mutableStateOf("") }
    val classMessage = remember { mutableStateOf("") }
    val canStartClass = remember { mutableStateOf(false) }
    val currentSubject = remember { mutableStateOf("") }
    val classCreated = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        isSchedulesLoading.value = true
        fireStoreManager.getCurrentSchedules(tutorEmail) { result ->
            result.onSuccess { schedules ->
                currentSchedules.value = schedules
                isSchedulesLoading.value = false
                schedulesError.value = null
            }.onFailure {
                schedulesError.value = it.localizedMessage ?: "Error desconocido"
                isSchedulesLoading.value = false
            }
        }
    }

    LaunchedEffect(currentSchedules.value, classCreated.value) {
        if (classCreated.value) return@LaunchedEffect

        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val adaptedDayOfWeek = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1


        for (schedule in currentSchedules.value) {
            if (currentYear !in schedule.startYear..schedule.endYear ||
                !fireStoreManager.isMonthWithinRange(currentYear, currentMonth, schedule)
            ) {
                continue // Skip this schedule if it's not within the valid date range
            }
            for (session in schedule.sessions) {
                if (session.dayOfWeek == adaptedDayOfWeek && currentHour == session.startTime) {
                    canStartClass.value = true
                    currentSubject.value = schedule.subject
                    break // Exit inner loop once a matching session is found
                }
            }
            if (canStartClass.value) break // Exit outer loop if canStartClass is true
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
                UpcomingSchedules(fireStoreManager, tutorEmail)  // Keep UpcomingSchedules
            } else {
                // Mostrar siempre la información de la materia y salón si hay horarios disponibles.
                if (currentSchedules.value.isNotEmpty()) {
                    Text(
                        "Clase de ${currentSubject.value}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text("Salón: ${currentSchedules.value.firstOrNull()?.salonId ?: ""}")
                    Spacer(Modifier.height(16.dp))
                }


                OutlinedTextField(
                    value = topic.value,
                    onValueChange = { topic.value = it },
                    label = { Text("Tema de la clase") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canStartClass.value && !classCreated.value //Simplified
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (topic.value.isBlank()) {
                            classMessage.value = "Ingresa el tema de la clase."
                            return@Button
                        }
                        classCreated.value = true // Simplified

                        val savedClass = SavedClass(
                            scheduleId = currentSchedules.value.first { it.subject == currentSubject.value }.tutorEmail,
                            tutorEmail = tutorEmail,
                            subject = currentSubject.value,
                            classroom = currentSchedules.value.first { it.subject == currentSubject.value }.salonId,
                            topic = topic.value,
                            date = Timestamp.now(),
                            students = listOf()
                        )

                        fireStoreManager.startInstantClass(savedClass) { result ->
                            result.onSuccess { classDocumentId ->
                                classMessage.value = "Clase iniciada."  // Simplified message
                                navController.navigate("classDetalles/$classDocumentId")
                            }.onFailure {
                                classMessage.value = "Error al iniciar: ${it.localizedMessage}"
                                classCreated.value = false // Reset on failure
                            }
                        }
                    },
                    enabled = canStartClass.value && !classCreated.value, //Simplified
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Clase")
                }

                if (classMessage.value.isNotEmpty()) {
                    Text(classMessage.value)
                    Spacer(Modifier.height(16.dp))
                }
                if (currentSchedules.value.isNotEmpty() && !classCreated.value) {
                    UpcomingSchedules(
                        fireStoreManager,
                        tutorEmail,
                        currentSubject.value
                    ) // Keep Upcoming Schedule
                }

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
                    .filter { it.subject != currentSubject } // Filter out the current subject
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

    if (isLoading.value) {
        CircularProgressIndicator()
    } else if (error.value != null) {
        Text(error.value!!, color = MaterialTheme.colorScheme.error)
    } else if (upcomingSchedules.value.isNotEmpty()) {
        Text("Próximas clases:", style = MaterialTheme.typography.titleMedium)
        upcomingSchedules.value.forEach { (schedule, formattedTime) ->
            Text("${schedule.subject} (${schedule.salonId}): $formattedTime")
        }
    } else {
        Text("No hay próximas clases programadas.") // Add this line
    }
}


@Composable
fun ClassDetallesScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    classDocumentId: String
) {
    val classDetails = remember { mutableStateOf<SavedClass?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val timeLeftToAddStudents = remember { mutableStateOf("") } // Estado para el tiempo restante
    val showAddStudents = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope() // Necesario para agregar estudiantes


    //Estados para agregar estudiantes:
    val studentName = remember { mutableStateOf("") }
    val studentID = remember { mutableStateOf("") }
    val studentProgram = remember { mutableStateOf("") }
    val studentEmail = remember { mutableStateOf("") }
    val studentIsRegular = remember { mutableStateOf(true) }
    val studentMessage = remember { mutableStateOf("") }


    LaunchedEffect(classDocumentId) {
        fireStoreManager.getClassDetalles(classDocumentId) { result ->
            result.onSuccess { savedClass ->
                classDetails.value = savedClass
                isLoading.value = false
                error.value = null
            }.onFailure {
                error.value = it.localizedMessage ?: "Error desconocido"
                isLoading.value = false
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (classDetails.value != null) {
                val now = Calendar.getInstance()
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val currentMinute = now.get(Calendar.MINUTE)

                // Obtenemos la hora en que finaliza el tiempo para agregar estudiantes
                val classCreationTime = classDetails.value!!.date.toDate()
                val calendar = Calendar.getInstance().apply { time = classCreationTime }
                val startHour = calendar.get(Calendar.HOUR_OF_DAY)
                val endTimeHour = startHour + 1
                val addStudentsTimeHour = endTimeHour - 1
                val addStudentsTimeMinute = 50

                // Actualizamos si se debe mostrar el formulario para agregar estudiantes
                showAddStudents.value =
                    (currentHour == addStudentsTimeHour && currentMinute >= addStudentsTimeMinute) ||
                            (currentHour == endTimeHour && currentMinute < 60)

                if (showAddStudents.value) {
                    val timeLeft = if (currentHour == addStudentsTimeHour) {
                        val totalMinutesLeft = 60 - currentMinute
                        "$totalMinutesLeft minutos restantes"
                    } else {
                        val totalMinutes = 60 - currentMinute
                        "$totalMinutes minutos restantes"
                    }
                    timeLeftToAddStudents.value = timeLeft
                } else {
                    val endTime = Calendar.getInstance().apply {
                        time = classCreationTime
                        set(Calendar.HOUR_OF_DAY, addStudentsTimeHour)
                        set(Calendar.MINUTE, addStudentsTimeMinute)
                        set(Calendar.SECOND, 0)
                    }
                    val diffInMillis = endTime.timeInMillis - now.timeInMillis
                    timeLeftToAddStudents.value = if (diffInMillis > 0) {
                        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                        when {
                            diffInHours > 0 -> "Podrás agregar estudiantes en $diffInHours horas y ${diffInMinutes - (diffInHours * 60)} minutos"
                            diffInMinutes > 0 -> "Podrás agregar estudiantes en $diffInMinutes minutos"
                            else -> "Podrás agregar estudiantes en menos de un minuto"
                        }
                    } else {
                        "Ya puedes agregar estudiantes"
                    }
                }
            }
            delay(1000L) // Se actualiza cada segundo
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

                if (showAddStudents.value) {
                    Text(timeLeftToAddStudents.value, style = MaterialTheme.typography.bodyLarge)
                    //Formulario para agregar
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
                            // Agregar estudiante *inmediatamente*
                            coroutineScope.launch { // <-- Usa el coroutineScope
                                try {
                                    fireStoreManager.addStudent(
                                        classDetails.value!!.tutorEmail, //Usar el valor, ya que ahora SavedClass? es opcional
                                        classDetails.value!!.subject,
                                        classDetails.value!!.classroom,
                                        newStudent
                                    ) { result ->
                                        if (result.isSuccess) {
                                            studentMessage.value =
                                                "Estudiante ${newStudent.name} agregado."
                                            // Limpiar campos
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
                    Text(timeLeftToAddStudents.value, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Text("No se encontraron detalles de la clase.")
            }
        }
    }
}



// Helper function to get the *next* session time, even if it's next week.
private fun nextSessionTime(schedule: Schedule, now: Calendar): Pair<Session, Calendar>? {
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
    val adaptedCurrentDayOfWeek =
        if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1
    val currentHour = now.get(Calendar.HOUR_OF_DAY)
    val currentMinute = now.get(Calendar.MINUTE)

    // 1. Filter by date range (Year and Month)
    if (currentYear !in schedule.startYear..schedule.endYear) return null

    val validMonth = when {
        schedule.startYear == schedule.endYear -> currentMonth in schedule.startMonth..schedule.endMonth
        currentYear == schedule.startYear -> currentMonth >= schedule.startMonth
        currentYear == schedule.endYear -> currentMonth <= schedule.endMonth
        else -> true // Current year is between start and end year
    }
    if (!validMonth) return null


    // 2. Find the next session, considering wrapping around to next week.
    var nextSession: Session? = null
    var nextSessionTime: Calendar? = null

    for (session in schedule.sessions) {
        val adaptedSessionDayOfWeek = if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1
        val sessionTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)  // Calendar.MONTH is 0-indexed
            set(Calendar.DAY_OF_WEEK, adaptedSessionDayOfWeek)
            set(Calendar.HOUR_OF_DAY, session.startTime)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Check if session is today and in the future, or in the upcoming days.
        if (adaptedCurrentDayOfWeek == session.dayOfWeek && currentHour < session.startTime) {
            // Today, and the session hasn't started
            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        } else if (sessionTime.after(now)) {
            //Upcoming days
            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        }
    }
    //If no nextSession was found on the remaining days, check from the beginning of the week
    if (nextSession == null) {
        for (session in schedule.sessions) {
            val adaptedSessionDayOfWeek =
                if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1 //Necessary for correct day
            val sessionTime = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth - 1)
                set(Calendar.DAY_OF_WEEK, adaptedSessionDayOfWeek)
                set(Calendar.HOUR_OF_DAY, session.startTime)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                //Add one week, because the session must be in the future
                add(Calendar.WEEK_OF_YEAR, 1)
            }

            //This time, we only consider if nextSessionTime is null or sessionTime is before
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


// Helper function to format the session time, including relative time.
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

    // Calculate the time difference
    val diffInMillis = sessionTime.timeInMillis - now.timeInMillis
    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    val relativeTimeString = when {
        diffInDays > 7 -> "el $dayOfWeekString a las $timeString" //For dates that are far in the future.
        diffInDays > 0 -> "en $diffInDays días, $dayOfWeekString a las $timeString"
        diffInHours > 0 -> "en $diffInHours horas"
        diffInMinutes > 0 -> "en $diffInMinutes minutos"
        else -> "próximamente" // Should not be reached, for next classes
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