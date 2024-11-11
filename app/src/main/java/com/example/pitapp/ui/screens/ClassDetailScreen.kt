package com.example.pitapp.ui.screens


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@Composable
fun ClassDetailScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    classDocumentId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val classData = remember { mutableStateOf<ClassData?>(null) }

    LaunchedEffect(classDocumentId) {
        fireStoreManager.getClassDetails(classDocumentId) { fetchedClass ->
            classData.value = fetchedClass.getOrNull()
        }
    }

    classData.value?.let { data ->
        BackScaffold(
            navController = navController,
            authManager = authManager,
            topBarTitle = data.topic
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column {
                    Text(
                        text = "Horario de clase",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tutor: ${data.tutoring}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Hora de inicio: ${data.startTime.toDate()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Alumnos asistentes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

//                    data.students?.forEach { student ->
//                        Row(
//                            modifier = Modifier.padding(vertical = 4.dp)
//                        ) {
//                            Text(
//                                text = "• ${student.name} (${student.boleta})",
//                                style = MaterialTheme.typography.bodyLarge,
//                                color = MaterialTheme.colorScheme.onBackground
//                            )
//                        }
//                    }
                }

                // Show Finish Class button or Generate Excel button based on class status
                if (data.realDuration == null) {
                    // Class is in progress
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                fireStoreManager.finishClass(
                                    classDocumentId,
                                    data.startTime
                                ) { result ->
                                    result.onSuccess {
                                        Toast.makeText(
                                            context,
                                            "Clase finalizada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Error al finalizar la clase",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Text(text = "Terminar clase")
                    }
                } else {
                    // Class is finished, show Generate Excel button
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Generando Excel...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "Generar Excel")
                    }

                    // Show duration if available
                    Text(
                        text = "Duración: ${data.realDuration} minutos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Show Camera icon if class is in progress
                if (data.realDuration == null) {
                    Icon(
                        imageVector = Icons.Default.Camera, // You can change this to a custom camera icon if needed
                        contentDescription = "Camera Icon",
                        modifier = Modifier.size(40.dp).padding(16.dp)
                    )
                }
            }
        }
    } ?: run {
        LoadingScreen()
    }
}



