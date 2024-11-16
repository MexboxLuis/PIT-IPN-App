package com.example.pitapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.data.ClassData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
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

    var isLoading by remember { mutableStateOf(true) }
    var tutoring by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("") }
    var classroom by rememberSaveable { mutableStateOf("") }
    var isFreeTime by rememberSaveable { mutableStateOf(false) }
    var durationHours by rememberSaveable { mutableIntStateOf(0) }
    var durationMinutes by rememberSaveable { mutableIntStateOf(0) }



    val isButtonEnabled =
        (durationHours > 0 || durationMinutes > 0 || isFreeTime) && tutoring.isNotEmpty() && topic.isNotEmpty() && classroom.isNotEmpty()
    if (authManager.isUserLoggedIn())
        isLoading = false


    if (isLoading) {
        LoadingScreen()
    } else {

        BackScaffold(
            navController = navController,
            authManager = authManager,
            topBarTitle = "Start Class Now"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    modifier = Modifier.fillMaxWidth(0.4f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFreeTime,
                        onCheckedChange = { isFreeTime = it },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(text = "Free Time")
                }

                if (!isFreeTime) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = durationHours.toString(),
                            onValueChange = { newValue ->
                                val intValue = newValue.toIntOrNull() ?: 0
                                if (intValue in 0..3) durationHours = intValue
                            },
                            label = { Text("Hours") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(0.65f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = durationMinutes.toString(),
                            onValueChange = { newValue ->
                                val intValue = newValue.toIntOrNull() ?: 0
                                if (intValue in 0..59) durationMinutes = intValue
                            },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        )
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
                                isFreeTime = isFreeTime
                            )

                            if (result.isSuccess) {
                                Toast.makeText(
                                    context,
                                    "Class created successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                delay(500)
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
}

