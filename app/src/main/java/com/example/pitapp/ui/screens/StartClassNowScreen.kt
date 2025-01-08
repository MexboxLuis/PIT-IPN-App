package com.example.pitapp.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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

    val isButtonEnabled = (durationHours > 0 || durationMinutes > 0 || isFreeTime) &&
            tutoring.isNotEmpty() && topic.isNotEmpty() && classroom.isNotEmpty()

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Start Class Now"
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
            // Tutoring field
            OutlinedTextField(
                value = tutoring,
                onValueChange = { tutoring = it },
                label = { Text("Tutoring") },
                modifier = Modifier.fillMaxWidth()
            )

            // Topic field
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic") },
                modifier = Modifier.fillMaxWidth()
            )

            // Classroom field
            OutlinedTextField(
                value = classroom,
                onValueChange = { classroom = it },
                label = { Text("Location/Classroom") },
                modifier = Modifier.fillMaxWidth()
            )

            FieldValue.serverTimestamp()

            // Free Time and Duration Selector
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
                        onCheckedChange = { isFreeTime = it }
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





            // Create Class Button
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val result = fireStoreManager.createClass(
                            tutoring = tutoring,
                            topic = topic,
                            classroom = classroom,
                            durationHours = durationHours,
                            durationMinutes = durationMinutes,
                            isFreeTime = isFreeTime
                        )

                        if (result.isSuccess) {
                            Toast.makeText(
                                context,
                                "Class created successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            delay(50)
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Error creating class", Toast.LENGTH_SHORT)
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

