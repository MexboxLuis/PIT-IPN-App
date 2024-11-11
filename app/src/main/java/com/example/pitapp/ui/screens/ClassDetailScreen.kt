package com.example.pitapp.ui.screens


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager

@Composable
fun ClassDetailScreen(
    navController: NavHostController,
    authManager: AuthManager,
) {
    val context = LocalContext.current
    val classData = remember { mutableStateOf<ClassData?>(null) }

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


                    data.students?.forEach { student ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• $student",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Botón para generar Excel
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Generando Excel...", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Text(text = "Generar Excel")
                }

                // Botón para finalizar clase
                OutlinedButton(
                    onClick = {

                            Toast.makeText(context, "Clase finalizada", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Text(text = "Terminar clase")
                }
            }
        }
    } ?: run {
        LoadingScreen()
    }
}

