package com.example.pitapp.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp
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

    val calendar = Calendar.getInstance()
    val timeScheduledPickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            startTime = Timestamp(calendar.time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

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


    val isButtonEnabled =
        !isErrorDate && tutoring.isNotEmpty() && topic.isNotEmpty() && classroom.isNotEmpty() &&
                (isFreeTime || (durationHours > 0 || durationMinutes > 0) && startTime != null)

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Schedule Class"
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
                label = { Text("Tutoring") },
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
                    label = { Text("Date") },
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
                    label = { Text("Time") },
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
                    text = "Please select a future date and time.",
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
                        text = "Free Time",
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
                        Text(text = "Duration: $durationHours h $durationMinutes m")
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
