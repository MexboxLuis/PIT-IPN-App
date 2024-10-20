package com.example.pitapp.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun AgendarClaseScreen(navController: NavHostController, authManager: AuthManager) {
    val calendar = Calendar.getInstance()
    val context = LocalContext.current


    val tutorName = rememberSaveable { mutableStateOf("") }
    val tutoria = rememberSaveable { mutableStateOf("") }
    val tema = rememberSaveable { mutableStateOf("") }
    val lugar = rememberSaveable { mutableStateOf("") }
    val selectedDate = rememberSaveable { mutableStateOf(calendar.time) }
    val selectedTime = rememberSaveable { mutableStateOf("") }


    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            if (!selectedCalendar.before(calendar)) {
                selectedDate.value = selectedCalendar.time
            } else {
                Toast.makeText(
                    context,
                    "No puedes seleccionar una fecha pasada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    BackScaffold(
        navController = navController, authManager = authManager, topBarTitle = "Agendar clase"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(selectedDate.value),
                onValueChange = {},
                label = { Text("Fecha") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                enabled = false
            )

            OutlinedTextField(
                value = tutorName.value,
                onValueChange = { tutorName.value = it },
                label = { Text("Nombre del tutor") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tutoria.value,
                onValueChange = { tutoria.value = it },
                label = { Text("Tutoría") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = selectedTime.value,
                onValueChange = { selectedTime.value = it },
                label = { Text("Hora") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de tema
            OutlinedTextField(
                value = tema.value,
                onValueChange = { tema.value = it },
                label = { Text("Tema") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de lugar/salón
            OutlinedTextField(
                value = lugar.value,
                onValueChange = { lugar.value = it },
                label = { Text("Lugar/Salón") },
                modifier = Modifier.fillMaxWidth()
            )

            // Botón de agendar
            OutlinedButton(
                onClick = {
                    // Lógica para validar los campos y agendar la clase
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agendar")
            }
        }
    }
}
