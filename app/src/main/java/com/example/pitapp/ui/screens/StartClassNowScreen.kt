package com.example.pitapp.ui.screens

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId


@SuppressLint("NewApi")
@Composable
fun StartClassNowScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tutoring by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("") }
    var classroom by rememberSaveable { mutableStateOf("") }
    var isFreeTime by rememberSaveable { mutableStateOf(false) }
    var durationHours by rememberSaveable { mutableIntStateOf(0) }
    var durationMinutes by rememberSaveable { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val imeNestedScrollConnection = rememberNestedScrollInteropConnection()

    val currentDate = LocalDate.now()
    val year = currentDate.year.toString()

    var isValidDate by rememberSaveable { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }

    var reason by rememberSaveable { mutableStateOf("") }


    LaunchedEffect(Unit) {
        try {
            val nonWorkingDays = mutableListOf<LocalDate>()
            val periods = mutableListOf<LocalDate>() // Cambiado a List<LocalDate>

            // Obtener NonWorkingDays
            val nonWorkingDaysDeferred = coroutineScope.async {
                try {
                    val days = fireStoreManager.getNonWorkingDays(year)
                    nonWorkingDays.addAll(days.map {
                        it.date.toDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    })
                } catch (exception: Exception) {
                    Log.e("Firestore", "Error fetching non-working days", exception)
                }
            }

            // Obtener Periods y convertirlos a List<LocalDate>
            val periodsDeferred = coroutineScope.async {
                try {
                    val fetchedPeriods = fireStoreManager.getPeriods(year)
                    periods.addAll(fetchedPeriods.flatMap { it.toLocalDateList() })
                } catch (exception: Exception) {
                    Log.e("Firestore", "Error fetching periods", exception)
                }
            }

            // Esperar a que ambas operaciones se completen
            nonWorkingDaysDeferred.await()
            periodsDeferred.await()

            // Verificar condiciones
            if (nonWorkingDays.contains(currentDate)) {
                isValidDate = false
                reason = "Un día no laborable"
            } else if (periods.contains(currentDate)) { // Usar la lista de LocalDate
                isValidDate = false
                reason = "Un período activo"
            } else if (currentDate.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                isValidDate = false
                reason = "Es fin de semana"
            } else {
                isValidDate = true
                reason = ""
            }

        } catch (e: Exception) {
            Log.e("Error", "Exception occurred: $e")
            isValidDate = false
            reason = "un error inesperado"
        }
    }

    val isButtonEnabled = isValidDate && (durationHours > 0 || durationMinutes > 0 || isFreeTime) &&
            tutoring.isNotEmpty() && topic.isNotEmpty() && classroom.isNotEmpty()

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.create_class),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .nestedScroll(imeNestedScrollConnection)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            if (!isValidDate) {
                Text(
                    text = "No se puede iniciar una clase hoy debido a $reason.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp),
                )
            }


            OutlinedTextField(
                value = tutoring,
                onValueChange = { tutoring = it },
                label = { Text("Tutoria a dar") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Tema a dar") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = classroom,
                onValueChange = { classroom = it },
                label = { Text("Salon de clases") },
                modifier = Modifier.fillMaxWidth()
            )

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isFreeTime,
                        onCheckedChange = { isFreeTime = it }
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
                        Log.d("Firestore", "Attempting to create class with: $tutoring, $topic, $classroom, Duration: $durationHours h $durationMinutes m, Free Time: $isFreeTime")

                        val result = fireStoreManager.createClass(
                            tutoring = tutoring,
                            topic = topic,
                            classroom = classroom,
                            durationHours = durationHours,
                            durationMinutes = durationMinutes,
                            isFreeTime = isFreeTime
                        )

                        if (result.isSuccess) {
                            Log.d("Firestore", "Class created successfully!")
                            Toast.makeText(
                                context,
                                "La clase se creo correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        } else {
                            Log.e("Firestore", "Error creating class.")
                            Toast.makeText(context, "Error creando la clase", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled
            ) {
                Text(text = stringResource(id = R.string.create_class))
            }
        }
    }

}

