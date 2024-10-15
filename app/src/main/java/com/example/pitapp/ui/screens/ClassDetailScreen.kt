package com.example.pitapp.ui.screens

//import org.apache.poi.xssf.usermodel.XSSFWorkbook
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager

@Composable
fun ClassDetailScreen(
    navController: NavHostController,
    authManager: AuthManager,
    className: String,
    tutor: String,
    startTime: String,
    studentList: List<String>
) {

    val context = LocalContext.current

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = className
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Más espacio para que no se vea tan ajustado
            verticalArrangement = Arrangement.SpaceBetween, // Distribuye el contenido
            horizontalAlignment = Alignment.Start // Todo alineado a la izquierda
        ) {
            Column {
                Text(
                    text = "Horario de clase",
                    style = MaterialTheme.typography.headlineMedium, // Cambié el estilo para mayor jerarquía
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary // Color primario para más distinción
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tutor: $tutor",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Hora de inicio: $startTime",
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

                // Lista de alumnos con bullets y mejor espaciado
                studentList.forEach { student ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp) // Espaciado entre los alumnos
                    ) {
                        Text(
                            text = "• $student", // Cambié a un bullet point en lugar de un guion
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    // Lógica para crear el archivo Excel
                    Toast.makeText(context, "Generando Excel...", Toast.LENGTH_SHORT).show()
                },


            ) {
                Text(text = "Generar Excel")
            }
        }
    }

}


