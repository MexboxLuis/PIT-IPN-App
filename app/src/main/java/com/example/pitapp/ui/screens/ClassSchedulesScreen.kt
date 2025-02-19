package com.example.pitapp.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ClassSchedulesScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Horarios Definidos", "Horarios por Aprobar")
    val context = LocalContext.current

    val schedules = remember { mutableStateOf<List<Pair<String, Schedule>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Obtener horarios en tiempo real
    LaunchedEffect(Unit) {
        fireStoreManager.getSchedules { result ->
            result.onSuccess { list ->
                schedules.value = list
                isLoading.value = false
            }.onFailure {
                errorMessage.value = it.localizedMessage ?: "Error desconocido"
                isLoading.value = false
            }
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Horarios de Clase"
    ) {
        Column {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }) {
                        Text(title)
                    }
                }
            }

            val filteredSchedules = when (selectedTabIndex) {
                0 -> schedules.value.filter { it.second.approved }
                1 -> schedules.value.filter { !it.second.approved }
                else -> emptyList()
            }

            if (isLoading.value) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage.value != null) {
                Text(errorMessage.value!!, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(filteredSchedules) { (scheduleId, schedule) ->
                        ScheduleItem(
                            schedule = schedule,
                            onEdit = { navController.navigate("editSchedule/$scheduleId") },
                            onApprove = {
                                // Verificar traslape ANTES de aprobar
                                CoroutineScope(Dispatchers.Main).launch {
                                    val isOverlapping =
                                        fireStoreManager.checkForOverlap(schedule)
                                    if (isOverlapping) {
                                        Toast.makeText(
                                            context,
                                            "Traslape detectado.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        fireStoreManager.approveSchedule(scheduleId) { result ->
                                            if (result.isFailure) {
                                                Toast.makeText(
                                                    context,
                                                    "Error al aprobar.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            },
                            showApproveButton = !schedule.approved // Mostrar botón solo si no está aprobado
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: Schedule,
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    showApproveButton: Boolean // Controla la visibilidad del botón
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Salón: ${schedule.salonId}", style = MaterialTheme.typography.bodyMedium)
            Text("Tutor: ${schedule.tutorEmail}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Materia: ${schedule.subject}",
                style = MaterialTheme.typography.bodySmall
            ) // Mostrar materia
            Text("Periodo: ${schedule.startYear}/${schedule.startMonth} - ${schedule.endYear}/${schedule.endMonth}")
            Text("Sesiones: " + schedule.sessions.joinToString {
                "${dayOfWeekToString(it.dayOfWeek)} ${it.startTime}"
            })

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar")
                }
                if (showApproveButton) { // Mostrar el botón según la bandera
                    IconButton(onClick = onApprove) {
                        Icon(Icons.Default.Check, "Aprobar")
                    }
                }
            }
        }
    }
}

// Función para convertir el número del día a String (reutilizable)
fun dayOfWeekToString(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "Lun"
        2 -> "Mar"
        3 -> "Mié"
        4 -> "Jue"
        5 -> "Vie"
        6 -> "Sáb"
        7 -> "Dom"
        else -> ""
    }
}



@Composable
fun EditScheduleScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    scheduleId: String?
) {


    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
    val startYearState = remember { mutableStateOf("") }
    val endYearState = remember { mutableStateOf("") }
    val startMonthState = remember { mutableStateOf("") }
    val endMonthState = remember { mutableStateOf("") }
    val subjectState = remember { mutableStateOf("") } // Campo de materia
    val tutorEmailState = remember { mutableStateOf("") }

    val selectedDays = remember { mutableStateMapOf<Int, Boolean>() }
    (1..5).forEach { selectedDays.getOrPut(it) { false } } //Dias de la semana
    val sessionsState = remember { mutableStateMapOf<Int, String>() }
    val message = remember { mutableStateOf("") }

    // Estados para el Dropdown de salones
    val classrooms = remember { mutableStateOf<List<Pair<Int, Classroom>>>(emptyList()) }
    val isLoadingClassrooms = remember { mutableStateOf(true) }
    val errorLoadingClassrooms = remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val selectedClassroom = remember { mutableStateOf<Classroom?>(null) }

    // Estado para loading y error al cargar el Schedule
    val isLoadingSchedule = remember { mutableStateOf(true) }
    val errorLoadingSchedule = remember { mutableStateOf<String?>(null) }

    // Cargar datos del Schedule existente
    LaunchedEffect(scheduleId) {
        if (scheduleId != null) {
            fireStoreManager.getScheduleById(scheduleId) { result ->
                result.onSuccess { schedule ->
                    // Inicializar estados
                    startYearState.value = schedule.startYear.toString()
                    endYearState.value = schedule.endYear.toString()
                    startMonthState.value = schedule.startMonth.toString()
                    endMonthState.value = schedule.endMonth.toString()
                    subjectState.value = schedule.subject  // Cargar la materia
                    tutorEmailState.value = schedule.tutorEmail

                    schedule.sessions.forEach { session ->
                        selectedDays[session.dayOfWeek] = true
                        sessionsState[session.dayOfWeek] = session.startTime.toString()
                    }

                    // Cargar salón (si existe)
                    try {
                        fireStoreManager.getClassroomByNumber(schedule.salonId) { result -> //  <-  Directamente schedule.salonId (es String)
                            result.onSuccess { selectedClassroom.value = it }
                            result.onFailure {
                                selectedClassroom.value =
                                    null   //  <-  Establecer a null en caso de fallo
                                errorLoadingSchedule.value =
                                    "El salón original no existe o hubo un error." // Mejor mensaje
                            }
                        }
                    } catch (e: Exception) {
                        errorLoadingSchedule.value = "El salón original no existe."
                    }

                    isLoadingSchedule.value = false
                    errorLoadingSchedule.value = null
                }.onFailure {
                    errorLoadingSchedule.value = "Error al cargar: ${it.localizedMessage}"
                    isLoadingSchedule.value = false
                }
            }
        } else {
            errorLoadingSchedule.value = "ID de horario inválido."
            isLoadingSchedule.value = false
        }
    }

    // Cargar salones
    LaunchedEffect(Unit) {
        fireStoreManager.getClassrooms { result ->
            result.onSuccess { list ->
                classrooms.value = list.map { it.number to it }.sortedBy { it.first }
                isLoadingClassrooms.value = false
            }.onFailure {
                errorLoadingClassrooms.value = it.localizedMessage ?: "Error desconocido"
                isLoadingClassrooms.value = false
            }
        }
    }
    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Editar Horario"
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Permite scroll
        ) {
            // Mostrar loading/error para el Schedule
            if (isLoadingSchedule.value) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else if (errorLoadingSchedule.value != null) {
                Text(errorLoadingSchedule.value!!, color = MaterialTheme.colorScheme.error)
            } else {

                // Selector de Salón (Dropdown)
                ClassroomDropdown(
                    classrooms = classrooms.value,
                    selectedClassroom = selectedClassroom.value,
                    isLoading = isLoadingClassrooms.value,
                    errorMessage = errorLoadingClassrooms.value,
                    onClassroomSelected = { classroom -> selectedClassroom.value = classroom },
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                )
                Spacer(Modifier.height(8.dp))

                // Campo de Materia (OutlinedTextField)
                OutlinedTextField(
                    value = subjectState.value,
                    onValueChange = { subjectState.value = it },
                    label = { Text("Materia") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))


                // Campos de Año de Inicio y Fin
                OutlinedTextField(
                    value = startYearState.value,
                    onValueChange = { startYearState.value = it },
                    label = { Text("Año de Inicio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = endYearState.value,
                    onValueChange = { endYearState.value = it },
                    label = { Text("Año de Fin") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                // Campos de Mes de Inicio y Fin (Dropdowns)
                MonthDropdown(
                    label = "Mes de Inicio",
                    selectedMonth = startMonthState.value,
                    onMonthSelected = { startMonthState.value = it }
                )
                Spacer(Modifier.height(8.dp))

                MonthDropdown(
                    label = "Mes de Fin",
                    selectedMonth = endMonthState.value,
                    onMonthSelected = { endMonthState.value = it }
                )
                Spacer(Modifier.height(16.dp))

                // Selección de Días y Horas
                Text("Selecciona los días y la hora (7-19):")
                DaysOfWeekSelection(
                    selectedDays = selectedDays,
                    sessionsState = sessionsState
                )
                Spacer(Modifier.height(16.dp))

                // Botón de Actualizar
                Button(
                    onClick = {
                        // Validaciones (reutilizando la función)
                        val validationResult = validateScheduleData(
                            currentYear = currentYear,
                            selectedClassroom = selectedClassroom.value,
                            startYear = startYearState.value,
                            endYear = endYearState.value,
                            startMonth = startMonthState.value,
                            endMonth = endMonthState.value,
                            subject = subjectState.value,
                            selectedDays = selectedDays,
                            sessionsState = sessionsState
                        )

                        if (validationResult != null) {
                            message.value = validationResult
                            return@Button
                        }

                        // Crear Schedule actualizado
                        val updatedSchedule = Schedule(
                            salonId = selectedClassroom.value!!.number.toString(),
                            tutorEmail = tutorEmailState.value,
                            subject = subjectState.value, // Actualizar materia
                            approved = true, // Aprobado automáticamente al editar
                            startYear = startYearState.value.toInt(),
                            startMonth = startMonthState.value.toInt(),
                            endYear = endYearState.value.toInt(),
                            endMonth = endMonthState.value.toInt(),
                            sessions = createSessions(selectedDays, sessionsState)
                        )

                        // Verificar traslape ANTES de actualizar
                        CoroutineScope(Dispatchers.Main).launch {
                            val isOverlapping =
                                fireStoreManager.checkForUpdatedOverlap(updatedSchedule, scheduleId)
                            if (isOverlapping) {
                                message.value = "Hay traslape con otro horario. Ajusta los datos."
                            } else {
                                // Actualizar en Firestore
                                if (scheduleId != null) {
                                    fireStoreManager.updateSchedule(
                                        scheduleId,
                                        updatedSchedule
                                    ) { result ->
                                        result.onSuccess {
                                            message.value = "Horario actualizado."
                                            navController.popBackStack() // Regresar
                                        }.onFailure {
                                            message.value =
                                                "Error al actualizar: ${it.localizedMessage}"
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar Horario")
                }

                Spacer(Modifier.height(16.dp))
                if (message.value.isNotEmpty()) {
                    Text(message.value, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
