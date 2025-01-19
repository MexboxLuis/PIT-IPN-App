package com.example.pitapp.ui.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

@SuppressLint("NewApi")
@Composable
fun ScheduleClassScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tutoring by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("") }
    var classroom by rememberSaveable { mutableStateOf("") }
    var durationHours by rememberSaveable { mutableIntStateOf(0) }
    var durationMinutes by rememberSaveable { mutableIntStateOf(0) }
    var isFreeTime by rememberSaveable { mutableStateOf(false) }
    var startTime by rememberSaveable { mutableStateOf<Timestamp?>(null) }
    var isErrorDate by rememberSaveable { mutableStateOf(false) }
    var isInvalidSchedule by rememberSaveable { mutableStateOf(false) } // Nuevo: Para validar días no laborables y períodos
    var invalidScheduleMessage by rememberSaveable { mutableStateOf("") } // Nuevo: Mensaje para el error de horario

    val calendar = Calendar.getInstance()


    @SuppressLint("NewApi")
    suspend fun isValidSchedule(
        date: LocalDate,
        fireStoreManager: FireStoreManager
    ): Pair<Boolean, String> {
        val year = date.year.toString()
        try {
            val nonWorkingDays = fireStoreManager.getNonWorkingDays(year).map {
                it.date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            val periods =
                fireStoreManager.getPeriods(year).flatMap { it.toLocalDateList() }

            return when {
                nonWorkingDays.contains(date) -> Pair(
                    false,
                    "Dia Laboral activo"
                )

                periods.contains(date) -> Pair(
                    false,
                    "Periodo Activo"
                )
                date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY -> Pair(
                    false,
                    "Es fin de semana"
                )

                else -> Pair(true, "")
            }
        } catch (e: Exception) {
//            Log.e("ScheduleClassScreen", "Error validating schedule: ${e.message}")
//            return Pair(false, context.getString(R.string.error_validating_schedule))
            return Pair(false, "Error validando el horario" )
        }
    }
    // TimePickerDialog para la hora, sin cambios
    val timeScheduledPickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            startTime = Timestamp(calendar.time)
            // Validación de la fecha
            coroutineScope.launch {
                val selectedLocalDate =
                    startTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                if (selectedLocalDate != null) {
                    val isValid =
                        isValidSchedule(selectedLocalDate, fireStoreManager)
                    isInvalidSchedule = !isValid.first
                    invalidScheduleMessage = isValid.second
                }
            }
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // DatePickerDialog con la validación integrada
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            timeScheduledPickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Función para validar la fecha (reutilizada de StartClassNowScreen)


    // Validar la fecha cuando se selecciona una nueva
    val isButtonEnabled = !isErrorDate && !isInvalidSchedule && tutoring.isNotEmpty() && topic.isNotEmpty() && classroom.isNotEmpty() &&
            (isFreeTime || (durationHours > 0 || durationMinutes > 0) && startTime != null)

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Agenda una clase"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = tutoring,
                onValueChange = { tutoring = it },
                label = { Text("Tutoria a dar") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("El nombre de tu tema") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = classroom,
                onValueChange = { classroom = it },
                label = { Text("Selecciona el salon") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (startTime != null) {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(startTime!!.toDate())
                    } else "",
                    onValueChange = {},
                    label = { Text("Selecciona la fecha") },
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            datePickerDialog.show()
                        }
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = if (startTime != null) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTime!!.toDate())
                    } else "",
                    onValueChange = {},
                    label = { Text("Selecciona la hora") },
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            timeScheduledPickerDialog.show()
                        }
                )
                val currentTime = Timestamp.now()
                isErrorDate = startTime != null && startTime!!.seconds <= currentTime.seconds
            }

            if (isErrorDate) {
                Text(
                    text = "Ha seleccionado una fecha y hora anteriores a la actual",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isInvalidSchedule) {
                Text(
                    text = invalidScheduleMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }




            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        if (!isFreeTime) {
                            val timePickerDialog = TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    durationHours = selectedHour
                                    durationMinutes = selectedMinute
                                },
                                durationHours,
                                durationMinutes,
                                true
                            )
                            timePickerDialog.show()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFreeTime,
                        onCheckedChange = {
                            isFreeTime = it
                            durationHours = 0
                            durationMinutes = 0
                        }
                    )
                    Text(
                        text = "Tiempo libre",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (!isFreeTime) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = " $durationHours h $durationMinutes m")
                    }
                }
            }


            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val result = fireStoreManager.createClass(
                            tutoring = tutoring,
                            topic = topic,
                            classroom = classroom,
                            durationHours = durationHours,
                            durationMinutes = durationMinutes,
                            isFreeTime = isFreeTime,
                            startTime = startTime
                        )

                        if (result.isSuccess) {
                            Toast.makeText(
                                context,
                                "Class scheduled successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Error scheduling class", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled
            ) {
                Text(text = "Schedule Class")
            }
        }
    }
}
