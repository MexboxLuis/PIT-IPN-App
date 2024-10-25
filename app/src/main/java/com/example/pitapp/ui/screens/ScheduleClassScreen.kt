package com.example.pitapp.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun ScheduleClassScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val calendar = Calendar.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var tutoring by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("") }
    var classroom by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(calendar.time) }
    var selectedTime by rememberSaveable { mutableStateOf("") }

//    LaunchedEffect(Unit) {
//        coroutineScope.launch {
//            val result = fireStoreManager.getUserData()
//
//            if (result.isSuccess) {
//                userData = result.getOrNull()
//                println("User data retrieved: $userData")
//            } else {
//                println("Failed to retrieve user data: ${result.exceptionOrNull()?.message}")
//            }
//
//            isLoading = false
//        }
//    }

//    rehacer todo paro jaja


    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            if (!selectedCalendar.before(calendar)) {
                selectedDate = selectedCalendar.time
            } else {
                Toast.makeText(
                    context,
                    "You cannot select a past date",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (isLoading) {
        LoadingScreen()
    } else {

        BackScaffold(
            navController = navController, authManager = authManager, topBarTitle = "Schedule Class"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                userData?.let {
                    OutlinedTextField(
                        value = it.name + " " + it.surname,
                        onValueChange = { },
                        label = { Text("Tutor Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }

                OutlinedTextField(
                    value = SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                    ).format(selectedDate),
                    onValueChange = { },
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    }

                )

                OutlinedTextField(
                    value = tutoring,
                    onValueChange = { tutoring = it },
                    label = { Text("Tutoring") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { selectedTime = it },
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("Location/Classroom") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        // Logic to validate fields and schedule the class
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Schedule")
                }
            }
        }
    }
}
