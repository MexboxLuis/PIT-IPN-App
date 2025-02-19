package com.example.pitapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

// 3. Composable GenerateScheduleScreen para que el tutor ingrese el horario


@Composable
fun GenerateScheduleScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val tutorEmail = authManager.getUserEmail() ?: ""

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val startYearState = remember { mutableStateOf("$currentYear") }  // Año de inicio
    val endYearState = remember { mutableStateOf("$currentYear") }    // Año de fin
    val startMonthState = remember { mutableStateOf("") }
    val endMonthState = remember { mutableStateOf("") }
    val subjectState = remember { mutableStateOf("") }      // Materia

    val selectedDays = remember { mutableStateMapOf<Int, Boolean>() }
    (1..5).forEach { selectedDays.getOrPut(it) { false } } // Inicializar mapa para 5 días
    val sessionsState = remember { mutableStateMapOf<Int, String>() }

    val message = remember { mutableStateOf("") }

    // Estados para el Dropdown de salones
    val classrooms = remember { mutableStateOf<List<Pair<Int, Classroom>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val selectedClassroom = remember { mutableStateOf<Classroom?>(null) }

    // Cargar salones (sin cambios significativos, solo limpieza)
    LaunchedEffect(Unit) {
        fireStoreManager.getClassrooms { result ->
            result.onSuccess { list ->
                classrooms.value = list.map { it.number to it }.sortedBy { it.first }
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
        topBarTitle = "Crear Horario"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Permite scroll si el contenido es largo
        ) {
            // Selector de Salón (Dropdown)
            ClassroomDropdown(
                classrooms = classrooms.value,
                selectedClassroom = selectedClassroom.value,
                isLoading = isLoading.value,
                errorMessage = errorMessage.value,
                onClassroomSelected = { classroom -> selectedClassroom.value = classroom },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            )


            Spacer(Modifier.height(8.dp))

            // Campo de Materia
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

            // Campos de Mes de Inicio y Fin (con Dropdowns para mejor UX)
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


            // Selección de Días y Horas (Lunes a Domingo)
            Text("Selecciona los días y la hora (7-19):")
            DaysOfWeekSelection(
                selectedDays = selectedDays,
                sessionsState = sessionsState
            )
            Spacer(Modifier.height(16.dp))

            // Botón de Agendar
            Button(
                onClick = {
                    val validationResult = validateScheduleData(
                        currentYear = 2023,
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

                    // Verificar traslape por email ANTES de crear
                    CoroutineScope(Dispatchers.Main).launch {
                        val isOverlapping = fireStoreManager.checkForEmailOverlap(schedule)
                        if (isOverlapping) {
                            // Mostrar mensaje de traslape
                            message.value =
                                "Ya tienes un horario aprobado que coincide con este.  Por favor, revisa tus horarios."
                            // O, si prefieres usar un Toast:
                            // Toast.makeText(context, "Ya tienes un horario...", Toast.LENGTH_LONG).show()

                        } else {
                            // Si no hay traslape, crear el horario
                            fireStoreManager.createSchedule(schedule) { result ->
                                result.onSuccess {
                                    message.value = "Horario agendado exitosamente."
                                    // ... (limpiar campos) ...
                                }.onFailure {
                                    message.value = "Error al agendar: ${it.localizedMessage}"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agendar Horario")
            }

            Spacer(Modifier.height(16.dp))
            if (message.value.isNotEmpty()) {
                Text(message.value, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


// Componente para el Dropdown de Salones (reutilizable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomDropdown(
    classrooms: List<Pair<Int, Classroom>>,
    selectedClassroom: Classroom?,
    isLoading: Boolean,
    errorMessage: String?,
    onClassroomSelected: (Classroom) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedClassroom?.let { "Salón ${it.number} - ${it.description}" }
                ?: "Ningún salón seleccionado",
            onValueChange = {},
            label = { Text("Seleccione un Salón") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            if (isLoading) {
                DropdownMenuItem(
                    text = { Text("Cargando...") },
                    onClick = {}
                ) // Mostrar estado de carga
            } else if (errorMessage != null) {
                DropdownMenuItem(
                    text = { Text("Error: $errorMessage") },
                    onClick = {}
                ) // Mostrar error
            } else {
                classrooms.forEach { (_, classroom) ->
                    DropdownMenuItem(
                        text = { Text("Salón ${classroom.number} - ${classroom.description}") },
                        onClick = {
                            onClassroomSelected(classroom)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

// Componente para el Dropdown de Meses (reutilizable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDropdown(label: String, selectedMonth: String, onMonthSelected: (String) -> Unit) {
    val months = listOf(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedMonth.ifEmpty { "Seleccionar" },
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}


// Componente para la Selección de Días de la Semana (reutilizable)
@Composable
fun DaysOfWeekSelection(
    selectedDays: MutableMap<Int, Boolean>,
    sessionsState: MutableMap<Int, String>
) {
    val dayLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie")

    Column {
        for (day in 1..5) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = selectedDays[day] ?: false,
                    onCheckedChange = { selectedDays[day] = it }
                )
                Text(text = dayLabels[day - 1])

                if (selectedDays[day] == true) {
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = sessionsState[day] ?: "",
                        onValueChange = { sessionsState[day] = it },
                        label = { Text("Hora") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp)
                    )
                }
            }
        }
    }
}


// 1. Data Classes para la estructura del horario

data class Session(
    val dayOfWeek: Int = 0, // 1 = Lunes, 2 = Martes, ..., 5 = Viernes
    val startTime: Int = 0  // Hora de inicio en formato 24h (e.g., 7, 13, 19)
)

data class Schedule(
    val salonId: String = "",          // ID del salón (String, para flexibilidad)
    val tutorEmail: String = "",       // Email del tutor
    val subject: String = "",          //  <-- NUEVO:  Materia
    val approved: Boolean = false,     // Estado de aprobación
    val startYear: Int = 0,         //  <-- CAMBIO: Año de inicio
    val startMonth: Int = 0,           // Mes de inicio (1-12)
    val endYear: Int = 0,            // <-- NUEVO:  Año de fin
    val endMonth: Int = 0,             // Mes de fin (1-12)
    val sessions: List<Session> = emptyList()  // Lista de sesiones
)





fun validateScheduleData(
    currentYear: Int,
    selectedClassroom: Classroom?,
    startYear: String,
    endYear: String,
    startMonth: String,
    endMonth: String,
    subject: String, // Validar materia
    selectedDays: Map<Int, Boolean>,
    sessionsState: Map<Int, String>
): String? {
    if (selectedClassroom == null) {
        return "Debes seleccionar un salón."
    }

    if (subject.isBlank()) { // Validación de materia
        return "Debes ingresar una materia."
    }

    val startYearInt = startYear.toIntOrNull()
    val endYearInt = endYear.toIntOrNull()
    val startMonthInt = startMonth.toIntOrNull()
    val endMonthInt = endMonth.toIntOrNull()


    if (startYearInt == null || endYearInt == null || startMonthInt == null || endMonthInt == null) {
        return "Año y mes deben ser números."
    }

    if (startYearInt > endYearInt || (startYearInt == endYearInt && startMonthInt > endMonthInt)) {
        return "El año/mes de inicio no puede ser posterior al año/mes de fin."
    }
    if (startMonthInt !in 1..12 || endMonthInt !in 1..12) {
        return "Meses inválidos (deben estar entre 1 y 12)."
    }

    if (startYearInt < currentYear || endYearInt < currentYear) {
        return "No se pueden seleccionar años anteriores al año actual ($currentYear)."
    }

    val hasSelectedDays = selectedDays.values.any { it }
    if (!hasSelectedDays) {
        return "Debes seleccionar al menos un día."
    }

    for ((day, isSelected) in selectedDays) {
        if (isSelected) {
            val hour = sessionsState[day]?.toIntOrNull()
            if (hour == null || hour !in 7..19) {
                return "Hora inválida para el día ${day} (debe estar entre 7 y 19)."
            }
        }
    }

    return null // No hay errores
}

// Función para crear la lista de sesiones (extraída para mejor legibilidad)
fun createSessions(
    selectedDays: Map<Int, Boolean>,
    sessionsState: Map<Int, String>
): List<Session> {
    return selectedDays.mapNotNull { (day, isSelected) ->
        if (isSelected) {
            val hour = sessionsState[day]?.toIntOrNull()
            if (hour != null) Session(dayOfWeek = day, startTime = hour) else null
        } else {
            null
        }
    }
}
